// Command libpinpadabecsgo executa o menu local de validaÃ§Ã£o manual da
// biblioteca ABECS e configura seus adaptadores sem expor serviÃ§os de rede.
package main

import (
	"bufio"
	"context"
	"encoding/hex"
	"errors"
	"fmt"
	"log/slog"
	"os"
	"path/filepath"
	"strconv"
	"strings"
	"time"

	"br.com.romulopenha/lib-pinpad-abecs-go/internal/application/service"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/command"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/model"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/protocol"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/config"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/logging"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/serial"
)

// menuTimeout e usado para operacoes que podem exigir interacao manual no
// pinpad (ex.: MNU, GKY, GCX aguardando cartao), evitando que o timeout curto
// de configuracao interrompa o teste antes da acao do operador.
const menuTimeout = 60 * time.Second

const (
	modulePath          = "br.com.romulopenha/lib-pinpad-abecs-go"
	defaultTraceRelPath = "logs/LogPinpadAbecs.txt"
)

func main() {
	os.Exit(run())
}

func run() int {
	logger := logging.New()
	cfg, err := config.Load()
	if err != nil {
		logger.Error("invalid configuration", slog.Any("error", err))
		fmt.Fprintln(os.Stderr, err)
		return 1
	}
	logger.Info("configuracao carregada", slog.String("port", cfg.Port), slog.Int("baudRate", cfg.BaudRate), slog.Duration("timeout", cfg.Timeout))

	tracer := logging.NewTracer()
	destination, err := configureTracer(tracer)
	if err != nil {
		logger.Error("falha ao configurar rastro serial", slog.Any("error", err))
		fmt.Fprintln(os.Stderr, err)
		return 1
	}
	logger.Info("rastro serial configurado", slog.String("arquivo", destination))
	fmt.Printf("Log serial ativo: %s\n", destination)

	port := serial.New(cfg.Port, cfg.BaudRate, cfg.Timeout)
	port.SetTracer(tracer)
	svc := service.New(cfg, port)
	svc.SetTracer(tracer)

	reader := bufio.NewReader(os.Stdin)
	menuErr := runMenu(reader, svc, cfg, logger, tracer)
	svc.Shutdown()
	traceErr := tracer.Err()
	closeErr := tracer.Close()
	if menuErr != nil || traceErr != nil || closeErr != nil {
		err = errors.Join(menuErr, traceErr, closeErr)
		logger.Error("falha no rastro serial", slog.Any("error", err))
		fmt.Fprintln(os.Stderr, err)
		return 1
	}
	return 0
}

// configureTracer habilita o rastro do utilitÃ¡rio local no destino configurado
// ou no arquivo canÃ´nico do mÃ³dulo. A biblioteca permanece opt-in porque essa
// composiÃ§Ã£o explÃ­cita existe somente no executÃ¡vel de validaÃ§Ã£o.
func configureTracer(tracer *logging.Tracer) (string, error) {
	destination, err := resolveLogDestination(strings.TrimSpace(os.Getenv("PINPAD_LOG_FILE")))
	if err != nil {
		return "", err
	}
	if err := os.MkdirAll(filepath.Dir(destination), 0o755); err != nil {
		return "", fmt.Errorf("criar diretorio do rastro serial: %w", err)
	}
	active, err := tracer.SetLogDestination(destination)
	if err != nil {
		return "", fmt.Errorf("configurar PINPAD_LOG_FILE: %w", err)
	}
	if !active {
		return "", fmt.Errorf("PINPAD_LOG_FILE nao ativou o rastro")
	}
	return destination, nil
}

// resolveLogDestination normaliza PINPAD_LOG_FILE sem depender do diretÃ³rio
// de trabalho. Caminhos relativos usam a raiz do mÃ³dulo Go como base.
func resolveLogDestination(configured string) (string, error) {
	configured = strings.TrimSpace(configured)
	if configured != "" && filepath.IsAbs(configured) {
		return filepath.Clean(configured), nil
	}
	cwd, err := os.Getwd()
	if err != nil {
		return "", fmt.Errorf("obter diretorio de trabalho: %w", err)
	}
	executable, err := os.Executable()
	if err != nil {
		return "", fmt.Errorf("obter caminho do executavel: %w", err)
	}
	return resolveLogDestinationFrom(configured, cwd, filepath.Dir(executable))
}

func resolveLogDestinationFrom(configured string, searchStarts ...string) (string, error) {
	configured = strings.TrimSpace(configured)
	if configured != "" && filepath.IsAbs(configured) {
		return filepath.Clean(configured), nil
	}
	root, err := findModuleRoot(searchStarts...)
	if err != nil {
		return "", fmt.Errorf("determinar raiz do modulo; defina PINPAD_LOG_FILE com caminho absoluto: %w", err)
	}
	if configured == "" {
		configured = filepath.FromSlash(defaultTraceRelPath)
	}
	destination, err := filepath.Abs(filepath.Join(root, configured))
	if err != nil {
		return "", fmt.Errorf("normalizar destino do rastro serial: %w", err)
	}
	return filepath.Clean(destination), nil
}

func findModuleRoot(searchStarts ...string) (string, error) {
	seen := make(map[string]struct{}, len(searchStarts))
	for _, start := range searchStarts {
		start = strings.TrimSpace(start)
		if start == "" {
			continue
		}
		directory, err := filepath.Abs(start)
		if err != nil {
			continue
		}
		for {
			clean := filepath.Clean(directory)
			if _, visited := seen[clean]; !visited {
				seen[clean] = struct{}{}
				contents, readErr := os.ReadFile(filepath.Join(clean, "go.mod"))
				if readErr == nil && declaresModule(contents, modulePath) {
					return clean, nil
				}
				if readErr != nil && !os.IsNotExist(readErr) {
					return "", fmt.Errorf("ler go.mod em %s: %w", clean, readErr)
				}
			}
			parent := filepath.Dir(clean)
			if parent == clean {
				break
			}
			directory = parent
		}
	}
	return "", fmt.Errorf("modulo %s nao encontrado", modulePath)
}

func declaresModule(contents []byte, expected string) bool {
	for _, line := range strings.Split(string(contents), "\n") {
		fields := strings.Fields(line)
		if len(fields) == 2 && fields[0] == "module" {
			return fields[1] == expected
		}
	}
	return false
}

func runMenu(reader *bufio.Reader, svc *service.Service, cfg model.PinpadConfig, logger *slog.Logger, tracer *logging.Tracer) error {
	var lastGCX *model.GCXResponse
	var lastGCXAmount string
	var lastGOX *model.GOXResponse
	for {
		printMenu(cfg)
		choice := readLine(reader, "Escolha uma opcao: ")
		ctx, cancel := context.WithTimeout(context.Background(), menuTimeout)

		switch choice {
		case "1":
			if handleErr(logger, "Open", svc.Open(ctx)) {
				lastGCX = nil
				lastGCXAmount = ""
				lastGOX = nil
			}
		case "2":
			if handleErr(logger, "Close", svc.Close(ctx)) {
				lastGCX = nil
				lastGCXAmount = ""
				lastGOX = nil
			}
		case "3":
			fmt.Printf("Estado atual do pinpad: %s\n", stateName(svc.GetState()))
		case "4":
			info, err := svc.GetInfo(ctx)
			if handleErr(logger, "GetInfo (GIX)", err) {
				printDeviceInfo(info)
			}
		case "5":
			if handleErr(logger, "Reset (CAN)", svc.Reset(ctx)) {
				lastGCX = nil
				lastGCXAmount = ""
				lastGOX = nil
			}
		case "6":
			fmt.Println("[ERRO] Comando indisponivel; use CAN na opcao 5.")
		case "7":
			line1 := readLine(reader, "Linha 1 (max 16 caracteres): ")
			line2 := readLine(reader, "Linha 2 (max 16 caracteres): ")
			_, err := svc.DisplayDSP(ctx, line1, line2)
			handleErr(logger, "DisplayDSP (DSP)", err)
		case "8":
			message := readLine(reader, "Mensagem: ")
			_, err := svc.DisplayDEX(ctx, message)
			handleErr(logger, "DisplayDEX (DEX)", err)
		case "9":
			title := readLine(reader, "Titulo do menu: ")
			optionsRaw := readLine(reader, "Opcoes separadas por virgula: ")
			options := splitOptions(optionsRaw)
			resp, err := svc.DisplayMNU(ctx, 30, title, options)
			if handleErr(logger, "DisplayMNU (MNU)", err) {
				fmt.Printf("Opcao selecionada: %d\n", resp.SelectedIndex)
			}
		case "10":
			timeoutSec := readInt(reader, "Timeout em segundos (0 = aguardar indefinidamente): ")
			key, err := svc.WaitForKeyPress(ctx, timeoutSec)
			if handleErr(logger, "WaitForKeyPress (GKY)", err) {
				fmt.Printf("Tecla pressionada: 0x%02X\n", key)
			}
		case "11":
			lastGCX = nil
			lastGCXAmount = ""
			lastGOX = nil
			enableCTLS, hideAmount, err := readGCXOptions(reader)
			if err != nil {
				fmt.Printf("[ERRO] Opcoes GCX: %v\n", err)
				break
			}
			amount := readLine(reader, "Valor da transacao (centavos, ex: 000000010000): ")
			date := readLine(reader, "Data da transacao (AAMMDD): ")
			clock := readLine(reader, "Hora da transacao (HHMMSS): ")
			cancel()
			ctx, cancel = context.WithTimeout(context.Background(), menuTimeout)
			resp, err := svc.PurchaseGCX(ctx, amount, date, clock, enableCTLS, hideAmount)
			if handleErr(logger, "PurchaseGCX (GCX)", err) {
				lastGCX = resp
				lastGCXAmount = amount
				fmt.Printf("Tipo de cartao: %s\n", resp.CardType)
				if resp.AidTableInfo != "" {
					fmt.Printf("Informacoes das tabelas AID: %s\n", resp.AidTableInfo)
				}
			}
		case "12":
			response, err := svc.GetInfoRaw(ctx)
			if handleErr(logger, "GetInfoRaw (GIX)", err) {
				fmt.Printf("Status: %s | Bytes recebidos: %d\n", response.StatusCode, len(response.RawData))
			}
		case "13":
			capabilities, err := svc.GetDisplayCapabilities(ctx)
			if handleErr(logger, "GetDisplayCapabilities (GIX)", err) {
				fmt.Printf("Display: %dx%d texto, %dx%d grafico | PNG=%t JPG=%t GIF=%t CTLS=%t\n", capabilities.TextLines, capabilities.TextCols, capabilities.GraphicWidth, capabilities.GraphicHeight, capabilities.SupportsPNG, capabilities.SupportsJPG, capabilities.SupportsGIF, capabilities.SupportsCTLS)
			}
		case "14":
			privateKey, _, err := protocol.GenerateSecureOPN()
			if err == nil {
				err = svc.OpenSecure(ctx, privateKey)
			}
			handleErr(logger, "OpenSecure (OPN RSA/AES)", err)
		case "15":
			message := readLine(reader, "Mensagem visual (vazio para limpar): ")
			mediaName := readLine(reader, "Nome da midia (opcional): ")
			_, err := svc.CloseVisual(ctx, command.CLXRequest{Message: message, MediaName: mediaName})
			handleErr(logger, "CloseVisual (CLX)", err)
		case "16":
			path := readLine(reader, "Caminho do arquivo local: ")
			name := readLine(reader, "Nome da midia no pinpad: ")
			handleErr(logger, "LoadMultimediaPath (MLI/MLR/MLE)", svc.LoadMultimediaPath(ctx, path, name, printProgress))
		case "17":
			name := readLine(reader, "Nome da midia carregada: ")
			_, err := svc.DisplayImage(ctx, name)
			handleErr(logger, "DisplayImage (DSI)", err)
		case "18":
			acquirer := readLine(reader, "Indice do adquirente (ex.: 00): ")
			version := readLine(reader, "Versao da tabela: ")
			records := splitRecords(readLine(reader, "Registros separados por ';': "))
			handleErr(logger, "LoadCompleteEMVTable (TLI/TLR/TLE)", svc.LoadCompleteEMVTable(ctx, acquirer, version, records, printProgress))
		case "19":
			request, err := readGTKRequest(reader)
			if err != nil {
				fmt.Printf("[ERRO] Opcoes GTK: %v\n", err)
				break
			}
			cancel()
			ctx, cancel = context.WithTimeout(context.Background(), menuTimeout)
			tracks, err := svc.GetTracks(ctx, request)
			if handleErr(logger, "GetTracks (GTK)", err) {
				logged, traceErr := recordGTKResult(tracer, request, tracks)
				if traceErr != nil {
					cancel()
					return fmt.Errorf("registrar trilhas GTK em claro: %w", traceErr)
				}
				if logged {
					fmt.Println("Trilhas em claro registradas no arquivo de log ativo.")
				} else {
					fmt.Println("Trilhas criptografadas recebidas e mantidas redigidas.")
				}
			}
		case "20":
			request, err := readGOXRequest(reader, lastGCX, lastGCXAmount)
			if err != nil {
				fmt.Printf("[ERRO] Opcoes GOX: %v\n", err)
				break
			}
			if err := tracer.RecordGOXConfig(request.AcquirerReference, request.PinMethod, request.KeyIndex); err != nil {
				cancel()
				return fmt.Errorf("registrar configuracao GOX: %w", err)
			}
			cancel()
			ctx, cancel = context.WithTimeout(context.Background(), menuTimeout)
			response, err := svc.ContinueEMV(ctx, request)
			if handleErr(logger, "ContinueEMV (GOX)", err) {
				lastGOX = response
				fmt.Printf("Resultado EMV: %s\n", response.Result)
			}
		case "21":
			request, err := readFCXRequest(reader, lastGOX)
			if err != nil {
				fmt.Printf("[ERRO] Opcoes FCX: %v\n", err)
				break
			}
			cancel()
			ctx, cancel = context.WithTimeout(context.Background(), menuTimeout)
			response, err := svc.FinalizeEMV(ctx, request)
			if handleErr(logger, "FinalizeEMV (FCX)", err) {
				lastGOX = nil
				fmt.Printf("Resultado final EMV: %s\n", response.Result)
			}
		case "22":
			keyIndex := readInt(reader, "Indice da chave: ")
			keyHex := readLine(reader, "WKENC TDES (32 digitos hexadecimais): ")
			workingKey, decodeErr := hex.DecodeString(keyHex)
			pan := readLine(reader, "PAN (nao sera exibido): ")
			message := readLine(reader, "Mensagem de PIN: ")
			err := decodeErr
			if err == nil {
				_, _, err = svc.SendGPNCommandMK(ctx, keyIndex, workingKey, pan, message)
			}
			if handleErr(logger, "SendGPNCommandMK (GPN)", err) {
				fmt.Println("PIN coletado; PIN block e KSN foram redigidos.")
			}
		case "23":
			keyIndex := readInt(reader, "Indice da chave DUKPT: ")
			pan := readLine(reader, "PAN (nao sera exibido): ")
			message := readLine(reader, "Mensagem de PIN: ")
			_, _, err := svc.SendGPNCommandDUKPT(ctx, keyIndex, pan, message)
			if handleErr(logger, "SendGPNCommandDUKPT (GPN)", err) {
				fmt.Println("PIN coletado; PIN block e KSN foram redigidos.")
			}
		case "24":
			fmt.Println("DisplayQRCode requer QRCodeGenerator injetado pelo consumidor; o CLI nao adiciona gerador concreto.")
		case "25":
			_, err := svc.TransactionGCX(ctx, service.TransactionGCXRequest{})
			handleErr(logger, "TransactionGCX completo (reservado)", err)
		case "0":
			cancel()
			fmt.Println("Encerrando...")
			return nil
		default:
			fmt.Println("Opcao invalida.")
		}
		cancel()
		if traceErr := tracer.Err(); traceErr != nil {
			return fmt.Errorf("persistir rastro serial: %w", traceErr)
		}
	}
}

func printMenu(cfg model.PinpadConfig) {
	fmt.Println()
	fmt.Println("==========================================")
	fmt.Println("Biblioteca Go ABECS - Menu de teste local")
	fmt.Println("==========================================")
	fmt.Printf("Porta: %s | Baud: %d | Timeout: %s\n", cfg.Port, cfg.BaudRate, cfg.Timeout)
	fmt.Println()
	fmt.Println(" 1) Abrir conexao serial (Open)")
	fmt.Println(" 2) Fechar conexao (Close)")
	fmt.Println(" 3) Status atual do pinpad")
	fmt.Println(" 4) Obter informacoes do pinpad (GIX)")
	fmt.Println(" 5) Reset rapido (CAN)")
	fmt.Println(" 6) Indisponivel no ABECS 2.12 (use CAN na opcao 5)")
	fmt.Println(" 7) Exibir mensagem fixa (DSP)")
	fmt.Println(" 8) Exibir mensagem estendida (DEX)")
	fmt.Println(" 9) Exibir menu interativo (MNU)")
	fmt.Println("10) Aguardar tecla pressionada (GKY)")
	fmt.Println("11) Iniciar transacao de compra (GCX)")
	fmt.Println("12) Obter resposta bruta resumida (GIX)")
	fmt.Println("13) Obter capacidades do display (GIX)")
	fmt.Println("14) Abrir sessao segura (OPN RSA/AES)")
	fmt.Println("15) Atualizar/limpar display visual (CLX)")
	fmt.Println("16) Carregar arquivo de midia (MLI/MLR/MLE)")
	fmt.Println("17) Exibir midia carregada (DSI)")
	fmt.Println("18) Carregar tabela EMV (TLI/TLR/TLE)")
	fmt.Println("19) Obter trilhas apos GCX elegivel (GTK)")
	fmt.Println("20) Continuar transacao EMV (GOX)")
	fmt.Println("21) Finalizar transacao EMV (FCX)")
	fmt.Println("22) Capturar PIN MK/WK (GPN, redigido)")
	fmt.Println("23) Capturar PIN DUKPT (GPN, redigido)")
	fmt.Println("24) Exibir QR Code (requer gerador injetado)")
	fmt.Println("25) Transacao GCX completa (reservada na SPEC)")
	fmt.Println(" 0) Sair")
	fmt.Println("==========================================")
}

func stateName(state model.PinpadState) string {
	switch state {
	case model.StateOpen:
		return "aberto"
	case model.StateBusy:
		return "ocupado"
	default:
		return "fechado"
	}
}

// printDeviceInfo exibe os dados do GIX ja refinados (nomes legiveis e
// campos compostos decompostos), conforme secao 6.4.3 da especificacao
// ABECS. Dados brutos nÃ£o sÃ£o exibidos para evitar vazamento acidental.
func printDeviceInfo(info *model.DeviceInfo) {
	fmt.Println("--- Informacoes do pinpad (GIX) ---")
	fmt.Printf("numeroSerie:: %s\n", info.SerialNumber)
	fmt.Printf("partNumber:: %s\n", info.PartNumber)
	fmt.Printf("modelo:: %s\n", info.Model)
	fmt.Printf("fabricante:: %s\n", info.Manufacturer)
	fmt.Printf("capacidadePinpad:: %s\n", info.Capabilities)
	fmt.Printf("versaoSO:: %s\n", info.OSVersion)
	fmt.Printf("especificacao:: %s\n", info.Specification)
	fmt.Printf("versaoGerenciadora:: %s\n", info.ManufacturerVersion)
	fmt.Printf("versaoAbecs:: %s\n", info.AbecsVersion)
	fmt.Printf("versaoExtensaoAbecs:: %s\n", info.ExtendedAbecsVersion)
	fmt.Printf("kernelEMV:: %s\n", info.ContactlessCapabilities)
	fmt.Printf("kernelVersion:: %s\n", info.KernelVersion)
	fmt.Printf("contactlessVersion:: %s\n", info.ContactlessVersion)
	fmt.Printf("masterCardPaypass:: %s\n", info.MasterCardPaypass)
	fmt.Printf("visaPaypass:: %s\n", info.VisaPaypass)
	fmt.Printf("aexp:: %s\n", info.Aexp)
	fmt.Printf("discoverContactless:: %s\n", info.DiscoverContactless)
	fmt.Printf("qpContactless:: %s\n", info.QPContactless)
	fmt.Printf("linha:: %d\n", info.TextRows)
	fmt.Printf("colunas:: %d\n", info.TextCols)
	fmt.Printf("largura:: %d\n", info.GraphicWidth)
	fmt.Printf("altura:: %d\n", info.GraphicHeight)
	fmt.Printf("formatosSuportados:: %s\n", info.SupportedFormats)
}

func handleErr(logger *slog.Logger, operation string, err error) bool {
	if err != nil {
		logger.Error("falha ao executar operacao", slog.String("operacao", operation), slog.Any("error", err))
		fmt.Printf("[ERRO] %s: %v\n", operation, err)
		return false
	}
	fmt.Printf("[OK] %s concluido com sucesso.\n", operation)
	return true
}

func readLine(reader *bufio.Reader, prompt string) string {
	fmt.Print(prompt)
	text, _ := reader.ReadString('\n')
	return strings.TrimSpace(text)
}

func readInt(reader *bufio.Reader, prompt string) int {
	text := readLine(reader, prompt)
	value, err := strconv.Atoi(text)
	if err != nil {
		return 0
	}
	return value
}

func readGCXOptions(reader *bufio.Reader) (bool, bool, error) {
	fmt.Println("Modo de leitura do cartao:")
	fmt.Println("1) Chip ou tarja (sem contactless)")
	fmt.Println("2) Chip, tarja ou contactless")
	interfaceChoice := readLine(reader, "Escolha o modo: ")
	if interfaceChoice != "1" && interfaceChoice != "2" {
		return false, false, fmt.Errorf("modo de leitura invalido")
	}

	fmt.Println("Exibir o valor durante a espera pelo cartao?")
	fmt.Println("1) Sim, mostrar o valor")
	fmt.Println("2) Nao, ocultar o valor")
	visibilityChoice := readLine(reader, "Escolha a exibicao: ")
	if visibilityChoice != "1" && visibilityChoice != "2" {
		return false, false, fmt.Errorf("opcao de exibicao invalida")
	}
	return interfaceChoice == "2", visibilityChoice == "2", nil
}

func readGTKRequest(reader *bufio.Reader) (command.GTKRequest, error) {
	fmt.Println("Modo de retorno das trilhas:")
	fmt.Println("1) Em claro (sem chave)")
	fmt.Println("2) Criptografadas com DUKPT TDES DAT#3/ECB (metodo 50)")
	choice := readLine(reader, "Escolha o modo: ")
	switch choice {
	case "1":
		return command.GTKRequest{}, nil
	case "2":
		keyText := readLine(reader, "Indice da chave DUKPT (00 a 99): ")
		keyIndex, err := strconv.Atoi(keyText)
		if err != nil || keyIndex < 0 || keyIndex > 99 {
			return command.GTKRequest{}, fmt.Errorf("indice de chave invalido")
		}
		return command.GTKRequest{
			Tracks:     "1111",
			DataMethod: "50",
			KeyIndex:   &keyIndex,
		}, nil
	default:
		return command.GTKRequest{}, fmt.Errorf("modo de trilha invalido")
	}
}

func recordGTKResult(tracer *logging.Tracer, request command.GTKRequest, response *model.GTKResponse) (bool, error) {
	if request.DataMethod != "" {
		return false, nil
	}
	if response == nil {
		return false, fmt.Errorf("resposta GTK ausente")
	}
	return true, tracer.RecordGTKClearTracks(response.Track1, response.Track2, response.Track3)
}

func readGOXRequest(reader *bufio.Reader, gcx *model.GCXResponse, amount string) (command.GOXRequest, error) {
	if gcx == nil || (gcx.CardType != command.GCXCardICC && gcx.CardType != command.GCXCardContactlessEMV) {
		return command.GOXRequest{}, fmt.Errorf("execute antes um GCX com cartão ICC EMV ou CTLS EMV")
	}
	if len(amount) != 12 || !decimalText(amount) {
		return command.GOXRequest{}, fmt.Errorf("valor do GCX ausente ou inválido")
	}
	acquirers, err := goxAcquirerReferences(gcx.AidTableInfo)
	if err != nil {
		return command.GOXRequest{}, err
	}
	fmt.Printf("Redes credenciadoras disponíveis: %s\n", strings.Join(acquirers, ", "))
	acquirer := readLine(reader, fmt.Sprintf("Rede credenciadora (Enter = %s): ", acquirers[0]))
	if acquirer == "" {
		acquirer = acquirers[0]
	}
	if !containsString(acquirers, acquirer) {
		return command.GOXRequest{}, fmt.Errorf("rede credenciadora não consta em PP_AIDTABINFO")
	}

	fmt.Println("Método de criptografia do PIN online:")
	fmt.Println("0) MK/WK DES")
	fmt.Println("1) MK/WK TDES")
	fmt.Println("2) DUKPT DES")
	fmt.Println("3) DUKPT TDES")
	pinMethod := readLine(reader, "Escolha o método: ")
	if len(pinMethod) != 1 || pinMethod[0] < '0' || pinMethod[0] > '3' {
		return command.GOXRequest{}, fmt.Errorf("método de PIN inválido")
	}
	keyText := readLine(reader, "Índice da chave (00 a 99): ")
	keyIndex, err := strconv.Atoi(keyText)
	if err != nil || keyIndex < 0 || keyIndex > 99 {
		return command.GOXRequest{}, fmt.Errorf("índice de chave inválido")
	}

	request := command.GOXRequest{
		AcquirerReference: acquirer,
		PinMethod:         pinMethod,
		KeyIndex:          keyIndex,
		Amount:            amount,
	}
	if pinMethod == "0" || pinMethod == "1" {
		digits := 16
		wantBytes := 8
		if pinMethod == "1" {
			digits = 32
			wantBytes = 16
		}
		workingKeyHex := readLine(reader, fmt.Sprintf("WKENC (%d dígitos hexadecimais): ", digits))
		workingKey, decodeErr := hex.DecodeString(workingKeyHex)
		if decodeErr != nil || len(workingKey) != wantBytes {
			return command.GOXRequest{}, fmt.Errorf("WKENC inválida para o método selecionado")
		}
		request.WorkingKey = workingKey
	}
	if _, err := command.BuildGOXCommand(request); err != nil {
		return command.GOXRequest{}, err
	}
	return request, nil
}

func readFCXRequest(reader *bufio.Reader, gox *model.GOXResponse) (command.FCXRequest, error) {
	if gox == nil || !validGOXResult(gox.Result) {
		return command.FCXRequest{}, fmt.Errorf("execute antes um GOX válido")
	}
	fmt.Printf("Resultado do GOX conservado: %s\n", gox.Result)
	fmt.Println("Resultado da comunicação com a Rede Credenciadora:")
	fmt.Println("1) Transação aprovada pela rede")
	fmt.Println("2) Transação negada pela rede")
	fmt.Println("3) Comunicação malsucedida ou sem resposta válida")
	choice := readLine(reader, "Escolha o resultado: ")
	request := command.FCXRequest{}
	switch choice {
	case "1":
		request.Options = "0000"
	case "2":
		request.Options = "1000"
	case "3":
		request.Options = "2000"
	default:
		return command.FCXRequest{}, fmt.Errorf("resultado da rede inválido")
	}

	if choice == "1" || choice == "2" {
		request.Authorization = readLine(reader, "ARC devolvido pela rede (2 caracteres ASCII): ")
		if !validASCII(request.Authorization, 2) {
			return command.FCXRequest{}, fmt.Errorf("ARC deve conter exatamente 2 caracteres ASCII")
		}
	}

	var err error
	request.EMVData, err = readOptionalHex(reader, "Dados EMV da rede em hexadecimal (Enter = nenhum): ")
	if err != nil {
		return command.FCXRequest{}, fmt.Errorf("dados EMV inválidos: %w", err)
	}
	request.TagList, err = readOptionalHex(reader, "Tags EMV solicitadas em hexadecimal (Enter = nenhuma): ")
	if err != nil {
		return command.FCXRequest{}, fmt.Errorf("lista de tags inválida: %w", err)
	}
	timeoutText := readLine(reader, "Timeout para reapresentação CTLS em segundos (Enter = padrão): ")
	if timeoutText != "" {
		timeout, parseErr := strconv.Atoi(timeoutText)
		if parseErr != nil || timeout < 1 || timeout > 255 {
			return command.FCXRequest{}, fmt.Errorf("timeout FCX deve estar entre 1 e 255 segundos")
		}
		value := byte(timeout)
		request.Timeout = &value
	}
	if _, err := command.BuildFCXCommand(request); err != nil {
		return command.FCXRequest{}, err
	}
	return request, nil
}

func validGOXResult(result []byte) bool {
	return len(result) == 6 &&
		(result[0] == '0' || result[0] == '1' || result[0] == '2') &&
		(result[1] == '0' || result[1] == '1') &&
		(result[2] == '0' || result[2] == '1' || result[2] == '2') &&
		(result[3] == '0' || result[3] == '1') && string(result[4:]) == "00"
}

func validASCII(value string, size int) bool {
	if len(value) != size {
		return false
	}
	for index := range len(value) {
		if value[index] < 0x20 || value[index] > 0x7E {
			return false
		}
	}
	return true
}

func readOptionalHex(reader *bufio.Reader, prompt string) ([]byte, error) {
	value := readLine(reader, prompt)
	if value == "" {
		return nil, nil
	}
	decoded, err := hex.DecodeString(value)
	if err != nil {
		return nil, fmt.Errorf("use quantidade par de dígitos hexadecimais: %w", err)
	}
	return decoded, nil
}

func goxAcquirerReferences(aidTableInfo string) ([]string, error) {
	if aidTableInfo == "" || len(aidTableInfo)%6 != 0 {
		return nil, fmt.Errorf("PP_AIDTABINFO ausente ou inválido")
	}
	result := make([]string, 0, len(aidTableInfo)/6)
	for offset := 0; offset < len(aidTableInfo); offset += 6 {
		entry := aidTableInfo[offset : offset+6]
		for _, value := range []byte(entry) {
			if value < '0' || value > '9' {
				return nil, fmt.Errorf("PP_AIDTABINFO ausente ou inválido")
			}
		}
		acquirer := entry[:2]
		if !containsString(result, acquirer) {
			result = append(result, acquirer)
		}
	}
	return result, nil
}

func containsString(values []string, wanted string) bool {
	for _, value := range values {
		if value == wanted {
			return true
		}
	}
	return false
}

func decimalText(value string) bool {
	for index := range len(value) {
		if value[index] < '0' || value[index] > '9' {
			return false
		}
	}
	return true
}

func splitOptions(raw string) []string {
	parts := strings.Split(raw, ",")
	options := make([]string, 0, len(parts))
	for _, part := range parts {
		trimmed := strings.TrimSpace(part)
		if trimmed != "" {
			options = append(options, trimmed)
		}
	}
	return options
}

// splitRecords separa registros fornecidos no CLI sem transportar linhas vazias.
func splitRecords(raw string) []string {
	parts := strings.Split(raw, ";")
	records := make([]string, 0, len(parts))
	for _, part := range parts {
		if value := strings.TrimSpace(part); value != "" {
			records = append(records, value)
		}
	}
	return records
}

// printProgress informa apenas progresso agregado, sem imprimir payloads ou dados sensÃ­veis.
func printProgress(_ context.Context, current, total int64) error {
	fmt.Printf("Progresso: %d/%d\n", current, total)
	return nil
}

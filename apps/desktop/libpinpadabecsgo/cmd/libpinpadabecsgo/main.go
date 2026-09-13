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
	for {
		printMenu(cfg)
		choice := readLine(reader, "Escolha uma opcao: ")
		ctx, cancel := context.WithTimeout(context.Background(), menuTimeout)

		switch choice {
		case "1":
			handleErr(logger, "Open", svc.Open(ctx))
		case "2":
			handleErr(logger, "Close", svc.Close(ctx))
		case "3":
			fmt.Printf("Estado atual do pinpad: %s\n", stateName(svc.GetState()))
		case "4":
			info, err := svc.GetInfo(ctx)
			if handleErr(logger, "GetInfo (GIX)", err) {
				printDeviceInfo(info)
			}
		case "5":
			handleErr(logger, "Reset (CAN)", svc.Reset(ctx))
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
				fmt.Printf("Tipo de cartao: %s\n", resp.CardType)
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
			keyIndex := readInt(reader, "Indice da chave: ")
			_, err := svc.GetTracks(ctx, command.GTKRequest{Tracks: "1111", DataMethod: "50", KeyIndex: &keyIndex})
			if handleErr(logger, "GetTracks (GTK)", err) {
				fmt.Println("Trilhas recebidas e mantidas redigidas no utilitario local.")
			}
		case "20":
			_, err := svc.ContinueEMV(ctx, command.GOXRequest{AcquirerReference: "01", PinMethod: "3", KeyIndex: 1})
			handleErr(logger, "ContinueEMV (GOX)", err)
		case "21":
			_, err := svc.FinalizeEMV(ctx, command.FCXRequest{Options: "0000", Authorization: "00"})
			handleErr(logger, "FinalizeEMV (FCX)", err)
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
	fmt.Println("19) Obter trilhas apos GCX elegivel (GTK, redigido)")
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

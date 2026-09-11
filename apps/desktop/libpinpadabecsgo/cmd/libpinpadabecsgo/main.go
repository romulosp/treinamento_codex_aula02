// Command libpinpadabecsgo executa o menu local de validação manual da
// biblioteca ABECS e configura seus adaptadores sem expor serviços de rede.
package main

import (
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/application/service"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/command"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/model"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/protocol"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/config"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/logging"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/serial"
	"bufio"
	"context"
	"fmt"
	"log/slog"
	"os"
	"strconv"
	"strings"
	"time"
)

// menuTimeout e usado para operacoes que podem exigir interacao manual no
// pinpad (ex.: MNU, GKY, GCX aguardando cartao), evitando que o timeout curto
// de configuracao interrompa o teste antes da acao do operador.
const menuTimeout = 60 * time.Second

func main() {
	logger := logging.New()
	cfg, err := config.Load()
	if err != nil {
		logger.Error("invalid configuration", slog.Any("error", err))
		fmt.Fprintln(os.Stderr, err)
		os.Exit(1)
	}
	logger.Info("configuracao carregada", slog.String("port", cfg.Port), slog.Int("baudRate", cfg.BaudRate), slog.Duration("timeout", cfg.Timeout))

	tracer := logging.NewTracer()
	destination, err := configureTracer(tracer)
	if err != nil {
		logger.Error("falha ao configurar rastro serial", slog.Any("error", err))
		fmt.Fprintln(os.Stderr, err)
		os.Exit(1)
	}
	if destination != "" {
		logger.Info("rastro serial configurado", slog.String("arquivo", destination))
	}
	defer func() {
		if closeErr := tracer.Close(); closeErr != nil {
			logger.Error("falha ao fechar rastro serial", slog.Any("error", closeErr))
		}
	}()

	port := serial.New(cfg.Port, cfg.BaudRate, cfg.Timeout)
	port.SetTracer(tracer)
	svc := service.New(cfg, port)
	svc.SetTracer(tracer)
	defer svc.Shutdown()

	reader := bufio.NewReader(os.Stdin)
	runMenu(reader, svc, cfg, logger)
}

// configureTracer habilita o rastro somente quando PINPAD_LOG_FILE foi
// definido pelo operador ou pelo script local. Retorna o caminho ativo para
// exibição no CLI e preserva a biblioteca sem destino padrão implícito.
func configureTracer(tracer *logging.Tracer) (string, error) {
	destination := strings.TrimSpace(os.Getenv("PINPAD_LOG_FILE"))
	if destination == "" {
		return "", nil
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

func runMenu(reader *bufio.Reader, svc *service.Service, cfg model.PinpadConfig, logger *slog.Logger) {
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
			handleErr(logger, "Reset completo (RST)", svc.ResetPinpad(ctx))
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
			amount := readLine(reader, "Valor da transacao (centavos, ex: 000000010000): ")
			date := readLine(reader, "Data da transacao (DDMMAA): ")
			clock := readLine(reader, "Hora da transacao (HHMMSS): ")
			resp, err := svc.PurchaseGCX(ctx, amount, date, clock, false, false)
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
			_, err := svc.SendGCXInitialization(ctx)
			handleErr(logger, "SendGCXInitialization (GCX)", err)
		case "20":
			keyIndex := readInt(reader, "Indice da chave: ")
			_, err := svc.GetTracks(ctx, command.GTKRequest{Tracks: "1111", DataMethod: "50", KeyIndex: &keyIndex})
			if handleErr(logger, "GetTracks (GTK)", err) {
				fmt.Println("Trilhas recebidas e mantidas redigidas no utilitario local.")
			}
		case "21":
			_, err := svc.ContinueEMV(ctx, command.GOXRequest{AcquirerReference: "01", PinMethod: "3", KeyIndex: 1})
			handleErr(logger, "ContinueEMV (GOX)", err)
		case "22":
			_, err := svc.FinalizeEMV(ctx, command.FCXRequest{Options: "0000", Authorization: "00"})
			handleErr(logger, "FinalizeEMV (FCX)", err)
		case "23":
			keyIndex := readInt(reader, "Indice da chave: ")
			pan := readLine(reader, "PAN (nao sera exibido): ")
			message := readLine(reader, "Mensagem de PIN: ")
			_, _, err := svc.SendGPNCommandMK(ctx, keyIndex, pan, message)
			if handleErr(logger, "SendGPNCommandMK (GPN)", err) {
				fmt.Println("PIN coletado; PIN block e KSN foram redigidos.")
			}
		case "24":
			ksn := readLine(reader, "KSN (nao sera exibido): ")
			pan := readLine(reader, "PAN (nao sera exibido): ")
			message := readLine(reader, "Mensagem de PIN: ")
			_, _, err := svc.SendGPNCommandDUKPT(ctx, ksn, pan, message)
			if handleErr(logger, "SendGPNCommandDUKPT (GPN)", err) {
				fmt.Println("PIN coletado; PIN block e KSN foram redigidos.")
			}
		case "25":
			fmt.Println("DisplayQRCode requer QRCodeGenerator injetado pelo consumidor; o CLI nao adiciona gerador concreto.")
		case "26":
			_, err := svc.TransactionGCX(ctx, service.TransactionGCXRequest{})
			handleErr(logger, "TransactionGCX completo (reservado)", err)
		case "0":
			cancel()
			fmt.Println("Encerrando...")
			return
		default:
			fmt.Println("Opcao invalida.")
		}
		cancel()
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
	fmt.Println(" 6) Reset completo do pinpad (RST)")
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
	fmt.Println("19) Inicializar transacao GCX")
	fmt.Println("20) Obter trilhas apos GCX elegivel (GTK, redigido)")
	fmt.Println("21) Continuar transacao EMV (GOX)")
	fmt.Println("22) Finalizar transacao EMV (FCX)")
	fmt.Println("23) Capturar PIN MK/WK (GPN, redigido)")
	fmt.Println("24) Capturar PIN DUKPT (GPN, redigido)")
	fmt.Println("25) Exibir QR Code (requer gerador injetado)")
	fmt.Println("26) Transacao GCX completa (reservada na SPEC)")
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
// ABECS. Dados brutos não são exibidos para evitar vazamento acidental.
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

// printProgress informa apenas progresso agregado, sem imprimir payloads ou dados sensíveis.
func printProgress(_ context.Context, current, total int64) error {
	fmt.Printf("Progresso: %d/%d\n", current, total)
	return nil
}

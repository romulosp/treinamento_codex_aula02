package main

import (
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/application/service"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/model"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/config"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/logging"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/serial"
	"bufio"
	"context"
	"encoding/hex"
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

	port := serial.New(cfg.Port, cfg.BaudRate, cfg.Timeout)
	svc := service.New(cfg, port)
	defer svc.Shutdown()

	reader := bufio.NewReader(os.Stdin)
	runMenu(reader, svc, cfg, logger)
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
				fmt.Printf("Tipo de cartao: %s | PAN: %s | Nome: %s\n", resp.CardType, resp.PAN, resp.CardholderName)
			}
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
// ABECS. O hex bruto e mantido apenas como informacao de diagnostico.
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
	fmt.Printf("Resposta bruta (hex): %s\n", hex.EncodeToString(info.RawData))
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


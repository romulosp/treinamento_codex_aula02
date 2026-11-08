# Plano de implementação — revisão 3

Registrado em 2026-10-15 após SPEC_APROVADA e antes do código desta revisão. O plano original estava ausente; este plano não retroage à implementação anterior.

1. Implementar decorador port.Transport no pacote logging, sem alterações nos transports reais, protocolo ou Android. Reutilizar ValidatePacket, catálogo e decoder. Guardas de tamanho e schema antes de detalhar; limpar cópias.
2. Configurar formato no executável Bridge, falhar para valor inválido e registrar formato. BAT default DETAILED respeita override. Documentar operação e limitação dos comandos sensíveis.
3. Testes sintéticos observáveis: bytes de ida/volta inalterados, campos/CRC/HEX, fragmentação, controles agregados, seguro/DC2, desconhecido, CRC inválido, campos sensíveis e desconhecidos, troca de sessão, limite e falhas. Corrigir bounds do decoder se necessário para dados curtos.
4. go test ./... -count=1, go vet ./..., go build ./..., integração existente e smoke TCP contra executável scripted. Não executar compra física. Registrar comandos/exits e preparar binário usado pelo BAT.
5. Se Bridge físico estiver ocioso, reiniciar com a composição nova, preservando porta e perfil. Não interromper sessão ativa. Parar em IMPLEMENTADA, sem revisão formal da implementação, aprovação final ou commit.
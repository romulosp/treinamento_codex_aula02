# Tarefas: 066-lib-pinpad-abecs-go

## Regra de leitura deste arquivo

Os checks abaixo representam tarefas documentadas, não uma prova automática de que o comportamento está completo. A conversão integral Java + JNI + C somente poderá ser marcada como concluída depois da matriz legado → Go → SPEC → evidência, da revisão de implementação e da validação com pinpad físico real.

## SPECs individuais

Cada comando possui agora uma SPEC própria ou uma SPEC complementar própria:

- Ciclo de vida: `spec-command-can.md`, `spec-command-opn.md`, `spec-command-clo.md`, `spec-command-clx.md`, `spec-command-rst.md`.
- Informações/display: `spec-command-gix.md`, `spec-command-dsp.md`, `spec-command-dex.md`, `spec-command-mnu.md`, `spec-command-dsi.md`, `spec-command-qrcode.md`.
- Multimídia: `spec-command-mli.md`, `spec-command-mlr.md`, `spec-command-mle.md`.
- Tabelas EMV: `spec-command-tli.md`, `spec-command-tlr.md`, `spec-command-tle.md`.
- Teclas: `spec-command-gky.md`.
- Transação/cartão: `spec-command-gcx.md`, `spec-command-gtk.md`, `spec-command-gox.md`, `spec-command-fcx.md`.
- PIN: `spec-command-gpn.md`.
- Transversal: `spec-logging.md`, `spec-infra-serial-cancel.md`, `spec-protocolo-seguro.md`.

Todas as SPECs individuais permanecem sujeitas a revisão formal. A existência do arquivo não autoriza implementação antes de `SPEC_APROVADA`.

## Pré-condições

- [x] Receber definição funcional da Change 066.
- [x] Definir `groupId`, `artifactId`, módulo, diretório e executável.
- [x] Remover REST, WebSocket, UI e servidores do escopo.
- [x] Aprovar `go.bug.st/serial` como adaptador serial.
- [ ] Revisar e aprovar formalmente a SPEC após inclusão do RF-016.

## Implementação

- [x] Criar `apps/desktop/libpinpadabecsgo/go.mod` com o módulo aprovado.
- [x] Criar README e executável `cmd/libpinpadabecsgo` para validação local.
- [x] Criar pacotes de domínio conforme o DESIGN.
- [x] Implementar `PinpadConfig` e carregamento/validação de ambiente.
- [x] Implementar modelos de estado, resposta, dispositivo, display e GCX.
- [x] Implementar catálogo de comandos e status ABECS.
- [x] Gerar constantes de protocolo, `RSP_DATID`, mídia e limites definidos na SPEC.
- [x] Gerar catálogo completo de `RSP_STAT`, parâmetros `SPE_xxx`, códigos `GKY`, `GPN` e `GCX`.
- [x] Implementar erros sentinela e tipos de erro.
- [x] Implementar CRC-16-CCITT.
- [x] Implementar ApplySubstitution e RemoveSubstitution.
- [x] Implementar BuildPacket e ReadFullResponse.
- [x] Implementar parser ABECS e tags obrigatórias.
- [x] Implementar parser BER-TLV.
- [x] Implementar porta `SerialPort`, adaptador real e fake.
- [x] Implementar `SessionManager` com relógio injetável.
- [x] Implementar `CommandQueue` FIFO, capacidade 100, `Enqueue` não bloqueante e `ErrQueueFull`.
- [x] Implementar `Submit` bloqueante com espera por resultado ou cancelamento de contexto.
- [x] Implementar worker único, contexto, cancelamento e shutdown.
- [x] Implementar `PinpadService` e comandos CAN/OPN/GIX/CLO.
- [x] Implementar builders, parsers e fachada de display DSP/DEX/MNU.
- [x] Implementar fluxo serial de multimídia DSI/MLI/MLR/MLE.
- [x] Implementar fluxo serial de tabelas EMV TLI/TLR/TLE.
- [x] Implementar GKY, incluindo parser de teclas e timeout. (builder, parser e fachada WaitForKeyPress implementados)
- [x] Implementar GCX, modelo, builder, parser e fluxo protegido (subconjunto valor/data/hora/opções).
- [x] Implementar stub `ErrNotImplemented` para `TransactionGCX` com parâmetros completos.
- [x] Implementar GPN MK/WK e DUKPT, validações e parser binário.
- [x] Implementar RST e confirmação de resposta.
- [x] Especificar e implementar fachada de ciclo de vida e configuração.
- [x] Implementar fachada de comandos básicos, display e capabilities.
- [x] Implementar fachada de imagens, QR Code (via `QRCodeGenerator` injetável) e progresso.
- [x] Implementar `ErrQRCodeGeneratorNotConfigured` e validação de tamanho/margem do QR Code.
- [x] Implementar fachada de carga completa de tabelas EMV.
- [x] Implementar fachada transacional GCX (subconjunto especificado) sem expor `SendRawCommand`.
- [x] Implementar fachada GKY, RST e GPN.
- [x] Testar concorrência, cancelamento, shutdown e redaction na fachada.
- [x] Revisar e completar o contrato da fila FIFO, cancelamento e shutdown.
- [x] Revisar e completar o contrato do `SessionManager` e expiração de 300 segundos.
- [x] Confirmar que a bridge HTTP não será convertida nem adicionada ao módulo Go.
- [x] Criar `start_aplication.bat` para execução local sem privilégios administrativos, com `COM7` temporário.
- [ ] Confirmar que `.gocache`, `.gomodcache`, `.bin` e executáveis locais não serão versionados.
- [ ] Implementar logging `slog` com redaction e tracer SPE/PP/RSP conforme `spec-logging.md`.

## Documentação obrigatória do código

- [ ] Ler e aplicar `.agents/skills/golang-documentation/SKILL.md` antes de gerar ou alterar código Go.
- [ ] Adicionar comentário de pacote a todos os pacotes Go entregues.
- [ ] Adicionar comentários Go a todas as funções, métodos, tipos, interfaces, constantes e variáveis exportadas.
- [ ] Documentar funções internas complexas de protocolo, framing, parser, serial, concorrência, segurança, redaction, fila, worker e cancelamento.
- [ ] Documentar builders, parsers, modelos, erros e fachadas conforme as SPECs individuais dos comandos.
- [ ] Criar `ExampleXxx` para APIs públicas e fluxos relevantes quando a documentação executável for aplicável.
- [ ] Garantir que código gerado por templates ou ferramentas também seja emitido com documentação; ajustar o gerador quando necessário.
- [ ] Verificar ausência de segredos e dados sensíveis em comentários, exemplos, fixtures e documentação.
- [ ] Registrar a inspeção documental na revisão da implementação e em `validation.md`.

## Conversão integral do legado

- [ ] Inventariar todas as classes Java, funções JNI e funções C/C++.
- [ ] Criar matriz de rastreabilidade legado → pacote Go → SPEC → teste → evidência física.
- [ ] Implementar OPN/CLO/CLX e o protocolo seguro RSA/AES conforme `spec-protocolo-seguro.md`.
- [ ] Implementar GTK, GOX e FCX em modelos próprios, sem misturar dados com GCX.
- [ ] Implementar `TransactionGCX` somente após aprovação da SPEC de seus parâmetros completos.
- [ ] Confirmar que cada comando possui builder, parser, fluxo serial, timeout, cancelamento, logging e validação física.
- [ ] Confirmar que cada pacote e símbolo implementado atende RF-017 e CA-021/CA-023.

## Revisão e validação

- [x] Inventariar arquivos `.go` e testes aplicáveis.
- [x] Criar testes orientados a tabela para CRC, bytes, substitution e parsers.
- [x] Criar testes dos builders avançados e fake serial.
- [ ] Criar testes de sessão, estados, erros, fila e shutdown.
- [x] Executar `gofmt`.
- [x] Executar `go vet ./...`.
- [x] Executar `go test ./...` (compilação concluída; execução bloqueada por política de grupo do Windows).

## Estado da implementação

`REPROVADA` na revisão da implementação. Retornar à implementação para corrigir os achados IMP-REV-001 a IMP-REV-015 antes da validação. A conversão integral não está concluída.
- [ ] Executar `go test ./... -coverprofile=coverage.out`.
- [ ] Executar `go tool cover -func=coverage.out`.
- [ ] Executar `go test -race ./...`.
- [ ] Validar build Windows/Linux ou registrar limitação objetiva.
- [ ] Executar auditoria de segurança aplicável e revisar dependências.
- [ ] Executar revisão de documentação Go conforme `golang-documentation` e registrar lacunas.
- [ ] Registrar evidências em `validation.md`.
- [ ] Executar revisão da implementação, validação, aprovação e encerramento conforme workflow.

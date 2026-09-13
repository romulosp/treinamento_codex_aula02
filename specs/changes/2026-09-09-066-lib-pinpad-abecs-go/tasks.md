# Tarefas: 066-lib-pinpad-abecs-go

Autor: Rômulo Penha

## Regra de leitura deste arquivo

Os checks abaixo representam tarefas documentadas, não uma prova automática de que o comportamento está completo. A integração completa ABECS v2.12 somente poderá ser marcada como concluída depois da matriz protocolo → Go → SPEC → evidência, da revisão de implementação e da validação com pinpad físico real.

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
- [x] Revisar e aprovar formalmente a SPEC após resolução dos achados REV-001 a REV-006.

## Implementação

- [x] Criar `apps/desktop/libpinpadabecsgo/go.mod` com o módulo aprovado.
- [x] Criar README e executável `cmd/libpinpadabecsgo` para validação local.
- [x] Expor no menu local as fachadas implementadas de ciclo de vida, GIX, display, sessão segura, multimídia, tabelas EMV, GCX, GTK, GOX, FCX e GPN, preservando a redaction de dados sensíveis e as indisponibilidades contratuais de QR e `TransactionGCX` completo.
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
- [x] Cobrir a resposta física MNU com comprimento `006` e seleção no campo
  TLV `0x804D`, preservando índices de dois dígitos.
- [x] Implementar fluxo serial de multimídia DSI/MLI/MLR/MLE.
- [x] Implementar fluxo serial de tabelas EMV TLI/TLR/TLE.
- [x] Implementar GKY, incluindo parser de teclas e timeout. (builder, parser e fachada WaitForKeyPress implementados)
- [x] Corrigir o builder GCX para parâmetros ABECS `0013`, `0015`, `0016` e
  `0017`, com valores N12/N6/N6/N5 e validação antes da serial.
- [x] Remover o GCX preliminar com data/hora zeradas, sua configuração e sua
  opção de menu; `PurchaseGCX` deve emitir exatamente um GCX.
- [x] Criar regressão unitária byte a byte para CA-GCX-008 a CA-GCX-010.
- [x] Implementar o consumo de zero ou mais notificações `NTM` antes da resposta
  final GCX, sem escrita de ACK e sem interpretação TLV da mensagem.
- [x] Perguntar na opção 11 se o GCX aceita chip/tarja ou
  chip/tarja/contactless, se mostra ou oculta o valor, e validar as escolhas.
- [x] Criar as regressões automatizadas CA-GCX-011 e CA-GCX-012.
- [x] Iniciar o prazo da opção 11 após a coleta das entradas e preservar o
  contexto do consumidor no GCX, sem aplicar o timeout genérico da configuração.
- [x] Criar a regressão automatizada CA-GCX-013 para propagação do prazo.
- [x] Implementar stub `ErrNotImplemented` para `TransactionGCX` com parâmetros completos.
- [x] Implementar GPN MK/WK e DUKPT, validações e parser binário.
- [x] Remover RST por ausência no manual ABECS 2.12.
- [x] Especificar e implementar fachada de ciclo de vida e configuração.
- [x] Implementar fachada de comandos básicos, display e capabilities.
- [x] Implementar fachada de imagens, QR Code (via `QRCodeGenerator` injetável) e progresso.
- [x] Implementar `ErrQRCodeGeneratorNotConfigured` e validação de tamanho/margem do QR Code.
- [x] Implementar fachada de carga completa de tabelas EMV.
- [x] Implementar fachada transacional GCX (subconjunto especificado) sem expor `SendRawCommand`.
- [x] Implementar fachadas GKY e GPN; reset usa CAN/EOT e RST foi excluído.
- [x] Testar concorrência, cancelamento, shutdown e redaction na fachada.
- [x] Revisar e completar o contrato da fila FIFO, cancelamento e shutdown.
- [x] Revisar e completar o contrato do `SessionManager` e expiração de 300 segundos.
- [x] Confirmar que a bridge HTTP não será convertida nem adicionada ao módulo Go.
- [x] Criar `start_aplication.bat` para execução local sem privilégios administrativos, com `COM7` temporário.
- [x] Confirmar que `.gocache`, `.gomodcache`, `.bin` e executáveis locais não serão versionados.
- [x] Implementar logging `slog` com redaction e tracer SPE/PP/RSP conforme
  `spec-logging.md`, incluindo destino absoluto canônico
  `<raiz-do-módulo>/logs/LogPinpadAbecs.txt`, `PINPAD_LOG_FILE`, marcador de
  ativação, caminho ativo exibido, eventos de ciclo de vida, cobertura de todos
  os comandos tipados e coleta segura de evidências.
- [x] Remover a dependência do diretório de trabalho na resolução do arquivo de
  rastro e impedir a criação de `cmd/libpinpadabecsgo/logs/LogPinpadAbecs.txt`
  ou de qualquer outro arquivo homônimo acidental.
- [x] Fazer o adaptador serial real emitir uma única linha `SPE` após escrita
  integral confirmada e uma linha `PP` por leitura não vazia; manter `RSP` no
  serviço após interpretação do status, sem duplicação entre camadas.
- [x] Criar teste de integração do adaptador com pacote GIX conhecido que
  compare byte a byte o hexadecimal de `SPE` com o buffer escrito e cada `PP`
  com o retorno de leitura, incluindo ACK isolado, resposta completa e resposta
  fragmentada.
- [x] Propagar ou tornar observável qualquer falha de escrita/flush/fechamento
  do tracer; nenhuma falha de persistência poderá ser descartada silenciosamente.

## Documentação obrigatória do código

- [x] Ler e aplicar `.agents/skills/golang-documentation/SKILL.md` antes de gerar ou alterar código Go.
- [ ] Adicionar comentário de pacote a todos os pacotes Go entregues.
- [ ] Adicionar comentários Go a todas as funções, métodos, tipos, interfaces, constantes e variáveis exportadas.
- [ ] Documentar funções internas complexas de protocolo, framing, parser, serial, concorrência, segurança, redaction, fila, worker e cancelamento.
- [ ] Documentar builders, parsers, modelos, erros e fachadas conforme as SPECs individuais dos comandos.
- [ ] Criar `ExampleXxx` para APIs públicas e fluxos relevantes quando a documentação executável for aplicável.
- [ ] Garantir que código gerado por templates ou ferramentas também seja emitido com documentação; ajustar o gerador quando necessário.
- [ ] Verificar ausência de segredos e dados sensíveis em comentários, exemplos, fixtures e documentação.
- [ ] Registrar a inspeção documental na revisão da implementação e em `validation.md`.

## Integração completa do protocolo ABECS v2.12

- [ ] Inventariar todas as funções e fluxos do protocolo ABECS v2.12 relevantes ao escopo.
- [ ] Criar matriz de rastreabilidade protocolo → pacote Go → SPEC → teste → evidência física.
- [ ] Implementar OPN/CLO e o protocolo seguro RSA/AES conforme `spec-protocolo-seguro.md`; implementar CLX como fluxo visual independente conforme `spec-command-clx.md`.
- [ ] Implementar GTK, GOX e FCX em modelos próprios, sem misturar dados com GCX.
- [ ] Implementar `TransactionGCX` somente após aprovação da SPEC de seus parâmetros completos.
- [ ] Confirmar que cada comando possui builder, parser, fluxo serial, timeout, cancelamento, logging e validação física.
- [ ] Confirmar que cada pacote e símbolo implementado atende RF-017 e CA-021/CA-023.

## Revisão e validação

- [x] Inventariar arquivos `.go` e testes aplicáveis.
- [x] Criar testes orientados a tabela para CRC, bytes, substitution e parsers.
- [x] Criar testes dos builders avançados e fake serial.
- [x] Criar testes de sessão, estados, erros, fila e shutdown.
- [x] Executar `gofmt`.
- [x] Executar `go vet ./...`.
- [x] Executar `go test ./...` (compilação concluída; execução bloqueada por política de grupo do Windows).

## Estado da implementação

`IMPLEMENTADA`. O contrato de logging revisado em 2026-09-12 foi implementado,
testado e exercitado no pinpad físico para o fluxo `Open → GIX → Close`. A
serialização GCX foi corrigida conforme a seção 3.7.1 do manual ABECS v2.12 e
possui regressão unitária byte a byte. O fluxo também consome notificações NTM
até a resposta final e o CLI expõe as quatro combinações válidas de GCXOPT. A
opção 11 também inicia seu prazo somente após as entradas, e o GCX preserva o
contexto do consumidor sem ser limitado pelo timeout genérico. A
Change aguarda revisão independente da implementação e validação formal; os
demais fluxos físicos completos continuam pendentes conforme suas SPECs.
- [x] Executar `go test ./... -coverprofile=coverage.out`.
- [x] Executar `go tool cover -func=coverage.out` (perfil `coverage` no PowerShell; cobertura total aferida: 81,7%).
- [x] Executar `go test -race ./...` (não suportado pela distribuição Go `windows/386`; limitação de ambiente registrada).
- [x] Validar build Windows/Linux ou registrar limitação objetiva.
- [ ] Executar auditoria de segurança aplicável e revisar dependências.
- [x] Testar a resolução do mesmo caminho absoluto a partir da raiz do módulo e
  de `cmd/libpinpadabecsgo`, comprovando que somente um arquivo é criado.
- [x] Testar a matriz de `CAN`, `OPN`, `CLO`, `CLX`, `GIX`, `DSP`, `DEX`, `MNU`,
  `DSI`, `MLI`, `MLR`, `MLE`, `TLI`, `TLR`, `TLE`, `GKY`, `GCX`, `GTK`, `GOX`,
  `FCX` e `GPN` quanto a `CMD=`, ordem e redaction aplicável.
- [ ] Executar revisão de documentação Go conforme `golang-documentation` e registrar lacunas.
- [x] Registrar evidências em `validation.md`.
- [ ] Executar revisão da implementação, validação, aprovação e encerramento conforme workflow.


## Correção de conformidade ABECS 2.12 — 2026-09-13

- [x] Revisar o manual 2.12 e registrar divergências em revisão formal.
- [x] Corrigir a fonte de verdade em `spec-conformidade-abecs-v212.md`.
- [ ] Corrigir transporte ACK/NAK, retransmissão, prazos e CAN/EOT.
- [ ] Corrigir OPN clássico/seguro, CLO e proteção de CLO/CLX.
- [ ] Corrigir builders e parsers de display, MNU, GKY e GPN.
- [ ] Corrigir multimídia MLI/MLR/MLE/DSI e CRC16.
- [ ] Corrigir TLI/TLR/TLE e semântica dos status 000/020.
- [ ] Corrigir data GCX, contratos GTK/GOX/FCX e validações condicionais.
- [ ] Corrigir PP_MFSUP, PP_DSPGRSZ e catálogo de status.
- [x] Remover RST do código, fachada e CLI.
- [ ] Substituir falsos oráculos por testes byte a byte derivados do manual.
- [ ] Executar revisão de implementação e validação formal.

## Estado atual da implementação

`EM_IMPLEMENTACAO`. A revisão de 2026-09-12 reprovou a implementação anterior;
os checks históricos acima não comprovam conformidade com o manual 2.12.

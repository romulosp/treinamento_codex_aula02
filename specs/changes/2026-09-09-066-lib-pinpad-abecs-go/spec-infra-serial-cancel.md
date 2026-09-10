# SPEC: 066-lib-pinpad-abecs-go — Infraestrutura serial: cancelamento de leitura

## Status
`RASCUNHO`

## Papel da infraestrutura

Esta SPEC não define um comando ABECS. Ela define a porta de transporte compartilhada por todos os comandos e garante que o contexto da operação alcance o ponto em que o driver serial bloqueia. O cancelamento do contexto deve interromper a espera local; o fluxo de protocolo decide separadamente se também precisa transmitir `CAN` ou aguardar `EOT`.

## Identificação

- pacote afetado: `internal/infrastructure/serial` (`SerialPort.Read`, `Adapter.Read`)
- Esta SPEC é complementar a `spec.md` (RF-007) e não substitui nenhum RF já aprovado; ela detalha e corrige exclusivamente o cancelamento de leituras seriais em andamento. Não é uma SPEC de comando ABECS; trata de infraestrutura de transporte comum a todos os comandos.

## Motivação

**IMP-REV-011** — `SerialPort.Read()` não recebe `context.Context`, e o caminho de leitura não demonstra interrupção garantida durante uma leitura bloqueante cancelada, contrariando RF-007 e o critério CA-015 de `spec.md` (preservação de bytes excedentes e leitura frame-aware) quando combinado com cancelamento de operação.

## Referências e dependências

- `spec.md` RF-007 (porta `SerialPort`), RF-009 (worker/fila, cancelamento via contexto).
- `go.bug.st/serial` (timeout de leitura configurável via `SetReadTimeout`, já usado em `Adapter.Open`).
- `.agents/skills/golang-context/SKILL.md`, `.agents/skills/golang-concurrency/SKILL.md`.

## Contrato de transporte

- `SerialPort` continua sendo a única porta usada pelo serviço.
- O adaptador real é responsável por 8/N/1, baud rate, timeout de leitura, abertura, fechamento e observação dos bytes.
- O adaptador não deve interpretar tags de comando, status ABECS ou dados de negócio.
- O `byteStream` deve preservar bytes excedentes mesmo quando uma leitura for cancelada depois de receber parte de um frame.
- Cancelamento não pode transformar silenciosamente `context.Canceled` em `ErrTimeout`.

## Requisitos funcionais

### RF-SER-001 — Assinatura com contexto

A interface `SerialPort` deverá expor `Read(ctx context.Context) ([]byte, error)`, substituindo a assinatura atual `Read() ([]byte, error)`. Todos os chamadores internos (`byteStream`, `protocol.Reader`, adaptadores de teste) deverão ser atualizados para propagar o contexto da operação corrente.

### RF-SER-002 — Cancelamento efetivo

Quando `ctx` for cancelado (deadline ou `Cancel()`) durante uma leitura em andamento no `Adapter` real, `Read` deverá retornar em tempo limitado (não superior ao timeout de leitura já configurado na porta, RF-002) com o erro do contexto (`ctx.Err()`), sem deixar a goroutine presa aguardando o driver serial indefinidamente. Como `go.bug.st/serial` não aceita um contexto diretamente, a implementação deverá usar o timeout de leitura já configurado (`SetReadTimeout`) como cota máxima de bloqueio por chamada, verificando `ctx.Err()` antes e depois de cada tentativa de leitura, de forma que uma sequência de leituras curtas permita o cancelamento aparecer em, no máximo, um timeout de leitura após o cancelamento.

### RF-SER-003 — Assinatura consistente em todos os adaptadores

Qualquer outro adaptador de `SerialPort` existente no código (usado apenas para testes unitários de baixo nível de framing/CRC, já presentes no projeto) deverá também adotar `Read(ctx context.Context) ([]byte, error)`, para manter uma única interface `SerialPort` em todo o módulo. A validação funcional de cancelamento desta SPEC, porém, é feita com o `Adapter` real conectado a um pinpad físico via porta serial real.

### RF-SER-004 — Compatibilidade com o rastro de comunicação

Esta mudança de assinatura não altera o comportamento de logging definido em `spec-logging.md`; a instrumentação de leitura (`PP`) continua registrando exatamente os bytes retornados por `Read`, agora com contexto propagado.

## Requisitos não funcionais

- Sem `panic` para cancelamento; sempre erro Go retornado.
- Sem vazamento de goroutine comprovável por `go test -race` com um cenário de leitura cancelada.

## Regras de negócio

- Cancelamento de contexto durante leitura serial é sempre reportado como erro de cancelamento, nunca como timeout de protocolo (`ErrTimeout`), para permitir que o chamador distinga as duas causas.

## Cenários e critérios de aceite

Os cenários de integração de cancelamento são validados por comunicação serial real com um pinpad físico conectado. Testes unitários podem usar adaptadores determinísticos somente para verificar assinatura, propagação de contexto, framing/CRC e ausência de goroutine presa; eles não substituem a evidência do adaptador real.

- [ ] **CA-SER-001:** com contexto já cancelado antes da chamada, `Adapter.Read(ctx)` contra a porta serial real retorna `ctx.Err()` imediatamente, sem iniciar nova tentativa de leitura no driver.
- [ ] **CA-SER-002:** teste com `Adapter` real conectado ao pinpad físico demonstra que cancelar o contexto durante uma leitura em andamento faz `Read` retornar em tempo limitado com o erro do contexto.
- [ ] **CA-SER-003:** `byteStream` e `protocol.ReadFullResponse`, exercitados contra o pinpad real, continuam funcionando corretamente com a nova assinatura, sem regressão nos cenários de frame completo, ACK/NAK/EOT.
- [ ] **CA-SER-004:** `go test -race ./...` cobre o cenário de cancelamento concorrente (com o `Adapter` real ou com o adaptador de teste de baixo nível) sem apontar corrida de dados.

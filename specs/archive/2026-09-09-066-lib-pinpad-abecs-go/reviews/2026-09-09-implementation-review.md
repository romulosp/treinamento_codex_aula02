# Revisão da implementação — 066-lib-pinpad-abecs-go

**Data:** 2026-09-09  
**Change:** `2026-09-09-066-lib-pinpad-abecs-go`  
**Escopo revisado:** código Go em `apps/desktop/libpinpadabecsgo/`, SPEC, DESIGN, tasks e testes.  
**Estado da Change no início:** `IMPLEMENTADA`.

## Evidências verificadas

- O módulo está no diretório aprovado e usa `br.com.romulopenha/lib-pinpad-abecs-go`.
- `go build ./...` concluído com sucesso.
- `go vet ./...` concluído com sucesso.
- O executável de testes compila, mas a execução dos binários continua bloqueada pela política de grupo do Windows (`fork/exec ...test.exe: Este programa está bloqueado por uma política de grupo`).
- Não foram encontrados pacotes REST, WebSocket, UI, gRPC ou servidor.
- A dependência serial está isolada no adaptador de infraestrutura.
- Existem testes para partes de protocolo, CRC, bytes, BER-TLV, sessão, serviço e fila, mas a matriz exigida pela SPEC não está completa.

## Divergências

### IMP-REV-001 — Leitura de frames perde bytes excedentes

- **Severidade:** Alta
- **Requisito:** RF-004/RF-007; o leitor deve aceitar leituras seriais que contenham múltiplos bytes e preservar o frame completo.
- **Evidência:** `protocol.ReadFullResponse` processa o chunk até `ETB` e descarta bytes que estejam depois do `ETB`; `Service.exchange` também consome o primeiro retorno serial esperando somente o ACK. Um retorno contendo ACK, payload e CRC em uma única leitura perde dados e pode produzir `ErrInvalidResponse` ou bloqueio.
- **Impacto:** respostas válidas podem falhar dependendo da segmentação entregue pelo driver serial.
- **Ação necessária:** introduzir buffer de leitura por instância/transport adapter ou um leitor frame-aware que preserve bytes excedentes entre chamadas; cobrir ACK+frame e frame+CRC no mesmo chunk.

### IMP-REV-002 — `CommandQueue.Enqueue` não possui semântica de enfileiramento

- **Severidade:** Alta
- **Requisito:** RF-009; `Enqueue` deve retornar erro de capacidade sem bloquear o produtor, enquanto o worker executa FIFO.
- **Evidência:** `Enqueue` chama `Submit`, que aguarda o resultado da execução do comando. Portanto, o produtor permanece bloqueado até o hardware concluir a operação.
- **Impacto:** a API pública não representa a fila assíncrona aprovada e pode causar bloqueios indevidos em chamadas de enfileiramento.
- **Ação necessária:** separar `Enqueue` (inserção não bloqueante) de `Submit` (inserção e espera de resultado), mantendo `ErrQueueFull` imediato.

### IMP-REV-003 — Shutdown não cancela operação em andamento nem acorda todos os consumidores

- **Severidade:** Alta
- **Requisito:** DESIGN decisão 7 e RF-009; shutdown deve cancelar worker/operações e ser seguro.
- **Evidência:** `Queue.Stop` fecha `done`, aguarda o worker e limpa itens pendentes, mas não cancela o contexto da operação em execução. Consumidores de `Submit` sem contexto cancelável podem continuar aguardando; itens removidos por `Clear` não recebem resultado.
- **Impacto:** risco de goroutine presa e encerramento não determinístico.
- **Ação necessária:** usar contexto interno de shutdown, propagar cancelamento ao comando em execução e concluir itens pendentes com erro definido; adicionar teste de shutdown com comando bloqueante.

### IMP-REV-004 — Matriz de testes da SPEC não está comprovada

- **Severidade:** Média
- **Requisito:** CA-003 a CA-009 e CA-011/CA-012.
- **Evidência:** não há evidência executável registrada para cobertura de CRC com todos os vetores, CRC inválido, NAK, timeout, CAN/EOT, OPN/ACK, GIX/ACK+payload, CLO/ACK, capacidade 100, `ErrQueueFull`, cancelamento, shutdown e estados. A execução de testes está bloqueada pelo Windows Group Policy.
- **Impacto:** os critérios de aceite não podem ser declarados validados; não é permitido inventar percentual de cobertura.
- **Ação necessária:** completar os testes e registrar na validação quais foram executados, quais foram apenas compilados e a limitação ambiental.

### IMP-REV-005 — Parser ABECS aceita respostas inválidas silenciosamente

- **Severidade:** Média
- **Requisito:** RF-005/RF-006; pacote inválido, truncamento e NAK devem ser distinguíveis.
- **Evidência:** `ParseAbecsResponse` interrompe o parsing quando um TLV excede o buffer e retorna resposta sem erro; para NAK retorna um erro genérico em vez de `ErrNakReceived`.
- **Impacto:** truncamento pode ser tratado como resposta parcialmente válida e consumidores não conseguem usar o erro sentinela aprovado.
- **Ação necessária:** retornar `ErrInvalidResponse` para truncamento e `ErrNakReceived` para NAK, com testes específicos.

### IMP-REV-006 — Contratos futuros ainda são genéricos

- **Severidade:** Baixa
- **Requisito:** RF-010/CA-010; contratos, tipos, builders, constantes e modelos futuros devem existir sem regra transacional.
- **Evidência:** há catálogo de tipos e um builder genérico, mas não existem contratos/modelos nomeados para todos os comandos avançados e GCX futuro não possui fluxo. A suficiência do contrato não está demonstrada por testes ou documentação de cada comando.
- **Impacto:** consumidores futuros não têm uma superfície claramente identificável para todos os comandos reservados.
- **Ação necessária:** documentar ou completar contratos mínimos nomeados sem adicionar regra de negócio.

## Resultado

## Reavaliação após expansão da SPEC

### IMP-REV-007 — Alta — `GetDisplayCapabilities` não implementa o contrato

`internal/application/service/service.go` retorna `DisplayCapabilities{}` sem executar GIX nem interpretar as tags de capacidade previstas em RF-010. A fachada não entrega capacidades reais do pinpad.

### IMP-REV-008 — Alta — resposta GCX não é interpretada

`GCXResponseFromResponse` preserva somente dados brutos; `ParsedEMVData` e os campos funcionais do modelo não são preenchidos a partir das tags previstas em RF-012.7.

### IMP-REV-009 — Alta — TLI não trata `StatusTableVersionDifferent`

`LoadCompleteEMVTable` trata qualquer erro de `TableLoadInitiate` como falha fatal e não reconhece o status 020 conforme CA-016c.

### IMP-REV-010 — Alta — ausência de redaction e logging operacional

Não há evidência de redaction efetiva para PAN, TRACK2, PIN, KSN e EMV sensível nem logs estruturados com operação, duração e resultado conforme RF-011/CA-013.

### IMP-REV-011 — Alta — cancelamento de leitura serial não comprovado

`SerialPort.Read()` não recebe contexto e o caminho de leitura não demonstra interrupção garantida durante uma leitura bloqueante cancelada, contrariando RF-007 e CA-015.

### IMP-REV-012 — Média — QR Code sem aviso estruturado de posição

`QRCodeResult` possui `PositionSupported`, mas não possui aviso ou motivo estruturado para `xPos`/`yPos` não suportados conforme CA-016a.

### IMP-REV-013 — Média — `GetInfoRaw` ausente

RF-013 especifica `GetInfo` e `GetInfoRaw`, mas a interface/fachada implementa somente `GetInfo`.

### IMP-REV-014 — Média — testes e evidências incompletos

`tasks.md` mantém pendentes testes de sessão, estados, erros, fila e shutdown; não existe `validation.md`; não há cobertura registrada nem testes explícitos para frames divididos, status 020, redaction e limites avançados.

### IMP-REV-015 — Média — coordenação de shutdown e submissão não comprovada

O estado do serviço é liberado antes de `queue.Submit`; `Shutdown` pode ocorrer entre essas operações, permitindo tentativa de submissão após o encerramento sem contrato testado.

`REPROVADA`

A implementação não está aprovada para validação formal enquanto os achados `IMP-REV-001` a `IMP-REV-015` permanecerem abertos. A Change deve retornar à fase de implementação. A aprovação para arquivamento, atualização de `specs/system/` e commit permanece deliberadamente pendente e deverá ser realizada pelo usuário após a validação manual, conforme solicitado.

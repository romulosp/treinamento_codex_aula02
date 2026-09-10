# SPEC: 066-lib-pinpad-abecs-go — Comando TLI (início de carga de tabela EMV)

## Status
`RASCUNHO`

## Papel do comando no projeto

`TLI` inicia a carga de uma tabela EMV. Embora o fluxo de aplicação seja normalmente `TLI → TLR* → TLE`, os três comandos possuem contratos de transporte e respostas diferentes. Esta SPEC detalha o início do fluxo; `TLR` e `TLE` devem possuir SPECs próprias e não podem ser considerados implementados somente por existirem no mesmo método de orquestração.

## Identificação

- pacote afetado: `internal/application/service` (`TableLoadInitiate`, `LoadCompleteEMVTable`)
- Esta SPEC é complementar a `spec.md` (RF-012.5, RF-013 seção "Tabelas EMV") e não substitui nenhum RF já aprovado; ela detalha e corrige exclusivamente o tratamento do status de `TLI`.

## Motivação

**IMP-REV-009** — `LoadCompleteEMVTable` trata qualquer erro de `TableLoadInitiate` como falha fatal e não reconhece o status `020` (`ST_TABVERDIF`/`StatusTableVersionDifferent`) como resultado específico e não fatal de `TLI`, contrariando o critério CA-016c aprovado em `spec.md`.

## Referências e dependências

- `spec.md` RF-006 (`StatusTableVersionDifferent`), RF-013 seção "Tabelas EMV" ("reconhecimento de `StatusTableVersionDifferent` como resultado específico de TLI, conforme regra do protocolo").
- Catálogo de status ABECS (`ST_TABVERDIF = "020"`) já existente em `internal/domain/state` ou equivalente.

## Contrato de transporte

- O comando deve ser enviado pela fila/worker único.
- O payload lógico contém o adquirente e a versão da tabela nos formatos definidos pelo manual.
- A resposta deve ser parseada como `model.Response`, preservando status e dados brutos.
- O rastro deve registrar `SPE CMD=TLI`, leituras `PP` e `RSP CMD=TLI STATUS=...`.
- A operação deve respeitar contexto, timeout, cancelamento e retransmissão ABECS.

## Requisitos funcionais

### RF-TLI-001 — Distinção do status 020

`TableLoadInitiate` deverá devolver a resposta (`*model.Response`) normalmente quando o `StatusCode` for `000` (OK) ou `020` (`StatusTableVersionDifferent`); ambos são considerados sucesso de protocolo (o pinpad respondeu e o comando foi aceito), sem erro Go. Para qualquer outro `StatusCode` de erro (ex.: `010`, `011`, `021`, `040` etc.), o comportamento atual (erro) é mantido.

O status `020` não significa que a biblioteca deve ignorar silenciosamente a resposta. Ele deve permanecer disponível ao chamador para observabilidade e para a decisão de não transmitir os registros novamente.

### RF-TLI-002 — Sinalização ao chamador de `LoadCompleteEMVTable`

`LoadCompleteEMVTable` deverá permitir que o chamador saiba quando a tabela de destino já está na versão esperada (status `020`), sem tratar isso como falha fatal do fluxo. A forma de sinalização é um valor de retorno adicional (por exemplo, um booliano `versionAlreadyCurrent` ou um resultado tipado), documentado na assinatura, mantendo compatibilidade retroativa mínima com o restante da orquestração TLI → TLR → TLE.

### RF-TLI-003 — Continuação do fluxo

Quando `TableLoadInitiate` responder com status `020`, `LoadCompleteEMVTable` não deverá enviar `TLR`/`TLE` (a tabela já está na versão esperada), retornando imediatamente com a sinalização de RF-TLI-002 e sem erro.

## Requisitos não funcionais

- Nenhuma heurística adicional sobre outros status não documentados nesta SPEC ou em `spec.md`.

## Regras de negócio

- Status `020` é sucesso de protocolo, não erro; qualquer outro status de erro em `TLI` permanece erro fatal do fluxo `LoadCompleteEMVTable`.

## Cenários e critérios de aceite

Todos os cenários abaixo são validados por comunicação serial real com um pinpad físico conectado, provocando os status reais de protocolo descritos (nunca por resposta simulada/mock).

- [ ] **CA-TLI-001:** `TableLoadInitiate`, executado contra um pinpad real que responda status `020` (tabela já na versão enviada), não retorna erro Go.
- [ ] **CA-TLI-002:** `LoadCompleteEMVTable`, com o pinpad real respondendo `020` ao `TLI`, não envia `TLR` nem `TLE` e sinaliza ao chamador que a versão já está corrente.
- [ ] **CA-TLI-003:** `LoadCompleteEMVTable`, com o pinpad real respondendo `000` ao `TLI`, prossegue normalmente para `TLR`/`TLE`, sem sinalizar versão já corrente.
- [ ] **CA-TLI-004:** `LoadCompleteEMVTable`, com o pinpad real respondendo um status de erro (ex.: `021`) ao `TLI`, continua retornando erro fatal, sem enviar `TLR`/`TLE`.

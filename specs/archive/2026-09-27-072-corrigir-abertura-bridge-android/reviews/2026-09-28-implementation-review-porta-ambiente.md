# Revisão da implementação — 072 — porta por ambiente

## Estado

`IMPLEMENTACAO_APROVADA`

## Escopo revisado

Revisão posterior à emenda aprovada de 28/09/2026. Foram comparados
`proposal.md`, `spec.md`, `DESIGN.md`, `implementation-plan.md` e `tasks.md` com
launcher/helper, Bridge, ownership, tracer, transporte serial/scripted,
fachada gomobile, aplicativo Android, testes e documentação.

## Matriz de aderência

| Critérios | Evidência de implementação | Decisão |
|---|---|---|
| CA-072-01/02 | `testar_bridge_pinpad.bat` exige e preserva `PORTA_PINPAD`; testes cobrem valores distintos, ausência e espaços; configuração efetiva é registrada | conforme |
| CA-072-03/04 | readiness ocorre após bind; testes cobrem conflito e helper ADB com seleção/reverse verificável | conforme |
| CA-072-05/07 | preflight, framing e erros públicos estáveis preservam correlação e não abrem COM no Ping | conforme |
| CA-072-06 | Bridge, ownership e serial escrevem eventos e rastro redigido no mesmo tracer | conforme |
| CA-072-08/09 | ViewModel separa ação, Bridge e sessão; Compose apresenta erro antes do catálogo e bloqueia comandos sem Open | conforme |
| CA-072-10/11 | supervisão de erro, cleanup, serialização de saída e mutex Windows preso à thread possuem testes de regressão e multiprocesso | conforme |
| CA-072-12/13 | teste Android recebe `pinpadPort` do ambiente; transcript e DSP usam o valor efetivo, sem escolher uma COM | conforme para implementação; confirmação visual pertence à validação |
| CA-072-14/15 | Go/Android, AAR/APK, READMEs e precedência entre Changes possuem evidências atuais | conforme |

## Verificações adicionais

- Não há valor de porta física fixado no launcher, no código de produção nem no
  teste instrumentado. Ocorrências de nomes de COM em testes unitários são
  fixtures deliberadamente variadas e não selecionam o dispositivo real.
- `start_aplication.bat` pertence ao menu local legado e não é o launcher
  físico contratado por RF-072-01; não foi alterado nem usado como evidência.
- Nenhuma dependência de produção foi adicionada e o listener permanece em
  `127.0.0.1`.
- `git diff --check` não encontrou erro; os avisos observados são apenas sobre
  futura normalização LF/CRLF no Windows.
- Cobertura selecionada: 73,7% no conjunto que inclui código legado; a função
  nova `NewDiagnosticScriptedTransportForPort` atingiu 100%. Os caminhos novos
  relevantes estão inventariados em `validation.md`.

## Achados

Nenhum `IMP-REV-*` bloqueante, importante ou material foi identificado.

Permanece a observação não bloqueante da revisão anterior: o teste Compose usa
uma API deprecated, sem falha de build ou impacto no contrato desta Change.

## Decisão

`IMPLEMENTACAO_APROVADA`. A validação deve consumir a porta do ambiente na
execução e registrar separadamente a confirmação visual do DSP físico.

# Validation — 069 diagnosticopinpad Functional Lab

## Status

`VALIDADA`

As evidências de implementação e validação estão registradas abaixo. A
aprovação formal e o encerramento ainda são executados pelo workflow.

## Evidências da implementação

Ambiente usado em 27/09/2026:

- Windows 10.0.19045;
- Go 1.26.5 `windows/amd64` isolado em `.tmp`;
- JDK 17 do Android Studio;
- Android SDK API 37, Build Tools 36.0.0 e NDK 28.2.13676358;
- Gradle Wrapper 9.6.0;
- Emulator `emulator-5554`, API 35/Medium Tablet;
- AAR gerado com SHA-256 `BCB2C0CE31DEC7CC7FAC992A5830CB9A5FA0152330DCF3896FB8A506107C27DC`;
- APK debug gerado com SHA-256 `7EEB8745851B56921A49E525DD29D6D418429019AC3B156CE7B0DF1156DF5EE1`.

### Gates executados

| Comando | Resultado | Saída |
|---|---|---:|
| `go test ./...` no módulo Go | aprovado | 0 |
| `go vet ./...` no módulo Go | aprovado | 0 |
| `go build ./cmd/libpinpadabecsgo-bridge` | aprovado | 0 |
| `build-mobile-aar.ps1 -AllowDirty` | aprovado; ABI inspecionada com `javap` | 0 |
| `validate_android_project.py apps/frontend/smartphone/diagnosticopinpad` | `OK: invariantes estruturais Android atendidos` | 0 |
| `:app:testDebugUnitTest` | aprovado | 0 |
| `:app:lintDebug` | aprovado | 0 |
| `:app:assembleDebug` | aprovado | 0 |
| `:app:installDebug` | APK instalado no Emulator | 0 |
| `adb shell am start .../.MainActivity` | Activity em foreground | 0 |

O teste visual confirmou a tela do catálogo, a opção 6 desabilitada com
justificativa e a presença das ações numeradas. A validação do fluxo serial
real, EMV, PIN e mídia permanece dependente do pinpad físico e não foi
simulada.

## Auditoria de segurança

Auditoria estática executada após a implementação, sem alterações externas:

- Conforme: métodos sensíveis no Go retornam allowlists de resumo e não
  serializam PAN, trilhas, PIN block, KSN, chaves ou bytes raw.
- Conforme: campos sensíveis são mascarados visualmente no formulário e os
  parâmetros não são persistidos pelo ViewModel.
- Conforme: `PORTA_PINPAD` é responsabilidade do Bridge Windows; o Android só
  envia host, porta TCP e timeout.
- Conforme: não há autenticação, multi-tenant, banco, IDOR ou XSS aplicáveis
  ao aplicativo local desta change.
- Limitação: o Bridge de desenvolvimento escuta em loopback e o protocolo não
  fornece autenticação; isso é aceitável para o cenário local especificado,
  mas não autoriza exposição em rede.
- Limitação: testes de redaction são unitários e não substituem inspeção de
  tráfego em um pinpad real.

Nenhum segredo real foi incluído no código, relatório ou evidências.

### Achado SEC-069-001 — Bridge local sem autenticação

- Severidade: P2 — contexto de desenvolvimento/local.
- Evidência: o contrato da change restringe o listener do Bridge a `127.0.0.1`
  e o Android usa `adb reverse`/endpoint local.
- Impacto: se o processo for exposto em uma interface de rede, outro processo
  local ou host alcançável poderá tentar executar operações do laboratório.
- Correção recomendada: manter loopback como padrão e exigir autenticação e
  transporte protegido antes de qualquer uso fora da máquina de desenvolvimento.
- Critério de aceite: nenhuma configuração de produção deve permitir listener
  não autenticado fora de loopback sem uma change de segurança aprovada.

## Pendências formais

Esta seção não aprova a change. Ainda devem ocorrer a aprovação formal antes de
arquivar ou preparar commit, conforme o workflow do projeto.

## Evidências VAL

- `VAL-069-001`: `go test ./...` — aprovado, código 0.
- `VAL-069-002`: `go vet ./...` e `go build ./cmd/libpinpadabecsgo-bridge` — aprovados, código 0.
- `VAL-069-003`: `go test -race ./mobile` — não executado; o ambiente isolado
  recusou o gate por `CGO_ENABLED=0`. Não há afirmação de cobertura de race.
- `VAL-069-004`: validador estrutural Android — aprovado, código 0.
- `VAL-069-005`: `:app:testDebugUnitTest`, `:app:lintDebug` e
  `:app:assembleDebug` — aprovados, código 0.
- `VAL-069-006`: `:app:connectedDebugAndroidTest` no `emulator-5554` —
  aprovado, código 0.
- `VAL-069-007`: instalação do APK, `adb reverse tcp:39100 tcp:39100` e
  abertura da `MainActivity` — aprovados; Activity confirmada em foreground.
- `VAL-069-008`: inspeção `javap` do AAR confirmou métodos nomeados para
  conexão, display, GCX, EMV, PIN, QR e multimídia.
- `VAL-069-009`: auditoria de segurança atual gerada em
  `docs/security-audit/relatorio-069-diagnosticopinpad-functional-lab.pdf`;
  nenhum achado confirmado em aberto.

Os cenários dependentes de pinpad físico continuam explicitamente não
executados; não foram convertidos em sucesso simulado.

## Interrupção do encerramento

Antes da aprovação formal foi identificado que o caminho Android → Bridge não
configura o mesmo `LogPinpadAbecs.txt` usado pelo menu desktop. A Change 069
não foi aprovada naquele momento; a correção foi isolada nas Changes 070 e 072
para preservar o escopo e o histórico desta entrega. As correções sucessoras
foram concluídas, permitindo o encerramento desta baseline.

## Regressão reportada após 070/071

O usuário relatou falha de Abrir, falso estado OPEN e log só com ativação.
Diagnóstico e contrato corretivo estão na
[Change 072](../../archive/2026-09-27-072-corrigir-abertura-bridge-android/diagnostico.md).
As evidências acima são históricas do baseline; os gates corretivos e o fluxo
físico atual foram executados e aprovados na Change 072.

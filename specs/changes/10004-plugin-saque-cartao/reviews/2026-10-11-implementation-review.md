# Revisão de implementação — 10004 (2026-10-11)

Decisão: `REPROVADA`. Revisão das alterações posteriores à baseline
`131a0af`, confrontadas com REV-005 a REV-010. Não foi alterado código de
produção durante esta revisão. A prontidão PBRG não equivale a EMV validado.

## IMP-REV-001 — Tabela inválida não bloqueia o início físico

Severidade: alta. `SaqueRepository.kt:84` lê `emv.json` como texto limitado;
`loadProfile` valida somente o perfil, sem analisar o array ou cruzar registros
com o adquirente. Portanto, uma tabela contendo `[]`, JSON inválido ou registros
de outro adquirente ainda permite `configReady=true` e habilita AVANÇAR.
`SaqueJourney.kt` chama `repository.open` antes de `loadTable`; o conteúdo só
é validado pelo Go nessa segunda chamada (`SaqueRepository.kt:110`).

Impacto: OPN/GIX podem ocorrer com configuração EMV inválida; a falha reaparece
na etapa física em vez de ser `CONFIGURATION/preflight`, contrariando REV-010
e o contrato executivo (perfil/tabela ausente ou inválido bloqueia comandos).
O Go continua validando antes de TLI/TLR/GCX; não foi identificado envio de
registros inválidos ao pinpad. Isso limita o impacto, mas não resolve o gate.

Ação: validar integralmente a tabela antes de publicar prontidão, reutilizando
a validação Go para não duplicar regras ABECS em Kotlin. Cobrir tabela vazia,
JSON inválido, adquirente divergente e registro inválido no adapter real;
comprovar zero OPN/TLI/GCX nesses casos. Os testes atuais de ausência usam fake
do repository e não cobrem esse comportamento de arquivos privados.

## IMP-REV-002 — Entrada monetária inválida é convertida em compra válida

Severidade: média. `SaqueJourney.kt:67` aplica
`event.value.filter(Char::isDigit).take(12)` antes da validação.
Uma colagem `-10000` vira `10000` (compra positiva de R$ 100,00), em vez de
bloquear a entrada negativa. Uma colagem de 13 dígitos perde o último dígito
silenciosamente; o resumo pode representar outro valor, dentro de N12.

Impacto: RF-001 exige bloquear negativo, inválido e overflow, não transformar
esses valores em outra compra. Teclado numérico não impede colagem.

Ação: distinguir separadores da máscara de sinais/caracteres inválidos,
rejeitar excesso de dígitos sem truncar o valor e testar o evento real da
jornada e a edição/colagem na UI. Preservar a regra centavos digitados.

## IMP-REV-003 — Contrato monetário e KDoc ainda descrevem revisões antigas

Severidade: baixa. `spec.md` RF-001 e
`contrato-implementacao-homologacao.md:65` descrevem cálculo percentual sem
separar o débito. REV-008 aprova R$ 1,00 fixo no débito e o código implementa
essa regra, mas o contrato integrado não a consolida. O KDoc de
`SummaryPanel` (`SaqueCartaoScreen.kt:163`) afirma que AVANÇAR depende apenas
do resumo, embora REV-010 e o código exijam `configReady`. `tasks.md:49`
registra cobertura 94,6% para REV-010; o relatório atual comprova 94,2%.

Ação: consolidar a regra vigente no contrato, atualizar KDoc/gate e corrigir
a métrica da REV-010 sem reescrever as evidências históricas da REV-008/009.

## Aspectos conferidos sem divergência nas alterações recentes

- Nome/menu/manifestos concordam em Pagamento com Cartão; IDs técnicos do
  módulo/pacote continuam compatíveis com o carregamento dinâmico.
- Crédito usa 5,34% + 0,80 ponto por parcela adicional; débito uma parcela e
  tarifa fixa R$ 1,00; cálculo em centavos/BigDecimal e limite do total N12.
- Combobox, seleção de modalidade e resumo congelado chegam ao filtro GCX
  configurado; não há inferência de modalidade pela bandeira.
- Indicador de etapas removido; animações renderizam o snapshot da lambda
  AnimatedContent, sem disparar hardware; spinner e callbacks verificados.
- Confirmação usa quatro páginas DSP/GKY; PIN fica no terminal; sequência
  GPN/GTK/GOX/FCX e cleanup mantêm contratos anteriores. Tipo de transação 1
  permanece definido no contrato da 10004; renomeação visual não prova mudança
  desse tipo protocolar nem autorização financeira real.
- Falhas mostram código/fase sanitizados. Ausência de perfil/tabela bloqueia
  AVANÇAR; nenhum fallback DES sintético continua em produção.

## Evidências desta revisão

Ambiente: Windows PowerShell 5.1, Gradle Wrapper 9.6.0/AGP 9.4, JBR 25.0.3,
emulator-5554 API 35. Comandos Gradle no projeto Android.

| Verificação | Resultado | Exit |
| --- | --- | --- |
| `:plugin-saque-cartao:testDebugUnitTest :plugin-saque-cartao:saqueCoverageVerify :plugin-saque-cartao:lintDebug :plugin-saque-cartao:assembleDebugAndroidTest --console=plain` | BUILD SUCCESSFUL; 18 unitários, zero falhas; 210/223 linhas = 94,2%; lint zero erros/3 avisos | 0 |
| `adb -s emulator-5554 shell am instrument -w br.com.romulopenha.sistemaprototipoandroid.pluginsaquecartao.test/androidx.test.runner.AndroidJUnitRunner` | OK (11 tests), 109,972 s | 0 |
| Validador estrutural Android na árvore local | Rejeita local.properties ignorado pelo Git; caminho SDK local preservado. Mesma limitação já documentada | 1 |
| `adb -s emulator-5554 reverse --list` | host-16 tcp:39100 tcp:39100 | 0 |
| `adb ... shell run-as br.com.romulopenha.sistemaprototipoandroid ls -la files/saque-cartao` | Diretório não existe; nada provisionado | 1 |
| Listener TCP 39100 | 127.0.0.1:39100, PID 2544 | 0 |
| Probe local PBRG hello 1→2 e ping 7→8, sem DATA/OPN | Magic, versão, tipo e correlação conferidos | 0 |
| `PORTA_PINPAD` do processo/usuário | Definida; consumir variável em tempo de execução, sem COM estática | 0 |
| Inspeção somente de `D:\desenvolvimento\chave_des\emv_des.json` | 36 registros, 36 TAB_LEN coerentes, um adquirente; SHA-256 873d94fa1ec3be17d5a58d130f1ab07b82367eee01c05b4745218c14ebad0ab2 | 0 |

Os achados IMP-REV-001/002 são verificações de código com exemplos concretos;
não foram apresentados como testes de regressão executados. Os 20 testes do
host são evidência anterior REV-010, não execução nova desta revisão.

## Continuação da jornada física

O caminho recebido é a tabela EMV; não fornece sozinho o perfil externo
exigido. Não foi encontrado `*perfil*.json` no diretório indicado. Faltam
parâmetros aprovados como versão A10, filtros de aplicação, limite/prazos,
índice DATA e cenário FCX. Solicitação de esclarecimento permanece pendente.
Não provisionar valores sintéticos nem reiniciar o Bridge saudável. Não houve
OPN, carga de tabela, captura de cartão, PIN ou finalização física nesta revisão.

Correções dos achados e parâmetros do laboratório precedem a execução física.

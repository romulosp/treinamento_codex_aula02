# Arquitetura implementada e inventário — 10011

Revisão em 2026-10-13. Abrange todas as implementações da Change e suas
fronteiras com host, SharedApi, Bridge e configuração externa. O contrato
aprovado permanece na [SPEC](../spec.md) e no [DESIGN](../DESIGN.md).

## Responsabilidades e dependências

| Fronteira | Fonte de verdade / responsabilidade | Contrato e evidência |
| --- | --- | --- |
| Host `:app` | Autenticação, descoberta/carga dos APKs, router e tela hospedada. | Empacota o plugin em `assets/plugins/business/plugin-saque-cartao.apk`; hash coincide com o APK construído. Sem `implementation(:plugin-saque-cartao)`. Login/menu humano permanece pendente. |
| `:shared-api` | Interfaces estáveis e manifesto do microkernel. | Plugin usa `compileOnly(:shared-api)` em produção. Manifesto business-menu 1.1 é compatível com host 1.2. Nenhuma alteração SharedApi. |
| `SaqueModels.kt` | Modalidade DES, cálculo monetário exato, resumo imutável e modo efetivo de cartão. | Modo 03/06 é EMV, 00/05 não EMV; desconhecido bloqueia. Crédito 01/débito 02; tipos não inferidos por bandeira. |
| `SaqueJourney.kt` | Owner da View, eventos serializados, estado permitido, ordem e cleanup. | Resumo congelado antes de GCX; GTK precede continuação; somente GOX=2 chama FCX. Sem dados de cartão/PIN no `SaqueState`, replay ou nova compra por cleanup. |
| `GoSaqueRepository` | Adapter main-safe, perfil/tabela/PIN privados e fachada gomobile. | `Dispatchers.IO`, leitura limitada UTF-8 estrita, preflight completo antes de Open; snapshot PIN fora da UI. Falha limpa caches; close descarta cliente/configuração. |
| `mobile/payment.go` e `mobile/client.go` | ABI primitiva aditiva, DTOs públicos mínimos, admissão serial e correlação. | Sete APIs de pagamento/preflight comprovadas no source jar. Nenhuma recebe PAN. Cancelamento e erro invalidam contexto. ABI `CODE:phase` existente mantida. |
| `internal/application/service/payment*.go` | Sessão, estado da captura, PAN temporário, sequência, descarte e diagnóstico. | Contexto por instância, operationID e geração. Mutex de pagamento e fila existentes; consumo único. GOX usa o mesmo valor/adquirente/PIN da captura. |
| `internal/domain/command` / `parser` | Validação de campos, candidatos integrais, payloads, respostas e PAN. | Compra binária 00h; GCX lê trilhas incompletas 8041/8042/8043. Não selecionar AID por prefixo nem decryptar GTK. |
| Bridge / serial | Transporte PBRG opaco, ownership e bytes ABECS. | Sem endpoint/regra financeira novos. Integração testa fachada através do Bridge com serial roteirizada; compra física não executada. |
| Launcher PowerShell | Importação externa, build, prontidão e provisionamento privado com hash. | Três arquivos privados; nenhuma chave instalada; `ValidateOnly` sem mutação. [Inventário externo](configuracao-externa-des.md). |

Não há novo singleton global, persistência de PAN, dependência entre plugins
concretos, API de PIN na UI ou acesso do Bridge aos arquivos privados Android.
O nome histórico saque não determina o tipo de transação ABECS.

## Estado e processamento

Depois de Open e carga EMV confirmada, uma captura de pagamento reserva a sessão:
`capture → card → gtk → tracks → gox/gpn → done → close`.
Erros, cancelamento, CAN, troca por API genérica, reconnect, shutdown e close
invalidam o contexto; a geração impede publicação tardia. A captura permanece
consumida até nova abertura explícita, mesmo depois de descartar o PAN.

| `PP_CARDTYPE` | Sequência | Semântica |
| --- | --- | --- |
| 00 / 05 | GCX → GTK DATA → GPN PIN DUKPT | PAN numérico obtido do GCX permanece somente no core até consumo; resultado é captura, sem autorização financeira. |
| 03 / 06 | GCX → GTK DATA → GOX | Kernel decide CVM/PIN conforme parâmetros EMV, inclusive limite CTLS quando aplicável; sem GPN externo. GOX 0/1 conclui, 2 permite FCX do cenário DES. |

Tarja ausente/conflitante/mascarada, seleção AARR incoerente, configuração
alterada, correlação alheia ou transição repetida falham antes do próximo comando.
Fallback é limitado; conserva snapshot, filtros e candidatos. Status 086 exige
DEX para ação externa; 081 desabilita CTLS após segunda falha consecutiva.

Close após comando concluído não chama Cancel incondicional. Close da fachada
cancela operações pendentes e aguarda a admissão serial antes de CLO. Falha de
transporte/fechamento não é convertida em confirmação física: a jornada mantém
resultado indeterminado e a falha principal junto da falha de cleanup.

## Privacidade e diagnóstico

PAN pertence ao serviço Go, nunca ao Kotlin/JSON público. Buffers mutáveis
controlados de PAN, GTK, PIN block e KSN são sobrescritos nos caminhos aplicáveis;
strings transitórias/runtime não têm garantia de apagamento, conforme ADR-001.
Logs de comando usam whitelist, status N3 tipado, fase/tentativa/correlação e
duração. Não inspecionam mensagens livres para descobrir status nem abrem
frames redigidos. DATA GTK e PIN usam configurações distintas.

Revisão local dessas fronteiras não substitui auditoria formal independente
HIGH_ASSURANCE, race ou demonstração física.

## Inventário KDoc e testes

| Arquivo Kotlin produtivo alterado | Contratos documentados / exclusões | Verificação |
| --- | --- | --- |
| `SaqueModels.kt` | `SaqueModoCartao` e `fromCardType`: tipo normativo, elegibilidade e rejeição do desconhecido. Propriedades/construtor do enum são os dados descritos pelo KDoc do tipo. | Testes de jornada para quatro modos e tipo desconhecido; modelos/JVM. |
| `SaqueRepository.kt` | Porta e métodos capture/tracks/PIN/EMV; Go adapter/preflight; snapshot privado `PaymentPinConfiguration`, WK vazia; leitura UTF-8; `goCall` e getters exigem sessão/snapshot. Overrides seguem o contrato da porta e da classe; não duplicam sua descrição. | Preflight instrumentado com fontes sintéticas, falhas de configuração e limpeza. Fachada/core com integração Bridge. |
| `SaqueJourney.kt` | Owner/load/begin, cancel/checkpoint, cleanup e mensagem principal; descarte por lifecycle. Métodos triviais de atualização usam o contrato de não publicar após dispose. | 24 testes JVM de jornada, testes Compose existentes, gates de cobertura. |

Sem mudança de UI/layout ou API pública SharedApi. Código Java gerado gomobile
é excluído do KDoc. Dokka não está configurado; não foi instalado. Testes têm
fixtures sintéticas, não arquivos reais de credenciais/cartões.

Cobertura Android elegível definida na tarefa existente: Journey, regras
monetárias/perfil/resumo; adapter Android/Compose exige instrumentação e não é
medido por JaCoCo JVM. Resultado: 253/260 linhas = 97,31%. Go: quatro pacotes
focais 1.940/2.394 statements = 81,04%; cinco arquivos novos 332/375 = 88,53%.
A fachada mobile legada inteira tem 62,3%; o agregado não afirma 80% em todo
pacote legado. Cobertura não substitui os cenários de isolamento, descarte e
encerramento descritos em [validation.md](../validation.md).

Proveniência: [resultado de conferência](artifact-verification-2026-10-13.json),
[verificador reproduzível](verify-artifacts.py). 64 hashes de fontes, quatro
ABIs, API 29 declarada, sete APIs e plugin idêntico ao embutido no host conferidos.
O AAR foi gerado sobre HEAD com árvore dirty; hashes identificam o fonte efetivo,
não se afirma que essas alterações estejam no commit de HEAD.

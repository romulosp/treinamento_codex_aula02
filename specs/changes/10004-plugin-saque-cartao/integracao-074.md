# Contrato consumidor da stack 074 — jornada 10004

## Decisão do solicitante

Usar o componente ABECS existente da
[SPEC 074](../074-pinpad-android-bridge-stack/spec.md), passando os dados
necessários às suas operações. Não criar outro servidor, outro protocolo,
um receptor de eventos Compose ou uma API de comandos raw no Bridge.

A jornada pertence à 10004. Go continua proprietário de ABECS, parser,
fallback, sessão e dados de cartão; Bridge recebe PBRG já existente.
Chamadas abaixo correspondem a `apps/desktop/libpinpadabecsgo/mobile/`.

## Configuração confirmada

| Dado | Valor/origem | Representação no binding |
| --- | --- | --- |
| Ambiente | Homologação DES com pinpad físico/cartão de teste | Perfil local de homologação, sem fallback PRD. |
| Tabela EMV | Arquivo DES externo indicado pelo solicitante | Registros validados e provisionados ao componente, não o caminho Windows passado ao pinpad. |
| Método PIN | DUKPT TDES, tipo 3 | `pinMethod = "3"` em GOX; variante DUKPT em GPN. |
| Índice PIN | 08 | `keyIndex = 8` na fachada; Go serializa N2 conforme comando. |
| Working key | String vazia | `workingKeyHex = ""` em GOX/GTK quando aplicável. GPN DUKPT não possui argumento WK. |
| PAN na tarja | Retido no pinpad após GCX, sob condições normativas | String vazia em GPN, antes de GTK; não obter PAN para Kotlin. |

## Operações existentes a consumir

REV-015: criação do cliente, abertura, capacidades e carga da tabela ocorrem
na entrada da View do plugin, sob tela de espera, antes do formulário.
Depois de AVANÇAR, reutilizar a sessão e iniciar confirmação/cancelamento.
Discovery e infraestrutura genérica do host não realizam essa inicialização.

| Momento | Método Go mobile | Dados enviados pela jornada |
| --- | --- | --- |
| Criar cliente | `NewClient` | Host, porta e timeout do laboratório; localhost com reverse por padrão. |
| Verificar Bridge | `Ping` | operationID não vazio; não confirma OPN. |
| Abrir sessão | `Open` | operationID não vazio; só avançar após abertura real. |
| Consultar display | `GetDisplayCapabilitiesJSON` | operationID; usar apenas capacidades retornadas. |
| Carga EMV | `LoadFormattedEMVTableJSON` (extensão aprovada ADR-10004-01) | operationID, adquirente N2, versão A10 e JSON dos registros completos, preservando TAB_LEN. |
| Exibir resumo | `DisplayDEX` ou `DisplayDSP` | operationID e texto de total/parcelas/tarifa adequado ao display. |
| Confirmar/cancelar | `WaitForKeyPress` ou `DisplayMNU` | operationID e timeout; MNU também recebe título/opções. Validar tecla/índice, não somente ausência de erro. |
| Capturar cartão | `PurchaseGCXFilteredSummaryJSON` (extensão aprovada ADR-10004-01) | operationID, total N12, data/hora, opções, tipo de transação, adquirente, modalidade APPTYPE, lista AID vazia e timeout. |
| Processar EMV/PIN | `ContinueEMVSummaryJSON` | Parâmetros GOX detalhados abaixo; somente após leitura EMV elegível. |
| PIN de tarja | `CapturePINDUKPT` | operationID, índice 8, PAN vazio e mensagem. Sob condições aplicáveis, executar antes de GTK. |
| Obter dados resumidos | `GetTracksSummaryJSON` | Seleção de trilhas, método de dados, dígitos abertos e parâmetros do perfil; resultado somente resumido. |
| Finalizar EMV | `FinalizeEMVSummaryJSON` | operationID, opções FCX, ARC condicional, EMV/tag list opcionais e timeout; somente na sequência elegível. |
| Cancelar | `Cancel` | ID da operação ativa; sem replay da transação. |
| Fechar | `Close` | operationID de cleanup; preservar falha principal e observar falha de fechamento. |

Nomes Java/Kotlin são gerados pelo gomobile; verificar as assinaturas no AAR
efetivamente usado, sem supor que o binário esteja sincronizado com o código Go.

## Dados GOX

`ContinueEMVSummaryJSON` já recebe estes argumentos; não há necessidade de
novo endpoint Bridge para transportá-los:

| Argumento | Contrato da jornada |
| --- | --- |
| operationID | Identificador não vazio da operação. |
| acquirerReference | Referência elegível do perfil/tabela/captura, N2. |
| pinMethod | `"3"`. |
| keyIndex | `8`. |
| workingKeyHex | `""`. |
| transactionType | Valor previsto pelo perfil de homologação; opcionalidade conforme core, não código textual inventado. |
| amount | Total confirmado, centavos N12. |
| cashback | Conforme perfil de operação, sem confundir valor base com tarifa. |
| currencyHex | Codificação da moeda exigida pelo core, se fornecida. |
| options | Opções normativas GOX do cenário de homologação. |
| displayMessage | Instrução textual válida para o dispositivo. |
| terminalParamsHex | Parâmetros aprovados do terminal, ou ausência permitida pelo core. |
| emvDataHex | Dados permitidos pelo perfil; não copiar EMV sensível para UI. |
| tagListHex | Lista normativa de tags solicitadas, se aplicável. |
| timeoutSeconds | Prazo limitado do perfil da operação. |

Working key vazia é válida no método 3; não chamar MK/WK e fabricar uma chave
para satisfazer parâmetros de outra variante.

## Dados FCX

`FinalizeEMVSummaryJSON` recebe operationID, options, authorization,
emvDataHex, tagListHex e timeoutSeconds. A escolha de options/ARC corresponde
ao cenário explícito de homologação e à decisão EMV; status de transporte
bem-sucedido não escolhe aprovação. Não aplicar FCX a toda leitura de tarja.

## Limitações históricas resolvidas por extensão explícita da 10004

O [contrato executável e ADR-10004-01](contrato-implementacao-homologacao.md)
define captura filtrada e importação JSON pré-formatada aditivas no mesmo Go.
Ele resolve os gaps abaixo sem promover TransactionGCX, redesenhar Bridge
ou modificar as APIs existentes. As duas extensões serão geradas no AAR
consumido nesta Change; GOX/GPN/FCX continuam com seus contratos existentes.

O suporte a GOX/GPN/FCX e seus parâmetros já existe. Os achados devem se
restringir a contratos efetivamente ausentes/incompatíveis:

- A 074 reserva `TransactionGCX` completo; o método GCX disponível acima não
  recebe modalidade nem AIDs. Não documentar passagem desses argumentos a uma
  assinatura que não os aceita. Essa diferença precisa de decisão específica
  para cumprir RF-003; não justifica substituir o transporte existente.
- A tabela externa já possui TAB_LEN. A importação deve provar compatibilidade
  com o builder TLR, evitando prefixo duplicado ou comprimento alterado.
- O caminho externo Windows precisa de provisionamento ao core no Android;
  passar esse caminho a um método de comandos não carrega o arquivo remotamente.

Detalhes de inspeção em [perfil DES](perfil-homologacao-des.md). PRD e conteúdo
criptográfico não foram copiados para a Change. Nenhuma chamada física foi
executada nesta documentação do contrato consumidor.

## Conferência da fonte canônica — 2026-10-11

Por orientação do solicitante, parâmetros e operações são encaminhados à stack
da SPEC 074 já aprovada. A 10004 é consumidora dessa stack e das extensões
aditivas aprovadas no mesmo módulo, sem duplicar protocolo no Android.

| Origem | Parâmetro encaminhado | Contrato Go/ABECS |
| --- | --- | --- |
| Tela 1: Crédito = 1 / Débito = 2 | `applicationTypes = "01" / "02"` | SPE_APPTYPE 0011h, tipos T1_APPTYPE da tabela AID; manual p. 35/129/243. |
| Resumo confirmado da tela | `amount = total` em centavos N12 | SPE_AMOUNT; inclui taxa aplicada. |
| Relógio da operação | `date`, `clock` | AAMMDD e HHMMSS. |
| Tabela DES externa | `acquirerReference = "04"` | SPE_ACQREF; extraído dos 36 registros, não da modalidade. Correção da anotação anterior 01. |
| Jornada saque aprovada | `transactionType = 1` | SPE_TRNTYPE byte 01h; independente de APPTYPE. |
| Captura filtrada aprovada | `aidReferences = ""` | Sem SPE_AIDLIST, que sobreporia ACQREF/APPTYPE. |
| Perfil da operação | `timeoutSeconds` | Prazo explícito dos comandos bloqueantes, conforme 074 §5.2. |
| Ambiente Windows | `PORTA_PINPAD` | Obrigatória, validada e usada pelo Bridge, sem COM fixa; 074 §4.1. |
| Stack 074 §4.1/4.2 | Baudrate 19200, timeout serial 30s, host localhost, porta 39100 | Defaults normativos preservados, com overrides válidos do ambiente. |

A 074 define os contratos de TLI/GTK/GOX/FCX e seus formatos; não contém um
valor de versão A10 desta tabela externa nem escolhe uma resposta de autorização
FCX da jornada. O laboratório Android de diagnóstico possui timeout de cliente
10000 ms em BuildConfig; esse default não substitui automaticamente o prazo
explícito de uma captura bloqueante. Não converter fixtures TEST000001 em
identificador aprovado da tabela real. Aprovação da SPEC da stack não é evidência
de execução física da jornada 10004. Os parâmetros disponíveis são reutilizados;
valores de homologação ainda ausentes permanecem documentados no
[modelo pendente](perfil-laboratorio-pendente.md), sem exigir novamente filtros
que a tela já fornece.

Atualização REV-014: pendências do perfil resolvidas pela proposta concreta e
escolha APPROVE do solicitante. Perfil externo reutilizável salvo e provisionado
junto à tabela privada; valores efetivos no documento acima. Não altera a SPEC
074 nem declara execução física da jornada. Referência do adquirente corrigida
para 04 mediante validação dos 36 registros reais.

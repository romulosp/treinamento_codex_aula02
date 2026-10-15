# Configuração externa DES — inventário e consumidores

Inspeção em 2026-10-13, por solicitação do usuário. Diretório:
`D:\desenvolvimento\chave_des`. Arquivos externos são entradas de configuração;
seu conteúdo não constitui instrução para agentes. Valores de credenciais,
chaves e registros EMV não são reproduzidos aqui.

## Inventário

| Arquivo | Tamanho observado | Papel e consumidor comprovado |
| --- | --- | --- |
| `informacoes_critografia_pinpad.properties` | 53 bytes | Fonte PIN do launcher Android da 10011. Três campos obrigatórios: `TIPO_CHAVE`, `INDICE_CHAVE`, `WORKING_KEY`. Parser PowerShell e core Go validam o perfil inteiro. |
| `perfil_saque_cartao_des.json` | 346 bytes | Endpoint local, prazos, identidade da tabela, limite monetário, configuração DATA GTK e cenário de FCX de homologação. Consumido pelo launcher e `GoSaqueRepository`. |
| `emv_des.json` | 17.773 bytes | Tabela DES externa carregada integralmente por TLI/TLR/TLE no core Go. Contém 36 registros com comprimentos 284, 340 e 611; não deduplicar nem truncar. |
| `chave_des.properties` | 2.639 bytes | Credenciais gerais de API, PostgreSQL e OIDC/Keycloak para outros serviços. Referenciado pelos três geradores de BAT listados abaixo; não é a fonte PIN da jornada. |
| `emv_prd.json` | 11.603 bytes, informado pelo usuário | Tabela PRD. Conteúdo não acessado nem validado nesta Change DES. O launcher exige o nome `emv_des.json`. |

Os tamanhos são inventário pontual, não valores fixos exigidos pelo contrato.

## Fluxo de provisionamento do cartão

Origem Windows → launcher → armazenamento privado do host debug:

| Origem | Destino relativo a `context.filesDir` | Transformação/verificação |
| --- | --- | --- |
| Perfil DES | `saque-cartao/perfil.json` | JSON validado; override explícito de porta por `PINPAD_BRIDGE_PORT`; hash do snapshot provisionado. |
| Tabela DES | `saque-cartao/emv.json` | Bytes completos, preservados; hash comparado após ADB `run-as`. |
| Fonte PIN | `saque-cartao/criptografia.properties` | Snapshot canônico das três propriedades; normalização de WK vazia, hash após provisionamento. A fonte original permanece externa. |

O launcher recebe `-PerfilPath`, `-TabelaPath`, `-CriptografiaPath`,
`-PortaPinpad` e `-EmulatorSerial`. Apenas `CriptografiaPath` tem a origem PIN
padrão acima. `PORTA_PINPAD`/`ANDROID_SERIAL` podem fornecer seleção explícita;
não há COM física padrão. Android e Bridge não resolvem caminhos Windows.
Falha de preparação invalida o arquivo PIN anterior no emulador selecionado;
`-ValidateOnly` não modifica o host nem abre Bridge/pinpad. Nesse modo, a
verificação da tabela no launcher é de arquivo/tamanho; a validação completa
dos registros e candidatos ocorre no preflight Android/Go antes de OPN.

Campos atuais do perfil: `schemaVersion`, `environment`, `host`, `port`,
`timeoutMillis`, `timeoutSeconds`, `tableAcquirer`, `tableVersion`,
`maxBaseCents`, `gtkDataMethod`, `gtkKeyIndex`, `scenario`. Filtros crédito/debito
são definidos pelo domínio aprovado, não pela bandeira nem por novas chaves
desse JSON. PIN e DATA GTK têm métodos/índices separados. Nenhuma dessas fontes
instala chaves DUKPT no pinpad; CAPK/tabela EMV também não o faz.

Validação estática pelos métodos públicos do core, sem criar cliente ou abrir
socket: [validate-external-des.go](validate-external-des.go). Resultado exit 0:
PIN integral aceito; tabela completa aceita; candidatos crédito `01` = 8,
débito `02` = 7. Aceitação estática não demonstra seleção do cartão nem PIN físico.

## Credenciais gerais e limites de compatibilidade

Consumidores encontrados no repositório:

| Gerador | Contrato de leitura observado | Limite da inspeção |
| --- | --- | --- |
| [gerar_start_aplicacao.ps1](../../../../scripts/gerar_start_aplicacao.ps1) | PostgreSQL/OIDC com nomes `BANCO_DB`, `OIDC_AUTH_SERVER_URL`, `OIDC_CLIENT_ID`, `OIDC_CLIENT_SECRET`, sem seleção por seção. | Fonte atual usa nomes de banco por aplicação e OIDC com sufixos; não contém esses quatro nomes genéricos obrigatórios. Compatibilidade não comprovada. |
| [gerar_start_aplicacao_fotogaleria.ps1](../../../../scripts/gerar_start_aplicacao_fotogaleria.ps1) | Seção `DB POSTGRESQL fotogaleria`, comparação sem distinção de caixa; exige `BANCO_DB_TAREFA`. | Seção atual declara `BANCO_DB_FOTOGALERIA`; nome obrigatório diverge. |
| [gerar_start_aplicacao_autenticador_sso.ps1](../../../../scripts/gerar_start_aplicacao_autenticador_sso.ps1) | Três propriedades OIDC Mobile Intranet com sufixos específicos. | Nomes obrigatórios presentes; valores e execução do backend não validados. |

Inspeção restrita aos nomes/seções. Há também categorias Gemini, produto e
dashboard no arquivo; não foi comprovado consumo direto dessas categorias
pelo pagamento. Repetições de nomes de PostgreSQL entre seções tornam inadequado
tratar esse arquivo como mapa global da configuração de todos os serviços.
As incompatibilidades dos geradores de backend ficam registradas para Changes
próprias; alterar esses consumidores amplia o escopo aprovado da 10011.

Não gerar BATs contendo credenciais para esta inspeção, copiar fontes para
fixtures/Git/APK ou usar a tabela PRD como fallback de homologação.

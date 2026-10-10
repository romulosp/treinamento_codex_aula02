# SPEC: 10004-plugin-saque-cartao

**Autor:** Rômulo Penha

## Status
`SPEC_APROVADA` — revisão de 2026-10-10 em
[REV-005](reviews/2026-10-10-spec-review-implementacao.md). A baseline anterior
permanece histórica em `references/baseline-2026-09-25/`.

O módulo `:plugin-saque-cartao` tem `:shared-api` como dependência de contrato,
implementa `IPluginNegocioApp` e declara capacidade `business-menu`. O contrato
mínimo e a minor requerida no manifesto são 1.1. O host 1.2 mantém essa versão
elegível pela regra `requiredSharedApiMinor <= hostMinor` para plugins de
negócio. Seu manifesto declara
`br.com.romulopenha.sistemaprototipoandroid.saquecartao`; o serviço declara sua
entry class. A única folha é `Saque Cartão`, no caminho
`Principal > Outros Serviços > Saque Cartão`.

O plugin exibirá cinco etapas em sua própria Compose View e consumirá o
componente Go/ABECS da [074](../074-pinpad-android-bridge-stack/spec.md).
A coleta local de senha da baseline anterior será removida; PIN será digitado
somente no pinpad. O cabeçalho global, a autenticação e o menu pertencem ao host.

O plugin NÃO DEVE declarar Activity `MAIN/LAUNCHER` em nenhuma variante. O teste humano DEVE executar `:app`, autenticar pelo plugin de login, selecionar a folha no menu montado pelo host e somente então renderizar `createBusinessScreen`.

## Plataforma e fronteiras

### Ambiente decidido em 2026-10-10

`REQUIRED`: homologação com cartão de teste e pinpad físico, modo DES.
Fonte externa de tabela: `D:\desenvolvimento\chave_des\emv_des.json`.
Produção não faz parte desta execução; não usar `emv_prd.json` como fallback.
O caminho deve ser parâmetro local de provisionamento, sem fixá-lo em código
ou incorporar o arquivo bruto ao repositório/APK. DES não determina algoritmo
de chave de PIN. Ver [perfil de homologação](perfil-homologacao-des.md).

PIN confirmado pelo solicitante: DUKPT TDES, método ABECS `3`, índice `08`
(inteiro 8 no binding) e `workingKey = ""`. GOX usa método 3, índice 8 e
workingKeyHex vazio. GPN usa `CapturePINDUKPT`, sem WK; não usar a variante
MK/WK com chave vazia. A chave deve estar provisionada no dispositivo;
essa configuração não autoriza instalar chave, inventar material ou trocar
de método/índice diante de erro.

Conclusões da jornada devem ser identificadas como homologação, sem autorizar
entrega de dinheiro ou fabricar NSU. O cenário de aprovação/negação/erro para
FCX deve ser configuração explícita de teste compatível com a decisão EMV;
sucesso de transporte nunca escolhe esse cenário automaticamente.

Manter namespace/applicationId
`br.com.romulopenha.sistemaprototipoandroid.pluginsaquecartao`, Kotlin/Compose,
Java 17, minSdk 29, compileSdk 37, targetSdk 36 e toolchain vigente. Não há
upgrade proposto. Perfil STANDARD para UI; exigências HIGH_ASSURANCE da 074
para cartão/PIN. Dependências: `compileOnly(:shared-api)` e AAR Go.
Não introduzir dependência de implementação do host no plugin.

Kotlin observa estado imutável e envia eventos por repository tipado/main-safe.
O AAR/core Go mantém comandos, fila, framing, CRC, parser, fallback e sessão.
O Bridge Windows encaminha PBRG opaco e controla ownership/serial; não recebe
diretamente eventos Compose nem autoriza financeiramente a operação.

### Consumo do componente existente

Conforme orientação do solicitante, passar os dados necessários às operações
já oferecidas pela 074. O [contrato consumidor](integracao-074.md) relaciona
os métodos e argumentos por etapa, incluindo DUKPT TDES/3/08/WK vazia.
Não criar novo endpoint Bridge nem tratar eventos de UI como protocolo.
Questões de GCX completo e importação TLR devem ser demonstradas pela
assinatura/formato concreto; não presumir que todos os comandos precisam de
extensão. GOX, GPN e FCX já recebem os parâmetros de seus contratos.

## RF-001 — Modalidade e valores

Oferecer crédito/débito, valor editável e quantidade de parcelas de 1 a 12.
Mostrar separadamente valor base, taxa, tarifa e total não editável,
recalculados ao editar. Fórmula de homologação: `tarifa = valor × taxa / 100`
e `total = valor + tarifa`, arredondados HALF_UP para centavos.
Taxa definida em REV-007: 5,34% + 0,80 ponto percentual por parcela adicional.

Usar representação monetária exata; bloquear vazio, inválido, zero, negativo,
precisão maior que dois decimais e overflow. Limite de homologação vem
do perfil validado; débito permite uma parcela. N12 também limita o total.
Não apresentar "sem juros" sem contrato.

Ao continuar, congelar modalidade/base/tarifa/total/parcelas em um único resumo
para confirmação, leitura e conclusão. Não editar durante operação física.

## RF-002 — Confirmação no pinpad

Exibir "Peça ao cliente que confira os valores e confirme no pinpad" e o
resumo congelado. Enviar total, parcelas e tarifa ao componente Go para
exibição adequada às capacidades do display e aguardar confirmação ou
cancelamento real. Usar DEX/DSP + GKY conforme sequência e capacidades
do contrato executável. Não avançar por clique de confirmação no
Android, por envio bem-sucedido ao display ou por tecla/índice inesperado.

## RF-003 — Leitura e seleção de aplicação

Exibir "Peça ao cliente que insira, passe ou aproxime o cartão" e instruções
para chip, tarja e aproximação. Iniciar GCX pelo componente Go e refletir
aguardo real. Selecionar aplicação elegível pela modalidade e pesquisar AID
base/estendido conforme tabela de homologação/adquirente aprovada.

O modo de entrada vem da leitura, nunca do formulário ou escolha do operador.
GTK somente após captura elegível, conforme modo/método do core. Fallback
é decidido pelo Go e perfil normativo; a sequência genérica dos complementos
não define política de fallback. Clique duplicado não inicia outro GCX.

**Extensão aditiva na 10004:** seção 8 da 074 mantém `TransactionGCX` como `ErrNotImplemented`.
`PurchaseGCXSummaryJSON` recebe valor/data/hora/flags, sem modalidade ou AIDs;
não substitui o requisito completo silenciosamente. Implementar a captura
filtrada da ADR-10004-01, preservando o stub e o Bridge.

## RF-004 — PIN e processamento

Exibir "Peça ao cliente que siga as instruções no pinpad" e, quando houver
solicitação explícita GPN, "Peça ao cliente que digite a senha no pinpad".
Durante GOX, orientar senha somente se solicitada pelo pinpad, pois a fachada
resumida não publica evento intermediário de solicitação PIN.
GOX após GCX ICC/CTLS EMV; GPN no fluxo de tarja quando aplicável ao perfil.
Não exigir senha em todo contactless nem reduzir GOX somente à digitação.

Não criar campo de senha no Android. PAN, trilhas, PIN/PIN block, KSN, chaves
e EMV sensível permanecem no Go; não cruzam estado Compose, host, logs ou
JSON público. Não usar PAN de exemplo para completar GPN.

**Reavaliação normativa:** o builder GPN aceita PAN vazio. ABECS 2.12 permite
usar o PAN retido no dispositivo após GCX, antes de GTK, sob as condições do
perfil End-to-End. Portanto, avaliar `GCX → GPN(PAN vazio) → GTK` para tarja,
sem transferir PAN a Kotlin. Não executar GTK antes e presumir retenção de PAN.
Método/índice PIN já foram definidos; precondições serão provadas em hardware.
Se esse perfil não for aplicável, resolver o encadeamento no Go sem expor PAN.

## RF-005 — Resultado e finalização

Distinguir sucesso do comando, decisão EMV e autorização financeira.
FCX somente após GOX e para modo/sessão elegível. Mapear opções, ARC e retorno
conforme contrato normativo; fluxo não EMV não envia FCX indiscriminadamente.

Mostrar aprovado, negado, erro, cancelado ou indeterminado com texto e sinal
visual. Indicar remoção de cartão quando exigida. Total final igual ao total
confirmado. Não fabricar NSU, ARC ou código ABECS. Sem autorizador, eventual
modo de homologação deve estar explícito e não anunciar saque real aprovado.

Timeout/desconexão após envio não permite afirmar aprovação/negação ou saque
não efetuado sem evidência. Não oferecer repetição automática ou liberação
de dinheiro em resultado indeterminado. Permitir retorno por `IPluginRouter`
à rota `menu-principal` após finalizar/limpar a sessão.

## RF-006 — Concorrência, cancelamento e lifecycle

Uma operação ativa por jornada. Cada chamada recebe operationID não vazio,
com correlação da 074. Cancelar pelo Android/pinpad alcança o core; coroutine
cancelada sozinha não basta. Erro de cleanup não substitui a causa principal.
Ao sair/desanexar/destruir o proprietário da jornada, cancelar/fechar sessão
com prazo limitado. Respostas tardias não alteram jornada nova; recriação
não reenvia transação. Chamadas gomobile bloqueantes fora da main thread.

Ping confirma Bridge, não sessão aberta. OPN real é obrigatório antes dos
comandos físicos. Orçamentos por fase devem integrar configuração da 074 e
perfil aprovado; os 30 segundos dos complementos não são requisito fechado.

## RF-007 — Bridge antes da aplicação

Launcher Windows deve validar toolchain, AAR/APKs, endpoint e `PORTA_PINPAD`
herdada, sem COM fixa, provisionamento de chaves ou encerramento de processo
concorrente. Iniciar Bridge físico em loopback `127.0.0.1:39100` por padrão,
com log único e overrides válidos preservados. Processo auxiliar oculto.

Confirmar prontidão PBRG/Ping com prazo limitado; porta aberta não basta.
Selecionar emulador online sem ambiguidade, verificar `adb reverse` da porta
configurada e somente então instalar/entregar APKs e abrir `:app`.
Falha impede abertura automática do host. Android usa `localhost` com reverse;
`10.0.2.2` somente por override. Login e clique no menu continuam obrigatórios.

Registrar processo criado/log/resultado e oferecer encerramento explícito
apenas desse processo. Prontidão Bridge não comprova COM/OPN.
Todo evento de hardware usa o componente ABECS; edição, foco, layout e menu
são eventos de UI e não geram comandos físicos artificiais.

## RNF — Visual, acessibilidade e segurança

- Referência visual principal: os cinco SVGs de `source-material/gemini/`,
  escolhidos expressamente pelo solicitante em 2026-10-10 e aprovados em
  [REV-006](reviews/2026-10-10-spec-review-layout.md). Etapa 1 com dois painéis;
  etapas 2–5 com cartão central, stepper conectado, fundo claro, azul #003366,
  cantos arredondados, resumo e ilustrações equivalentes aos anexos.
- Indicador não interativo de cinco etapas com número/rótulo da etapa atual.
  Cancelamento precoce não marca passos não executados como concluídos.
- Cartões de entrada/resumo, total destacado e ações sólidas azul-escuro
  sem ícones. Estado sempre textual, não apenas por cor.
- Duas colunas quando couber; coluna rolável em janela reduzida/fonte grande,
  sem posições absolutas 1280 × 800 e sem esconder ações essenciais.
- Toque mínimo 48 dp, foco padrão, semântica acessível e textos em pt-BR.
- Aguardo descreve estado real; spinner indeterminado e transições suaves
  entre etapas conforme solicitação de 2026-10-10 e REV-006. Implementação
  nativa Compose com fade/deslocamento curto e escala de animação do sistema;
  movimento não inicia comandos nem representa avanço físico ou financeiro.
- Não duplicar cabeçalho do host nem copiar marcas/dados pessoais dos SVGs.
- AAR com origem/versão/ABI/SHA-256. Provar libs nativas no carregamento
  dinâmico e INTERNET no processo host; permissão do plugin sozinho não basta.
- Logs só com metadados permitidos; UI não evidencia certificação PCI/EMV.

## Critérios de aceite

| ID | Evidência obrigatória |
| --- | --- |
| CA-01 | Manifesto/serviço/API 1.1/menu mantidos, sem launcher do plugin. |
| CA-02 | Cálculo monetário exato, inválidos bloqueados e perfil/tarifa/parcelas aprovados. |
| CA-03 | Cinco etapas, resumo consistente e confirmação real no pinpad. |
| CA-04 | Modalidade/AIDs/modos elegíveis e fallback comprovados pelo Go. |
| CA-05 | GOX/GPN condicionais; sem senha ou dados sensíveis no Android. |
| CA-06 | FCX/autorização separados; aprovado/negado/erro/cancelado/indeterminado comprovados. |
| CA-07 | Duplo clique, timeout, cancelamento, detach e resposta tardia sem replay. |
| CA-08 | Bridge pronto antes do Android; COM ausente/porta ocupada/ADB ambíguo falham com evidência. |
| CA-09 | AAR/nativas carregados pelo host após login/discovery; retorno com cleanup. |
| CA-10 | Unitários, lint, build, UI/lifecycle/adaptação e integração com exit code; cobertura elegível mínima 80%. |
| CA-11 | Hardware físico validado separadamente de fake/scripted; sem segredos nas evidências. |
| CA-12 | KDoc, revisão de implementação e validação registrados; aprovação formal antes de encerrar. |

## Decisões históricas REV-003/REV-004 (substituídas pelo contrato abaixo)

1. Homologação física DES decidida; falta cenário controlado/decisão para FCX.
2. Perfil adquirente/EMV: tabela DES recebida e PIN DUKPT TDES/3/08/WK vazia
   confirmados; fechar parâmetros GCX/GOX/FCX e provisionamento da tabela no
   Android. Não anexar chaves; comprovar provisionamento PIN no teste físico.
3. Contrato Go para GCX completo e carga de registros pré-formatados compatível
   com a 074, ou revisão explícita dessa baseline. Validar GPN com PAN vazio
   antes de GTK; esta Change não pode promover o stub GCX implicitamente.
4. Taxa, arredondamento, limites, parcelas de débito e prazos por fase.

## Contrato da implementação para teste

O [contrato executável](contrato-implementacao-homologacao.md) integra esta
SPEC e define extensões Go aditivas, schema do perfil obrigatório,
provisionamento Android, regras monetárias, confirmação e cenários FCX.
Ele substitui as decisões pendentes acima. Nenhum parâmetro obrigatório
ausente será suprido por valor financeiro inventado. Homologação física
ocorre após implementação e testes técnicos; parar em IMPLEMENTADA conforme
o prompt implementar-mudanca-para-teste.


## Correção da primeira tela — REV-007

Contrato aprovado em [REV-007](reviews/2026-10-10-spec-review-primeira-tela.md):
remover faixa Voltar ao menu do host, manter Voltar Android, oferecer SAIR
vermelho abaixo de AVANÇAR. Modalidade e valor da compra editáveis sem perfil;
cálculo local independente do hardware. Taxa = 5,34% + 0,80 ponto percentual
por parcela adicional (1..12); débito em uma parcela. Total = compra + tarifa
HALF_UP em centavos. Perfil fornece limites e filtros de aplicação distintos,
não a taxa. AVANÇAR exige perfil válido. Testar cálculo, edição, seleção enviada
à captura, retorno e layout; preservar animações já implementadas.

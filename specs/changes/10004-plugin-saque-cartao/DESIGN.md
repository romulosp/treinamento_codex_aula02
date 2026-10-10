# Design: plugin-saque-cartao

Revisão de 2026-10-10; contrato aprovado em REV-005. Baseline anterior
preservada em `references/baseline-2026-09-25/`.

## Fronteiras

```text
Host :app (login, discovery, menu, cabeçalho)
  → ComposeView :plugin-saque-cartao
  → estado imutável + eventos → repository tipado/main-safe
  → AAR mobile/core Go da 074 → EmulatorTransport/PBRG
  → Bridge Windows → ownership/serial → Pinpad ABECS 2.12
```

O APK mantém `compileOnly(:shared-api)`, entrada `PluginSaqueCartaoApp` e
retorno por `IPluginRouter`. Somente o host possui Activity executável.
Kotlin não monta protocolo nem decide fallback. O app diagnosticopinpad é
referência do binding, não dependência a importar. Go continua proprietário
dos dados sensíveis e das regras ABECS.

## Estado e lifecycle

Um proprietário da jornada mantém etapa, resumo congelado, ação, conectividade,
estado da sessão, operationID e resultado seguro. Renderização separada em
`SaqueCartaoScreen(state, onEvent, modifier)`; a rota compõe dependências e menu.
Callbacks sobem, estado desce. Trabalho bloqueante fora da main thread;
cancelamento explícito Go, uma operação ativa e descarte de resposta tardia.
Cleanup tem orçamento limitado e erros observáveis. Recriação não reenvia.

## Mapeamento e gaps

O [contrato consumidor da 074](integracao-074.md) registra as chamadas e os
dados necessários. Reutilizar a fachada/AAR existentes para comandos já
disponíveis. Não criar outra API Bridge nem duplicar transporte; mudanças
eventuais devem se limitar ao gap confirmado de assinatura/formato.

| Etapa | Fachada/core existente | Pendência |
| --- | --- | --- |
| Sessão | NewClient/Ping/Open/GetState/Close/Cancel | Perfil seguro e prova no processo host. |
| Valores | Cálculo exato no plugin | Tarifa, limites e parcelas. |
| Confirmação | DEX/DSP + GKY ou MNU | Sequência por capacidade e interpretação normativa da confirmação. |
| Leitura | PurchaseGCXSummaryJSON; GTK resumido | GCX completo/modalidade/AIDs reservado pela 074. |
| EMV/PIN | ContinueEMVSummaryJSON; CapturePINMK/DUKPT | Perfil e GPN com PAN vazio após GCX, antes de GTK, quando normativamente aplicável. |
| Finalização | FinalizeEMVSummaryJSON | Origem da autorização e mapeamento FCX. |

A extensão necessária deve manter dados de cartão no Go e cruzar apenas
handle/ID opaco e summaries permitidos. Não retornar PAN de GTK para Kotlin,
nem colocar chaves/blobs EMV em Compose, estado salvo ou BuildConfig.

Reavaliação DES: a sequência de GPN com PAN vazio pode dispensar nova fachada
de retenção de PAN. A tabela recebida contém registros já formatados com N3;
validar importação/preservação do comprimento no Go, sem duplicação pelo
builder TLR. Perfil e evidências em [perfil DES](perfil-homologacao-des.md).

## Layout e launcher

Ver [REV-006](reviews/2026-10-10-spec-review-layout.md): os SVGs Gemini são a
referência visual principal. Conteúdo do plugin: título, indicador,
formulário/resumo e instrução/aguardo; duas colunas quando couber e rolagem
em janela reduzida/fonte grande. Não expor comandos/protocolo ao operador.

Etapa 1: painéis equivalentes de formulário branco e resumo #F8FAFC, total
em #EBF4FF. Etapas seguintes: cartão central limitado a 600–700 dp, com
resumo de confirmação, modos ilustrados de leitura, terminal decorativo
na etapa 4 e recibo do resumo real no resultado. Ajustar espaços à janela
e ao cabeçalho existente; não duplicar o cabeçalho desenhado nos SVGs.

Launcher dedicado em `scripts/` do projeto Android compõe o executável Bridge
da 074, prontidão, reverse e abertura do host. Não usar o executável CLI como
Bridge nem executar BAT com `pause` em pipeline. Processo auxiliar oculto;
encerramento só da instância que o launcher criou.

Provar resolução do binding/nativas pelo carregador dinâmico e permissão de
rede do host. Ajustes necessários no loader são impacto técnico da 10004,
sem levar regras de saque ao host. Não alterar a 074 silenciosamente.

## Contrato aditivo e configuração

A [ADR-10004-01 e contrato executável](contrato-implementacao-homologacao.md)
resolvem os gaps da tabela acima: captura GCX filtrada e TLR preservado no Go;
perfil runtime obrigatório, validado antes de I/O; provisionamento debug
privado no host. GPN existente dispensa nova API PAN. Regras detalhadas desse
contrato prevalecem sobre pendências históricas desta tabela.

## Verificação

Unitários: dinheiro/transições/concorrência/cancelamento. UI: semântica,
callbacks, foco, ausência de campo PIN e layout com fonte ampliada.
Integração: host/plugin/AAR/nativas/lifecycle. Go: GCX/AIDs/fallback,
sessão GTK/GPN/GOX/FCX e redaction. Launcher: COM, porta, ADB e prontidão.
Hardware: cartão de homologação autorizado, evidência própria separada de fake.


## Correção da primeira tela — REV-007

Contrato aprovado em [REV-007](reviews/2026-10-10-spec-review-primeira-tela.md):
remover faixa Voltar ao menu do host, manter Voltar Android, oferecer SAIR
vermelho abaixo de AVANÇAR. Modalidade e valor da compra editáveis sem perfil;
cálculo local independente do hardware. Taxa = 5,34% + 0,80 ponto percentual
por parcela adicional (1..12); débito em uma parcela. Total = compra + tarifa
HALF_UP em centavos. Perfil fornece limites e filtros de aplicação distintos,
não a taxa. AVANÇAR exige perfil válido. Testar cálculo, edição, seleção enviada
à captura, retorno e layout; preservar animações já implementadas.

# SPEC — retorno personalizado do cartão

## Status
`SPEC_APROVADA` — [revisão](reviews/2026-11-08-spec-review.md).

## Requisitos
1. API 1.5 aditiva: CardPaymentResultPresentation COMPONENT/CALLER; CardPaymentOptions(resultPresentation=COMPONENT). Novas facetas ICardPaymentPresentationHostContext e ICardPaymentPresentationComponent estendem as 1.4, com overloads que recebem pedido/opções/callback. Não alterar construtor/copy ou assinatura de CardPaymentRequest, CardWithdrawalResult, métodos 1.3/1.4 ou autenticação 1.2.
2. Ingressos exige minor 1.5 e usa somente nova faceta, com CALLER. Ausência de faceta/provedor elegível retorna UNAVAILABLE sem fallback que ignore a opção ou abra hardware. Host valida consumidor ativo, encaminha pedido/opções sem alteração, mantém concorrência, cleanup e supressão de callbacks após abandono.
3. Componente novo implementa a faceta 1.5. Rotas antigas createWithdrawal e createPayment 1.4 mantêm modo COMPONENT/resultado manual. CALLER só termina automaticamente quando resultado local é APPROVED ou CAPTURED, cleanup está confirmado e não existe erro/cancelamento/detach pendente. Publicar closedState e callback uma vez depois de close; não repetir dispose/close ao remover View. Falha, recusa, UNKNOWN, cancelamento e cleanup falho não disparam retorno automático de sucesso.
4. Status é preservado integralmente: CAPTURED continua CAPTURED. Nenhuma conversão ou alegação de autorização financeira. A bilheteria, por decisão explícita do usuário, aceita APPROVED ou CAPTURED com cleanup confirmado para emitir somente demonstração e mostrar Transação Realizada com Sucesso; mantém aviso local/sem comprovação financeira/ingresso sem validade. Demais guardas por ID de pedido e timer da 10024 permanecem.
5. Componente não conhece classe/rota/ID do plugin. CALLER apenas devolve controle ao host, que restaura a mesma View do consumidor e chama seu callback. Sucesso da bilheteria permanece propriedade do plugin; continua por 3s e retorna à seleção inicial com estoque vendido preservado.
6. Preservar defaults antigos, valores e tarifas, sequência de confirmação/captura/PIN/EMV e cleanup. Nenhum novo comando físico automático do agente. A mudança é somente contrato de apresentação e encerramento visual local.
7. Testar owner com repositório sintético pausando close, atrasos, duplicação, erro/cleanup falho e modo padrão; testar fábrica real do componente com repositório sintético injetado (sem adapter Go), fluxo CAPTURED automático e legado manual. Integração host verifica opções CALLER/status CAPTURED, sucesso da bilheteria e retorno inicial. Provedor antigo que não implementa faceta nova não pode receber fallback.

## Arquitetura e baseline
Mesmos módulos/packages, stack e versões SDK da 10024; única evolução de versão é contrato interno SharedApi 1.5. Atualizar manifestos binário/JSON dos consumidores/provedor que usam facetas novas e publicação shared-api para 1.5.0. Ponto de entrada do componente mantém construtor público sem argumentos para DexClassLoader; construtor interno com factory de SaqueRepository permite teste real de sua composição sem I/O físico. Novo parâmetro do owner tem default false e preserva chamadores antigos.

## Critérios de aceite
- CA-01: CALLER + captura concluída com cleanup devolve CAPTURED automaticamente, uma vez, sem clique em VOLTAR e sem close repetido.
- CA-02: APPROVED também retorna; cleanup falho, DENIED/ERROR/UNKNOWN/cancelamento/detach não concluem automaticamente como sucesso.
- CA-03: consumidor 1.3/1.4 e opções padrão continuam vendo tela terminal/VOLTAR; novo pedido não recua a provedor incapaz de personalização.
- CA-04: no host, ingressos recebe CAPTURED/cleanup confirmado, mostra sua própria tela de sucesso e retorna em 3s, preservando o lugar vendido. Nenhuma conversão de status ou perda das guardas por ID.
- CA-05: JVM/shared/componente/ingressos/host, coberturas mínimas 80%, lint/builds e instrumentação sintética aprovados com evidências; validador estrutural executado e limitações registradas. Hash do AAR/nativos, configuração e criptografia preservados.
- CA-06: host final instalado para teste humano sem nova operação física automática; registrar marco na memória 10011. Sem archive/commit antes dos gates formais.

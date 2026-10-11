# Remoção do indicador de etapas — REV-009

Data: 2026-10-10. Decisão: `SPEC_APROVADA`, por solicitação explícita do
solicitante sobre a captura da tela hospedada no host. Complementa
REV-005/006/007/008.

Remover o indicador não interativo de cinco etapas (círculos `1..5` com rótulos e
o texto "Etapa X de 5") de todas as telas do plugin. Motivo declarado: sob o
cabeçalho global do host, o indicador consome altura e provoca rolagem
desnecessária já na primeira tela.

A remoção substitui a referência visual da
[REV-006](2026-10-10-spec-review-layout.md) somente nesse elemento: painéis,
cartão central, ilustrações, total destacado, ações sólidas, cores e movimento
permanecem. A etapa atual continua identificada pelo conteúdo do painel e pela
transição animada; não é criado indicador substituto, contador de progresso ou
barra de etapas.

O estado `completedSteps`, cujo único consumidor era o indicador, é removido do
estado público e do proprietário da jornada, com os testes correspondentes
atualizados. A conclusão de cada fase continua sendo comunicada pelo conteúdo do
painel, pelo resultado e pelo recibo real; cancelamento precoce não fabrica
progresso, pois não há mais progresso exibido.

Aceite: ausência dos rótulos do indicador em todas as etapas; primeira tela sem
rolagem em 1280 × 800 dp com ações essenciais visíveis sem gesto; demais
critérios de layout, acessibilidade, preço e jornada preservados. Não valida
hardware, cartão, PIN ou autorização financeira.

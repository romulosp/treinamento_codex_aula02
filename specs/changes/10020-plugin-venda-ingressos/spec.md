# SPEC: 10020 — teste demonstrativo de ingressos de cinema

Evolução: a Change 10021 envia valor base e ID do pedido por API 1.4; o valor
base é bloqueado no componente e tarifa/total seguem a 10004. Ela substitui as
limitações de transferência de valor descritas historicamente abaixo.

## Status
`SPEC_APROVADA` — revisão r4 em reviews/2026-11-07-spec-review-r4.md.

## Escopo e limites
Plugin Android independente para um teste manual local de cinema, após autenticação no host. O usuário exige chamada do componente de cartão existente e ingresso demonstrativo sem validade real. A abertura explícita do componente pode iniciar preparação física do pinpad: nenhum comando físico será executado automaticamente pelo agente. A 10004 e a 10019 não são alteradas: seu contrato não recebe o valor do pedido nem a modalidade e seu resultado não constitui autorização financeira. Uma venda real requer Change independente para a infraestrutura; esta demonstração não prova essa capacidade.

O módulo implementa `IPluginNegocioApp`, `compileOnly(:shared-api)`, manifesto, serviço e exatamente uma folha `Principal > Venda de ingressos`. O host descobre e organiza o menu. Requer API compartilhada 1.3 para `IComponentHostContext.launchCardWithdrawal`. Nenhum launcher, dependência concreta do host ou de outro plugin.

## Comportamento verificável
1. Identificar claramente o modo demonstrativo, a ausência de cobrança e a falta de validade dos ingressos.
2. Permitir cadastrar uma sessão por vez com título de filme, sala, horário textual e preço em reais com até duas casas, de R$ 0,01 a R$ 999,99. Campos vazios ou valores inválidos não criam sessão e mostram mensagem. A sala contém 12 assentos A1–A6 e B1–B6.
3. Selecionar um assento livre e criar pedido por ação explícita. A seleção é temporária na tela; não há reserva com prazo nem concorrência entre terminais. Seleção de assento vendido é proibida. Não permitir substituir a sessão durante um pedido pendente ou depois de uma venda; uma nova sessão pode ser criada após cancelar o pedido sem vendas.
4. Apresentar filme, sala, horário, assento e preço no pedido; ao solicitar pagamento, chamar o componente de cartão pelo host, sem transmitir parâmetros que não existem no contrato. Mostrar que o valor deve ser conferido na jornada do cartão e que o resultado é local, sem comprovar pagamento do pedido. Somente resultado local APPROVED com cleanup confirmado habilita a emissão demonstrativa explícita; não é autorização financeira. Emissão marca assento vendido uma única vez e mostra ingresso SIMULAÇÃO, SEM VALIDADE. DENIED, CAPTURED, CANCELED, ERROR, UNKNOWN, UNAVAILABLE e BUSY não emitem ingresso; cleanup falho não habilita emissão. Ausência de host é indisponibilidade. Ao cancelar o pedido não se emite ingresso.
5. Consultar vendas da sessão e o ingresso demonstrativo gerado. Dados residem em memória da instância do plugin e podem ser perdidos no encerramento do processo, logout ou recarga; não se destinam a estoque real. Nenhuma garantia multiusuário, de retomada após falha ou de reserva persistente é alegada.
6. UI de cinema elegante, mapa de assentos, azul do sistema e hierarquia clara, com disposição adaptável, rolagem, rótulos textuais e ações acessíveis; sem segredos e sem logs financeiros. Testes exercitam a chamada por host sintético, todos os resultados e estados; sem acionar hardware automaticamente.

## Critérios de aceite
- CA-01: host descobre APK verificado, organiza caminho e abre tela pelo menu após login, sem substituir o plugin de operações.
- CA-02: campos inválidos são bloqueados; valores e resumo são coerentes.
- CA-03: aprovação, recusa, cancelamento, indisponibilidade e prevenção de duplicação satisfazem os comportamentos acima.
- CA-04: simulação, ausência de cobrança, ausência de validade e perda de dados estão visíveis na interface e no roteiro.
- CA-05: usar Gradle Wrapper, testes JVM, lint, build, instrumentação e evidências, incluindo cobertura mínima de 80% do núcleo elegível e limites do ambiente.
- CA-06: componente é invocado pelo host apenas mediante ação humana; sem execução física automática ou alteração de componentes financeiros; nenhum resultado é apresentado como autorização real.
- CA-07: diagnóstico opt-in do binding real no processo host comprova Ping sem abrir sessão física; localhost depende de reverse e 10.0.2.2 somente por override explícito conforme 074. Registrar endpoint efetivo sem dados sensíveis.

## Fora de escopo
Shows, venda real, reembolso, persistência, múltiplos dispositivos, autenticação adicional, relatórios, validação real de acesso e políticas comerciais ou legais de produção. Esses assuntos permanecem no Discovery.

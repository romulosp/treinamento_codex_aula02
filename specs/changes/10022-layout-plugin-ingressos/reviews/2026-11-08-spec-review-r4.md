# Revisão r4 — fidelidade à imagem do protótipo

Decisão: `SPEC_APROVADA` para correção visual.

O usuário rejeitou a composição entregue e forneceu imagem de referência concreta. A revisão altera a geometria da apresentação para carrossel largo no topo, mapa e resumo branco abaixo. A referência visual mais recente prevalece sobre a proporção vertical dos cards do texto inicial.

REV-007 — Importante, resolvido: primeira entrega criou slogan, etapas, cartaz único vertical e resumo com cabeçalho escuro, ausentes na referência. Requisitos 2/4/8 e CA-07 agora descrevem formas e posições verificáveis por captura.

REV-008 — Importante, resolvido: a imagem mostra múltiplas sessões/assentos e catálogo; o usuário já delimitou somente layout com funções preservadas. Carrossel é galeria local de capas e atalho de preenchimento do cadastro explícito existente; sessão atual só muda pela ação Cadastrar sessão. Não inventar disponibilidade, horários ou assentos extras. Pedido/venda continuam protegendo a sessão.

REV-009 — Importante, resolvido: cartões gerados são verticais. Recortar visualmente no card largo e manter miniatura vertical no resumo, sem regenerar imagens nem chamar Google. O título continua exposto em texto nativo para leitura.

Preservados contratos de pagamento, callbacks, preço, cleanup, modo demonstrativo, limites e gates. Sem nova dependência, SDK, integração ou decisão financeira. O cadastro em diálogo é estado visual local, coberto por testes atualizados.

REV-010 — Importante, resolvido na SPEC antes da correção: BusinessPluginManager.createScreen passa activityContext do host. IDs drawable do APK de plugin não são resolvidos de forma segura nos Resources desse Context. Requisito 10 prevê arquivos pelo classloader do plugin e decodificação main-safe, sem alterar o host. Atualizar o teste de integração já existente para abrir cadastro em diálogo e conferir uma imagem efetivamente carregada no processo host.

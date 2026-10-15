# DESIGN — 10014

paymentContext possui GTKResponse privado e buffers PIN/KSN/EMV/decisão.
PaymentTracks transfere ownership do resultado validado para o contexto sob
s.mu somente após conferir contexto, geração e cancelamento. Falha limpa resposta.
GPN/GOX copiam resultado válido para contexto antes de entregá-lo ao wrapper.
clearPaymentLocked sobrescreve todos os buffers retidos.

PaymentAuthorizationSnapshot retorna objeto interno com mutex e campos privados,
somente fase done/operação correspondente/sessão aberta. Getters retornam cópias;
Clear sobrescreve os buffers próprios. O futuro serviço deve usar esse contrato
durante a operação, antes de finalizar/fechar, e limpar snapshot e cópias.
Android mantém apenas decisões e consumo PIN já autorizado pela 10013.

Não alterar stack, UI, SharedApi, manifesto, configuração externa, protocolo
ou sequenciamento. Retenção efêmera é decisão de arquitetura aprovada aqui;
não equivale a persistência ou integração com rede credenciadora.

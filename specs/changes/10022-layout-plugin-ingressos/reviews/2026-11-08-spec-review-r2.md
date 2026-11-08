# Revisão r2 — ilustrações Gemini

Decisão: `SPEC_APROVADA`.

O usuário solicitou explicitamente gerar cartazes com Nano Banana/Google Gemini e autorizou leitura de propriedade específica em arquivo externo, sem memorizar ou registrar a chave. Requisito 2 e fronteira de segurança foram atualizados antes da integração de imagens no código.

REV-004 — Importante, resolvido: uso de API de imagens poderia introduzir chave no Android, logs ou chamadas de rede em execução. O contrato restringe geração ao desenvolvimento, autenticação HTTPS em cabeçalho e persistência apenas dos artefatos/prompts/metadados sanitizados. Modelo disponível foi confirmado por listagem autenticada, sem imprimir chave.

REV-005 — Importante, resolvido: associação de cartazes não pode criar catálogo nem alterar cadastro ou pedido. Apenas apresentação por título; fallback local para títulos desconhecidos. As quatro referências vêm do anexo. Sem alteração de regras financeiras ou arquitetura.

Mantidos os demais critérios e aprovação da r1. Não representa validação da implementação.

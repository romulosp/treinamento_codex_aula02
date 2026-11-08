# Revisão r3 — gerador integrado e fallback

Decisão: `SPEC_APROVADA`.

O usuário desistiu expressamente do Gemini após três respostas HTTP 429 RESOURCE_EXHAUSTED e pediu geração pelo próprio assistente. Nenhuma nova chamada Google será feita. O gerador integrado produziu capas de Coringa 2, Avatar 3, Batman (cidade noturna) e cinema genérico. A referência Spider-Man foi recusada pelo filtro do provedor; usar a capa genérica, sem alegar que foi gerada a capa específica.

REV-006 — Importante, resolvido: fonte e disponibilidade das imagens mudaram. SPEC requisito 2, segurança e DESIGN foram atualizados antes da integração. O fallback genérico mantém a tela utilizável sem ampliar regras de negócio. Nenhum conteúdo de chave foi copiado ou incluído nos artefatos.

Manter os critérios funcionais e técnicos anteriores. Aprovação da SPEC não aprova implementação, teste humano ou encerramento.

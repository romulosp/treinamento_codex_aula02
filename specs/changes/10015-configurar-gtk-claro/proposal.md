# Proposta — 10015

Status: `SPEC_APROVADA` pela [REV-001](reviews/2026-10-14-spec-review.md).

Usuário esclareceu que a aplicação deve obter GTK em claro agora e permitir
mudar depois para criptografia DUKPT TDES DATA no índice08. A obrigatoriedade
de método30 introduzida nas Changes anteriores não representa esse requisito.
Adicionar escolha explícita pelo perfil externo, manter GTK obrigatório e
retenção privada da10014. Atualizar perfil DES real conforme pedido.

Escopo: validação perfil Go/Kotlin/launcher, montagem GTK, testes e artefatos,
documentação e memória. Fora de escopo: chaves, compra automática, autorizador,
PRD, mudanças de CVM/PIN ou logs de cartão.

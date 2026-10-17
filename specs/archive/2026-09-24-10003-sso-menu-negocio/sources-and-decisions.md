# Fontes e decisões

## DEC-001 — Direct Access Grant

- Classificação: `CONTEXTUAL` e risco aceito.
- Fonte: documentação oficial Keycloak, consultada em 2026-09-22.
- Evidência: Direct Access Grant recebe credenciais, client id e segredo para
  clientes confidenciais; precisa estar habilitado no cliente e é desaconselhado
  pelas práticas atuais para novos sistemas.
- Decisão: usar somente no protótipo intranet, conforme `ADR-001`.

## DEC-002 — Cleartext local Android

- Classificação: `CONTEXTUAL`.
- Fonte: Android Network Security Configuration, consultada em 2026-09-22.
- Evidência: Android 9+ bloqueia cleartext por padrão e permite configuração por
  domínio.
- Decisão: liberar apenas `10.0.2.2` no manifest/configuração de debug; release
  permanece sem permissão de cleartext.

## SKILL-CANDIDATE-001 — Android Skills oficial

- Classificação: `REJECT`
- Origem: catálogo oficial Android Skills.
- Problema: integração REST pequena e específica do contrato existente.
- Sobreposição: as skills locais já cobrem estado, efeitos, foco, componentes e
  testes Compose.
- Decisão: nenhuma skill externa será incorporada nesta Change.

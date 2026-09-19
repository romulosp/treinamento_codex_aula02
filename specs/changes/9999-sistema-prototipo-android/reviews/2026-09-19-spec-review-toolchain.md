# Revisão extraordinária da SPEC — toolchain: 9999-sistema-prototipo-android

## Contexto

Durante o planejamento técnico, antes da criação de código, a combinação aprovada foi confrontada com a documentação oficial atualizada do Compose.

## Achado

### REV-005 — Bloqueante — BOM Compose incompatível com `compileSdk = 36`

- Evidência: a documentação oficial Android, atualizada em 2026-09-16, informa que Compose 1.12.0 ou superior exige `compileSdk = 37`; o BOM `2026.09.00` resolve `ui` e `foundation` para 1.12.1, enquanto a SPEC fixa `compileSdk = 36`.
- Impacto: a configuração aprovada não forma um build implementável sem violar o SDK ou a versão de Compose definidos.
- Recomendação: manter o SDK 36 solicitado e selecionar o BOM estável mais recente cujo Compose permaneça abaixo de 1.12, comprovando a resolução no POM oficial.

## Conclusão

`REPROVADA`

A implementação foi suspensa antes da criação de código. A Change retorna à especificação para alinhar o BOM com `compileSdk = 36`.

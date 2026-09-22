# Revisão da SPEC — ressubmissão de toolchain: 9999-sistema-prototipo-android

## Escopo

Revisão exclusiva da correção de `REV-005`, sem alteração do comportamento funcional, SDK alvo, SDK mínimo ou canal de distribuição.

## Verificação

- `compileSdk = 36` e `targetSdk = 36` permanecem inalterados.
- O BOM `2026.09.00` foi removido do contrato.
- O BOM oficial `2026.06.01` existe no Google Maven.
- O POM oficial resolve `ui`, `foundation` e `ui-test-junit4` para 1.11.4 e Material 3 para 1.4.0.
- A exigência de `compileSdk = 37` inicia em Compose 1.12.0; portanto, a linha 1.11.4 é compatível com o SDK 36 aprovado.
- `spec.md`, `DESIGN.md`, `sources-and-decisions.md` e `implementation-plan.md` registram a mesma combinação.

## Novos achados

Nenhum achado bloqueante ou importante.

## Conclusão

`SPEC_APROVADA`

`REV-005` está resolvido. O planejamento e a implementação podem prosseguir com BOM `2026.06.01` e sem atualização automática de Compose.

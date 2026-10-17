# Compatibility Review — prompt2

## Resultado

`SPEC_APROVADA`

## Evidencias

- O prompt2 substituiu `compileSdk = 36` por `compileSdk = 37`.
- A documentação oficial do [AGP 9.4.0](https://developer.android.com/build/releases/agp-9-4-0-release-notes), consultada em 2026-09-19, confirma suporte à API 37, Gradle 9.6.0, JDK 17 e KGP 2.2.10.
- A documentação oficial do Compose confirma BOM `2026.09.00`, Compose 1.12.x, `compileSdk = 37` e AGP 9.
- `minSdk = 26` foi confirmado para o protótipo interno: Compose aceita API 21+, não há requisito funcional de API posterior e os testes de compatibilidade já cobrem a API 26.

## Decisao operacional

A Compatibility Review terminou em `PASS`. O baseline pode seguir para
implementação sem downgrade, upgrade silencioso ou troca de biblioteca.

## Skills aplicaveis na retomada

A skill base `android-native-engineering` sera obrigatoria. As sub-skills serao
lidas sob demanda: `compose-component-design`, `compose-focus-navigation`,
`compose-state-and-effects`, `compose-ui-testing-patterns` e
`grounded-writing`, conforme os gatilhos definidos no `AGENTS.md`.

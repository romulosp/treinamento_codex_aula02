# ADR-001 — Toolchain para compileSdk 37

## Status

`ACCEPTED`

## Decisao

Adotar AGP 9.4.0, Gradle 9.6.0, JDK 17, KGP 2.2.10, Compose BOM
`2026.09.00`, Compose 1.12.x stable, `compileSdk = 37`, `targetSdk = 36` e
`minSdk = 26`.

## Evidencia

O prompt2 aprovou `compileSdk = 37`. A verificação inicial considerou apenas
AGP 9.0.1 e produziu um falso bloqueio. A documentação oficial do AGP 9.4.0,
consultada em 2026-09-19, confirma suporte à API 37 e publica o restante da
combinação adotada.

## Impacto

A implementação pode começar após a revisão formal da SPEC. Qualquer mudança
de versão exige nova Compatibility Review completa.

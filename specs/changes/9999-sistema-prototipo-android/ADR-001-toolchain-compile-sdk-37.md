# ADR-001 — Toolchain para compileSdk 37

## Status

`AMENDED_BY_CHANGE_10000`

## Decisao

Adotar AGP 9.4.0, Gradle 9.6.0, JDK 17, KGP 2.2.10, Compose BOM
`2026.09.00`, Compose 1.12.x stable, `compileSdk = 37`, `targetSdk = 36` e
`minSdk = 29`.

O ADR original aprovou `minSdk = 26` para a fundação monolítica. A Change
`2026-09-24-10000-arquitetura-microkernel-plugins-apk` e seu ADR de fronteira
de segurança elevaram o mínimo para 29. Os demais elementos do toolchain foram
preservados.

## Evidencia

O prompt2 aprovou `compileSdk = 37`. A verificação inicial considerou apenas
AGP 9.0.1 e produziu um falso bloqueio. A documentação oficial do AGP 9.4.0,
consultada em 2026-09-19, confirma suporte à API 37 e publica o restante da
combinação adotada.

## Impacto

A plataforma de plugins e todos os seus módulos usam API mínima 29. Qualquer
mudança posterior de versão exige nova Compatibility Review completa.

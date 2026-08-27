# ADR-001: Usar Java 17 com Quarkus 3

## Status
Aceito em 2026-08-26.

## Contexto

A configuração de origem solicitava `maven.compiler.release` igual a 11 e a plataforma Quarkus `3.2.10.Final`. Quarkus 3 requer Java 17 ou superior, portanto essa combinação não produz um backend suportado.

## Decisão

O projeto usará Java 17 e manterá Quarkus `3.2.10.Final`.

## Consequências

O ambiente de desenvolvimento e CI precisa fornecer JDK 17. Para usar Java 11 no futuro, será necessária uma mudança aprovada que adote uma versão compatível do Quarkus e revise dependências e testes.

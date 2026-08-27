---
name: java-integration-test
description: 'Use when: criar ou executar testes de integração Quarkus com JUnit 5, Rest Assured, Mockito ou H2.'
argument-hint: 'Informe a mudança e os cenários a validar.'
---

# Testes de integração Java

1. Derive os cenários dos critérios de aceite da SPEC.
2. Use `@QuarkusTest` para inicialização e Rest Assured para contratos HTTP.
3. Use H2 no perfil de teste quando DB2 não for requisito do cenário.
4. Registre comandos, resultados e falhas em `validation.md`.

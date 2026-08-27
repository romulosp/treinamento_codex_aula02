# Estratégia de testes

- Testes unitários usam JUnit 5 e Mockito.
- Testes de integração Quarkus usam `@QuarkusTest` e H2 no perfil de teste quando não for necessária compatibilidade específica com DB2.
- Toda validação registra ambiente, resultado e evidência em `validation.md`.

---
name: java-unit-test
description: 'Use when: criar, refatorar ou revisar testes unitários Java com JUnit 5 e Mockito, incluindo Panache sem container Quarkus e cobertura de regra de negócio.'
argument-hint: 'Informe o módulo, pacote ou classes Java a testar.'
---

# Testes unitários Java

## Objetivo

Criar, refatorar ou revisar testes unitários Java que validem regras de negócio com JUnit 5 e Mockito, sem banco de dados nem container Quarkus.

## Processo obrigatório

1. Leia `AGENTS.md`, `specs/shared/testing/testing-strategy.md`, a SPEC aprovada e o `validation.md` da mudança.
2. Inventarie todos os arquivos `.java` de produção no escopo e classifique-os como aplicáveis ou excluídos conforme a estratégia. Registre toda exclusão e sua justificativa na validação.
3. Para cada classe aplicável, localize o teste existente ou crie uma classe de teste correspondente. Teste a API pública e a regra de negócio, não detalhes de implementação.
4. Crie cenários de sucesso, erro e decisão: coleções nulas, vazias e preenchidas; `Optional.empty()` e `Optional.of()`; `if`, `else`, `switch` e enums, quando aplicáveis.
5. Use Mockito somente nos colaboradores necessários. Para execução parcial deliberada, use `spy()` com `doReturn()`. Use `doNothing()` somente para método `void` e `verify()` para interações relevantes.
6. Não execute `persist()`, `find()`, `list()` nem operações dependentes de `EntityManager` ou `Arc`. Simule ou intercepte as chamadas Panache; mova cenários de infraestrutura para teste de integração.
7. Faça o mock exato da consulta: `TypedQuery<T>` para `createQuery(sql, Classe.class)` e `Query` para `createNativeQuery(sql)`. Em varargs `Object...`, use `any(Object[].class)` ou cast explícito.
8. Use reflexão apenas para regra relevante em método privado, por meio de utilitário reutilizável e com justificativa. Invoque métodos públicos e estáticos diretamente.
9. Monte dados válidos, preenchendo enums e demais atributos requeridos pela regra. Não produza falhas artificiais com objetos incompletos.
10. Execute os testes e a aferição de cobertura definida pelo módulo. A meta para classes aplicáveis é de 80% a 100% de linhas e branches, sem substituir a qualidade do cenário pela métrica.
11. Registre em `validation.md` ferramenta, escopo, exclusões, percentuais aferidos, comandos, resultados, códigos de saída e evidências. Se não houver ferramenta ou módulo executável, registre a indisponibilidade sem inventar porcentagens.

## Checklist de qualidade

- [ ] O teste é independente de banco, `EntityManager`, `Arc` e container Quarkus.
- [ ] Não há mocks desnecessários, suppressions injustificadas ou ambiguidades de overload.
- [ ] O teste valida resultado e comportamento de negócio, inclusive tratamento de exceções relevante.
- [ ] Não há `setAccessible(true)` sem necessidade documentada.
- [ ] O teste permanece compreensível e resistente a pequenas refatorações internas.
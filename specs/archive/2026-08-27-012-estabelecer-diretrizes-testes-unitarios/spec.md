# SPEC: 012-estabelecer-diretrizes-testes-unitarios

## Status
`SPEC_APROVADA`

## Referências e dependências

- `AGENTS.md`
- `specs/shared/testing/testing-strategy.md`
- `specs/templates/validation-template.md`
- `.agents/skills/README.md`
- `specs/shared/process/workflow.md`

## Requisitos funcionais

1. A estratégia canônica de testes deve definir as diretrizes obrigatórias para testes unitários Java com JUnit 5 e Mockito, incluindo todos os tópicos definidos na proposta.
2. A estratégia deve exigir inventário de cada arquivo `.java` de produção e, para cada classe aplicável, ao menos um teste unitário correspondente. Serviços de aplicação e domínio, repositórios, conversores, validadores, exceções customizadas, recursos utilitários e enums que contenham lógica são aplicáveis.
3. Interfaces sem implementação, DTOs ou records sem validação ou lógica customizada, enums isolados sem método com lógica, classes puramente declarativas de configuração e código gerado podem ser excluídos somente com justificativa na validação da mudança. Uma classe excluída permanece no inventário.
4. A estratégia deve definir a meta de cobertura de linhas e branches entre 80% e 100% para código de produção aplicável, priorizando a validação da regra de negócio sobre a métrica. A ferramenta de aferição, o baseline e o comando serão especificados pela mudança que introduzir o módulo Maven executável; esta mudança estabelece somente a política.
5. Deve existir a Skill `.agents/skills/java-unit-test/SKILL.md` para inspecionar classes Java, criar ou refatorar testes, evitar runtime Quarkus e registrar a evidência de cobertura. Ela deve ser usada na implementação ou revisão de mudanças que alterem classes Java aplicáveis ou introduzam testes unitários.
6. O índice `.agents/skills/README.md` deve listar `java-unit-test` na seção de Skills técnicas, com uma descrição que mencione criação, refatoração e revisão de testes unitários Java com JUnit 5 e Mockito.
7. O modelo de validação deve ter a seção `Testes unitários e cobertura` e exigir o registro da ferramenta, escopo de classes aplicáveis e excluídas, percentual de linhas e branches quando aferível, comando, resultado, código de saída e justificativas para exclusões ou indisponibilidade de medição.
8. `AGENTS.md` deve remeter à estratégia canônica e estabelecer que as diretrizes unitárias são obrigatórias nas mudanças Java.

## Requisitos não funcionais

1. A documentação deve estar em português do Brasil, ser objetiva e usar Markdown válido.
2. As diretrizes não podem exigir banco de dados, `EntityManager` real ou container Quarkus em testes unitários.
3. As diretrizes devem favorecer testes legíveis, estáveis e compatíveis com SonarQube.
4. A Skill deve ter frontmatter YAML válido, nome igual ao diretório e descrição descobrível com os termos de criação, refatoração e revisão de testes unitários Java.

## Regras de negócio

1. Em teste unitário, a lógica de negócio deve ser exercitada sem operações reais de Panache, como `persist()`, `find()` ou `list()`.
2. `spy()` só é permitido para execução parcial deliberada da lógica real; stubs em spies usam `doReturn()`, e `doNothing()` é reservado a métodos `void`.
3. Consultas JPQL criadas com `entityManager.createQuery(sql, Classe.class)` usam `TypedQuery<T>`; consultas nativas criadas com `entityManager.createNativeQuery(sql)` usam `Query`.
4. Stubs e verificações de varargs `Object...` usam `any(Object[].class)` ou casts explícitos para eliminar ambiguidade de overload.
5. Reflexão é excepcional, centralizada em utilitário reutilizável e limitada a regras relevantes em métodos privados; métodos públicos e estáticos são invocados diretamente.
6. Objetos de teste que dependam de enums devem conter todos os valores válidos exigidos pela regra; falhas artificiais por inicialização incompleta não representam cobertura válida.
7. Cada decisão relevante deve ter cenários de sucesso e erro, incluindo `if`, `else`, `switch`, enums, listas nulas, vazias e preenchidas, e `Optional.empty()` e `Optional.of()` quando aplicáveis.
8. Falhas e blocos `catch` com regra de negócio devem ser testados pelo comportamento observável após a exceção.

## Cenários e critérios de aceite

- [ ] A estratégia descreve Mockito, Panache sem container, tipos de query, varargs, reflexão, enums, SonarQube, estabilidade, exceções, branches e objetivo final.
- [ ] A estratégia fixa a cobertura-alvo de 80% a 100%, define classes aplicáveis e exclusões justificadas, com prioridade para validação da regra de negócio.
- [ ] A Skill `.agents/skills/java-unit-test/SKILL.md` orienta inventário de classes, testes diretos, Mockito sem container, cobertura, validação e registro de evidências.
- [ ] O modelo de validação contém a seção `Testes unitários e cobertura` com todos os campos de evidência especificados.
- [ ] `AGENTS.md` remete à estratégia e à obrigação para mudanças Java.
- [ ] A validação registra que não há classes Java nem módulo executável no estado atual, sem declarar cobertura inexistente como medida.

# Estratégia de testes

## Princípios gerais

- Testes unitários usam JUnit 5 e Mockito, exercitam a lógica real e são independentes de banco de dados e do container Quarkus.
- Testes de integração Quarkus usam `@QuarkusTest` e H2 no perfil de teste quando não for necessária compatibilidade específica com DB2.
- Toda validação registra ambiente, resultado e evidência em `validation.md`.
- A prioridade é a qualidade da validação da regra de negócio, seguida pela cobertura, legibilidade e manutenção futura.

## Inventário e cobertura

1. Cada mudança Java deve inventariar os arquivos `.java` de produção alterados ou incluídos no escopo.
2. Toda classe aplicável deve ter pelo menos uma classe de teste unitário correspondente. São aplicáveis serviços de aplicação e de domínio, repositórios, conversores, validadores, exceções customizadas, utilitários e enums que contenham lógica.
3. Interfaces sem implementação, DTOs e records sem validação ou lógica customizada, enums isolados sem método com lógica, classes declarativas de configuração e código gerado podem ser excluídos somente com justificativa explícita na validação. A exclusão não remove o arquivo do inventário.
4. A meta é obter de 80% a 100% de cobertura de linhas e branches no código de produção aplicável. A métrica não substitui cenários relevantes de negócio.
5. A ferramenta, o baseline e o comando de aferição serão definidos pela mudança que introduzir o módulo Maven executável. Não declare porcentagem de cobertura sem uma medição reproduzível.

## Mockito avançado

- Use `spy()` apenas quando for necessário executar parcialmente a lógica real da classe.
- Em spies, use `doReturn(...).when(spy)` em vez de `when(spy.metodo()).thenReturn(...)`, evitando a execução antecipada do método real.
- Use `doNothing()` exclusivamente para métodos `void`.
- Use `verify()` para validar interações que façam parte do comportamento relevante.
- Não crie mocks que não participem do comportamento testado. Prefira objetos reais simples quando isso tornar a regra mais clara.

## Panache sem container Quarkus

- Testes unitários não executam com o container Quarkus ativo; `Arc` pode estar nulo.
- Não execute chamadas reais a `persist()`, `find()`, `list()` ou qualquer operação que dependa de `EntityManager`.
- Simule ou intercepte chamadas Panache com Mockito e cubra a lógica anterior à persistência sem depender do runtime Quarkus.
- Quando o comportamento exigir banco, ORM ou ciclo de vida do Quarkus, ele pertence a teste de integração, não a teste unitário.

## Consultas, varargs e reflexão

- Use `TypedQuery<T>` ao simular `entityManager.createQuery(sql, Classe.class)`.
- Use `Query` ao simular `entityManager.createNativeQuery(sql)`.
- O tipo do mock deve corresponder exatamente ao tipo esperado pelo método de produção.
- Em métodos com `Object...`, prefira `any(Object[].class)` a `any()` e aplique casts explícitos quando necessários para resolver overloads ambíguos.
- Use reflexão somente para acessar método privado que contenha regra relevante e que não possa ser coberta pela API pública.
- Centralize a reflexão em utilitários reutilizáveis. Não use `setAccessible(true)` sem justificativa documentada.
- Métodos públicos devem ser testados diretamente; métodos estáticos devem ser invocados diretamente, sem reflexão.

## Dados válidos e regras dependentes de enum

- Construa objetos de teste válidos para a regra exercitada.
- Quando houver dependência de enums, como `CodigoTransacaoEnum` ou `SituacaoMovimentoEnum`, preencha todos os atributos exigidos pelo cenário.
- Não use objetos parcialmente inicializados para produzir exceções artificiais; elas não representam a regra de negócio.

## Qualidade, exceções e branches

- Mantenha os testes compatíveis com SonarQube: elimine ambiguidades de tipo, suppressions desnecessárias, múltiplos `continue` ou `break` no mesmo loop e reflexão injustificada.
- Valide comportamento e resultado, e não apenas a execução de linhas ou detalhes internos. Priorize asserts de negócio em vez de asserts puramente técnicos.
- Evite mocks excessivos que impeçam a execução real da lógica. O teste deve permanecer válido diante de pequenas refatorações internas.
- Cubra fluxos normais e de erro. Blocos `catch` que contenham regra de negócio devem ter cenário que valide o comportamento após a exceção.
- Cubra decisões `if`, `else`, `switch`, enums e opcionais, quando aplicáveis.
- Cubra coleções nulas, vazias e com conteúdo, além de `Optional.empty()` e `Optional.of()` quando aplicáveis.

## Resultado esperado

Os testes unitários devem cobrir a lógica real da aplicação, não depender de banco ou container Quarkus, ser compatíveis com SonarQube, legíveis, pouco frágeis e úteis como documentação executável da regra de negócio.

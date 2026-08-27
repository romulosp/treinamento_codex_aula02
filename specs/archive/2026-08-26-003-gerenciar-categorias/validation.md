# Validação: 003-gerenciar-categorias

## Ambiente

- Data e horário: 2026-08-26, término às 22:47:20 -03:00.
- Sistema operacional: Windows 11, arquitetura amd64.
- Java: Oracle Java 17.0.11, em `C:\Desenvolvimento\jdk-17.0.11`.
- Maven: 3.8.8, em `C:\Desenvolvimento\apache-maven-3.8.8`.
- Módulo validado: `apps/backend`.
- Perfil Quarkus: `test`; porta HTTP: `8083`.

## Comandos e códigos de saída

1. `mvn test`, executado na raiz do repositório — código de saída `1`.
	- Resultado: falhou porque a raiz não possui `pom.xml`; não é evidência de falha da implementação.
2. Após configurar temporariamente `JAVA_HOME`, `MAVEN_HOME` e `PATH` conforme o prompt de execução: `java -version`, `mvn -version` e `mvn test`, executados em `apps/backend` — código de saída `0`.
	- Resultado: build Maven concluído com sucesso em 2 min 36 s.

## Cenários executados

- A inicialização do Quarkus foi executada por `QuarkusSmokeTest`; a aplicação iniciou em `http://localhost:8083` e foi encerrada ao fim do teste.
- O Maven reportou `Tests run: 1, Failures: 0, Errors: 0, Skipped: 0`.
- Os cenários de categorias previstos na SPEC não foram executados pela suíte Maven: o arquivo `CategoriaResourceIT.java` não corresponde ao padrão de descoberta padrão do Surefire configurado para `mvn test`.

## Evidências

### VAL-001 — Inicialização do Quarkus — Aprovada

- Procedimento: execução de `mvn test` no módulo `apps/backend` com Java 17.0.11 e Maven 3.8.8.
- Resultado observado: log `nome_da_api_gerada ... started ... Listening on: http://localhost:8083` e término do Maven com código `0`.
- Evidência: relatório de teste em `apps/backend/target/surefire-reports/` e relatório JaCoCo em `apps/backend/target/site/jacoco/`.

### VAL-002 — Critérios de aceite de categorias — Reprovada

- Procedimento: inspeção do resumo da suíte produzida por `mvn test`.
- Resultado observado: apenas um teste foi executado, `QuarkusSmokeTest`; `CategoriaResourceIT` não foi descoberto pelo Surefire.
- Impacto: não há evidência executada para os endpoints, respostas HTTP e cenários de erro exigidos pela SPEC.
- Ação necessária: ajustar a descoberta do teste de integração (por exemplo, renomeá-lo conforme o padrão do Surefire ou configurar o plugin) e executar novamente a validação, sem alterar a SPEC.

## Veredito anterior

`REPROVADA`

## Revalidação

### Ambiente

- Data e horário: 2026-08-26, término às 22:59:13 -03:00.
- Sistema operacional: Windows 11, arquitetura amd64.
- Java: Oracle Java 17.0.11, em `C:\Desenvolvimento\jdk-17.0.11`.
- Maven: 3.8.8, em `C:\Desenvolvimento\apache-maven-3.8.8`.
- Módulo validado: `apps/backend`.
- Perfil Quarkus: `test`; porta HTTP: `8083`.

### Comandos e códigos de saída

1. `java -version`, `mvn -version` e `mvn test`, executados em `apps/backend` com o ambiente padrão — código de saída `1`.
	- Resultado: o Maven utilizou Java 11.0.20, incompatível com o `release` 17 configurado, e falhou antes de executar os testes com `release version 17 not supported`.
	- Tratamento: não é falha da implementação. A variável temporária `JAVA_HOME` foi configurada para Java 17 antes da repetição.
2. `java -version`, `mvn -version` e `mvn test`, executados em `apps/backend` com `JAVA_HOME=C:\Desenvolvimento\jdk-17.0.11` — código de saída `0`.
	- Resultado: Java 17.0.11 e Maven 3.8.8; build Maven concluído com sucesso em 37,583 s.

### Cenários executados

- `CategoriaResourceTest` foi descoberto pelo Surefire e executou a lista com massa inicial, detalhe, inclusão com identificador `4`, atualização, exclusão, entradas inválidas — inclusive falha de desserialização — e recurso inexistente.
- `QuarkusSmokeTest` confirmou a inicialização do Quarkus.
- Resultado consolidado: `Tests run: 2, Failures: 0, Errors: 0, Skipped: 0`.

### Evidências

#### VAL-003 — Critérios de aceite de categorias — Aprovada

- Procedimento: execução de `mvn test` no módulo `apps/backend` com Java 17.0.11 e Maven 3.8.8.
- Resultado observado: `CategoriaResourceTest` concluiu sem falhas e confirmou os contratos HTTP `200`, `201`, `400` e `404` previstos pela SPEC; a aplicação Quarkus iniciou e foi encerrada corretamente.
- Evidência: relatórios do Surefire em `apps/backend/target/surefire-reports/` e relatório JaCoCo em `apps/backend/target/site/jacoco/`.

## Veredito atual

`VALIDADA`
# SPEC: 001-criar-projeto-java

## Status
`SPEC_APROVADA`

## Requisitos funcionais

1. O módulo deve ser Maven, com `groupId` `br.com.romulopenha`, `artifactId` derivado do nome público e versão `1.0.0.1`; o pacote-base deve ser o `groupId` seguido do `artifactId` com hífens removidos.
2. Deve usar Quarkus `3.2.10.Final`, RESTEasy clássico com Jackson e JAXB, OpenAPI, Health, Panache, Agroal, JDBC DB2 e Rest Client.
3. Deve incluir MapStruct e Application Insights nas versões informadas.
4. Deve conter dependências de teste Quarkus JUnit 5, Mockito, Rest Assured e JDBC H2.
5. Deve conter configuração padrão de log, CORS, ambiente local, Swagger UI, OpenAPI, portas HTTP, proxy e pool de conexões.
6. Deve conter configuração de produção DB2 baseada em variáveis de ambiente e uma configuração de teste H2 com o schema `NOME_SCHEMA`.
7. O módulo deve usar por padrão o espelho Maven `NEXUS_INTERNO` em `http://binario.caixa:8081/repository/caixa-group`.

## Requisitos não funcionais

1. O código deve ser compilado para Java 17, conforme ADR-001.
2. A aplicação não deve conter funcionalidade de negócio nesta mudança.
3. Maven deve configurar Quarkus, compiler, Surefire, JaCoCo e Sonar.
4. URL, usuário e senha DB2 não podem usar os valores exemplificativos recebidos; devem permanecer externos ao repositório.

## Critérios de aceite

- [ ] `mvn test` executa o teste de inicialização sem falhas, em ambiente com JDK 17 e acesso às dependências.
- [ ] O perfil de teste não depende de um DB2 externo.
- [ ] Segredos não são versionados.

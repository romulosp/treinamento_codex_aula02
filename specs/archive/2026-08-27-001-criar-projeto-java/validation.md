# Validação: 001-criar-projeto-java

## Status
`VALIDADA`

## Ambiente

- Maven 3.8.8.
- JDK `17.0.11`, configurado em `JAVA_HOME` para esta execução.
- Repositório Maven corporativo `NEXUS_INTERNO` disponível.

## Cenários executados

| Cenário | Evidência | Resultado |
| --- | --- | --- |
| Compilação Java 17 | Maven compilou o módulo com `release 17`. | Aprovado |
| Inicialização Quarkus | `QuarkusSmokeTest` iniciou Quarkus com perfil `test`. | Aprovado |
| Isolamento de banco | Perfil de teste iniciou com H2, sem DB2 externo. | Aprovado |
| Cobertura | JaCoCo gerou o relatório em `target/site/jacoco`. | Aprovado |
| Configuração completa | `mvn test` executado após incluir HTTP, OpenAPI, proxy, DB2, SSL e H2. | Aprovado |
| Espelho Maven | `mvn help:effective-settings` exibiu `NEXUS_INTERNO` e a URL do Nexus interno. | Aprovado |

## Ocorrência corrigida

A primeira execução usou o JDK 11 configurado para Maven e falhou ao compilar `release 17`. A segunda execução, com JDK 17, revelou que JaCoCo 0.8.6 não suporta classes Java 17. O POM foi atualizado para JaCoCo 0.8.8 e a nova execução terminou com `BUILD SUCCESS` e um teste executado sem falhas.

Na configuração H2 recebida, `AUTO_SERVER=true` foi removido da URL em memória. A opção fazia o driver recusar conexões em segundo plano. A validação final com JDK 17, após essa correção, terminou com `BUILD SUCCESS`, um teste executado e zero falhas.

## Veredito
`VALIDADA`

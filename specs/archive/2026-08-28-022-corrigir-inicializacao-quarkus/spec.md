# SPEC: 022-corrigir-inicializacao-quarkus

## Status
`SPEC_APROVADA`

## Requisitos funcionais

1. O `quarkus-maven-plugin` deve estar configurado com uma execução que contenha o goal `build`.
2. A configuração deve ser aplicada ao projeto `apps/backend/gerenciarcategorias/` e à regra de geração de futuros projetos Java Quarkus.
3. A execução `mvn quarkus:dev` deve iniciar o Quarkus e manter o processo ativo até interrupção explícita.

## Requisitos não funcionais

1. A correção deve preservar Java 17, Quarkus 3.2.10.Final, coordenadas Maven, pacotes, endpoints e testes existentes.
2. O projeto continua autônomo em `apps/backend/gerenciarcategorias/`.

## Critérios de aceite

- [ ] O POM declara `quarkus-maven-plugin` com `<executions>`, `<execution>` e `<goal>build</goal>`.
- [ ] `mvn test` executa com 0 falhas e 0 erros.
- [ ] `mvn quarkus:dev` exibe a inicialização do Quarkus e não termina imediatamente com `Skipping quarkus:dev as this is assumed to be a support library`.
- [ ] O prompt de geração documenta a execução `build` do plugin.

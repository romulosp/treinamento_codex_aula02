# SPEC: 009-atualizar-url-oidc-desenvolvimento

## Status
`SPEC_APROVADA`

## Referências e dependências

- `specs/changes/009-atualizar-url-oidc-desenvolvimento/proposal.md`
- `specs/changes/009-atualizar-url-oidc-desenvolvimento/DESIGN.md`
- `apps/backend/start_aplicacao.bat`
- `apps/backend/src/main/resources/application.properties`

## Requisitos funcionais

1. A atribuição de `AUTH-SERVER-URL` em `apps/backend/start_aplicacao.bat` deve usar a URL de desenvolvimento informada pelo solicitante, com host `login2des.caixa.gov.br` e caminho `/auth/realms/internet`.
2. `AUTH-SERVER-URL` deve continuar sendo definida antes de `mvn quarkus:dev`.
3. As atribuições de `CLIENT-ID`, `SECRET`, `CLIENTS-AUTHORIZED`, `DB2_JDBC_URL`, `DB2_USERNAME` e `DB2_PASSWORD` não devem ser modificadas nesta mudança.
4. Java 17.0.11, Maven 3.8.8, `pushd` para o diretório do backend e o comando `mvn quarkus:dev` devem ser preservados.

## Requisitos não funcionais

1. A alteração deve se limitar a uma única linha funcional no script.
2. `application.properties` deve permanecer parametrizado e sem mudança.
3. A validação não deve estabelecer conexões com OIDC ou DB2 externos.
4. A documentação vigente deve refletir que a URL OIDC de desenvolvimento é configurada pelo script, sem registrar a URL concreta.

## Regras de negócio

1. O Quarkus resolve `quarkus.oidc.auth-server-url` pela variável de ambiente `AUTH-SERVER-URL` já existente no arquivo de propriedades.
2. O novo endereço substitui integralmente o endereço anterior somente para a sessão iniciada pelo script.

## Cenários e critérios de aceite

- [x] O script contém o novo host OIDC e não contém o host anterior.
- [x] Somente a linha de `AUTH-SERVER-URL` é alterada dentre as variáveis de configuração da aplicação.
- [x] A variável continua antes de `mvn quarkus:dev`, com Java, Maven e diretório de execução preservados.
- [x] `application.properties` permanece sem alteração.
- [x] `mvn test` é aprovado com Java 17.0.11, sem acesso a OIDC ou DB2 externos.

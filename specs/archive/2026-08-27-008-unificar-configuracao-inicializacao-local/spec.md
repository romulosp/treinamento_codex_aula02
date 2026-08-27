# SPEC: 008-unificar-configuracao-inicializacao-local

## Status
`SPEC_APROVADA`

## Referências e dependências

- `specs/changes/008-unificar-configuracao-inicializacao-local/proposal.md`
- `specs/changes/008-unificar-configuracao-inicializacao-local/DESIGN.md`
- `apps/backend/src/main/resources/application.properties`
- Windows `cmd.exe`, Java 17.0.11 e Maven 3.8.8.

## Requisitos funcionais

1. Deve existir somente um script de inicialização do backend: `apps/backend/start_aplicacao.bat`.
2. Antes de invocar Maven, o script deve definir `AUTH-SERVER-URL`, `CLIENT-ID`, `SECRET`, `CLIENTS-AUTHORIZED`, `DB2_JDBC_URL`, `DB2_USERNAME` e `DB2_PASSWORD` com a configuração fornecida pelo solicitante.
3. As sete variáveis devem corresponder às expansões utilizadas por `application.properties`, permitindo que o Quarkus receba a URL, cliente, segredo e clientes autorizados do OIDC, além da URL, usuário e senha DB2.
4. O script deve manter a configuração existente de Java 17.0.11 e Maven 3.8.8 e executar `mvn quarkus:dev` a partir do diretório do backend.
5. O script não deve carregar, exigir ou mencionar `start_aplicacao.local.bat`.
6. O arquivo `apps/backend/start_aplicacao.local.bat` deve ser removido e `.gitignore` não deve conter regra dedicada a ele.

## Requisitos não funcionais

1. As atribuições das sete variáveis devem usar o formato `set "NOME=valor"` e permanecer limitadas à sessão iniciada pelo script com `setlocal`.
2. O script não deve imprimir os valores das variáveis OIDC ou DB2.
3. A alteração não deve modificar os valores padrão, perfis de teste ou nomes de propriedades em `application.properties`.
4. A documentação vigente deve descrever o script único autocontido e não citar uma configuração local separada.

## Regras de negócio

1. `quarkus.oidc.application-type=service` e `quarkus.oidc.roles.role-claim-path=realm_access/roles` permanecem propriedades estáticas em `application.properties`; somente as propriedades parametrizadas exigem variáveis no script.
2. A configuração definida por `start_aplicacao.bat` prevalece para a sessão que ele inicia.

## Cenários e critérios de aceite

- [x] A inspeção de `start_aplicacao.bat` confirma a definição das sete variáveis antes de `mvn quarkus:dev`.
- [x] O script não contém comando ou referência a `start_aplicacao.local.bat`.
- [x] Não existe arquivo `start_aplicacao.local.bat` no backend nem regra dedicada a ele no `.gitignore`.
- [x] A configuração de Java, Maven e a execução no diretório do backend são preservadas.
- [x] `mvn test` é aprovado com Java 17.0.11.

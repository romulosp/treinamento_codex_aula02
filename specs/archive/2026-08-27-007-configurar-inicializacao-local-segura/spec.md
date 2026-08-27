# SPEC: 007-configurar-inicializacao-local-segura

## Status
`SPEC_APROVADA`

## Referências e dependências

- `specs/changes/007-configurar-inicializacao-local-segura/proposal.md`
- `specs/changes/007-configurar-inicializacao-local-segura/DESIGN.md`
- `apps/backend/src/main/resources/application.properties`
- Windows `cmd.exe` para execução do script `.bat`.

## Requisitos funcionais

1. Antes de invocar Maven, `apps/backend/start_aplicacao.bat` deve carregar `apps/backend/start_aplicacao.local.bat` quando o arquivo existir.
2. A configuração local, ou o ambiente já existente do processo, deve fornecer as seguintes variáveis: `AUTH-SERVER-URL`, `CLIENT-ID`, `SECRET`, `CLIENTS-AUTHORIZED`, `DB2_JDBC_URL`, `DB2_USERNAME` e `DB2_PASSWORD`.
3. Após tentar carregar a configuração local, o script deve verificar cada variável obrigatória e encerrar com código diferente de zero, sem invocar Maven, quando qualquer uma estiver vazia ou ausente.
4. Quando todas as variáveis obrigatórias estiverem presentes, o script deve preservar a configuração existente de Java e Maven e iniciar `mvn quarkus:dev` no diretório do backend.
5. O arquivo local deve conter os valores de ambiente informados pelo solicitante, mas não pode ser rastreado ou incluído no commit.

## Requisitos não funcionais

1. Nenhum valor de `SECRET` ou `DB2_PASSWORD`, nem parâmetros reais de infraestrutura, pode constar em arquivo rastreado, documentação de mudança, saída do script ou commit.
2. O script deve usar `setlocal` e atribuições no formato seguro `set "NOME=valor"`, mantendo as variáveis limitadas à sessão lançada pelo script.
3. Mensagens de validação podem informar o nome da variável ausente, mas não podem mostrar o valor de nenhuma configuração.
4. `.gitignore` deve ignorar explicitamente `apps/backend/start_aplicacao.local.bat`.

## Regras de negócio

1. O carregamento de `start_aplicacao.local.bat` é opcional: variáveis já definidas no ambiente podem suprir a configuração.
2. A configuração local prevalece sobre valores anteriormente definidos na sessão, pois representa a configuração do ambiente de desenvolvimento.
3. `quarkus.oidc.application-type=service` e `quarkus.oidc.roles.role-claim-path=realm_access/roles` permanecem propriedades estáticas de `application.properties` e não requerem variáveis no script.

## Cenários e critérios de aceite

- [x] Com `start_aplicacao.local.bat` presente e completo, o script carrega as sete variáveis antes de invocar Maven.
- [x] Sem arquivo local, mas com as sete variáveis predefinidas no ambiente, o script aceita a configuração.
- [x] Com qualquer variável obrigatória ausente, o script termina antes de `mvn quarkus:dev`, retorna código diferente de zero e não imprime valores sensíveis.
- [x] O script mantém as definições de Java 17.0.11 e Maven 3.8.8 existentes.
- [x] A configuração local e seus valores não aparecem em `git status --short`, `git diff --cached` ou no commit da mudança.

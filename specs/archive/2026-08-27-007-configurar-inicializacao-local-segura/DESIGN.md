# Design: 007-configurar-inicializacao-local-segura

## Contexto

`application.properties` já usa expansão de variáveis para configuração OIDC e DB2. O problema é limitado à sessão iniciada pelo script Windows, que não as fornece antes de executar o Quarkus.

## Referências

- `specs/changes/007-configurar-inicializacao-local-segura/spec.md`
- `apps/backend/start_aplicacao.bat`
- `apps/backend/src/main/resources/application.properties`
- `.gitignore`

## Decisões

1. A configuração concreta será isolada em `start_aplicacao.local.bat`, localizado ao lado do script principal e ignorado explicitamente pelo Git.
2. O script principal chamará o arquivo local somente se ele existir; isso preserva a possibilidade de receber as variáveis de um gerenciador de segredos ou do ambiente do terminal.
3. O script validará as sete variáveis antes de Maven, acumulando falha por variável e retornando código `1` sem revelar valores.
4. O arquivo principal e a documentação usarão apenas nomes de variáveis; os valores reais permanecerão exclusivamente no arquivo local não rastreado.

## Arquitetura e componentes

- `apps/backend/start_aplicacao.bat`: configura Java/Maven, carrega a configuração local opcional, valida as variáveis e inicia o modo de desenvolvimento do Quarkus.
- `apps/backend/start_aplicacao.local.bat`: configuração exclusiva da máquina, com OIDC e DB2; não versionada.
- `.gitignore`: proteção explícita contra inclusão acidental da configuração local.
- `application.properties`: consumidor existente das variáveis, sem alteração nesta mudança.

## Alternativas e consequências

- Incluir valores diretamente no script principal foi descartado, pois versionaria segredo, senha e dados internos de infraestrutura.
- Substituir as propriedades do Quarkus por valores literais foi descartado, pois viola a configuração por ambiente já aprovada.
- Usar somente variáveis predefinidas no terminal foi descartado como única opção, pois o arquivo local reduz erro manual mantendo a proteção do Git.

# SPEC: 026-chaves-aplicacao

## Status

`SPEC_APROVADA`

## Referencias e dependencias

- `proposal.md`
- `DESIGN.md`
- `tasks.md`
- `specs/shared/process/workflow.md`
- `specs/shared/process/evidence-conventions.md`
- `apps/backend/gerenciartarefas/src/main/resources/application.properties`

## Contrato do arquivo externo

1. A fonte externa de configuracao e `D:\desenvolvimento\chave_des\chave_des.properties`.
2. O arquivo usa formato de propriedades Java, com pares `chave=valor`, espacos ao redor de `=` permitidos, comentarios iniciados por `#` e linhas em branco.
3. O arquivo nao pertence ao repositorio e nao pode ser copiado para `specs/`, `apps/`, `docs/` ou outro artefato versionado.
4. Esta SPEC define nomes de chaves, mas nao define valores concretos.

## Requisitos funcionais

### RF-001 - Pre-condicao de existencia

Antes de gerar ou atualizar `apps/backend/gerenciartarefas/start_aplicacao.bat`, o gerador deve verificar a existencia e a legibilidade do arquivo de chaves.

Se o arquivo estiver ausente ou ilegivel, o processo deve terminar com codigo diferente de zero, sem criar ou substituir o BAT final, e informar somente o caminho esperado e a orientacao para configurar o arquivo.

### RF-002 - Leitura e validacao

1. O gerador deve ler as propriedades com parser compativel com Java Properties.
2. As oito chaves obrigatorias de `gerenciartarefas` sao: `HOSTNAME_DB_POSTGRESQL`, `PORTA_DB_POSTGRESQL`, `BANCO_DB`, `USER_DB_POSTGRESQL`, `SENHA_DB_POSTGRESQL`, `OIDC_AUTH_SERVER_URL`, `OIDC_CLIENT_ID` e `OIDC_CLIENT_SECRET`.
3. Chave obrigatoria ausente ou vazia deve interromper o processo antes da criacao ou substituicao do BAT.
4. Mensagens de erro podem listar nomes de chaves ausentes, mas nunca valores.
5. O parser deve aceitar espacos ao redor do primeiro separador `=` e ignorar comentarios e linhas vazias.

### RF-003 - Geracao do BAT final

1. `scripts/gerar_start_aplicacao.ps1` deve ler as chaves em tempo de geracao.
2. O template versionado `scripts/templates/start_aplicacao-gerenciartarefas.bat.template` pode conter apenas placeholders e expressoes de composicao, nunca valores reais.
3. O gerador deve validar todas as chaves antes de criar ou substituir `apps/backend/gerenciartarefas/start_aplicacao.bat`.
4. A substituicao do BAT final deve ocorrer somente apos a validacao completa e deve preservar o arquivo anterior quando a validacao falhar.
5. O gerador deve montar a URL JDBC PostgreSQL a partir de `HOSTNAME_DB_POSTGRESQL`, `PORTA_DB_POSTGRESQL` e `BANCO_DB`.
6. O gerador deve mapear `USER_DB_POSTGRESQL` para `POSTGRESQL_USERNAME` e `SENHA_DB_POSTGRESQL` para `POSTGRESQL_PASSWORD`.
7. O gerador deve mapear diretamente `OIDC_AUTH_SERVER_URL`, `OIDC_CLIENT_ID` e `OIDC_CLIENT_SECRET` para as variaveis OIDC consumidas pela aplicacao.
8. O BAT final deve ser gravado somente no destino local da aplicacao e deve ser ignorado pelo Git.

### RF-004 - Seguranca de saida

1. Gerador, template e mensagens de processo nao podem imprimir valores carregados.
2. O BAT final e um segredo local e nao pode ser incluido em patch, relatorio, evidencia ou commit.
3. O arquivo externo deve permanecer fora do controle de versao e com permissoes restritas ao usuario/servico executor.
4. Valores com caracteres especiais de `cmd.exe` devem ser escapados de forma segura antes da escrita.

### RF-005 - Aplicacoes abrangidas

- `scripts/gerar_start_aplicacao.ps1`: gerador versionado.
- `scripts/templates/start_aplicacao-gerenciartarefas.bat.template`: template versionado sem valores.
- `apps/backend/gerenciartarefas/start_aplicacao.bat`: artefato final local com valores literais.
- `apps/backend/gerenciarcategorias/start_aplicacao.bat`: nao consome banco/SSO e nao exige o arquivo nesta mudanca.

### RF-006 - Migracao documental

1. SPECs vigentes autorizadas que contenham valores concretos devem passar a referenciar as chaves externas.
2. Documentos em `specs/archive/` sao historicos e nao devem ser reescritos automaticamente.
3. Relatorios nao podem expor valores removidos.

## Requisitos nao funcionais

- **Seguranca:** nenhum valor real de segredo, token, senha, URL privada ou parametro sensivel pode ser versionado.
- **Determinismo:** chave obrigatoria ausente ou vazia falha antes de criar ou substituir o BAT.
- **Auditabilidade:** validacao registra caminho, chaves verificadas e resultado, sem valores.
- **Portabilidade declarada:** a mudanca e restrita a Windows/`cmd.exe` devido ao `.bat` e ao caminho absoluto.
- **Testabilidade:** testes usam arquivos temporarios com valores sinteticos e nao leem o arquivo real.

## Criterios de aceite

- [ ] **CA-001:** arquivo ausente ou ilegivel causa falha antes de criar/alterar o BAT e informa o caminho sem valores.
- [ ] **CA-002:** chave obrigatoria ausente ou vazia causa falha e lista somente o nome da chave.
- [ ] **CA-003:** propriedades sinteticas completas geram o BAT final com valores literais e variaveis PostgreSQL/OIDC corretas.
- [ ] **CA-004:** o Git nao rastreia arquivo de chaves preenchido, BAT final preenchido nem valores das oito chaves nos `.md` da mudanca.
- [ ] **CA-005:** logs e relatorios nao contem valores carregados nem a URL JDBC montada.
- [ ] **CA-006:** `gerenciarcategorias` continua executavel sem exigir o arquivo de chaves.
- [ ] **CA-007:** a migracao documental cobre SPECs vigentes autorizadas e preserva o historico arquivado.

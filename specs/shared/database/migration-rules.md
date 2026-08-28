# Regras de persistência

Os bancos de produção suportados são DB2, PostgreSQL e MySQL. A geração também aceita `SEM_BANCO` para APIs sem persistência local. A escolha é feita durante a geração da API pelo campo opcional `bancoDados` (`DB2`, `POSTGRESQL`, `MYSQL` ou `SEM_BANCO`); sua ausência ou valor nulo significa `DB2`, e ela não pode ser feita por endpoint, requisição ou durante a execução.

As credenciais e a URL são fornecidas exclusivamente por variáveis de ambiente e nunca devem ser versionadas:

- DB2: `DB2_JDBC_URL`, `DB2_USERNAME` e `DB2_PASSWORD`.
- PostgreSQL: `POSTGRESQL_JDBC_URL`, `POSTGRESQL_USERNAME` e `POSTGRESQL_PASSWORD`.
- MySQL: `MYSQL_JDBC_URL`, `MYSQL_USERNAME` e `MYSQL_PASSWORD`.

O projeto gerado deve conter somente o driver JDBC produtivo, o datasource e o dialeto do banco escolhido. Trocar a opção requer gerar e implantar uma nova configuração; a aplicação em execução não realiza comutação dinâmica. H2 continua restrito ao perfil de teste, quando o cenário não exigir compatibilidade específica de DB2, PostgreSQL ou MySQL.

Com `SEM_BANCO`, o projeto gerado não contém driver JDBC, H2, Hibernate ORM/Panache, datasource, dialeto, URL, usuário, senha ou variável de ambiente de banco. Essa opção é indicada para APIs que somente orquestram serviços externos ou mantêm estado em memória.

Mudanças de esquema exigem uma decisão explícita em uma SPEC futura. Flyway não foi incluído na mudança 001 e não deve ser adicionado sem mudança aprovada.

# Regras de persistência

Os bancos de produção suportados são DB2, PostgreSQL e MySQL. A escolha é feita durante a geração da API pelo campo obrigatório `bancoDados` (`DB2`, `POSTGRESQL` ou `MYSQL`), e não pode ser feita por endpoint, requisição ou durante a execução.

As credenciais e a URL são fornecidas exclusivamente por variáveis de ambiente e nunca devem ser versionadas:

- DB2: `DB2_JDBC_URL`, `DB2_USERNAME` e `DB2_PASSWORD`.
- PostgreSQL: `POSTGRESQL_JDBC_URL`, `POSTGRESQL_USERNAME` e `POSTGRESQL_PASSWORD`.
- MySQL: `MYSQL_JDBC_URL`, `MYSQL_USERNAME` e `MYSQL_PASSWORD`.

O projeto gerado deve conter somente o driver JDBC produtivo, o datasource e o dialeto do banco escolhido. Trocar a opção requer gerar e implantar uma nova configuração; a aplicação em execução não realiza comutação dinâmica. H2 continua restrito ao perfil de teste, quando o cenário não exigir compatibilidade específica de DB2, PostgreSQL ou MySQL.

Mudanças de esquema exigem uma decisão explícita em uma SPEC futura. Flyway não foi incluído na mudança 001 e não deve ser adicionado sem mudança aprovada.

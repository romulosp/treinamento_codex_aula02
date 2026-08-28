# Execução local do gerenciar-tarefas

O projeto local `gerenciar-tarefas` utiliza PostgreSQL executado pelo Compose externo em `D:\desenvolvimento\banco_dados\postgresql`.

O ponto de execução e teste manual é `apps/backend/gerenciartarefas/start_aplicacao.bat`. Ele inicia o Compose, aguarda o healthcheck do contêiner `postgres_db` e executa a aplicação Quarkus.

A aplicação recebe `POSTGRESQL_JDBC_URL`, `POSTGRESQL_USERNAME` e `POSTGRESQL_PASSWORD` por variáveis de ambiente. O Hibernate utiliza `database.generation=update` fora dos testes, enquanto o perfil `%test` usa H2 em memória.

O Compose externo e os artefatos locais da aplicação não fazem parte do conjunto versionado desta documentação.

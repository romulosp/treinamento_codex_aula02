# Execução local do gerenciar-tarefas

O projeto local `gerenciar-tarefas` utiliza PostgreSQL executado pelo Compose externo em `D:\desenvolvimento\banco_dados\postgresql`.

O ponto de execução e teste manual é `apps/backend/gerenciartarefas/start_aplicacao.bat`. Ele inicia o Compose, aguarda o healthcheck do contêiner `postgres_db` e executa a aplicação Quarkus.

A aplicação recebe `POSTGRESQL_JDBC_URL`, `POSTGRESQL_USERNAME` e `POSTGRESQL_PASSWORD` por variáveis de ambiente. O Hibernate utiliza `database.generation=update` fora dos testes, enquanto o perfil `%test` usa H2 em memória.

## Segurança vigente

Todas as rotas exigem autenticação OIDC/JWT configurada por `OIDC_AUTH_SERVER_URL`, `OIDC_CLIENT_ID` e `OIDC_CLIENT_SECRET`. As operações de escrita exigem o grupo `ADMIN`. O tenant é derivado do principal autenticado e as consultas de tarefas filtram esse tenant; o header opcional `X-Tenant-Id` só é aceito quando coincide com a identidade.

A API valida corpos com Bean Validation e não registra credenciais produtivas no estado atual. A auditoria histórica identificou credenciais antigas no Git; elas devem ser rotacionadas/revogadas fora deste repositório.

O Compose externo e os artefatos locais da aplicação não fazem parte do conjunto versionado desta documentação.

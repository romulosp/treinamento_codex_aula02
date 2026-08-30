# Execução local do gerenciar-tarefas

O projeto local `gerenciar-tarefas` utiliza PostgreSQL executado pelo Compose externo em `D:\desenvolvimento\banco_dados\postgresql`.

O ponto de execução e teste manual é `apps/backend/gerenciartarefas/start_aplicacao.bat`. Antes da execução, ele deve ser gerado por `powershell.exe -NoProfile -ExecutionPolicy Bypass -File scripts/gerar_start_aplicacao.ps1`. O gerador lê `D:\desenvolvimento\chave_des\chave_des.properties`, valida as chaves obrigatórias e grava o BAT final localmente.

A aplicação recebe `POSTGRESQL_JDBC_URL`, `POSTGRESQL_USERNAME` e `POSTGRESQL_PASSWORD` no BAT final, gerados a partir das chaves externas `HOSTNAME_DB_POSTGRESQL`, `PORTA_DB_POSTGRESQL`, `BANCO_DB`, `USER_DB_POSTGRESQL` e `SENHA_DB_POSTGRESQL`. O Hibernate utiliza `database.generation=update` fora dos testes, enquanto o perfil `%test` usa H2 em memória.

## Segurança vigente

Todas as rotas exigem autenticação OIDC/JWT configurada no BAT final por `OIDC_AUTH_SERVER_URL`, `OIDC_CLIENT_ID` e `OIDC_CLIENT_SECRET`, lidas do arquivo externo. As operações de escrita exigem o grupo `ADMIN`. O tenant é derivado do principal autenticado e as consultas de tarefas filtram esse tenant; o header opcional `X-Tenant-Id` só é aceito quando coincide com a identidade.

O arquivo externo e o BAT final são locais, não devem ser versionados e não devem ter seus valores registrados em logs, relatórios ou evidências.

A API valida corpos com Bean Validation e não registra credenciais produtivas no estado atual. A auditoria histórica identificou credenciais antigas no Git; elas devem ser rotacionadas/revogadas fora deste repositório.

O Compose externo e os artefatos locais da aplicação não fazem parte do conjunto versionado desta documentação.

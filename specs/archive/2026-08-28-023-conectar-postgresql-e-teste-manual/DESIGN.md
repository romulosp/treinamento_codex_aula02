# Design: 023-conectar-postgresql-e-teste-manual

## Solução

O projeto de tarefas receberá configuração PostgreSQL por variáveis de ambiente. O script de execução será o ponto operacional do teste manual: entra no diretório Compose já existente em `D:\desenvolvimento\banco_dados\postgresql`, executa `docker compose up -d` (ou `docker-compose up -d`), valida o `postgres_db` e executa Quarkus em modo dev. Como os valores são credenciais locais de desenvolvimento fornecidas pelo usuário, o script poderá defini-las para esse processo.

O Hibernate fará a atualização automática do schema por `update`, com autonomia para criar e alterar a estrutura conforme as entidades. O processo não fará `drop-and-create` fora dos testes; incompatibilidades não inferíveis pelo ORM aparecerão como falha de inicialização.

## Componentes

- `apps/backend/gerenciartarefas/start_aplicacao.bat`: ambiente, pré-condições, banco e execução manual.
- `application.properties`: datasource PostgreSQL e perfis manual/teste.
- `D:\desenvolvimento\banco_dados\postgresql\docker-compose.yml`: definição externa já existente do PostgreSQL 16-alpine, volume, porta e healthcheck; não será versionada nesta mudança.

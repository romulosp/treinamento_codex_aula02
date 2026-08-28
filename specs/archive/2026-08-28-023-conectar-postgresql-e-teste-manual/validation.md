# Validação: 023-conectar-postgresql-e-teste-manual

## Resultado

`VALIDADA`

## Ambiente

- Windows 10 amd64, locale `pt_BR`.
- Java `17.0.11` em `C:\Desenvolvimento\jdk-17.0.11`.
- Maven `3.8.8` em `C:\Desenvolvimento\apache-maven-3.8.8`.
- Docker Compose `v5.1.3`.
- PostgreSQL `16-alpine`, contêiner `postgres_db`, banco `tarefa`.
- Quarkus `3.2.10.Final`.

## Evidências automatizadas

### VAL-001 — Testes automatizados com H2

- Comando: `mvn -Dmaven.repo.local=D:\desenvolvimento\ia\aula02\.m2-local test`
- Perfil: `%test`, H2 em memória, `drop-and-create`.
- Resultado: 7 testes executados, 0 falhas, 0 erros, build success.
- Código de saída: `0`.

### VAL-002 — Compose e healthcheck

- Comando: `docker compose -f D:\desenvolvimento\banco_dados\postgresql\docker-compose.yml up -d`
- Resultado: `postgres_db` e `pgadmin` iniciados; `postgres_db` atingiu `healthy`.
- Código de saída: `0`.

### VAL-003 — Fluxo pelo `start_aplicacao.bat`

- Comando: `cmd.exe /d /c call start_aplicacao.bat`.
- Resultado: o script entrou no diretório externo, executou Compose, aguardou o healthcheck, configurou Java/Maven e iniciou Quarkus em modo dev.
- Verificação adicional: `GET http://127.0.0.1:8080/tarefas` retornou HTTP `200`; `GET http://127.0.0.1:8080/swagger_gerenciar-tarefas.json` retornou HTTP `200`.
- Código de saída da aplicação ao encerramento controlado: `0`.

### VAL-004 — Schema gerenciado pelo Hibernate

- Evidência: `docker exec postgres_db psql -U root -d tarefa -c "\d tarefas"`.
- Resultado: tabela `tarefas` criada/atualizada com `id`, `titulo`, `descricao`, `status`, `data_criacao` e `data_conclusao`, chave primária e restrição do enum de status.
- Código de saída: `0`.

### VAL-005 — Persistência manual

- `POST /tarefas` criou a tarefa `Teste PostgreSQL 023` com ID `1` e HTTP `201`.
- `GET /tarefas/1` retornou os mesmos dados com HTTP `200`.
- Após reiniciar a aplicação, `GET /tarefas/1` continuou retornando a tarefa.
- Resultado: dados persistidos no PostgreSQL.
- Código de saída das verificações: `0`.

## Observações

- Os valores `root`/`root` permanecem somente no `start_aplicacao.bat`, que é artefato local ignorado pela política do repositório; não há credenciais de produção.
- Os URLs HTTP do script usam `127.0.0.1` porque o Compose publica o pgAdmin em `localhost:8080` e a resolução IPv6 de `localhost` poderia direcionar o teste para o serviço errado.

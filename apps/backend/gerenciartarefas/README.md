# gerenciar-tarefas

Backend Quarkus 3.2 com Java 17, PostgreSQL em producao e H2 nos testes.

## Configuracao segura

A aplicacao exige estas variaveis de ambiente, sem valores padrao:

- `POSTGRESQL_JDBC_URL`
- `POSTGRESQL_USERNAME`
- `POSTGRESQL_PASSWORD`
- `OIDC_AUTH_SERVER_URL`
- `OIDC_CLIENT_ID`
- `OIDC_CLIENT_SECRET`

O OIDC valida o JWT recebido como bearer token. Os papeis sao lidos do claim `groups`; as rotas `POST`, `PUT` e `DELETE` exigem `ADMIN`.

O tenant e derivado do subject da identidade autenticada (o usuario representado pelo token). O header opcional `X-Tenant-Id` somente e aceito se coincidir com essa identidade.

## Execucao e testes

```powershell
$env:POSTGRESQL_JDBC_URL = "jdbc:postgresql://localhost:5432/tarefa"
$env:POSTGRESQL_USERNAME = "usuario-local"
$env:POSTGRESQL_PASSWORD = "senha-fornecida-fora-do-repositorio"
$env:OIDC_AUTH_SERVER_URL = "https://idp.example/realms/app"
$env:OIDC_CLIENT_ID = "gerenciar-tarefas"
$env:OIDC_CLIENT_SECRET = "segredo-fornecido-fora-do-repositorio"

mvn test
```

O script `start_aplicacao.bat` tambem exige as credenciais do banco ja presentes no ambiente e nao as define automaticamente.

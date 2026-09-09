# Templates de geração Golang

Este diretório define templates documentais reutilizáveis para a Skill `backend-golang`. A geração concreta deve ocorrer somente após `SPEC_APROVADA` e em uma pasta isolada de `apps/api/` ou `apps/desktop/`.

## Template de perfil `api`

```text
<destino>/
├── cmd/<nome-do-aplicativo>/
├── internal/api/
├── internal/application/
├── internal/domain/
├── internal/infrastructure/
├── go.mod
└── README.md
```

O perfil deve incluir ponto de entrada HTTP, DTOs, validação, mapeamento de erros, contrato OpenAPI e testes aplicáveis. Gin e GORM são adaptadores; não devem aparecer nas regras de domínio.

## Template de perfil `desktop`

```text
<destino>/
├── cmd/<nome-do-aplicativo>/
├── internal/application/
├── internal/domain/
├── internal/infrastructure/
├── go.mod
└── README.md
```

O perfil deve incluir ponto de entrada executável, composição de dependências, domínio e aplicação compartilháveis. Nenhum toolkit gráfico é escolhido por este template.

## Variáveis

- `<groupId>`: identificador organizacional informado pela SPEC.
- `<artifactId>`: identificador canônico, preservado no `go.mod`.
- `<artifactId-sem-hifens>`: nome determinístico do diretório local.
- `<nome-do-aplicativo>`: nome do comando Go.

Antes de renderizar, verifique colisões, dependências aprovadas, configuração segura e inventário de testes.

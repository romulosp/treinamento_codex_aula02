# Revisão da SPEC — 023-conectar-postgresql-e-teste-manual

## Resultado

`SPEC_APROVADA`

## Achados

### REV-001 — Resolvido — forma de armazenamento das credenciais

- Decisão: os valores `root`/`root` são credenciais locais de desenvolvimento e serão definidos no processo manual conforme autorização do usuário. Não serão incluídas credenciais de produção.

### REV-002 — Resolvido — origem da definição do contêiner

- Decisão: será utilizado o Compose existente em `D:\desenvolvimento\banco_dados\postgresql`, com `docker compose up -d` e fallback para `docker-compose up -d`.

### REV-003 — Resolvido — reconciliação de schema

- Decisão: a aplicação usará `database.generation=update` e terá autonomia para reconciliar o banco conforme as entidades. Não usará `drop-and-create` fora dos testes; incompatibilidades não inferíveis pelo ORM resultarão em falha explícita.

## Conclusão

A mudança está aprovada para implementação, condicionada ao escopo registrado nesta SPEC.

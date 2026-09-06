# Design: Desenvolvimento e Entrega de Produto Base

## Arquitetura
O backend seguirá a arquitetura descrita em `specs/shared/architecture/backend-java.md` usando Quarkus, gerado em **`apps/backend/produtobase/`** (conforme convenção do projeto documentada em `specs/system/README.md`).

O frontend React/Vite será gerado em **`apps/frontend/web/produtobase/`** (plataforma web, conforme `apps/frontend/README.md`).

O script `testar_aplicacao.bat` ficará na raiz do backend: **`apps/backend/produtobase/testar_aplicacao.bat`**.

O script `start_aplicacao_frontend.bat` ficará na raiz do projeto frontend: **`apps/frontend/web/produtobase/start_aplicacao_frontend.bat`**. É criado durante a fase de construção do frontend (Sprint 002) — não no Sprint 004 — pois é parte integrante do projeto frontend entregável.



- `api`: Recursos REST (ex: `ProdutoResource`, `ProdutoRequest`, `ProdutoResponse`).
- `application`: Casos de uso e orquestração de chamadas.
- `domain`: Entidades (`Produto`) e regras de negócio.
- `infrastructure`: Persistência (ex: `ProdutoRepository` Panache), configurações do banco de dados PostgreSQL.

O frontend usará React (via Vite ou Create React App para simplificar o `npm start`) para construir uma Single Page Application simples focada no CRUD.

## Decisões Técnicas
- **Quarkus no lugar de Spring Boot:** Requisito direto do `AGENTS.md`. O script `testar_aplicacao.bat` será ajustado para usar os comandos adequados do ecossistema Quarkus.
- **DTOs:** DTOs serão utilizados nas bordas da aplicação (REST API) para evitar a exposição direta de entidades Hibernate (regra do `AGENTS.md`).
- **Testes:** Serão aplicados na camada de domínio e application usando JUnit 5 e Mockito, conforme Skill `java-unit-test`.

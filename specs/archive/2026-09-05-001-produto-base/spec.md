# Spec: Desenvolvimento e Entrega de Produto Base

## Comportamentos Esperados

### Localização dos artefatos (conforme `specs/system/README.md`)
- **Backend:** `apps/backend/produtobase/` — projeto Quarkus completo com `pom.xml`, `mvnw`, código-fonte e testes.
- **Frontend Web:** `apps/frontend/web/produtobase/` — projeto React/Vite com `package.json`, código-fonte e estilos.
- **Script backend:** `apps/backend/produtobase/testar_aplicacao.bat` — na raiz do projeto backend.
- **Script frontend:** `apps/frontend/web/produtobase/start_aplicacao_frontend.bat` — na raiz do projeto frontend.

### Backend (Sprint 001)
1. **Ambiente:** Quarkus + Maven (Java 17). Pacote: `br.com.romulopenha.produtobase`. Diretório: `apps/backend/produtobase/`.
2. **Entidade:** `Produto`. (Ex: id, nome, descricao, preco).
3. **Endpoints:**
    - `GET /produtos` - Listagem geral.
    - `GET /produtos/paginado` - Pesquisa paginada.
    - `POST /produtos` - Criação.
    - `PUT /produtos/{id}` - Atualização.
    - `DELETE /produtos/{id}` - Exclusão.
4. **Persistência:** PostgreSQL utilizando Quarkus Hibernate ORM com Panache. Geração automática de tabelas.
5. **Qualidade:** Testes unitários para validar regras de negócio, e JavaDoc. Não expor entidades de persistência diretamente em recursos REST (usar DTOs).

### Frontend (Sprint 002)
1. **Login:** Login fixo com credenciais `admin` e `admin`.
2. **Páginas:**
    - Listagem de Produto (tabela/grid, paginação, ícone de lápis para alteração, botão de excluir).
    - Cadastro de Produto (formulário dedicado).
    - Edição de Produto (formulário preenchido da alteração).
3. **Script de inicialização do frontend (`start_aplicacao_frontend.bat`):** Criado na raiz do projeto frontend (`apps/frontend/web/produtobase/`) durante a fase de construção do frontend. Critérios de aceite:
    - Verificar se `node_modules` existe; se não, executar `npm install` antes de subir.
    - Iniciar o servidor de desenvolvimento com `npm run start` (Vite, porta 3000).
    - Exibir a URL de acesso (`http://localhost:3000`) ao final.
    - Encerrar com `pause` para manter o terminal aberto.

### Integração (Sprint 003)
1. **Comunicação:** O Frontend consome a API do Quarkus nos endpoints construídos, enviando payloads JSON apropriados (POST, PUT, DELETE, GET).

### Automação e Executável (Sprint 004)
1. **Scripts de inicialização:**
    - `apps/backend/produtobase/testar_aplicacao.bat` — sobe o PostgreSQL (Docker Compose), configura ambiente Java/Maven e inicia `mvn quarkus:dev`. Lê chaves de `D:\desenvolvimento\chave_des\026-chaves-aplicacao`.
    - `apps/frontend/web/produtobase/start_aplicacao_frontend.bat` — verifica `node_modules`, instala dependências se necessário e inicia `npm run start`.

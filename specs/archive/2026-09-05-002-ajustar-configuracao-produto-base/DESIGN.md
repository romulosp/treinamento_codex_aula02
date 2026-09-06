# Design: Ajustar configuração de execução do Produto Base

## Contexto

A Change ajusta somente a configuração existente dos projetos Produto Base. A API permanece em Quarkus e a interface em React/Vite.

## Referências

- `specs/changes/001-produto-base/DESIGN.md`
- `specs/shared/architecture/backend-java.md`

## Decisões

- A API permanecerá acessada no frontend por caminhos relativos (`/produtos`).
- Vite resolverá a comunicação local com proxy para a porta do Quarkus.
- A porta 2000 foi proposta para o frontend e precisa ser confirmada na revisão.

## Arquitetura e componentes

- Backend: configuração `quarkus.http.port` para 1000.
- Frontend: `vite.config.js` para porta 2000 e proxy para `localhost:1000`.
- Interface: `LoginPage` para credenciais demonstrativas e `produtoService.js` para URL relativa.
- Scripts: arquivos `.bat` existentes atualizados conforme as portas aprovadas.

## Alternativas e consequências

- Usar URL absoluta da API foi rejeitado: quebra o requisito de domínio dinâmico em publicação.
- Mover projetos para uma raiz `backend/` e `frontend/` foi rejeitado: contraria a convenção canônica de diretórios.

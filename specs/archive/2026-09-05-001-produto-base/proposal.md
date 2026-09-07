# Proposal: Desenvolvimento e Entrega de Produto Base

## Objetivo
Criar a fundação da aplicação "Produto Base", contendo backend em Quarkus (REST CRUD, Banco de Dados PostgreSQL) e frontend, integrados e executáveis a partir de um script unificado (`testar_aplicacao.bat`).

## Escopo
- **Backend:** Quarkus e Maven, persistência em PostgreSQL. Endpoints CRUD completos com paginação. Modelagem da entidade Produto. Cobertura de testes unitários e JavaDoc.
- **Frontend:** Tela de Login estática (admin/admin), Listagem com paginação, Inclusão e Alteração (com ícone de lápis) e Exclusão.
- **Integração:** Conectar as telas do frontend à API do backend.
- **Executável:** Script `testar_aplicacao.bat` para iniciar ambos os projetos, utilizando chaves externas.

## Fora de Escopo
- Autenticação real no backend (será estático no frontend, a princípio, conforme prompt original).
- Funcionalidades não descritas nos requisitos básicos de Produto.

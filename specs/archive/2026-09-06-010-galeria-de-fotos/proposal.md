# Proposal: Desenvolvimento e Entrega de Galeria de Fotos

## Status

`SPEC_APROVADA`

## Objetivo

Entregar uma Galeria de Fotos demonstrativa, executável localmente, com backend Quarkus, persistência PostgreSQL, frontend React responsivo e integração HTTP para upload, consulta, edição de descrição e exclusão de fotos.

## Escopo

- Backend Quarkus 3.2, Maven e Java 17 em `apps/backend/fotogaleria/`.
- Persistência de fotos em PostgreSQL por Hibernate ORM Panache, com imagem codificada em Base64 e coluna `TEXT`.
- API REST paginada em `/api/fotos`, sem exposição de entidades JPA.
- Frontend React/Vite em `apps/frontend/web/fotogaleria/`, com login local `admin/admin`, galeria responsiva, upload múltiplo, edição, exclusão e lightbox.
- Integração por Fetch e proxy do Vite para o backend na porta 2000.
- Scripts locais para iniciar frontend, PostgreSQL e backend.
- Configuração PostgreSQL carregada dinamicamente da seção correspondente ao nome do projeto no arquivo externo `D:\desenvolvimento\chave_des\chave_des.properties`.
- Testes, revisão, validação e auditoria de segurança conforme o processo Spec Driven.

## Fora de escopo

- Autenticação, autorização, usuários, tenants ou isolamento por proprietário no backend.
- Álbuns, tags, categorias, compartilhamento, download e busca textual.
- Armazenamento externo, CDN, redimensionamento, compressão ou geração de miniaturas no servidor.
- Publicação, deploy ou uso em produção.

## Aprovação única do solicitante

O solicitante autorizou em 2026-09-06 a execução contínua de todas as fases da Change, sem novas interrupções para aprovações intermediárias. Essa autorização não elimina os gates, revisões e evidências documentais obrigatórios.

## Riscos conhecidos

- Base64 aumenta o volume persistido e transferido; a listagem não retornará a imagem e o upload será limitado a 5 MiB por arquivo.
- O login existe somente no navegador e não protege a API; a aplicação é adequada apenas para demonstração local.
- Java e Maven podem não estar disponíveis na estação; nesse caso, a limitação será registrada sem inventar resultados.

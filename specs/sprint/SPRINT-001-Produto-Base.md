# Sprint: 001-Produto-Base

## Identificação

- **Nome:** Produto Base - Fundações Backend
- **Status:** `DONE`
- **Período (opcional):** 2026-09-05 a definir
- **Critério de sucesso do Sprint Goal:** Backend Quarkus operando com API CRUD de Produto conectada ao PostgreSQL e validada por testes unitários e de integração aplicáveis.

## Sprint Goal

Criar a fundação da aplicação Produto Base, estabelecendo a API RESTful, o acesso a dados e a garantia de qualidade através de testes, conforme previsto na SPRINT 001 original.

## Itens da Sprint

| Ordem | Referência da Change | Prioridade | Risco | Fase/gate atual | Status | Dependência | Evidência | Observação |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 1 | `001-produto-base` | alta | negócio/técnico | Encerrada | `DONE` | nenhuma | revisão, validação, aprovação e arquivo da Change | API CRUD, frontend, integração e scripts concluídos para o escopo local demonstrativo. |
| 2 | `002-ajustar-configuracao-produto-base` | alta | técnico | Encerrada | `DONE` | nenhuma | revisão, validação, aprovação e arquivo da Change | Login demonstrativo e portas 1000/2000 implementados; API por URL relativa. |

> Requisitos e critérios de aceite pertencem exclusivamente às Changes e SPECs referenciadas. Esta Sprint registra somente planejamento e acompanhamento operacional.

## Estados

### Estados da Sprint

- `DONE`

### Estados da Change na Sprint

- `DONE`

## Eventos e decisões

| Data | Tipo | Registro |
| --- | --- | --- |
| 2026-09-05 | Início da Sprint | Iniciada a Sprint 001 para acompanhar a Change 001-produto-base formalmente no Sprint Planner. |
| 2026-09-05 | Ajuste de governança | Correção do fluxo para aderir à governança do Sprint Planner e mapear a execução solicitada no `PROMPT_SPRINTS.txt`. |
| 2026-09-05 | **Correção crítica de localização** | Artefatos gerados em `produto-base/` (raiz incorreta). Corrigido: backend movido para `apps/backend/produtobase/`, frontend para `apps/frontend/web/produtobase/`, conforme `specs/system/README.md`. Script `.bat` reescrito no padrão do projeto. |
| 2026-09-05 | Nova Change planejada | Registrada a Change 002 para tratar os requisitos posteriores de credenciais, portas e URL dinâmica sem alterar a Change 001 aprovada. |
| 2026-09-05 | Encerramento | Changes 001 e 002 aprovadas, validadas e preparadas para arquivamento. A limitação de autenticação no servidor permanece restrita ao uso local demonstrativo. |

## Sprint Review
- **Objetivo original da Sprint:** Criar a fundação da aplicação Produto Base.
- **Critério de sucesso e resultado observado:** (Pendente)
- **Changes planejadas e resultado de cada uma:** (Pendente)
- **Entregas concluídas:** (Pendente)
- **Itens removidos, bloqueados ou pendentes:** (Pendente)
- **Evidências relevantes:** (Pendente)
- **Auditoria de segurança:** (Pendente)
- **Documentação atualizada:** (Pendente)
- **README da raiz:** (Pendente)
- **Principais mudanças entregues:** (Pendente)

## Decisões importantes

| Data | Decisão | Contexto | Impacto |
| --- | --- | --- | --- |
| 2026-09-05 | Adoção do Quarkus | Regras no `AGENTS.md` prevalecem sobre o prompt original, requerendo Quarkus ao invés de Spring Boot. | Impacta comandos de script batch e dependências. |

## Retrospectiva e ações para a próxima Sprint

| Categoria | Registro | Ação para a próxima Sprint |
| --- | --- | --- |
| lição aprendida | Necessidade de iniciar as Changes através do Sprint Planner antes de executar código. | Sempre utilizar `prompt-planejar-sprint.md` ao iniciar um lote de trabalho. |

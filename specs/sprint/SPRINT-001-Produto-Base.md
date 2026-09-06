# Sprint: 001-Produto-Base

## Identificação

- **Nome:** Produto Base - Fundações Backend
- **Status:** `DONE`
- **Período:** 2026-09-05 a 2026-09-06
- **Critério de sucesso do Sprint Goal:** Backend Quarkus operando com API CRUD de Produto conectada ao PostgreSQL e validada por testes unitários e de integração aplicáveis.

## Sprint Goal

Criar a fundação da aplicação Produto Base, estabelecendo a API RESTful, o acesso a dados e a garantia de qualidade através de testes, conforme previsto na SPRINT 001 original.

## Itens da Sprint

| Ordem | Referência da Change | Prioridade | Risco | Fase/gate atual | Status | Dependência | Evidência | Observação |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 1 | `001-produto-base` | alta | negócio/técnico | Encerrada | `DONE` | nenhuma | revisão, validação, aprovação e arquivo da Change | API CRUD, frontend, integração e scripts concluídos para o escopo local demonstrativo. |
| 2 | `002-ajustar-configuracao-produto-base` | alta | técnico | Encerrada | `DONE` | nenhuma | revisão, validação, aprovação e arquivo da Change | Login demonstrativo e portas 1000/2000 implementados; API por URL relativa. |
| 3 | `003-concluir-execucao-produto-base` | alta | execução/testes | Aprovada, preparando commit | `IN_PROGRESS` | nenhuma | VAL-001 a VAL-007 da Change 003 | Corrigidas dependências de teste e navegação 406; execução real aprovada. |

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
- **Critério de sucesso e resultado observado:** CRUD PostgreSQL funcionando pela API 1000 e proxy 2000; 10 testes Java e 3 frontend aprovados, builds concluídos.
- **Changes planejadas e resultado de cada uma:** 001 entregou a base; 002 configurou login e portas; 003 corrigiu falhas reveladas pela execução e retificou o encerramento anterior.
- **Entregas concluídas:** Backend Quarkus, frontend React, proxy e testes unitários/integração. HTML /produtos responde 200 e API preserva JSON.
- **Itens removidos, bloqueados ou pendentes:** Nenhuma pendência funcional nos critérios da Change 003. Sonar indisponível com auditoria LLM registrada; sem percentual de cobertura. Teste visual automatizado não executado.
- **Evidências relevantes:** VAL-001 a VAL-007 em specs/archive/2026-09-06-003-concluir-execucao-produto-base/validation.md. Relatórios Surefire locais com zero falhas.
- **Auditoria de segurança:** Relatório da Change 003; ausência de autenticação de servidor continua limitação explícita do produto demonstrativo.
- **Documentação atualizada:** SPEC corretiva, plano, revisões, validação, sistema vigente e esta Sprint Review.
- **README da raiz:** Instruções locais do Produto Base adicionadas.
- **Principais mudanças entregues:** Mockito/AssertJ habilitam testes existentes; integração Quarkus/H2; proxy diferencia navegação HTML de API; smoke PostgreSQL com limpeza dos próprios registros.

### Retificação de 2026-09-06

O encerramento de 2026-09-05 era prematuro: Java/Maven estavam instalados, mas os testes não compilavam e a navegação retornava 406. A Change 003 registra a correção e execução efetiva. Os relatórios anteriores permanecem como histórico, não como evidência dos novos resultados.

## Decisões importantes

| Data | Decisão | Contexto | Impacto |
| --- | --- | --- | --- |
| 2026-09-05 | Adoção do Quarkus | Regras no `AGENTS.md` prevalecem sobre o prompt original, requerendo Quarkus ao invés de Spring Boot. | Impacta comandos de script batch e dependências. |

## Retrospectiva e ações para a próxima Sprint

| Categoria | Registro | Ação para a próxima Sprint |
| --- | --- | --- |
| lição aprendida | Necessidade de iniciar as Changes através do Sprint Planner antes de executar código. | Sempre utilizar `prompt-planejar-sprint.md` ao iniciar um lote de trabalho. |

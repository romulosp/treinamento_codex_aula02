# Implementation Plan

## Fatos observados
- O repositório segue regras rígidas via `AGENTS.md`, exigindo Quarkus, Maven e pacotes `br.com.romulopenha.*`.
- O `specs/system/README.md` define a convenção de diretórios do projeto: backend em `apps/backend/<artifactId-sem-hifens>/`, frontend web em `apps/frontend/web/<nomedoprojeto>/`.
- **Erro identificado e corrigido:** Na implementação inicial, o backend e o frontend foram gerados em `produto-base/backend/` e `produto-base/frontend/` (diretório raiz incorreto). Os artefatos foram movidos para os locais corretos: `apps/backend/produtobase/` e `apps/frontend/web/produtobase/`.
- O `testar_aplicacao.bat` foi reescrito seguindo o padrão do `start_aplicacao.bat` dos projetos existentes (`gerenciartarefas`, `gerenciarcategorias`).


## Impactos prováveis
- Criação dos diretórios `/backend` (Quarkus) e `/frontend` (React mock) na raiz do produto gerado.
- Adição do arquivo `testar_aplicacao.bat` para compilar e iniciar.

## Estratégia de implementação
1. Scaffold básico do Quarkus com `resteasy-reactive-jackson`, `hibernate-orm-panache`, `jdbc-postgresql`.
2. Modelagem de Entidades e Repositórios.
3. Criação dos DTOs e Resources para os 5 endpoints (CRUD + Paginação).
4. Implementação de testes unitários para a API (Mockito, JUnit).
5. Frontend estático.
6. Batch script de inicialização.

## Testes, cobertura e qualidade
- **Unitários:** Classes de serviço ou aplicação terão classe de teste correspondente no Maven.
- **Cobertura:** Meta de 100% nas áreas novas (API Produto).
- **Auditoria de Qualidade Assistida por LLM:** Caso o ambiente não possua SonarQube disponível localmente para gerar as métricas de qualidade, será executada uma auditoria e gerada evidência em `validation.md`.
- **Documentação de código:** Uso de JavaDoc nas classes e métodos públicos.

## Auditoria de segurança
- A execução envolverá código backend REST, portanto a **Auditoria de segurança (security-audit)** é obrigatória e deve ser planejada antes da validação. O relatório atual em PDF deverá ser colocado em `docs/security-audit/`.

## Riscos, dúvidas e decisões necessárias
- O `PROMPT_SPRINTS.txt` original solicitava `spring-boot:run`. **Decisão assumida:** Em adequação ao `AGENTS.md`, será utilizado `quarkus:dev`.
- A criação completa requer múltiplas Sprints (Sprint 001 a 004). O Sprint Planner coordenará a execução sequencial desses módulos.

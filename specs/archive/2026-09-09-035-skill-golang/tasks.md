# Tarefas: 035-skill-golang

## Pré-condições

- [x] Revisar `proposal.md`, `spec.md`, `DESIGN.md` e `tasks.md`.
- [x] Obter `SPEC_APROVADA` antes de criar a Skill, templates ou exemplos operacionais.
- [x] Confirmar que a implementação Go é autocontida e não exige projeto, build ou ferramenta Java.

## Fase 1 — Arquitetura e Skill

- [x] Criar `.agents/skills/backend-golang/SKILL.md`.
- [x] Separar e importar os 30 blocos do anexo conforme `skill-import-manifest.md`.
- [x] Confirmar que cada destino contém exatamente um `SKILL.md` e que não houve colisão.
- [x] Criar `specs/shared/architecture/backend-golang.md`.
- [x] Criar `specs/shared/backend-golang/conventions.md`, templates e exemplos aprovados.
- [x] Documentar `cmd`, `api`, `application`, `domain` e `infrastructure`.
- [x] Documentar perfis `api` e `desktop`, diretórios isolados e identificação do projeto.
- [x] Atualizar `.agents/skills/README.md` somente com as Skills implementadas.

## Fase 2 — API e persistência

- [x] Criar `specs/shared/api/golang-rest.md`.
- [x] Definir Gin, DTOs, validação, erros, OpenAPI e correlação.
- [x] Criar `specs/shared/database/golang-persistence.md` ou registrar a decisão no documento compartilhado apropriado.
- [x] Definir GORM como adaptador relacional inicial e a porta de repositório CRUD própria do projeto Go.
- [x] Registrar configuração segura, logging e observabilidade.

## Fase 3 — Testes

- [x] Criar `specs/shared/testing/golang-testing.md`.
- [x] Definir inventário de arquivos `.go` aplicáveis e exclusões justificadas.
- [x] Definir padrão `_test.go`, mocks, testes unitários e testes de integração.
- [x] Definir comandos de cobertura e meta mínima de 80%, com recomendação de 90%.
- [x] Demonstrar a estratégia em pelo menos um exemplo ou template aprovado.

## Fase 4 — Segurança e qualidade

- [x] Criar `specs/shared/security/golang-security.md`.
- [x] Executar a Skill `security-audit` no escopo Golang.
- [ ] Executar `docs/security-audit/gerar_relatorio.py` quando o relatório formal for aplicável.
- [x] Verificar a disponibilidade do SonarQube para o projeto Go.
- [x] Executar Sonar quando disponível ou registrar Auditoria de Qualidade Assistida por LLM.
- [x] Redigir segredos em toda evidência e relatório.

## Fase 5 — Revisão, validação e encerramento

- [ ] Registrar revisão da SPEC em `reviews/`.
- [ ] Criar `implementation-plan.md` após `SPEC_APROVADA` e antes da implementação.
- [x] Implementar somente o contrato aprovado.
- [x] Registrar revisão da implementação em `reviews/`.
- [x] Executar testes, cobertura, auditoria e qualidade; registrar evidências em `validation.md`.
- [x] Obter aprovação formal.
- [x] Atualizar `specs/system/`.
- [x] Preparar o arquivo histórico e criar commit rastreável.

## System Documentation

Documento vigente: `specs/system/backend-golang.md`, referenciado também em `specs/system/README.md`. Gates concluídos: `SPEC_APROVADA`, `IMPLEMENTADA`, `IMPLEMENTACAO_APROVADA`, `VALIDADA` e `APROVADA`. Archive: `specs/archive/2026-09-09-035-skill-golang/`. Commit de entrega: `d40fe53`.

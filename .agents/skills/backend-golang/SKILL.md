---
name: backend-golang
description: 'Use when: projetar, gerar, testar ou revisar aplicações Go com os padrões de arquitetura, REST, persistência, segurança e qualidade definidos nesta Change.'
argument-hint: 'Informe a Change aprovada, o perfil api ou desktop e o artifactId.'
---

# Backend Golang

Use esta Skill somente quando a Change possuir status `SPEC_APROVADA` e o perfil Go estiver definido na SPEC da aplicação.

## Processo obrigatório

1. Leia `AGENTS.md`, a Change aprovada, `specs/shared/architecture/backend-golang.md`, `specs/shared/api/golang-rest.md`, `specs/shared/database/golang-persistence.md`, `specs/shared/testing/golang-testing.md` e `specs/shared/security/golang-security.md` quando existirem.
2. Confirme o perfil (`api` ou `desktop`), `groupId`, `artifactId`, módulo Go, diretório de destino e banco previsto.
3. Gere o projeto exclusivamente em `apps/api/<artifactId-sem-hifens>/` ou `apps/desktop/<artifactId-sem-hifens>/`.
4. Preserve a separação entre `cmd`, `internal/api`, `internal/application`, `internal/domain` e `internal/infrastructure`.
5. Mantenha o domínio independente de Gin, GORM, banco, rede e configuração técnica.
6. Use DTOs na API e mantenha entidades de persistência fora dos contratos HTTP.
7. Use GORM somente atrás de portas de repositório quando a SPEC aprovar persistência relacional.
8. Crie testes unitários para todo arquivo de produção aplicável e registre exclusões justificadas.
9. Aferir cobertura com comandos Go reproduzíveis; não invente percentuais.
10. Após a implementação, execute `security-audit` e verifique SonarQube ou registre o fallback de qualidade LLM.
11. Registre comandos, ambiente, resultados, limitações e códigos de saída em `validation.md`.

## Limites

- Esta Skill é autocontida em Go e não exige Java, Maven, Quarkus, JPA, Panache ou JUnit.
- Não altere o código de outra aplicação nem invente arquivos auxiliares ausentes da origem importada.
- Não registre segredos em código, logs, testes, documentação ou relatórios.

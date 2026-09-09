# Suporte Golang vigente

## Artefatos operacionais

- Skill principal: `.agents/skills/backend-golang/SKILL.md`.
- Skills complementares: 30 diretórios `golang-*` catalogados em `.agents/skills/README.md`.
- Convenções, templates e exemplos: `specs/shared/backend-golang/`.
- Arquitetura: `specs/shared/architecture/backend-golang.md`.
- REST: `specs/shared/api/golang-rest.md`.
- Persistência: `specs/shared/database/golang-persistence.md`.
- Testes: `specs/shared/testing/golang-testing.md`.
- Segurança: `specs/shared/security/golang-security.md`.

## Perfis e isolamento

A Skill suporta os perfis `api` e `desktop`. Cada projeto futuro deve ser gerado isoladamente em `apps/api/<artifactId-sem-hifens>/` ou `apps/desktop/<artifactId-sem-hifens>/`. O `artifactId` mantém hífens no módulo Go e o diretório remove hífens de forma determinística.

O perfil `api` usa Gin como adaptador REST de referência, DTOs, validação, OpenAPI e correlação. O perfil `desktop` possui ponto de entrada executável e camadas de domínio e aplicação, sem toolkit gráfico definido nesta base.

## Arquitetura e persistência

A organização padrão é `cmd`, `internal/api`, `internal/application`, `internal/domain` e `internal/infrastructure`. O domínio não depende de Gin, GORM, banco, rede ou configuração. GORM é o adaptador relacional inicial atrás de portas de repositório; o banco e o dialeto são definidos pela SPEC de cada aplicação.

## Qualidade e segurança

Projetos gerados devem inventariar arquivos Go, criar testes aplicáveis, aferir cobertura com comandos Go reproduzíveis e executar auditoria de segurança. SonarQube ou fallback de qualidade devem ser registrados com evidências, sem inventar métricas. Segredos não podem ser versionados ou registrados.

Esta documentação vigente foi consolidada após a aprovação formal da Change `035-skill-golang`.

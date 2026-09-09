# SPEC: 035-skill-golang

## Status

`SPEC_APROVADA`

## Referências documentais

- `proposal.md`
- `DESIGN.md`
- `tasks.md`
- `validation.md`
- `specs/shared/process/workflow.md`
- `specs/shared/architecture/backend-java.md` — somente referência de princípios, sem dependência de código Java.
- `specs/shared/api/rest-conventions.md` — somente referência de contratos HTTP.
- `specs/shared/testing/testing-strategy.md` — somente referência de governança de testes.
- `.agents/skills/security-audit/SKILL.md`
- `docs/security-audit/gerar_relatorio.py`
- `scripts/sonar/validar-codigo.ps1`
- `skill-import-manifest.md`

## Requisitos funcionais

### RF-001 — Skill operacional

Deverá existir uma Skill com nome descobrível `backend-golang`, em `.agents/skills/backend-golang/SKILL.md`, que descreva pré-condições, perfis, estrutura, testes, segurança, qualidade e limites da geração.

A Skill deverá impedir geração operacional antes de `SPEC_APROVADA` e orientar o agente a registrar evidências nos artefatos da Change.

### RF-002 — Perfis e diretórios

A Skill deverá suportar os perfis:

- `api`, gerado em `apps/api/<artifactId-sem-hifens>/`;
- `desktop`, gerado em `apps/desktop/<artifactId-sem-hifens>/`.

Cada projeto deverá ocupar uma pasta própria. A Skill não poderá escrever em outro perfil ou misturar artefatos entre projetos.

O perfil `api` deverá possuir um ponto de entrada HTTP. O perfil `desktop` deverá possuir um ponto de entrada executável Go e as camadas de domínio e aplicação compartilhadas; uma tecnologia de interface gráfica não faz parte desta Change.

### RF-003 — Identidade `groupId` e `artifactId`

A geração deverá exigir `groupId` e `artifactId` e registrar ambos na documentação do projeto.

- O `artifactId` canônico deverá preservar hífens em `go.mod` e metadados.
- O diretório gerado deverá remover hífens do `artifactId`, conforme regra própria e determinística desta especificação.
- O módulo Go deverá ser derivado como `<groupId>/<artifactId>`.
- Nomes de pacotes e símbolos deverão obedecer às regras da linguagem Go.

### RF-004 — Arquitetura

A documentação e os artefatos gerados deverão separar, no mínimo:

- `cmd`: composição e ponto de entrada;
- `internal/api`: transporte, DTOs, validação e erros, somente no perfil `api`;
- `internal/application`: casos de uso;
- `internal/domain`: regras e contratos de domínio;
- `internal/infrastructure`: persistência, configuração, observabilidade e integrações.

Regras de domínio não poderão depender de Gin, GORM, banco, rede ou detalhes de configuração.

### RF-005 — REST

O perfil `api` deverá usar Gin como adaptador REST de referência e atender às seguintes convenções:

- requests e responses públicos deverão usar DTOs;
- entidades de persistência não poderão ser expostas diretamente;
- entradas deverão ser validadas na fronteira HTTP;
- códigos HTTP, payloads de erro e correlação deverão ser consistentes;
- contratos públicos deverão ser documentados com OpenAPI;
- versionamento de recurso somente poderá ser criado se a SPEC da aplicação exigir.

### RF-006 — Persistência relacional

A referência de persistência deverá usar GORM atrás de uma porta de repositório no domínio ou na aplicação. O adaptador não poderá espalhar chamadas GORM pelas regras de negócio.

A interface de repositório deverá documentar, quando aplicável, operações CRUD equivalentes, por finalidade, a `FindByID`, `ListAll`, `Persist` e `Delete`, incluindo contexto, erros de ausência e transação. Esses nomes são uma referência conceitual de contrato, não uma dependência de Panache ou Java. O banco e o dialeto concreto deverão ser definidos pela SPEC da aplicação gerada.

### RF-007 — Configuração e observabilidade

A documentação deverá definir configuração por ambiente, sem credenciais versionadas, e orientar:

- logging estruturado;
- correlation ID e trace ID;
- health checks;
- integração opcional com Prometheus e OpenTelemetry por adaptadores.

A aplicação não poderá registrar segredos em logs, testes, documentação ou relatórios.

### RF-008 — Inventário e testes unitários

Para cada projeto gerado, a validação deverá inventariar todos os arquivos `.go` de produção e classificá-los como aplicáveis ou excluídos com justificativa.

Cada arquivo aplicável deverá possuir cobertura por teste correspondente, normalmente em arquivo `_test.go` do mesmo pacote ou em teste de pacote explicitamente relacionado. São aplicáveis, no mínimo, regras de domínio, casos de uso, validadores, mapeadores, repositórios com lógica e adaptadores que contenham comportamento próprio.

Pontos de entrada triviais, structs declarativas, interfaces sem implementação, arquivos gerados e configuração sem lógica poderão ser excluídos somente com justificativa registrada na validação.

### RF-009 — Cobertura mínima e meta

A cobertura de linhas do código de produção aplicável deverá ser maior ou igual a 80%. A meta recomendada deverá ser maior ou igual a 90%.

A medição deverá ser reproduzível, usando ao menos:

- `go test ./... -coverprofile=coverage.out`;
- `go tool cover -func=coverage.out`.

A validação não poderá declarar porcentagem sem registrar versão do Go, escopo, comando, resultado e código de saída.

### RF-010 — Testes de integração

Comportamentos que dependam de Gin real, GORM, banco, migrations, rede ou composição da aplicação deverão possuir testes de integração quando estiverem no escopo da SPEC da aplicação. Testes unitários não poderão depender de serviço externo ou banco disponível.

### RF-011 — Auditoria de segurança

Após a implementação de um projeto Golang, a validação deverá executar a Skill `security-audit` com stack, módulos e superfícies explicitamente delimitados.

Quando o relatório formal for aplicável, deverá reutilizar `docs/security-audit/gerar_relatorio.py`. O resultado deverá registrar controles conformes, achados confirmados, categorias não aplicáveis, limitações, severidade e recomendações, sem expor valores secretos.

### RF-012 — SonarQube e fallback

A validação deverá verificar se o SonarQube e os recursos exigidos estão disponíveis para o projeto Go.

- Se disponível, deverá executar a análise e registrar projeto, scanner, comando, resultado e código de saída.
- Se indisponível, deverá executar ou registrar Auditoria de Qualidade Assistida por LLM com arquivos, regras, comandos, achados e limitações.
- A indisponibilidade não poderá ser apresentada como análise Sonar concluída, nem o fallback poderá inventar métricas.

### RF-013 — Documentação do sistema

Depois de revisão da implementação, validação e aprovação formal, a mudança deverá atualizar `specs/system/` com o estado vigente da arquitetura Golang e a localização da Skill.

Nenhuma atualização de `specs/system/` poderá ser tratada como substituta dos gates de aprovação.

### RF-014 — Convenções reutilizáveis

Deverá existir o espaço `specs/shared/backend-golang/` para reunir convenções, templates e exemplos aprovados que sejam específicos da geração Golang, sem duplicar as regras comuns de processo.

### RF-015 — Importação das Skills complementares

Após `SPEC_APROVADA`, os 30 blocos de Skill Go listados em `skill-import-manifest.md` deverão ser separados e importados em `.agents/skills/<identificador>/SKILL.md`.

1. O conteúdo textual de cada bloco deverá ser preservado como corpo do `SKILL.md`; poderá ser precedido por frontmatter mínimo (`name` e `description`) para permitir a descoberta operacional.
2. Os destinos deverão usar os identificadores operacionais definidos no manifesto, inclusive a normalização de títulos em português e dos identificadores `golang-nameing`, `golang-lint` e `golang-modernizar`.
3. A implementação não poderá inventar arquivos auxiliares apontados por links quando eles não estiverem presentes no anexo.
4. Uma colisão de diretório, bloco ausente ou diferença de contagem deverá bloquear a conclusão e ser registrada em `validation.md`.

## Requisitos não funcionais

- **Rastreabilidade:** toda verificação deverá informar ambiente, comando, resultado e código de saída.
- **Reprodutibilidade:** nomes, diretórios, módulo e comandos básicos deverão ser determinísticos.
- **Isolamento:** um projeto Go não poderá sobrescrever outro projeto Go nem depender de um projeto Java.
- **Segurança:** segredos serão fornecidos por ambiente local e redigidos nas evidências.
- **Autonomia tecnológica:** a arquitetura deverá usar Go idiomático e ser gerável, compilável e testável sem Java, Maven, Quarkus, JPA, Panache ou JUnit.

## Cenários e critérios de aceite

- [ ] **CA-001:** a Skill `backend-golang` existe, é descobrível e exige SPEC aprovada antes da geração.
- [ ] **CA-002:** os perfis `api` e `desktop` e seus diretórios isolados estão documentados e demonstrados por templates ou exemplos aprovados.
- [ ] **CA-003:** `groupId`, `artifactId`, `go.mod` e diretório sem hífens seguem a regra determinística.
- [ ] **CA-004:** a arquitetura documenta `cmd`, `api`, `application`, `domain` e `infrastructure`, sem dependência de framework nas regras de domínio.
- [ ] **CA-005:** REST com Gin, DTOs, OpenAPI, validação e erros consistentes está documentado.
- [ ] **CA-006:** GORM está definido como adaptador inicial e as operações equivalentes ao Panache estão documentadas por porta de repositório.
- [ ] **CA-007:** o inventário de produção identifica teste para cada arquivo aplicável e justifica cada exclusão.
- [ ] **CA-008:** a cobertura aferida é de pelo menos 80%, ou a limitação é registrada sem percentual inventado; a meta de 90% é reportada separadamente.
- [ ] **CA-009:** a auditoria de segurança foi executada com evidências e relatório sem segredos reais.
- [ ] **CA-010:** SonarQube foi executado quando disponível ou o fallback LLM foi registrado com limitações.
- [ ] **CA-011:** `specs/system/` foi atualizado somente após aprovação formal.
- [ ] **CA-012:** convenções, templates e exemplos Golang estão organizados em `specs/shared/backend-golang/`.
- [ ] **CA-013:** os 30 blocos do anexo foram importados para 30 diretórios operacionais, cada um com `SKILL.md`, sem perda ou invenção de conteúdo.

## System Documentation

Implementação aprovada consolidada em `specs/system/backend-golang.md`, com referência em `specs/system/README.md`. O documento vigente registra os arquivos compartilhados, a Skill `backend-golang`, os perfis de geração, os comandos de teste, a auditoria documental, a decisão de não aplicabilidade do Sonar/fallback e o commit de encerramento será acrescentado ao relatório de aprovação.

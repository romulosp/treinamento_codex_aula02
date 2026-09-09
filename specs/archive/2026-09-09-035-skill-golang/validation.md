# Validação: 035-skill-golang

## Status

`VALIDADA`

Esta validação registra a implementação documental da Skill principal, da documentação compartilhada e da importação dos 30 blocos textuais. Não há aplicação Go executável nesta Change; por isso, testes Go, cobertura e Sonar são não aplicáveis e a qualidade foi verificada por revisão assistida, checagem do editor e verificações estáticas reproduzíveis.

## Critérios de aprovação da SPEC

- [ ] **SPEC-001:** objetivo, escopo e fora de escopo estão delimitados.
- [ ] **SPEC-002:** os perfis `api` e `desktop`, os destinos e o tratamento de `groupId`/`artifactId` são verificáveis.
- [ ] **SPEC-003:** Gin, GORM, arquitetura em camadas, testes e cobertura possuem decisões explícitas.
- [ ] **SPEC-004:** segurança, SonarQube e fallback LLM possuem fluxo de evidência sem resultados inventados.
- [ ] **SPEC-005:** o momento de atualizar `specs/system/` está subordinado à aprovação formal.
- [ ] **SPEC-006:** o espaço compartilhado de convenções, templates e exemplos está incluído sem ampliar o escopo para uma UI desktop.
- [ ] **SPEC-007:** a solução Golang é autocontida e não possui dependência de projeto, build, biblioteca ou ferramenta Java.
- [ ] **SPEC-008:** o manifesto identifica 30 blocos de Skill, seus destinos e a regra para ausência de arquivos auxiliares.

## Evidências pós-implementação

A validação deverá registrar, para cada evidência, ambiente, versão das ferramentas, comando completo, resultado observado e código de saída:

- **VAL-001 — Skill e documentação:** existência e conteúdo de `backend-golang`, documentos compartilhados e catálogo.
- **VAL-002 — Skills anexadas:** comparação dos 30 blocos de origem com os 30 diretórios de destino e seus `SKILL.md`; colisões, perdas e normalizações serão registradas.
- **VAL-003 — Estrutura e geração:** perfis, diretórios isolados, módulo Go, `groupId` e `artifactId`.
- **VAL-004 — Testes:** `go test ./...`, testes de integração aplicáveis e eventuais execuções com `-race`.
- **VAL-005 — Cobertura:** `go test ./... -coverprofile=coverage.out` e `go tool cover -func=coverage.out`, com inventário, exclusões e percentual real.
- **VAL-006 — Segurança:** execução da Skill `security-audit`, relatório oficial quando aplicável e confirmação de que segredos foram redigidos.
- **VAL-007 — Sonar ou fallback:** análise Sonar executada ou Auditoria de Qualidade Assistida por LLM, com a limitação correspondente.
- **VAL-008 — Documentação do sistema:** atualização feita somente após aprovação e vinculada ao commit final.

## Evidências executadas — importação

- **VAL-IMP-001:** o arquivo externo foi contado em Windows PowerShell; resultado: `30` marcadores `Cópia` em `5280` linhas; código de saída `0`.
- **VAL-IMP-002:** a separação criou 30 destinos em `.agents/skills/`; verificação retornou `destinos=30`, `missing=0`, `invalid=0`; código de saída `0`.
- **VAL-IMP-003:** cada Skill importada recebeu frontmatter mínimo com `name` e `description`, mantendo o corpo textual do anexo. A checagem do editor não encontrou erros nos 30 arquivos, na Skill principal ou no catálogo.
- **VAL-IMP-004:** não foram criados arquivos `references/` ou `assets/`, pois esses auxiliares não estavam presentes no anexo.
- **VAL-IMP-005:** comparação dos 30 corpos importados com os blocos de origem; resultado: `comparados=30`, `divergencias=0`; código de saída `0`.

## Ambiente e comandos de validação

- Ambiente: Windows PowerShell `5.1.22621.7517`; Git `2.52.0.windows.1`; Go `1.26.5 windows/386` disponível.
- Inventário documental: PowerShell retornou `required=10`, `missing=0`, `golang-skills=30`, `go-files-in-documentation-scope=0`, `diff-check-exit=0`; código de saída `0`.
- Inventário de módulos: `0` arquivos `go.mod` e `0` arquivos `.go` no escopo da Change e de `specs/shared`; código de saída `0`.
- Integridade textual: comparação dos corpos importados; `comparados=30`, `divergencias=0`; código de saída `0`.
- Qualidade de whitespace: `git diff --check`; código de saída `0`.
- Checagem do editor: nenhum erro nos documentos da Change, na Skill principal, no catálogo ou nos documentos compartilhados.

## Testes, cobertura e qualidade

- `go test ./...`: não aplicável; não existe `go.mod` ou código de produção Go no escopo documental.
- `go test ./... -coverprofile=coverage.out`: não aplicável pelo mesmo motivo; nenhum percentual foi declarado.
- `go tool cover -func=coverage.out`: não aplicável; não existe perfil de cobertura.
- `go test -race ./...`: não aplicável; não existe módulo Go.
- SonarQube: não aplicável; a Change não introduz código executável ou módulo analisável.
- Verificação operacional: `scripts/sonar/validar-codigo.ps1` existe; Docker não está disponível; ainda assim, a decisão principal é não aplicabilidade por ausência de código Go ou módulo analisável.
- Fallback de qualidade assistida por LLM: executado por revisão da implementação, revisão documental, checagem do editor, inventário de escopo, busca de segredos e `git diff --check`. Não foram declaradas métricas estimadas.

## Segurança

- Auditoria atual registrada em `reviews/2026-09-09-security-audit.md`.
- Foram inspecionados 30 arquivos importados; a busca de padrões de segredos de alta confiança retornou `0` ocorrências; código de saída `0`.
- Autenticação, autorização, tenant, IDOR, XSS, injeção, banco, rede e deploy foram classificados como não aplicáveis por ausência de implementação executável.
- Não foi gerado PDF, pois a Change é exclusivamente documental e não possui artefato técnico executável.

## Cenários e critérios

- CA-001 a CA-010: conformes para o escopo documental, com Skill, arquitetura, REST, persistência, testes, segurança e qualidade documentados.
- CA-012 e CA-013: conformes; convenções, templates, exemplos e 30 blocos importados estão presentes e íntegros.
- CA-011: conforme após a aprovação formal; `specs/system/backend-golang.md` e `specs/system/README.md` foram atualizados somente nesta fase.

## Limitações previstas

- A cobertura não será declarada se o comando não puder ser executado ou se o módulo não tiver escopo de produção mensurável.
- SonarQube poderá ser marcado como indisponível se Docker, scanner, servidor ou configuração necessária não estiverem acessíveis.
- O perfil `desktop` não incluirá toolkit de UI nesta Change.

## System Documentation

Documentação vigente criada em `specs/system/backend-golang.md` e indexada em `specs/system/README.md`. O hash do commit de encerramento será registrado no relatório de aprovação após o commit.

# Proposta: 035-skill-golang

## Status

`APROVADA`

## Responsável e data

- Responsável: equipe do projeto
- Data: 2026-09-09

## Referências

- `AGENTS.md`
- `specs/shared/process/workflow.md`
- `specs/shared/architecture/backend-java.md` — referência documental de separação de responsabilidades, não dependência de execução.
- `specs/shared/api/rest-conventions.md` — referência documental de contratos HTTP, não dependência de framework Java.
- `specs/shared/testing/testing-strategy.md` — referência documental de governança de testes, não dependência de JUnit, Maven ou Quarkus.
- `.agents/skills/security-audit/SKILL.md`
- `docs/security-audit/gerar_relatorio.py`
- `scripts/sonar/validar-codigo.ps1`
- `specs/system/README.md`

## Problema e objetivo

A arquitetura documentada do laboratório possui padrões de governança elaborados a partir da experiência com Java, mas não há um projeto Java que deva ser compilado, alterado ou utilizado como dependência desta Change. Ainda falta definir como gerar e validar aplicações Golang com o mesmo nível de governança. Faltam uma Skill própria, convenções de organização, contratos REST, persistência relacional, estratégia de testes, auditoria de segurança e procedimento de qualidade com SonarQube ou fallback assistido por LLM.

Esta Change cria um contrato autocontido para suporte oficial a Golang. Os documentos Java são usados somente como fonte de princípios e critérios de governança; nenhuma ferramenta, biblioteca, estrutura de build ou aplicação Java será necessária para gerar ou executar os projetos Go.

## Escopo

- Criar a Skill operacional `backend-golang` em `.agents/skills/backend-golang/`.
- Importar as 30 Skills complementares de Go listadas em `skill-import-manifest.md`, preservando o conteúdo textual fornecido no anexo.
- Criar documentação compartilhada para arquitetura, REST, testes e segurança Golang.
- Criar o espaço `specs/shared/backend-golang/` para convenções, templates e exemplos reutilizáveis da Skill.
- Definir a estrutura comum `cmd`, `api`, `application`, `domain` e `infrastructure`, inspirada na separação de responsabilidades documentada para Java e adaptada à linguagem Go.
- Definir o perfil REST com Gin e o padrão de persistência relacional com GORM, isolado por interfaces de repositório.
- Definir o contrato de identificação por `groupId` e `artifactId`, incluindo a regra de diretório e de módulo Go.
- Permitir que a Skill gere projetos em `apps/api/<artifactId-sem-hifens>/` ou `apps/desktop/<artifactId-sem-hifens>/`.
- Exigir inventário de arquivos Go aplicáveis, testes unitários correspondentes e aferição reproduzível de cobertura mínima de 80%, com meta de 90%.
- Integrar o fluxo pós-implementação à Skill `security-audit` e ao gerador existente de relatório quando aplicável.
- Verificar a disponibilidade do SonarQube; quando indisponível, registrar Auditoria de Qualidade Assistida por LLM sem inventar resultados.
- Atualizar `specs/system/` somente após aprovação formal, conforme o workflow Spec Driven.

## Fora de escopo

- Criar, compilar ou executar um projeto Java, Maven, Quarkus, JPA, Panache ou JUnit como parte do suporte Golang.
- Inventar ou importar arquivos `references/`, `assets/` ou outros auxiliares que não estejam presentes no anexo fornecido.
- Alterar as especificações de referência usadas para extrair princípios de arquitetura, API ou testes.
- Escolher ou implementar um toolkit de interface gráfica para o perfil `desktop`; o perfil estabelece o esqueleto Go e seus pontos de entrada, não uma UI específica.
- Criar uma matriz de fornecedores de banco ou suportar simultaneamente todos os dialetos SQL nesta Change.
- Versionar código, binários, módulos gerados, credenciais, relatórios locais ou artefatos de build ignorados pelo repositório.
- Declarar execução bem-sucedida de SonarQube, cobertura ou auditoria sem comando, ambiente e evidência reproduzíveis.

## Resultado esperado

O projeto deverá possuir uma referência oficial e reutilizável para desenvolvimento Golang. Uma futura geração aprovada deverá produzir uma aplicação isolada, com camadas documentadas, REST e persistência desacoplados, testes por arquivo aplicável, cobertura aferida, auditoria de segurança e decisão explícita sobre SonarQube ou fallback LLM.

## Riscos e mitigação

- **Diferença entre convenções de referência e Go:** reutilizar somente princípios de separação de responsabilidades e governança; adaptar nomes, módulos e idiomática Go sem copiar APIs Java literalmente.
- **Escolha de ORM inadequada:** encapsular GORM atrás de portas de domínio e registrar a decisão para permitir substituição futura.
- **Cobertura artificial:** medir somente produção aplicável e exigir cenários de comportamento, não apenas percentual.
- **Sonar ou serviços indisponíveis:** registrar a indisponibilidade e executar o fallback de qualidade previsto pelo processo.
- **Relatório de segurança com segredos:** exigir redação imediata de valores sensíveis e reutilizar somente o gerador oficial do projeto.

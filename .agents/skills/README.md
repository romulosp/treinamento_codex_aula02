# Skills do laboratório

## Processo Spec Driven

| Fase | Skill |
| --- | --- |
| Revisar contrato | `spec-review` |
| Implementar contrato aprovado | `spec-implement` |
| Revisar aderência | `implementation-review` |
| Validar por evidências | `implementation-validate` |
| Aprovar mudança | `change-approve` |
| Encerrar, arquivar e commitar | `git-commit` |

## Skills técnicas

- `java-quarkus-resource`: contratos e recursos REST Quarkus.
- `java-panache-repository`: persistência com JPA e Panache.
- `java-integration-test`: testes de integração Quarkus.
- `java-unit-test`: criar, refatorar e revisar testes unitários Java com JUnit 5 e Mockito, validando regra de negócio e cobertura.
- `security-audit`: auditar segurança pós-implementação com evidências verificáveis, histórico de segredos e relatório PDF opcional.

As Skills de processo seguem `specs/shared/process/workflow.md`.

## Skills importadas do estudo

As skills abaixo foram importadas de `D:\desenvolvimento\ia\estudo\skills` para uso local neste projeto. A categoria original foi preservada apenas neste catalogo; para descoberta operacional, cada skill fica diretamente em `.agents/skills/<nome-da-skill>/`.

### Arquitetura

Exemplo de uso: "analise este monolito e sugira limites de dominio" ou "monte um plano de decomposicao incremental".

- `component-common-domain-detection`: encontra logica de negocio duplicada entre componentes e sugere consolidacao.
- `component-flattening-analysis`: identifica classes fora do componente correto e problemas de hierarquia de modulos.
- `component-identification-sizing`: mapeia componentes arquiteturais e mede tamanho para priorizar extracoes.
- `coupling-analysis`: analisa acoplamento entre modulos por forca, distancia e volatilidade.
- `decomposition-planning-roadmap`: cria roteiro de decomposicao e migracao incremental de monolitos.
- `domain-analysis`: mapeia dominios de negocio e sugere bounded contexts com DDD estrategico.
- `domain-identification-grouping`: agrupa componentes existentes em dominios logicos para planejar servicos.
- `evolutionary-modular-architecture`: orienta arquitetura modular evolutiva com DDD, ACL, outbox e resiliencia.
- `frontend-blueprint`: conduz descoberta estruturada antes de criar interfaces frontend.
- `legacy-migration-planner`: planeja modernizacao e migracao incremental de sistemas legados.
- `modular-decomposition`: executa pipeline de analise para dividir monolitos em unidades modulares.
- `modular-design-principles`: orienta principios de desenho modular e separacao de responsabilidades.
- `react-composition-patterns`: orienta composicao de componentes React e APIs reutilizaveis.
- `tactical-ddd`: revisa e refatora modelos de dominio com DDD tatico.

### Criacao

Exemplo de uso: "crie uma skill para padronizar revisoes de contrato" ou "desenhe um subagente verificador".

- `skill-architect`: guia a criacao de skills de alta qualidade a partir de um fluxo estruturado.
- `subagent-creator`: orienta a criacao de subagentes especializados para fluxos complexos.

### Decisao

Exemplo de uso: "faça um pre-mortem deste plano" ou "monte um juri para decidir entre estas arquiteturas".

- `the-fool`: desafia ideias, planos e propostas por pre-mortem, red team e analise de pontos cegos.
- `the-jury`: organiza um painel de decisao com jurados independentes e veredito consolidado.

### Desenvolvimento

Exemplo de uso: "implemente uma tela React Native com Expo Router e lista performatica".

- `react-native-expert`: apoia desenvolvimento React Native e Expo para apps moveis de producao.

### Design

Exemplo de uso: "crie uma interface web de dashboard" ou "revise a UI contra boas praticas visuais".

- `frontend-design`: cria interfaces frontend polidas, distintas e prontas para producao.
- `web-design-guidelines`: revisa UI e UX contra diretrizes de design, interacao e acessibilidade visual.

### Ferramentas

Exemplo de uso: "gere um diagrama Mermaid da arquitetura", "corrija o CI do PR" ou "rode tarefas Nx afetadas".

- `chrome-devtools`: apoia depuracao, screenshots, rede e perfil de performance via Chrome DevTools.
- `excalidraw-studio`: gera diagramas Excalidraw a partir de descricoes naturais.
- `gh-fix-ci`: investiga falhas de checks de PR no GitHub Actions e prepara correcao com aprovacao.
- `mermaid-studio`: cria, valida e renderiza diagramas Mermaid em varios formatos.
- `nx-ci-monitor`: monitora pipelines Nx Cloud e conduz correcao assistida de falhas.
- `nx-generate`: usa generators do Nx para scaffold de projetos, libs e recursos.
- `nx-run-tasks`: executa build, test, lint, serve, run-many e affected em workspaces Nx.
- `nx-workspace`: explora, configura e otimiza workspaces Nx.

### Performance

Exemplo de uso: "rode Lighthouse local", "otimize o bundle" ou "melhore o score de um site Astro".

- `perf-astro`: aplica otimizacoes de performance especificas para sites Astro.
- `perf-lighthouse`: executa e interpreta auditorias Lighthouse e budgets de performance.
- `perf-web-optimization`: otimiza bundle, imagens, cache, lazy loading e velocidade web geral.

### Qualidade

Exemplo de uso: "revise este PR", "melhore SEO", "faça auditoria de acessibilidade" ou "audite qualidade web".

- `pr-review`: revisa pull requests no GitHub com comentarios e resumo consolidado.
- `react-best-practices`: aplica boas praticas de performance para React e Next.js.
- `seo`: melhora visibilidade em buscadores com metadados, sitemap e dados estruturados.
- `tlc-generative-engine-optimization`: otimiza paginas para descoberta e citacao por mecanismos de resposta com IA.
- `web-accessibility`: audita e melhora acessibilidade web conforme WCAG.
- `web-best-practices`: revisa seguranca, compatibilidade e qualidade de codigo web.
- `web-quality-audit`: consolida auditoria web de performance, acessibilidade, SEO e boas praticas.

### Seguranca

Exemplo de uso: "gere um modelo de ameacas", "revise praticas seguras" ou "mapeie ownership de codigo sensivel".

- `security-best-practices`: revisa praticas de seguranca por linguagem e framework.
- `security-ownership-map`: mapeia ownership de codigo sensivel a partir do historico Git.
- `security-threat-model`: produz modelo de ameacas baseado no repositorio, ativos e limites de confianca.

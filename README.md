# Aula 02 — Laboratório de Geração de APIs Java

> Projeto educacional para construção de APIs Java por meio de especificações, evidências e ciclos de entrega rastreáveis.

## Visão geral

Este repositório demonstra uma abordagem **Spec Driven** para criação de APIs. Antes de qualquer implementação, os requisitos, decisões técnicas, tarefas, critérios de aceite e evidências de validação são registrados e revisados.

O projeto mantém a documentação como fonte de verdade. O módulo executável é gerado localmente a partir de uma mudança aprovada, validado e pode ser removido para que o processo seja reproduzido do zero.

## Estado atual

O workspace está intencionalmente em estado documental: os backends executáveis são gerados localmente dentro de pastas próprias sob `apps/backend/`, como `apps/backend/gerenciarcategorias/`. As aplicações e suas evidências permanecem documentadas no histórico.

A política do repositório permite versionar documentos `.md` e `.txt`, além do próprio `.gitignore`. Código, configurações, scripts e artefatos gerados são mantidos apenas no ambiente local durante uma execução aprovada.

## Principais características

- **Processo rastreável:** cada mudança possui proposta, SPEC, design, tarefas, revisões, validação e aprovação.
- **Arquitetura de referência:** Java 17, Maven, Quarkus e separação entre `api`, `application`, `domain` e `infrastructure`.
- **Qualidade desde o início:** critérios de aceite verificáveis, testes unitários e integração Quarkus como parte do ciclo de mudança.
- **Segurança por configuração:** credenciais e dados de infraestrutura devem ser fornecidos pelo ambiente local, nunca registrados em documentos ou commits.
- **Reprodutibilidade:** a documentação permite gerar novamente o mesmo módulo e comprovar o resultado com evidências objetivas.

## Estrutura do repositório

| Local | Finalidade |
| --- | --- |
| [NotasProjeto.md](NotasProjeto.md) | Visão detalhada do projeto, arquitetura, histórico e guia de reprodução. |
| [AGENTS.md](AGENTS.md) | Regras obrigatórias para mudanças no repositório. |
| [specs/changes/](specs/changes/) | Mudanças em andamento antes do arquivamento. |
| [specs/archive/](specs/archive/) | Histórico de mudanças concluídas, com decisões e evidências. |
| [specs/shared/](specs/shared/) | Convenções de arquitetura, API, testes e processo. |
| [specs/system/](specs/system/) | Descrição vigente do estado do sistema. |
| [docs/adr/](docs/adr/) | Decisões arquiteturais duradouras. |
| [.agents/skills/](.agents/skills/) | Procedimentos especializados utilizados pelo agente. |

## Ciclo de entrega

Cada alteração segue o fluxo abaixo:

1. **Especificação** — definição da proposta, SPEC, design e tarefas.
2. **Revisão da SPEC** — confirmação de escopo e critérios verificáveis.
3. **Implementação** — produção limitada ao que foi aprovado.
4. **Revisão da implementação** — verificação de aderência à SPEC.
5. **Validação** — execução de testes e registro de evidências.
6. **Aprovação** — consolidação dos gates de qualidade.
7. **Encerramento** — atualização do estado vigente, arquivamento e commit rastreável.

As entradas, saídas e regras de retorno estão definidas em [specs/shared/process/workflow.md](specs/shared/process/workflow.md).

## Como contribuir ou iniciar uma nova API

1. Crie uma pasta em [specs/changes/](specs/changes/) com o próximo identificador e um nome descritivo.
2. Use os modelos em [specs/templates/](specs/templates/) para registrar a proposta, a SPEC, o design e as tarefas.
3. Solicite e registre a revisão da SPEC antes de gerar qualquer código.
4. Gere o módulo localmente, implemente somente o escopo aprovado e crie os testes previstos.
5. Registre revisão, validação, aprovação e evidências antes de arquivar a mudança.

Consulte [NotasProjeto.md](NotasProjeto.md) para instruções completas de criação, testes, execução e limpeza de um módulo Java Quarkus.

## Referências

- [Arquitetura do backend Java](specs/shared/architecture/backend-java.md)
- [Convenções REST](specs/shared/api/rest-conventions.md)
- [Estratégia de testes](specs/shared/testing/testing-strategy.md)
- [Estado vigente do sistema](specs/system/README.md)
- [Histórico das mudanças](specs/archive/)

## Créditos

**Autor:** f744113 - Rômulo Penha
**Projeto:** Treinamento Codex — Aula 02

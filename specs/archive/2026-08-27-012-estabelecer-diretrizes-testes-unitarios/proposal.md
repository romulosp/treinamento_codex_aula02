# Proposta: 012-estabelecer-diretrizes-testes-unitarios

## Status
`APROVADA`

## Responsável e data

f744113 — 2026-08-27.

## Referências

- `AGENTS.md`
- `specs/shared/testing/testing-strategy.md`
- `specs/templates/validation-template.md`
- `.agents/skills/`
- `specs/shared/process/workflow.md`

## Problema e objetivo

A estratégia de testes atual descreve apenas as tecnologias utilizadas e não estabelece critérios operacionais para testes unitários Java independentes de banco de dados e do container Quarkus. Consolidar uma política canônica e uma Skill específica para orientar a criação, a refatoração e a revisão desses testes.

## Escopo

- Definir diretrizes obrigatórias para testes unitários Java com JUnit 5 e Mockito.
- Registrar as regras para spies, Panache sem container, `TypedQuery` e `Query`, varargs, reflexão, métodos privados e estáticos, enums, SonarQube, exceções e branches.
- Estabelecer o objetivo de cobertura entre 80% e 100%, a classificação verificável das classes Java e a rastreabilidade de cada classe aplicável por teste unitário.
- Criar a Skill de uso sob demanda `.agents/skills/java-unit-test/SKILL.md` e atualizar seu índice.
- Ajustar o modelo de validação para registrar ferramenta, escopo, cobertura, comando, resultado, código de saída e justificativas dos testes unitários.

## Fora de escopo

- Criar, restaurar ou alterar um módulo Java executável.
- Escrever testes para código inexistente no estado atual do repositório.
- Alterar a estratégia de testes de integração ou adicionar dependências de build e ferramentas de cobertura.

## Impactos e riscos

A política é documental e passa a ser obrigatória para mudanças Java futuras. O repositório não contém arquivos `.java` nem um módulo Maven/Quarkus executável neste momento; portanto, não há testes unitários a criar ou métrica de cobertura a calcular nesta mudança. A ferramenta de aferição, como JaCoCo, será definida na mudança que introduzir o módulo executável.

## Critérios para aprovação da SPEC

- A estratégia contém todas as diretrizes fornecidas, sem contradizer `AGENTS.md` e o workflow.
- A Skill é específica para criação, refatoração e revisão de testes unitários Java.
- O modelo de validação permite evidenciar a meta de cobertura e a ausência de classes Java quando aplicável.
- A documentação preserva o português do Brasil e Markdown válido.

# Proposta: 032-documentar-java

## Status

`SPEC_APROVADA`

## Responsável e data

- Responsável: equipe do projeto
- Data: 2026-09-01

## Referências

- `AGENTS.md`
- `.agents/skills/java-javadoc/SKILL.md`
- `specs/shared/process/workflow.md`
- `specs/shared/testing/testing-strategy.md`

## Problema e objetivo

Os módulos Java existentes possuem 40 arquivos `.java` (30 de produção e 10 de teste) com documentação JavaDoc incompleta ou ausente. O objetivo é documentar todos os arquivos Java dentro do escopo, preservando comportamento e escrevendo em português do Brasil com base em evidências.

## Escopo

- Documentar todos os `.java` sob `apps/backend/**/src/main/java/` e `apps/backend/**/src/test/java/` presentes no inventário da implementação.
- Atualizar JavaDoc de tipos, construtores e métodos relevantes, incluindo tags aplicáveis e contratos comprovados.
- Manter a implementação, assinaturas, APIs, testes e dependências inalteradas.
- Executar os testes Maven aplicáveis e registrar evidências.

## Fora de escopo

- Alterar comportamento, nomes, assinaturas ou arquitetura Java.
- Documentar arquivos fora dos dois módulos backend inventariados.
- Inventar regras, exceções, nulabilidade, efeitos colaterais, transações ou garantias de concorrência.

## Impactos e riscos

- A mudança tocará muitos arquivos, aumentando o risco de JavaDoc desatualizado ou de alteração acidental de código.
- Comentários especulativos podem induzir uso incorreto; toda afirmação deverá ser rastreável à implementação, contrato ou teste.

## Critérios para aprovação da SPEC

- O escopo identifica os diretórios e o inventário inicial de 40 arquivos.
- Os critérios de aceite verificam cobertura, ausência de alterações funcionais, idioma e validação Maven.

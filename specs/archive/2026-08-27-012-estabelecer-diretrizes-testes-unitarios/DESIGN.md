# Design: 012-estabelecer-diretrizes-testes-unitarios

## Contexto

O repositório mantém a estratégia de testes como fonte compartilhada e usa Skills para orientar fluxos especializados. Como não há backend Java neste estado, a mudança estabelece o contrato que deve ser aplicado quando classes Java e seus testes forem introduzidos ou alterados.

## Referências

- `proposal.md`
- `spec.md`
- `AGENTS.md`
- `specs/shared/testing/testing-strategy.md`
- `specs/templates/validation-template.md`

## Decisões

1. Manter a política detalhada em `specs/shared/testing/testing-strategy.md`, pois é a fonte canônica de qualidade de testes.
2. Criar `.agents/skills/java-unit-test/SKILL.md` para o fluxo sob demanda de criar, refatorar ou revisar testes unitários Java; a Skill complementa, mas não substitui, a política canônica.
3. Acrescentar uma referência curta em `AGENTS.md` para tornar a adoção obrigatória em toda mudança Java sem duplicar regras extensas.
4. Evoluir o modelo de validação, em vez de alterar validações arquivadas, para que cada mudança futura registre cobertura, escopo e exclusões justificadas.
5. Não estabelecer ferramenta de cobertura nesta mudança, pois não há módulo Maven ou classes Java. A ferramenta e o comando serão definidos pela SPEC da mudança que introduzir o módulo executável.
6. Manter no inventário todas as classes Java. Exigir testes para classes com comportamento e permitir apenas as exclusões objetivas definidas na SPEC, sempre justificadas na validação.

## Arquitetura e componentes

- `specs/shared/testing/testing-strategy.md`: política detalhada e critérios de qualidade.
- `.agents/skills/java-unit-test/SKILL.md`: roteiro operacional especializado.
- `.agents/skills/README.md`: índice da nova Skill.
- `AGENTS.md`: obrigação concisa e referência à política.
- `specs/templates/validation-template.md`: evidências mínimas de testes unitários e cobertura.
- `specs/changes/012-estabelecer-diretrizes-testes-unitarios/validation.md`: registro desta mudança e da indisponibilidade atual de aferição.

## Alternativas e consequências

- Criar somente uma Skill foi descartado: Skills são carregadas sob demanda e não substituem uma regra normativa compartilhada.
- Duplicar integralmente a política em `AGENTS.md` foi descartado para evitar divergência.
- Medir cobertura ou criar testes nesta mudança foi descartado porque não existe código Java nem build executável; registrar uma porcentagem sem medição seria evidência inválida.

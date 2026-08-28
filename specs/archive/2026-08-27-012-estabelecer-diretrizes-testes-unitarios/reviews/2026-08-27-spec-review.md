# Revisão da SPEC: 012-estabelecer-diretrizes-testes-unitarios

## Escopo revisado

- `proposal.md`, `spec.md`, `DESIGN.md` e `tasks.md` da mudança.
- `AGENTS.md`, a estratégia compartilhada de testes e o workflow vigente.

## Achados

- `REV-001` — **menor** — A SPEC não indicava o caminho e o nome exatos da Skill. **Impacto:** risco de implementação em local inconsistente. **Recomendação:** definir `.agents/skills/java-unit-test/SKILL.md` no requisito.
- `REV-002` — **importante** — A atualização do modelo de validação não detalhava os campos de evidência de testes unitários e cobertura. **Impacto:** padronização insuficiente para mudanças futuras. **Recomendação:** exigir ferramenta, escopo, comando, resultado, código de saída e justificativas.
- `REV-003` — **importante** — O conceito de classe Java aplicável não possuía categorias objetivas. **Impacto:** exclusões e meta de cobertura não seriam verificáveis. **Recomendação:** definir inventário, classes obrigatórias e exclusões justificadas.
- `REV-004` — **importante** — A indexação da Skill não identificava o arquivo, a seção nem a descrição esperada. **Impacto:** risco de índice inconsistente. **Recomendação:** especificar a atualização de `.agents/skills/README.md` na seção de Skills técnicas.
- `REV-005` — **importante** — A meta de cobertura não esclarecia que o ferramental será definido com o módulo executável. **Impacto:** tentativa de aferição sem ferramenta ou baseline. **Recomendação:** registrar explicitamente a dependência futura de ferramenta, como JaCoCo.
- `REV-006` — **menor** — O momento de invocação da Skill não estava formalizado. **Impacto:** uso inconsistente. **Recomendação:** exigir o uso durante implementação ou revisão de mudanças com classes Java aplicáveis ou testes unitários.

## Veredito

`REPROVADA`

A SPEC deve incorporar as recomendações dos achados importantes antes de uma nova revisão.

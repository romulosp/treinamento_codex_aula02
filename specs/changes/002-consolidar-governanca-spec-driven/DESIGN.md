# Design: 002-consolidar-governanca-spec-driven

## Decisão

`specs/shared/process/workflow.md` será a fonte oficial do processo. `AGENTS.md` somente impõe suas regras; as Skills explicam como executar cada fase; os prompts antigos permanecem como referência didática.

## Fluxo escolhido

```text
RASCUNHO → EM_REVISAO_SPEC → SPEC_APROVADA → EM_IMPLEMENTACAO
→ IMPLEMENTADA → EM_REVISAO_IMPLEMENTACAO → IMPLEMENTACAO_APROVADA
→ EM_VALIDACAO → VALIDADA → EM_APROVACAO → APROVADA
→ atualizar system/ e preparar archive/ → commit → ARQUIVADA
```

O arquivamento é preparado antes do commit para que a atualização da verdade vigente e o histórico da mudança sejam atômicos no Git.
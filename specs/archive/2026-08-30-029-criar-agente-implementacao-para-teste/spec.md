# SPEC: 029-criar-agente-implementacao-para-teste

## Status

`SPEC_APROVADA`

1. Deve existir um agente `implementador-para-teste` em `.github/agents/`.
2. O agente deve ler os documentos da mudança e as regras do projeto, identificar a primeira fase pendente e executar exclusivamente especificação, revisão da SPEC e implementação.
3. O agente só pode editar código após `proposal.md` e `spec.md` estarem `SPEC_APROVADA`.
4. Ao chegar em `IMPLEMENTADA`, deve executar os testes aplicáveis e parar, mantendo a mudança em `specs/changes/`.
5. O agente não pode executar revisão da implementação, validação formal, aprovação, atualização de `specs/system/`, arquivamento, `git add` ou `git commit`.
6. Deve informar o caminho para a continuação posterior pelo prompt integral existente.

## Critérios de aceite

- Uma mudança aprovada pode ser implementada e testada sem gerar commit.
- Após a execução, a mudança mantém o estado `IMPLEMENTADA` e arquivos não são movidos para `specs/archive/`.
- O README contém exemplos de uso do agente parcial e do prompt integral.

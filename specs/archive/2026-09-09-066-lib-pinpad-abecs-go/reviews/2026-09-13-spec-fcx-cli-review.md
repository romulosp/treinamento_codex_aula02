# Revisão da SPEC — correção da opção 21 FCX

Data: 2026-09-13

## Escopo revisado

- `spec-command-fcx.md`
- `DESIGN.md`
- `implementation-plan.md`
- `tasks.md`
- `spec-logging.md`
- Manual ABECS v2.12, seção 3.7.4, páginas 140–141

## Verificação

O contrato distingue corretamente entrada e resposta. `SPE_FCXOPT` é
obrigatório e representa aprovação, negação ou falha da comunicação com a rede;
`SPE_ARC` é obrigatório somente nas duas primeiras decisões. `SPE_EMVDATA`,
`SPE_TAGLIST` e `SPE_TIMEOUT` permanecem opcionais. `PP_FCXRES` é resposta
obrigatória do pinpad e não integra o comando.

O fluxo proposto elimina a aprovação fixa que produziu o teste físico
inconsistente, recusa entradas inválidas antes da serial e preserva o status
`047` como código não catalogado pelo ABECS 2.12. Os critérios são observáveis
em testes unitários e deixam explícita a necessidade de nova validação física.
O diagnóstico `GOX_CONFIG` contém somente adquirente, método e índice já
validados; o contrato mantém os frames e dados criptográficos redigidos.

## Resultado

`SPEC_APROVADA`

Nenhuma pendência material foi encontrada para iniciar a implementação.

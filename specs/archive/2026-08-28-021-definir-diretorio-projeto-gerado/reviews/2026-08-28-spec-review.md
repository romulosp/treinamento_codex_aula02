# Revisão da SPEC: 021-definir-diretorio-projeto-gerado

## Escopo revisado

Foram revisados `proposal.md`, `spec.md`, `DESIGN.md`, `tasks.md`, as regras de `AGENTS.md` e o workflow canônico.

## Resultado

- `REV-001` — nenhum achado bloqueante. A regra diferencia corretamente o `artifactId` Maven, o diretório normalizado e o pacote Java.
- `REV-002` — nenhum achado importante. Os cenários de `gerenciar-categorias` e `gerenciar-tarefas` verificam isolamento entre aplicações independentes.
- `REV-003` — nenhum achado importante. O escopo limita a mudança à documentação de geração, execução, teste, limpeza e estado vigente, sem alterar contratos ou código.

## Veredito

`SPEC_APROVADA`

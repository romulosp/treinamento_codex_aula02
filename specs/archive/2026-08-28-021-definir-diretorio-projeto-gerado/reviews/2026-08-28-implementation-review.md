# Revisão da implementação: 021-definir-diretorio-projeto-gerado

## Escopo revisado

Foi comparada a documentação alterada com a SPEC aprovada. Esta é uma mudança documental; não há código de aplicação a revisar.

## Verificações

- `IMP-REV-001` — resolvido — A especificação de criação e a parametrização definem `apps/backend/<artifactId-sem-hifens>/`.
- `IMP-REV-002` — resolvido — O prompt cria a pasta antes da geração, muda para a pasta do projeto no script e executa Maven a partir dela.
- `IMP-REV-003` — resolvido — Os exemplos de categorias e tarefas usam diretórios independentes e preservam hífens somente no `artifactId`.
- `IMP-REV-004` — resolvido — README, Notas do Projeto e sistema vigente refletem o contêiner `apps/backend/` e as pastas específicas.

## Veredito

`IMPLEMENTACAO_APROVADA`

# Especificações

`system/` documenta o sistema vigente. `changes/` contém propostas e implementações em andamento. Após aprovação formal, a mudança atualiza `system/`, é preparada em `archive/` e ambas as alterações são registradas no mesmo commit.

Use os modelos em `templates/`, as regras comuns em `shared/` e o fluxo oficial em `shared/process/workflow.md`. Os prompts da Aula 01 permanecem como referência em `legacy-prompts/`.

`sprint/` organiza a ordem e o acompanhamento de Changes sem substituir o workflow Spec Driven. Cada Sprint concreta usa o modelo e os prompts desse diretório, referencia somente Changes em andamento e registra os gates, evidências, riscos e resultados de cada entrega.

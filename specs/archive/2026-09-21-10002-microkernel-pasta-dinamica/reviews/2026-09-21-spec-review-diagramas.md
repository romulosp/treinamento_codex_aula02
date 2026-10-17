# Revisão complementar da SPEC — diagramas de sequência

## Resultado

`SPEC_APROVADA`

## Entrada revisada

- `diagrama-sequencia-arquitetura.jfif`
- `Gemini_Generated_Image_q0hwlaq0hwlaq0hw.jfif`

## Achados resolvidos

| ID | Severidade | Achado | Resolução na SPEC |
| --- | --- | --- | --- |
| REV-DIAG-001 | alta | faltava explicitar todas as transições do pipeline | RF-04 exige `DISCOVERED` até `ACTIVE` |
| REV-DIAG-002 | alta | faltava carga no boot pelo repositório verificado | adicionado RF-04A |
| REV-DIAG-003 | alta | faltava fluxo cooperativo após falha em callbacks/UI | RF-06 exige `ERROR`, `onDetach`, limpeza e fallback |
| REV-DIAG-004 | média | destino de artefato rejeitado estava ambíguo | decidido excluir a quarentena contaminada |

Após os ajustes, não restam ambiguidades bloqueantes e a SPEC permanece
`SPEC_APROVADA` para implementação.

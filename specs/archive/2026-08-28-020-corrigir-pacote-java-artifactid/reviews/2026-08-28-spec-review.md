# Revisão de SPEC — 020-corrigir-pacote-java-artifactid

## Resultado

`SPEC_APROVADA`

## Achados

| ID | Severidade | Evidência | Impacto | Recomendação |
| --- | --- | --- | --- | --- |
| REV-001 | Alta | `001-criar-projeto-java` prescrevia `nomedaapigerada` no pacote. | Novos projetos geravam declarações e diretórios incompatíveis com o `artifactId`. | Aprovada a derivação sem hífens definida nesta mudança. |

## Verificação

- Objetivo, escopo e fora de escopo estão definidos.
- O exemplo `gerenciar-tarefas` → `gerenciartarefas` é verificável.
- A regra preserva coordenadas Maven e demais contratos.
- Não há dependência de ADR nova.

## Veredito

`SPEC_APROVADA`

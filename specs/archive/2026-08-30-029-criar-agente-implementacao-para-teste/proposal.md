# Proposta: 029-criar-agente-implementacao-para-teste

## Status

`SPEC_APROVADA`

## Objetivo

Disponibilizar um agente operacional que execute uma mudança Spec Driven até o fim da implementação, permitindo teste e avaliação humana antes de qualquer revisão formal, validação, arquivamento ou commit.

## Escopo

- Criar o agente em `.github/agents/`.
- Criar um prompt de invocação em `.github/prompts/`.
- Documentar o uso no README principal.

## Fora de escopo

- Alterar o fluxo canônico de sete fases.
- Criar commit, arquivar mudança ou atualizar `specs/system/` em nome do agente.

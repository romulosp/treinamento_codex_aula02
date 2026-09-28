# Revisão de SPEC — registro local de GTK em claro

**Data:** 2026-09-13

**Estado de entrada:** `EM_REVISAO_SPEC`

## Escopo revisado

A solicitação operacional exige que as trilhas devolvidas em claro sejam
visíveis no arquivo de diagnóstico. A revisão limita esse comportamento à
opção 19 do utilitário local, após resposta GTK válida e somente quando o modo
em claro tiver sido escolhido explicitamente.

## Decisões

- O frame serial GTK continua integralmente redigido.
- A linha `GTK_CLEAR` é criada a partir da resposta já validada.
- São registrados somente `TRACK1`, `TRACK2` e `TRACK3`; KSN e material de
  chave continuam proibidos.
- Delimitação e escaping impedem que o conteúdo recebido crie linhas falsas no
  rastro.
- O modo criptografado não registra os blobs retornados.
- A biblioteca não passa a registrar trilhas automaticamente; a exceção existe
  na composição do executável local de laboratório.

Os critérios são determinísticos e testáveis sem equipamento físico. Não foi
encontrada ambiguidade de implementação.

## Decisão

`SPEC_APROVADA`

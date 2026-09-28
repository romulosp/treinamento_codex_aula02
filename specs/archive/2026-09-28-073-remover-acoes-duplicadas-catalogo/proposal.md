# Proposta — 073 Remover ações duplicadas do catálogo Android

Autor: Rômulo Penha

## Status

`SPEC_APROVADA`

Revisão registrada em
[reviews/2026-09-28-spec-review.md](reviews/2026-09-28-spec-review.md).

## Objetivo

Eliminar a duplicidade visual e funcional de abrir/fechar conexão no
`diagnosticopinpad`. As ações continuarão disponíveis nos botões rápidos
superiores; o catálogo funcional exibirá somente as ações adicionais.

## Escopo

- remover `CatalogAction.OPEN` e `CatalogAction.CLOSE` da lista apresentada no
  catálogo;
- preservar os botões superiores `Abrir` e `Fechar`, seus contratos de estado,
  habilitação, erro e cancelamento;
- ajustar numeração, agrupamento e testes do catálogo;
- atualizar a documentação e a evidência visual do aplicativo.

## Fora do escopo

Não alterar as operações Go/ABECS, o protocolo, o Bridge, a fachada gomobile,
os botões rápidos, o estado da sessão, o layout de outras seções ou a
funcionalidade de abrir/fechar.

## Critérios de aceite

CA-073-01 a CA-073-06 estão definidos em `spec.md`. A implementação só começa
após a revisão formal da SPEC.

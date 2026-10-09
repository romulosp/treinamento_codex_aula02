# Proposta — 10010-estilo-menu-aplicacao

**Status:** SPEC_APROVADA
**Data:** 2026-10-03

## Objetivo e escopo
Aplicar à tela inicial de menu Android o layout POS fornecido pelo usuário: cabeçalho branco, fundo em gradiente azul, título de seção, grade de cards azuis, saída vermelha flutuante e rodapé branco. A SPEC de origem v2 está preservada em `references/spec-origem-v2.md`; o contrato Android está em `spec.md`.

A aplicação existente usa Kotlin e Compose Material 3, no módulo `app` de `apps/frontend/smartphone/sistema-prototipo-android`. Os pontos de integração são `MainActivity.kt` (`BusinessMenuScreen`, `BusinessMenuEntries`) e `AuthenticationHeader.kt` (`CoreShell`, `AuthenticationHeader`). O pacote é `br.com.romulopenha.sistemaprototipoandroid`.

## Fora de escopo
Regras de negócio, autenticação, autorização, sessão, logout, plugins, backend, APIs, DTOs, novas fontes de dados, novas dependências, atualização de SDK/stack e redesign de login ou telas dos plugins. O cabeçalho é compartilhado: a variante visual do menu deve ser isolada para preservar as demais telas.

## Riscos e decisões
A regressão funcional é possível e exige testes de callbacks, ordenação, agrupamento, estado vazio e erro fatal. Densidade Android exige distinguir pixels físicos de dp. Terminal e lotérica não têm valores dinâmicos no cabeçalho atual: não inventar os valores do SVG. Não incorporar skills externas nesta Change; usar as skills locais previstas em AGENTS.md. Nenhuma mudança arquitetural ou nova versão é proposta.

## Gate
Revisão técnica registrada em `reviews/2026-10-03-spec-review.md`. Esta aprovação da SPEC não significa implementação validada nem aprovação formal para arquivamento.

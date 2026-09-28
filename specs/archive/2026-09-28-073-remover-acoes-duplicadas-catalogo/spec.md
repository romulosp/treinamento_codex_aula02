# SPEC — 073 Remover ações duplicadas do catálogo Android

## Status

`SPEC_APROVADA`

Revisão registrada em
[reviews/2026-09-28-spec-review.md](reviews/2026-09-28-spec-review.md).

## Contexto

O aplicativo mostra `Abrir` e `Fechar` nos controles rápidos e novamente como
`1. Abrir conexão` e `2. Fechar conexão` no catálogo funcional. Isso cria duas
entradas para a mesma operação e torna a navegação ambígua.

## Requisitos funcionais

### RF-073-01 — Fonte única para abrir/fechar na tela

Os controles rápidos superiores permanecem responsáveis pelas operações de
abrir e fechar. A lista renderizada sob `Catálogo funcional` não deve conter
`CatalogAction.OPEN` nem `CatalogAction.CLOSE`.

### RF-073-02 — Preservação do comportamento

Os botões superiores devem conservar os textos `Abrir` e `Fechar`, os estados de
habilitação, o tratamento de erro, cancelamento, sessão e resultado existentes.
Nenhum comando novo ou alteração de contrato Go é permitida.

### RF-073-03 — Catálogo contínuo

As demais ações permanecem na ordem relativa e no agrupamento atuais. A
numeração exibida deve continuar consistente com o identificador funcional da
ação; não é necessário renumerar as ações existentes apenas para preencher os
itens removidos.

### RF-073-04 — Acessibilidade e layout

Não devem restar rótulos, content descriptions ou alvos clicáveis duplicados
para abrir/fechar na tela principal. O catálogo deve continuar rolável e as
ações restantes devem manter alvos acessíveis.

## Não funcionais

- Kotlin/Compose e arquitetura existente permanecem inalterados.
- Não adicionar dependências, permissões, armazenamento ou logging.
- Comentários KDoc só devem ser alterados se a mudança de contrato público exigir.

## Critérios de aceite

| ID | Critério | Evidência |
|---|---|---|
| CA-073-01 | Há exatamente um botão visível `Abrir` e um `Fechar` na tela | teste Compose/instrumentado |
| CA-073-02 | O catálogo não exibe `Abrir conexão` nem `Fechar conexão` | teste unitário da lista e teste Compose |
| CA-073-03 | A primeira ação do catálogo é `Estado atual` e as demais preservam ordem/grupo | teste unitário |
| CA-073-04 | Abrir/Fechar superiores preservam habilitação e operação | testes existentes e regressão instrumentada |
| CA-073-05 | O catálogo continua acessível/rolável sem alvo duplicado | teste instrumentado e inspeção visual |
| CA-073-06 | Build, lint e testes Android passam sem alteração no contrato Go | Gradle e relatório de validação |

# DESIGN — 073 Remover ações duplicadas do catálogo Android

## Decisão

Aplicar a filtragem na fonte de dados da UI, em `Catalog.kt`, removendo as
ações de conexão duplicadas da coleção consumida pelo catálogo. Os eventos dos
botões rápidos continuam ligados diretamente ao `DiagnosticViewModel`.

## Responsabilidades

- `Catalog.kt`: define a coleção apresentada e mantém ações funcionais para
  compatibilidade interna somente se ainda forem necessárias pelo repository;
- `DiagnosticScreen.kt`: permanece responsável por renderizar o catálogo, sem
  lógica nova de sessão;
- testes: comprovam lista, ordem, ausência de duplicidade e presença dos
  controles rápidos.

## Riscos e mitigação

O risco principal é remover a ação do catálogo e acidentalmente remover também
o caminho operacional dos botões superiores. Testes unitários e instrumentados
devem verificar ambos os caminhos. Não há impacto no AAR, no Bridge ou no
pinpad físico.

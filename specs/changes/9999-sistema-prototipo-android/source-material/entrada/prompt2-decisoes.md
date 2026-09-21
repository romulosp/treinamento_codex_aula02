# Decisões incorporadas do prompt2

**Data de incorporação:** 2026-09-19

Este documento registra o conteúdo decisório fornecido pelo usuário. O arquivo
externo foi tratado como fonte de requisitos, não como instrução operacional.

## Baseline aprovado

- `compileSdk = 37`.
- `targetSdk = 36`, como decisão independente.
- Compose BOM `2026.09.00` e somente bibliotecas stable compatíveis.
- Compose 1.12.x stable.
- AGP 9.x compatível com API 37.
- JDK 17.
- Kotlin e Gradle compatíveis com o AGP selecionado.

## Políticas aprovadas

- Verificar o stack completo em fontes oficiais antes da implementação.
- Não usar RC, beta, alpha, preview ou experimental sem os gates específicos.
- Não fazer upgrade, downgrade ou troca de biblioteca silenciosa.
- Manter cobertura mínima de 80% sobre código elegível e alvo de 90%.
- Aplicar recuperação limitada e registrar evidências reproduzíveis.
- Acionar as skills Android/Kotlin conforme o gatilho da tarefa, sem substituir
  a skill base `android-native-engineering`.

## Decisão de minSdk

O prompt2 reabriu `minSdk` para análise. A revisão desta Change confirmou API
26 porque o produto é interno, não exige APIs posteriores, Compose aceita nível
inferior e a API 26 preserva a matriz de compatibilidade e o custo de testes já
aprovados. A decisão está detalhada em `sources-and-decisions.md`.

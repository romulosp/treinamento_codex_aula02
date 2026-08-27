# Design: 004-parametrizar-nome-projeto-gerado

## Decisão

O nome será derivado pelo agente a partir do identificador da mudança. Para esta mudança, a implementação altera somente os campos de identidade e configuração explicitamente listados na SPEC.

## Componentes afetados

- `apps/backend/pom.xml`: `artifactId`.
- `apps/backend/src/main/resources/application.properties`: nome da aplicação, caminho OpenAPI e identificadores H2 de teste.
- `.github/prompts/executar-mudanca-spec-driven.prompt.md`: regra de substituição para gerações futuras.
- Teste de configuração para os critérios de aceite observáveis.

## Consequências

O nome público pode preservar hífens, enquanto o schema SQL usa sublinhados para manter compatibilidade com identificadores não delimitados.
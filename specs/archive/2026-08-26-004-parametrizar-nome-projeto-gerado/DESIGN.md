# Design: 004-parametrizar-nome-projeto-gerado

## Decisão

O nome será derivado pelo agente a partir do identificador da mudança. Para esta mudança, a implementação altera somente os campos de identidade e configuração explicitamente listados na SPEC.

O prompt será o orquestrador do ciclo de vida: identificará a primeira fase pendente, carregará a Skill correspondente e avançará apenas após o veredito exigido para o gate. Em reprovações ou bloqueios, informará a fase de retorno e não executará as fases posteriores.

## Componentes afetados

- `apps/backend/pom.xml`: `artifactId`.
- `apps/backend/src/main/resources/application.properties`: nome da aplicação, caminho OpenAPI e identificadores H2 de teste.
- `.github/prompts/executar-mudanca-spec-driven.prompt.md`: regra de substituição para gerações futuras.
- `.github/prompts/executar-mudanca-spec-driven.prompt.md`: orquestração automática das fases do processo Spec Driven.
- `apps/backend/start_aplicacao.bat`: inicialização local do Quarkus com Java 17.0.11 e Maven 3.8.8 configurados somente durante a execução do script.
- Teste de configuração para os critérios de aceite observáveis.

## Consequências

O nome público pode preservar hífens, enquanto o schema SQL usa sublinhados para manter compatibilidade com identificadores não delimitados.

O script de inicialização torna explícitas as versões locais requeridas pelo backend e evita modificar variáveis persistentes do Windows ao delimitar a configuração entre `setlocal` e `endlocal`.

A automação reduz a intervenção manual, mas não reduz os gates: cada fase continua registrando suas evidências e preservando suas restrições de alteração.
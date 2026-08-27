# Revisão da implementação: 004-parametrizar-nome-projeto-gerado

## Escopo revisado

- Configurações de nomenclatura do backend.
- Documentação do prompt para derivação de nomes e geração do script de inicialização.
- Script `start_aplicacao.bat` e teste automatizado de configuração.

## Conformidades verificadas

- `pom.xml` usa `artifactId` igual a `gerenciar-categorias`.
- `application.properties` usa o nome público, o caminho OpenAPI e o schema de teste `GERENCIAR_CATEGORIAS` aprovados, sem os placeholders definidos na SPEC.
- `start_aplicacao.bat` contém a configuração temporária de Java 17.0.11 e Maven 3.8.8, a exibição da versão Java, `mvn quarkus:dev`, `pause` e `endlocal`.
- `ConfiguracaoProjetoTest` cobre os valores de configuração e o conteúdo essencial do script.
- Não foram identificadas alterações indevidas no package Java, dependências Maven, versão Java, portas HTTP ou contratos dos endpoints.

## Achados

### IMP-REV-001 — Importante — Orquestração automática não pertence à SPEC aprovada

- Evidência: `.github/prompts/executar-mudanca-spec-driven.prompt.md` passou a exigir execução automática de todas as fases na mesma conversa, sem solicitações entre gates.
- Impacto: a mudança altera o comportamento do processo Spec Driven além dos requisitos funcionais 5 e 6, que se limitam à derivação de nomes e à geração do script `start_aplicacao.bat`. A implementação não pode ser aprovada com escopo ampliado sem decisão formal.
- Ação necessária: retornar à fase de especificação para registrar e revisar o requisito de orquestração automática em uma nova mudança ou por revisão explícita da SPEC 004. Não executar validação, aprovação ou commit até a regularização.

## Conclusão

`REPROVADA`

O fluxo automático foi interrompido. A primeira fase de retorno é a especificação.
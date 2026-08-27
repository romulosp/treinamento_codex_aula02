# Validação: 004-parametrizar-nome-projeto-gerado

## Ambiente

- Data e horário: 2026-08-26, término às 23:22:51 -03:00.
- Sistema operacional: Windows 11, arquitetura amd64.
- Java: Oracle Java 17.0.11, em `C:\Desenvolvimento\jdk-17.0.11`.
- Maven: 3.8.8, em `C:\Desenvolvimento\apache-maven-3.8.8`.
- Módulo validado: `apps/backend`.
- Perfil Quarkus: `test`; porta HTTP: `8083`.

## Comandos e códigos de saída


1. Após configurar temporariamente `JAVA_HOME`, `MAVEN_HOME` e `PATH`: `java -version`, `mvn -version` e `mvn test`, executados em `apps/backend` — código de saída `0`.
	- Resultado: Java 17.0.11 e Maven 3.8.8 confirmados; build Maven concluído com sucesso em 3 min 06 s.

## Cenários executados

- Verificar que `apps/backend/start_aplicacao.bat` existe e contém os comandos de configuração temporária, exibição de versão Java, inicialização `mvn quarkus:dev`, pausa e encerramento do escopo local definidos pela SPEC.
- Verificar que o prompt avança automaticamente entre fases aprovadas e interrompe com a fase de retorno diante de reprovação ou bloqueio.
- `ConfiguracaoProjetoTest` confirmou o `artifactId`, o nome da aplicação, o caminho OpenAPI, o schema H2, a ausência de placeholders, o conteúdo obrigatório do script e as regras de avanço e interrupção do prompt.
- `CategoriaResourceTest` e `QuarkusSmokeTest` continuaram aprovados após a parametrização.

## Evidências

### VAL-001 — Configurações, script e fluxo automático — Aprovada

- Procedimento: execução de `mvn test` no módulo `apps/backend` com Java 17.0.11 e Maven 3.8.8.
- Resultado observado: `ConfiguracaoProjetoTest`, `CategoriaResourceTest` e `QuarkusSmokeTest` foram executados; resultado consolidado `Tests run: 3, Failures: 0, Errors: 0, Skipped: 0`.
- Evidência: relatórios do Surefire em `apps/backend/target/surefire-reports/` e relatório JaCoCo em `apps/backend/target/site/jacoco/`.

## Veredito
`VALIDADA`
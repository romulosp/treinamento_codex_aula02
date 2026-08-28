# Tarefas: 001-criar-projeto-java

## Pré-condições

- [x] SPEC revisada e aprovada.
- [x] Incompatibilidade Java 11/Quarkus 3 registrada em ADR-001.
- [x] JDK 17 e Maven disponíveis no ambiente de validação.

## Implementação

- [x] Criar estrutura `apps/backend/<artifactId-sem-hifens>` dentro do contêiner `apps/backend`.
- [x] Criar `pom.xml` com coordenadas, propriedades, dependências e plugins definidos.
- [x] Criar configuração segura para DB2 e H2 de teste.
- [x] Criar teste de inicialização Quarkus.
- [x] Completar `application.properties` com parâmetros padrão, DB2, proxy, OpenAPI, SSL e perfil de teste fornecidos.
- [x] Configurar o espelho Maven padrão `NEXUS_INTERNO` no módulo backend.

## Revisão e validação

- [x] Revisar implementação contra `spec.md`.
- [x] Executar `mvn test` na pasta específica do projeto.
- [x] Registrar ambiente, resultado e evidências em `validation.md`.
- [x] Executar novamente `mvn test` após a configuração completa.
- [ ] Atualizar este checklist e `STATUS.md` após a validação.

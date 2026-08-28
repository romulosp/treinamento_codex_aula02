# Proposta: 014-suportar-db2-e-postgresql

## Status
`SPEC_APROVADA`

## Consolidação posterior

A mudança 016 substituiu o modelo originalmente proposto nesta mudança de empacotar DB2 e PostgreSQL simultaneamente e alterná-los por perfil em uma mesma API. Para encerramento, DB2 e PostgreSQL permanecem opções suportadas, mas a escolha passa a ocorrer na geração do projeto e produz exatamente um driver e uma configuração produtiva. A implementação e as evidências consolidadas estão na mudança 016.

## Responsável e data

- Responsável: equipe do projeto
- Data: 2026-08-27

## Referências

- `AGENTS.md`
- `specs/shared/process/workflow.md`
- `specs/shared/architecture/backend-java.md`
- `specs/shared/database/migration-rules.md`
- `specs/archive/2026-08-27-001-criar-projeto-java/`

## Problema e objetivo

A base documentada do backend prevê somente DB2 em produção. Quando um caso de uso precisar de PostgreSQL, a configuração e as dependências teriam de ser modificadas manualmente, com risco de remover a compatibilidade existente com DB2. Definir uma infraestrutura de persistência preparada para DB2 e PostgreSQL, na qual a API escolhe o banco no início da aplicação por configuração de ambiente, sem mudança de código do caso de uso.

## Escopo

- Definir os perfis Quarkus `db2` e `postgresql` para uma única fonte de dados ativa por execução.
- Definir as dependências JDBC e as propriedades de configuração isoladas por perfil.
- Preservar DB2 como alternativa suportada e manter H2 somente para testes que não requeiram compatibilidade específica do banco.
- Definir variáveis de ambiente, validações de inicialização, convenções de documentação e estratégia de testes para as duas opções.
- Planejar a atualização da documentação compartilhada de persistência e da configuração do módulo Maven/Quarkus quando existir ou for restaurado no workspace.

## Fora de escopo

- Implementar ou restaurar o módulo `apps/backend/`, alterar `pom.xml` ou `application.properties` nesta etapa documental.
- Selecionar banco de dados por endpoint, requisição HTTP, usuário ou durante a execução de uma mesma instância da aplicação.
- Usar simultaneamente DB2 e PostgreSQL na mesma instância, configurar múltiplos datasources, migrar dados ou alterar esquemas.
- Adicionar Flyway, Liquibase, credenciais versionadas ou funcionalidade de negócio.

## Impactos e riscos

- A implementação futura acrescentará o driver JDBC PostgreSQL e manterá o driver DB2; a distribuição poderá ter aumento de tamanho e exigirá avaliação de vulnerabilidades das versões escolhidas.
- A seleção por perfil ocorre antes da inicialização: trocar de banco exige reiniciar a aplicação com outro perfil e suas variáveis de ambiente.
- Dialetos SQL, tipos e migrations podem divergir entre bancos; funcionalidades que dependam dessas particularidades exigirão SPEC própria.
- O estado atual não possui `apps/backend/`; portanto, esta mudança entrega o contrato e o plano, sem executar testes de integração de banco.

## Critérios para aprovação da SPEC

- Os perfis, as variáveis de ambiente e o comportamento para perfil ausente ou inválido são verificáveis.
- DB2 e PostgreSQL permanecem alternativas explícitas, sem credenciais no repositório e sem seleção por endpoint.
- Dependências, configuração, testes e documentação compartilhada a alterar estão rastreáveis em tarefas verificáveis.

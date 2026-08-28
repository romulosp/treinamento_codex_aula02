# SPEC: 014-suportar-db2-e-postgresql

## Status
`SPEC_APROVADA`

## Precedência da consolidação

A SPEC 016, aprovada posteriormente, substitui os requisitos desta SPEC que determinavam drivers e perfis produtivos simultâneos no mesmo artefato. Para efeito de implementação, revisão, validação e encerramento desta mudança, os requisitos equivalentes passam a ser atendidos pelo catálogo de geração da 016: DB2 e PostgreSQL são escolhas mutuamente exclusivas no campo `bancoDados`, com um único driver, um único datasource produtivo e variáveis isoladas por saída gerada.

Os requisitos de não expor a escolha por endpoint, manter segredos externos, preservar H2 em testes e documentar as variáveis continuam vigentes e foram preservados pela implementação consolidada.

## Referências e dependências

- `specs/changes/014-suportar-db2-e-postgresql/proposal.md`
- `specs/shared/architecture/backend-java.md`
- `specs/shared/database/migration-rules.md`
- `specs/shared/testing/testing-strategy.md`
- `specs/archive/2026-08-27-001-criar-projeto-java/spec.md`

## Requisitos funcionais

1. O módulo Maven/Quarkus, quando presente, deve incluir os drivers JDBC DB2 e PostgreSQL compatíveis com a plataforma Quarkus adotada pelo projeto.
2. A aplicação deve iniciar com exatamente uma das opções de banco de produção: perfil `db2` ou perfil `postgresql`.
3. O perfil ativo deve ser selecionado antes da inicialização por `QUARKUS_PROFILE` (ou parâmetro equivalente suportado pelo Quarkus); não pode ser selecionado por endpoint, corpo HTTP, cabeçalho, usuário ou outra entrada de requisição.
4. O perfil `db2` deve ler exclusivamente `DB2_JDBC_URL`, `DB2_USERNAME` e `DB2_PASSWORD` para URL, usuário e senha do datasource.
5. O perfil `postgresql` deve ler exclusivamente `POSTGRESQL_JDBC_URL`, `POSTGRESQL_USERNAME` e `POSTGRESQL_PASSWORD` para URL, usuário e senha do datasource.
6. Cada perfil deve declarar explicitamente o tipo do banco e o dialeto Hibernate ORM correspondente, além de URL, credenciais e propriedades técnicas do datasource. Propriedades exclusivas de um banco não podem ser usadas pelo outro perfil.
7. A configuração efetiva deve falhar na inicialização com mensagem acionável se nenhum perfil de produção válido estiver ativo ou se faltar uma variável obrigatória do perfil selecionado.
8. O perfil de testes deve continuar independente de DB2 e PostgreSQL externos, usando H2 apenas quando o cenário não demandar semântica específica de um dos bancos de produção.
9. A documentação compartilhada de persistência deve declarar DB2 e PostgreSQL como opções suportadas, registrar as variáveis de ambiente e proibir segredos versionados.

## Requisitos não funcionais

1. Uma instância da aplicação deve manter apenas um datasource de produção ativo; não há múltiplos datasources nem comutação em tempo de execução nesta mudança.
2. Recursos REST, casos de uso e regras de domínio não podem conhecer qual perfil ou banco foi selecionado. A decisão pertence à infraestrutura e à configuração da aplicação.
3. URL, usuário e senha devem permanecer externos ao repositório, inclusive em exemplos, testes e documentação.
4. A solução deve preservar Java 17, Quarkus e a separação entre API, aplicação, domínio e infraestrutura definidos para o projeto.
5. A documentação deve estar em português do Brasil e em Markdown válido.

## Regras de negócio

1. A escolha do banco é uma decisão de implantação da API, não uma decisão do caso de uso que atende um endpoint.
2. `QUARKUS_PROFILE=db2` habilita somente a configuração DB2; `QUARKUS_PROFILE=postgresql` habilita somente a configuração PostgreSQL.
3. Alterar a opção de banco exige encerrar ou reiniciar a instância com o novo perfil e suas variáveis de ambiente completas.
4. Se a operação exigir SQL, tipos, índices, migrations ou comportamento específico de DB2 ou PostgreSQL, a mudança deve ser especificada e validada separadamente para os bancos afetados.

## Cenários e critérios de aceite

- [ ] Com `QUARKUS_PROFILE=db2` e as três variáveis `DB2_*` válidas, a aplicação inicia e expõe somente o datasource DB2.
- [ ] Com `QUARKUS_PROFILE=postgresql` e as três variáveis `POSTGRESQL_*` válidas, a aplicação inicia e expõe somente o datasource PostgreSQL.
- [ ] Com perfil de produção ausente, desconhecido ou com variável obrigatória ausente, a inicialização falha antes de atender requisições e informa a ação corretiva.
- [ ] Nenhum endpoint, DTO, caso de uso ou regra de domínio recebe parâmetro para escolher banco.
- [ ] Os testes comuns executam sem banco externo pelo perfil de teste; cenários dependentes de banco são executados para cada perfil afetado em ambiente controlado.
- [ ] A documentação compartilhada atualizada enumera DB2 e PostgreSQL, as variáveis obrigatórias, a exigência de reinicialização e a proibição de segredos versionados.

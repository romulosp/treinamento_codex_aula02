# SPEC: 015-adicionar-mysql-como-opcao

## Status
`SPEC_APROVADA`

## Precedência da consolidação

A SPEC 016, aprovada posteriormente, substitui os requisitos desta SPEC que exigiam `quarkus-jdbc-mysql` e o perfil `%mysql` simultaneamente aos demais bancos no mesmo artefato. Para o encerramento desta mudança, MySQL é atendido pela saída `bancoDados=MYSQL` da matriz de geração: um único driver produtivo, `db-kind=mysql`, variáveis `MYSQL_*`, dialeto e propriedades técnicas, sem fragmentos DB2 ou PostgreSQL.

Os requisitos de isolamento da API, segredos externos, H2 em testes comuns e inexistência de seleção por endpoint continuam vigentes e foram preservados pela implementação consolidada.

## Referências e dependências

- `specs/changes/015-adicionar-mysql-como-opcao/proposal.md`
- `specs/changes/014-suportar-db2-e-postgresql/spec.md`
- `specs/shared/database/migration-rules.md`
- `specs/shared/testing/testing-strategy.md`

## Requisitos funcionais

1. Quando o módulo Maven/Quarkus existir, ele deve incluir a extensão JDBC `quarkus-jdbc-mysql`, compatível com a versão Quarkus do módulo, além das extensões DB2 e PostgreSQL previstas na mudança 014.
2. A aplicação deve aceitar `QUARKUS_PROFILE=mysql` como terceira opção de produção e ativar somente o datasource MySQL nessa execução.
3. O perfil `mysql` deve ler exclusivamente `MYSQL_JDBC_URL`, `MYSQL_USERNAME` e `MYSQL_PASSWORD` como URL, usuário e senha.
4. O bloco `%mysql` deve declarar `quarkus.datasource.db-kind=mysql`, URL, credenciais, dialeto Hibernate ORM correspondente e propriedades técnicas exclusivas do MySQL.
5. A ausência de uma variável `MYSQL_*` obrigatória deve falhar na inicialização antes do atendimento de requisições, com mensagem acionável.
6. A documentação compartilhada deve listar DB2, PostgreSQL e MySQL, incluindo perfil e variáveis de ambiente de cada opção.

## Requisitos não funcionais

1. A seleção permanece exclusiva ao bootstrap: cada instância usa apenas um entre DB2, PostgreSQL e MySQL.
2. API, aplicação e domínio permanecem independentes da opção de banco.
3. Segredos nunca são versionados; H2 permanece limitado a testes que não dependam de semântica específica do MySQL.

## Regras de negócio

1. `QUARKUS_PROFILE=mysql` é uma decisão de implantação, não um parâmetro de endpoint ou caso de uso.
2. Trocar para ou sair de MySQL exige reiniciar a aplicação com o perfil e as variáveis completas.
3. Consultas, DDL, índices, tipos ou migrations com comportamento específico do MySQL exigem especificação e validação próprias para os bancos afetados.

## Cenários e critérios de aceite

- [ ] Com `QUARKUS_PROFILE=mysql` e `MYSQL_JDBC_URL`, `MYSQL_USERNAME` e `MYSQL_PASSWORD` válidas, a aplicação inicia com somente o datasource MySQL.
- [ ] Com perfil `mysql` e variável `MYSQL_*` obrigatória ausente, a inicialização falha de modo acionável antes de atender requisições.
- [ ] Os perfis `db2` e `postgresql` permanecem disponíveis e independentes das variáveis `MYSQL_*`.
- [ ] Nenhum endpoint ou caso de uso recebe a opção de banco como entrada.
- [ ] Testes comuns permanecem independentes de banco externo; testes dependentes de MySQL são executados em ambiente controlado quando disponível.

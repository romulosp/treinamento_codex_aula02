# SPEC: 016-renderizar-configuracao-banco-selecionado

## Status
`SPEC_APROVADA`

## Referências e dependências

- `specs/changes/016-renderizar-configuracao-banco-selecionado/proposal.md`
- `specs/changes/014-suportar-db2-e-postgresql/spec.md`
- `specs/changes/015-adicionar-mysql-como-opcao/spec.md`
- `specs/shared/database/migration-rules.md`
- `specs/shared/testing/testing-strategy.md`

## Requisitos funcionais

1. A geração da API deve aceitar o campo opcional `bancoDados`, com um único valor entre `DB2`, `POSTGRESQL` e `MYSQL`; quando o campo estiver ausente ou nulo, deve assumir `DB2`.
2. Para `bancoDados=DB2`, o `pom.xml` gerado deve conter `io.quarkus:quarkus-jdbc-db2` e não deve conter `quarkus-jdbc-postgresql` nem `quarkus-jdbc-mysql`.
3. Para `bancoDados=POSTGRESQL`, o `pom.xml` gerado deve conter `io.quarkus:quarkus-jdbc-postgresql` e não deve conter `quarkus-jdbc-db2` nem `quarkus-jdbc-mysql`.
4. Para `bancoDados=MYSQL`, o `pom.xml` gerado deve conter `io.quarkus:quarkus-jdbc-mysql` e não deve conter `quarkus-jdbc-db2` nem `quarkus-jdbc-postgresql`.
5. Dependências comuns da API e dependências de teste, inclusive H2 quando necessário ao perfil de teste, podem permanecer no `pom.xml`; elas não caracterizam driver produtivo selecionado.
6. Para `DB2`, `application.properties` deve renderizar somente a configuração produtiva DB2: `quarkus.datasource.db-kind=db2` e referências a `DB2_JDBC_URL`, `DB2_USERNAME` e `DB2_PASSWORD`.
7. Para `POSTGRESQL`, `application.properties` deve renderizar somente a configuração produtiva PostgreSQL: `quarkus.datasource.db-kind=postgresql` e referências a `POSTGRESQL_JDBC_URL`, `POSTGRESQL_USERNAME` e `POSTGRESQL_PASSWORD`.
8. Para `MYSQL`, `application.properties` deve renderizar somente a configuração produtiva MySQL: `quarkus.datasource.db-kind=mysql` e referências a `MYSQL_JDBC_URL`, `MYSQL_USERNAME` e `MYSQL_PASSWORD`.
9. A configuração produtiva renderizada deve incluir o dialeto Hibernate ORM e propriedades técnicas do datasource aplicáveis ao banco selecionado. Os valores específicos devem ser fornecidos por um catálogo de geração compatível com a versão Quarkus do projeto, sem segredos.
10. `application.properties` não deve conter propriedades, URLs, credenciais, prefixos de variável ou blocos de perfil dos outros bancos produtivos.
11. Valor fora do conjunto permitido para `bancoDados` deve interromper a geração com mensagem que informe os valores aceitos, sem gravar `pom.xml` ou `application.properties` parcialmente renderizados.

## Requisitos não funcionais

1. A decisão de banco ocorre exclusivamente na geração do projeto. A API gerada não expõe seleção de banco por REST nem comutação dinâmica após a inicialização.
2. Uma API gerada contém exatamente um datasource produtivo padrão e exatamente uma dependência JDBC produtiva correspondente.
3. URLs, usuários e senhas permanecem como referências a variáveis de ambiente; nenhum segredo é gravado em template, `pom.xml`, `application.properties`, teste ou documentação.
4. A geração deve manter Java 17, Quarkus e a arquitetura em camadas do projeto.
5. A documentação e as mensagens de erro devem estar em português do Brasil.

## Regras de negócio

1. `bancoDados` é uma escolha de infraestrutura da API a ser gerada; não pertence a DTO, endpoint, caso de uso, domínio ou entidade. Sua ausência significa a escolha padrão `DB2`.
2. A matriz de renderização é a única fonte de mapeamento entre o valor de `bancoDados`, a dependência JDBC, o `db-kind` e as variáveis de ambiente.
3. Regerar para outro banco substitui os artefatos de infraestrutura conforme a nova escolha; uma instância já em execução exige nova implantação para usar esses artefatos.
4. As mudanças 014 e 015 permanecem como registro das opções suportadas, mas esta SPEC substitui seu modelo de inclusão simultânea de drivers e perfis produtivos no artefato gerado.

## Cenários e critérios de aceite

- [ ] Gerar com `DB2` cria `pom.xml` com `quarkus-jdbc-db2` somente como driver produtivo e `application.properties` com `db-kind=db2` e referências exclusivamente `DB2_*`.
- [ ] Gerar com `POSTGRESQL` cria `pom.xml` com `quarkus-jdbc-postgresql` somente como driver produtivo e `application.properties` com `db-kind=postgresql` e referências exclusivamente `POSTGRESQL_*`.
- [ ] Gerar com `MYSQL` cria `pom.xml` com `quarkus-jdbc-mysql` somente como driver produtivo e `application.properties` com `db-kind=mysql` e referências exclusivamente `MYSQL_*`.
- [ ] Em cada uma das três gerações, os dois drivers produtivos não selecionados e suas propriedades ou variáveis não estão presentes nos artefatos gerados.
- [ ] Gerar sem `bancoDados` produz os mesmos fragmentos produtivos da geração explícita com `DB2`.
- [ ] Valor inválido para `bancoDados` falha antes de criar ou sobrescrever artefatos de infraestrutura.
- [ ] A API gerada possui um único datasource produtivo e nenhum endpoint oferece escolha de banco.

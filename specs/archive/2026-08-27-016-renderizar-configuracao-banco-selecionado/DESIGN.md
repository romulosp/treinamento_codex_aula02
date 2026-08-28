# Design: 016-renderizar-configuracao-banco-selecionado

## Contexto

O gerador recebe a definição da API antes de criar seus arquivos. A escolha de banco deve ser resolvida nesse ponto, usando um único catálogo declarativo. Assim, o projeto gerado não carrega drivers produtivos nem configuração de bancos que não utilizará.

## Referências

- `spec.md`
- `proposal.md`
- `specs/shared/database/migration-rules.md`
- `specs/changes/014-suportar-db2-e-postgresql/DESIGN.md`
- `specs/changes/015-adicionar-mysql-como-opcao/DESIGN.md`

## Decisões

1. Modelar `bancoDados` como enum opcional do contrato de geração: `DB2`, `POSTGRESQL` ou `MYSQL`; normalizar valor ausente ou nulo para `DB2` antes da validação e da renderização.
2. Centralizar em uma matriz o fragmento de dependência Maven, `db-kind`, nomes das variáveis de ambiente, dialeto e propriedades técnicas de cada opção.
3. Renderizar o fragmento selecionado nos templates de `pom.xml` e `application.properties`; não gerar todos os fragmentos com perfis Quarkus alternativos.
4. Validar o valor antes de escrever qualquer artefato. Em erro, a geração deve terminar sem resultado parcial.
5. Manter H2, quando previsto para testes, como dependência e configuração de teste separadas da escolha produtiva.

## Matriz de renderização

| `bancoDados` normalizado | Dependência produtiva Maven | `db-kind` | Variáveis de ambiente |
| --- | --- | --- | --- |
| `DB2` | `io.quarkus:quarkus-jdbc-db2` | `db2` | `DB2_JDBC_URL`, `DB2_USERNAME`, `DB2_PASSWORD` |
| `POSTGRESQL` | `io.quarkus:quarkus-jdbc-postgresql` | `postgresql` | `POSTGRESQL_JDBC_URL`, `POSTGRESQL_USERNAME`, `POSTGRESQL_PASSWORD` |
| `MYSQL` | `io.quarkus:quarkus-jdbc-mysql` | `mysql` | `MYSQL_JDBC_URL`, `MYSQL_USERNAME`, `MYSQL_PASSWORD` |

## Arquitetura e componentes

```text
Entrada de geração: bancoDados (ausente = DB2)
              |
              v
     validação + matriz única
              |
       +------+------+
       |             |
       v             v
template pom     template application.properties
       |             |
       +------> projeto gerado <------+
                   um driver e um datasource produtivos
```

## Alternativas e consequências

- Incluir os três drivers e alternar por perfil foi rejeitado: o artefato não reflete a escolha feita na geração e carrega dependências sem uso.
- Escolher por endpoint foi rejeitado: altera contrato da API e não resolve a configuração do driver no build.
- Duplicar condicionais independentes em cada template foi rejeitado: pode gerar divergência entre dependência e propriedades; a matriz única mantém o vínculo verificável.

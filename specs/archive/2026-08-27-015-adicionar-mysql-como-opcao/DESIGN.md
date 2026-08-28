# Design: 015-adicionar-mysql-como-opcao

## Contexto

MySQL estende o modelo de perfis exclusivos já definido na mudança 014. O Quarkus reconhece `mysql` como `db-kind` integrado e fornece a extensão JDBC `quarkus-jdbc-mysql`; a implementação futura deve usar a versão gerenciada pela plataforma do módulo.

## Consolidação posterior

O perfil `%mysql` histórico foi substituído pela matriz de geração da mudança 016. Ela preserva o isolamento de variáveis MySQL e evita carregar DB2 ou PostgreSQL na saída selecionada. Esta decisão tem precedência sobre o desenho de perfis simultâneos abaixo.

## Referências

- `spec.md`
- `specs/changes/014-suportar-db2-e-postgresql/DESIGN.md`
- `specs/shared/database/migration-rules.md`

## Decisões

1. Adicionar somente o bloco `%mysql` ao modelo de configuração, sem datasource nomeado adicional.
2. Isolar as variáveis MySQL com o prefixo `MYSQL_` para impedir uso cruzado de credenciais.
3. Preservar `db2`, `postgresql` e `test`; todos os perfis produtivos permanecem mutuamente exclusivos.

## Arquitetura e componentes

```text
QUARKUS_PROFILE=db2 | postgresql | mysql
                 |
                 v
       um único datasource padrão
                 |
                 v
      infraestrutura; API sem escolha de banco
```

## Exemplo normativo de configuração futura

```properties
%mysql.quarkus.datasource.db-kind=mysql
%mysql.quarkus.datasource.jdbc.url=${MYSQL_JDBC_URL}
%mysql.quarkus.datasource.username=${MYSQL_USERNAME}
%mysql.quarkus.datasource.password=${MYSQL_PASSWORD}
```

O dialeto Hibernate ORM e os parâmetros de pool devem ser confirmados contra a versão Quarkus e o ambiente reais durante a implementação.

## Alternativas e consequências

- Tratar MySQL como substituição de PostgreSQL ou DB2 foi rejeitado: as três opções devem permanecer disponíveis.
- Adicionar seleção por requisição foi rejeitado pelas mesmas razões da mudança 014: a escolha é de implantação e não de contrato HTTP.

# Design: 014-suportar-db2-e-postgresql

## Contexto

A configuração histórica usa DB2 como banco de produção e H2 em testes. A infraestrutura precisa suportar PostgreSQL sem expor essa escolha à API ou duplicar casos de uso. Perfis de configuração do Quarkus permitem empacotar os dois drivers e ativar somente uma configuração de datasource no bootstrap da aplicação.

## Consolidação posterior

O desenho de perfis simultâneos foi substituído pelo renderizador da mudança 016. A matriz de geração preserva as opções DB2 e PostgreSQL, mas evita dependências e propriedades não selecionadas no projeto gerado. Esta seção tem precedência sobre as decisões e exemplos históricos abaixo quando houver conflito.

## Referências

- `spec.md`
- `proposal.md`
- `specs/shared/architecture/backend-java.md`
- `specs/shared/database/migration-rules.md`
- `specs/shared/testing/testing-strategy.md`

## Decisões

1. Usar perfis Quarkus nomeados `db2` e `postgresql`, selecionados por `QUARKUS_PROFILE` antes do processo iniciar.
2. Manter um único datasource nomeado padrão por instância. Os dois drivers estarão disponíveis no artefato, mas somente o driver e as propriedades do perfil ativo configurarão o datasource em execução.
3. Manter as propriedades compartilhadas sem URL ou credenciais e declarar em blocos de perfil as propriedades `quarkus.datasource.db-kind`, `quarkus.datasource.jdbc.url`, `quarkus.datasource.username`, `quarkus.datasource.password` e o dialeto Hibernate ORM compatível.
4. Mapear variáveis de ambiente por banco, sem reutilização cruzada: `DB2_JDBC_URL`/`DB2_USERNAME`/`DB2_PASSWORD` e `POSTGRESQL_JDBC_URL`/`POSTGRESQL_USERNAME`/`POSTGRESQL_PASSWORD`.
5. Configurar o perfil padrão para não conectar a um banco de produção e falhar de modo explícito; equipes de implantação devem sempre fornecer um dos dois perfis produtivos.
6. Preservar H2 no perfil de teste. Testes de contrato e de regra de negócio usam H2 quando não dependem de particularidades de banco; compatibilidade de dialeto, DDL ou consulta nativa deve ser testada no banco correspondente.
7. Não introduzir Flyway nem outro mecanismo de migration. A decisão permanece sujeita às regras compartilhadas e a uma SPEC futura.

## Arquitetura e componentes

```text
Implantação
  QUARKUS_PROFILE=db2 | postgresql
        |
        v
application.properties
  %db2.*          %postgresql.*          %test.*
        |                 |                 |
        v                 v                 v
Datasource DB2     Datasource PostgreSQL   H2 para testes
        \                 /
         \               /
          v             v
    infraestrutura de persistência
                |
                v
  aplicação e API independentes do banco
```

## Exemplo normativo de configuração futura

```properties
# Sem perfil produtivo válido, a aplicação deve falhar antes de atender requisições.

%db2.quarkus.datasource.db-kind=db2
%db2.quarkus.datasource.jdbc.url=${DB2_JDBC_URL}
%db2.quarkus.datasource.username=${DB2_USERNAME}
%db2.quarkus.datasource.password=${DB2_PASSWORD}

%postgresql.quarkus.datasource.db-kind=postgresql
%postgresql.quarkus.datasource.jdbc.url=${POSTGRESQL_JDBC_URL}
%postgresql.quarkus.datasource.username=${POSTGRESQL_USERNAME}
%postgresql.quarkus.datasource.password=${POSTGRESQL_PASSWORD}
```

O exemplo é um contrato de nomes e isolamento. A implementação deve completar os dialetos Hibernate ORM e a forma suportada de validar variáveis ausentes para a versão Quarkus utilizada, sem gravar valores secretos.

## Alternativas e consequências

- Seleção do banco por endpoint foi rejeitada: um datasource é configurado no bootstrap e a escolha por requisição mistura responsabilidade de implantação com a API, além de aumentar o risco de vazamento de transações e dados.
- Dois datasources ativos foram rejeitados: não é requisito atual e exigiria roteamento, qualificação de repositórios, transações e testes adicionais.
- Substituir DB2 por PostgreSQL foi rejeitado: a compatibilidade DB2 deve ser mantida.
- Sobrescrever manualmente o mesmo bloco de `application.properties` foi rejeitado: gera configuração não reproduzível e perda da alternativa não selecionada.

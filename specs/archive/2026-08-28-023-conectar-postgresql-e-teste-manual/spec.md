# SPEC: 023-conectar-postgresql-e-teste-manual

## Status

`SPEC_APROVADA`

## Requisitos

1. O projeto deve conectar ao banco `tarefa` em `localhost:5432`, usuário `root`, por meio das variáveis de ambiente e nunca por credenciais hardcoded na aplicação.
2. O `start_aplicacao.bat` de `gerenciartarefas` deve entrar em `D:\desenvolvimento\banco_dados\postgresql`, executar o Compose existente (`docker compose up -d`, com fallback para `docker-compose up -d`), aguardar/validar o contêiner `postgres_db` e então executar `mvn quarkus:dev` a partir da pasta do projeto.
3. O script deve configurar para o processo da aplicação `POSTGRESQL_JDBC_URL=jdbc:postgresql://localhost:5432/tarefa`, `POSTGRESQL_USERNAME=root` e `POSTGRESQL_PASSWORD=root`, pois são credenciais locais de desenvolvimento fornecidas para este fluxo manual.
4. O script deve exibir instruções/URLs para o teste manual, incluindo Swagger e `/tarefas`, e falhar com mensagem clara se Docker, Compose, contêiner ou banco não estiverem disponíveis.
5. A aplicação deve usar `quarkus.hibernate-orm.database.generation=update` no perfil manual/produtivo, para criar estruturas ausentes e atualizar automaticamente o banco conforme as entidades JPA existentes.
6. A aplicação deve manter os dados existentes quando o Hibernate conseguir reconciliar a entidade e a tabela; não deve usar `drop-and-create` fora do perfil de teste. Alterações que o ORM não consiga inferir continuam sujeitas a erro de inicialização e correção no banco.
7. O perfil `%test` continuará usando H2 em memória e `drop-and-create`, sem depender do PostgreSQL.
8. O schema esperado permanece `tarefas(id, titulo, descricao, status, data_criacao, data_conclusao)`, conforme `019-gerenciar-tarefas`.

## Critérios de aceite

- [x] Com Docker e PostgreSQL disponíveis, o `.bat` inicia a API conectada ao banco `tarefa`.
- [x] Uma tarefa criada manualmente permanece disponível após reiniciar a aplicação.
- [x] Uma tabela ausente é criada/atualizada sem apagar registros existentes.
- [x] O teste automatizado continua passando usando H2.
- [x] Nenhum segredo de produção é incluído em arquivo versionado.
- [x] O `.bat` de categorias existente permanece compatível; a regra passa a valer para projetos com banco, com os valores locais definidos no fluxo manual aprovado.

# Proposta: 016-renderizar-configuracao-banco-selecionado

## Status
`SPEC_APROVADA`

## Responsável e data

- Responsável: equipe do projeto
- Data: 2026-08-27

## Referências

- `AGENTS.md`
- `specs/shared/process/workflow.md`
- `specs/shared/database/migration-rules.md`
- `specs/changes/014-suportar-db2-e-postgresql/`
- `specs/changes/015-adicionar-mysql-como-opcao/`

## Problema e objetivo

As mudanças 014 e 015 tornaram DB2, PostgreSQL e MySQL opções conhecidas, mas o modelo de perfis e de todos os drivers no mesmo artefato não atende ao gerador. Quando o banco é escolhido para uma API, o `pom.xml` e o `application.properties` gerados devem conter apenas a dependência e a configuração produtiva daquela escolha.

Definir a renderização condicional dos artefatos de infraestrutura a partir da opção de banco informada na geração da API.

## Escopo

- Definir a opção de geração `bancoDados`, com `DB2` como padrão e os valores explícitos `DB2`, `POSTGRESQL` e `MYSQL`.
- Definir a matriz que renderiza a dependência JDBC, as variáveis de ambiente e as propriedades do datasource para cada valor.
- Exigir que o `pom.xml` contenha somente o driver produtivo selecionado e que `application.properties` contenha somente o bloco produtivo correspondente.
- Atualizar a regra compartilhada de persistência e o plano de testes da geração.

## Fora de escopo

- Seleção de banco por endpoint, requisição, usuário ou durante a execução da API gerada.
- Múltiplos datasources, múltiplos drivers produtivos, migrations, alteração de esquema ou migração de dados.
- Valores de credenciais, URLs reais, parâmetros finais de pool ou dialetos específicos do ambiente.

## Impactos e riscos

- Para trocar de banco será necessário gerar uma nova configuração da API com a nova opção e implantá-la; não há troca dinâmica em uma instância em execução.
- Geração incompatível entre dependência e configuração pode impedir a inicialização. A matriz única e os testes de artefato reduzem esse risco.
- SQL e recursos particulares de um banco seguem exigindo SPEC própria.

## Critérios para aprovação da SPEC

- Cada valor permitido possui dependência, `db-kind`, variáveis e critérios de aceitação verificáveis.
- A opção inválida falha antes da gravação de artefatos incompletos e a opção ausente produz a geração DB2.
- Os artefatos gerados não incluem drivers ou blocos produtivos de bancos não selecionados.

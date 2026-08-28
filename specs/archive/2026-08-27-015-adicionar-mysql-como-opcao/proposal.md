# Proposta: 015-adicionar-mysql-como-opcao

## Status
`SPEC_APROVADA`

## Consolidação posterior

A mudança 016 substituiu a inclusão simultânea do driver e do perfil MySQL no artefato por uma escolha de geração exclusiva. MySQL permanece uma opção suportada; a implementação consolidada seleciona `MYSQL` em `bancoDados` e renderiza somente o driver e as variáveis desse banco.

## Responsável e data

- Responsável: equipe do projeto
- Data: 2026-08-27

## Referências

- `specs/changes/014-suportar-db2-e-postgresql/`
- `specs/shared/database/migration-rules.md`
- `specs/shared/process/workflow.md`

## Problema e objetivo

A mudança 014 prepara DB2 e PostgreSQL como opções de produção. Incluir MySQL como terceira alternativa, preservando a mesma arquitetura de um único datasource selecionado na inicialização.

## Escopo

- Adicionar o perfil Quarkus `mysql`, o driver JDBC correspondente e as variáveis de ambiente exclusivas.
- Atualizar a diretriz compartilhada de persistência, os critérios de testes e a documentação de execução futura.

## Fora de escopo

- Múltiplos datasources, seleção por endpoint, comutação em tempo de execução, migrations, alteração de esquema ou restauração do módulo backend.

## Impactos e riscos

- A implementação futura acrescentará uma dependência JDBC e exigirá validação de versão, pool e dialeto no ambiente MySQL.
- SQL, tipos e comportamento de MySQL podem divergir de DB2 e PostgreSQL; requisitos dependentes dessas diferenças continuam exigindo SPEC própria.

## Critérios para aprovação da SPEC

- Perfil, variáveis, dependência e cenários de aceitação MySQL são verificáveis.
- A alteração preserva as alternativas DB2 e PostgreSQL e não expõe a escolha na API.

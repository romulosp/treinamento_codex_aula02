# Tarefas: 015-adicionar-mysql-como-opcao

## Pré-condições

- [x] Aprovar proposta, SPEC, design e tarefas em revisão formal.
- [ ] Confirmar versão da extensão `quarkus-jdbc-mysql`, dialeto Hibernate ORM e parâmetros de pool compatíveis com o módulo real.

## Encerramento por consolidação

- [x] Registrar que a mudança 016 substitui a inclusão simultânea do perfil e driver MySQL.
- [x] Associar MySQL à matriz de geração exclusiva, com `bancoDados=MYSQL`.
- [x] Reutilizar o teste reproduzível da mudança 016 para a saída MySQL.

## Implementação documental e de infraestrutura

- [x] Atualizar a diretriz compartilhada de persistência com MySQL, perfil `mysql` e variáveis `MYSQL_*`.
- [ ] Quando `apps/backend/` existir, adicionar `quarkus-jdbc-mysql` ao `pom.xml` e preservar os drivers já previstos.
- [ ] Quando `apps/backend/` existir, adicionar o bloco `%mysql` a `application.properties` e validar perfil ou variáveis ausentes.
- [ ] Atualizar a documentação de execução do módulo, se existente, com placeholders seguros para MySQL.

## Testes, revisão e validação

- [ ] Criar ou atualizar testes de bootstrap para sucesso e falhas do perfil `mysql`.
- [ ] Executar testes comuns em H2 e, quando disponível, conectividade controlada com MySQL.
- [ ] Registrar revisão de implementação, validação e aprovação formal antes de arquivar a mudança.

## Status da implementação

`IMPLEMENTADA`

A implementação foi consolidada na mudança 016, que renderiza MySQL somente quando ele é a escolha de banco do projeto gerado.

## Tarefas substituídas

Os itens pendentes que exigiam MySQL simultâneo a DB2 e PostgreSQL não são executados porque foram revogados pela precedência da mudança 016. Os cenários equivalentes foram revisados, testados e validados na matriz de geração; esta mudança segue para aprovação e arquivamento por consolidação.

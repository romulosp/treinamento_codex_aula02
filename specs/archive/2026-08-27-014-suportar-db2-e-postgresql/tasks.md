# Tarefas: 014-suportar-db2-e-postgresql

## Pré-condições

- [x] Aprovar `proposal.md`, `spec.md`, `DESIGN.md` e este plano em revisão formal de SPEC.
- [ ] Confirmar as versões de driver compatíveis com a versão Quarkus efetivamente presente no módulo a ser alterado.
- [ ] Confirmar os parâmetros técnicos de conexão, pool e dialeto exigidos pelo ambiente de cada banco, sem registrar segredos.

## Encerramento por consolidação

- [x] Registrar que a mudança 016 substitui o modelo de drivers e perfis simultâneos deste contrato.
- [x] Associar DB2 e PostgreSQL à matriz de geração consolidada, com um único driver e datasource por projeto.
- [x] Reutilizar os testes reproduzíveis da mudança 016 para as saídas DB2 e PostgreSQL.

## Implementação documental e de infraestrutura

- [x] Atualizar `specs/shared/database/migration-rules.md` para registrar DB2 e PostgreSQL como opções de produção, suas variáveis de ambiente e a inexistência de migrations nesta mudança.
- [ ] Quando `apps/backend/` existir, adicionar o driver JDBC PostgreSQL e preservar o driver JDBC DB2 no `pom.xml`.
- [ ] Quando `apps/backend/` existir, configurar `application.properties` com perfis `db2`, `postgresql` e `test`, um único datasource produtivo ativo e validação de perfil/variáveis ausentes.
- [ ] Manter recursos REST, casos de uso, domínio e contratos HTTP sem referência à escolha do banco.
- [ ] Atualizar a documentação de execução do módulo, se existente, com os comandos de inicialização e as variáveis de cada perfil, usando apenas placeholders seguros.

## Testes, revisão e validação

- [ ] Criar ou atualizar testes de inicialização para `db2`, `postgresql`, perfil inválido e variáveis obrigatórias ausentes, usando configuração controlada e sem segredos.
- [ ] Executar testes comuns no perfil `test` sem banco externo e registrar o resultado.
- [ ] Executar, quando houver ambiente disponível, os cenários de conectividade de DB2 e PostgreSQL e registrar ambiente, comando, código de saída e evidências.
- [ ] Revisar a implementação contra a SPEC e registrar achados em `reviews/`.
- [ ] Validar a mudança, registrar evidências em `validation.md` e obter aprovação formal antes de atualizar o estado do sistema, arquivar ou criar commit.

## Status da implementação

`IMPLEMENTADA`

A implementação foi consolidada na mudança 016, que substitui os perfis simultâneos por renderização exclusiva na geração do projeto.

## Tarefas substituídas

Os itens pendentes que exigiam todos os drivers e perfis simultâneos não são executados porque foram revogados pela precedência da mudança 016. Os cenários equivalentes foram revisados, testados e validados na matriz de geração; esta mudança segue para aprovação e arquivamento por consolidação.

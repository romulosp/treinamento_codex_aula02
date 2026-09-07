# SPEC: concluir execução do Produto Base

## Status
`SPEC_APROVADA`

## Critérios de aceite
1. Maven compila e executa os nove testes existentes sem falhas.
2. Testes Quarkus/Rest Assured com H2 comprovam criação, listagem geral e paginada, alteração, exclusão e 404 para identificador inexistente.
3. Backend local responde na porta 1000 usando PostgreSQL e frontend 2000 encaminha chamadas JSON para /produtos.
4. Navegação HTML para /produtos entrega a interface, não JSON.
5. Testes e build frontend passam; evidências registram comandos e códigos reais. Sprint Review deixa de conter placeholders.

## Restrições
Preservar dados existentes. Testes manuais removem somente registros que criarem. Preservar convenção documental de versionamento e Java 17/Quarkus.

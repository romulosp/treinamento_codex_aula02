# Proposta: 10004-plugin-saque-cartao

**Autor:** Rômulo Penha

## Status
`SPEC_APROVADA`

## Objetivo
Entregar APK independente de saque por cartão, compilado para o contrato mínimo
`shared-api` 1.1 e sem dependência do host. O plugin permanece compatível com o
host 1.2 porque a capacidade `business-menu` aceita a mesma major e uma minor
requerida menor ou igual à minor do host.

## Escopo
Módulo, manifesto, descritor de serviço, item `Principal > Outros Serviços > Saque Cartão`, fluxo Compose local de cartão, senha e conclusão. A execução humana ocorre exclusivamente pelo `:app`: login, descoberta dinâmica, clique no item e renderização da View pelo host.

## Fora de escopo
Leitura física do cartão, autorização por roles não exposta pelo contrato atual, persistência, rede e transação financeira real.

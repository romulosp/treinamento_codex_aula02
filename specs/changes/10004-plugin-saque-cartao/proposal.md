# Proposta: plugin-saque-cartao

## Status
`SPEC_APROVADA`

## Objetivo
Entregar APK independente de saque por cartão, compatível com `shared-api` 1.1 e sem dependência do host.

## Escopo
Módulo, manifesto, descritor de serviço, item `Principal > Outros Serviços > Saque Cartão`, fluxo Compose local de cartão, senha e conclusão. A execução humana ocorre exclusivamente pelo `:app`: login, descoberta dinâmica, clique no item e renderização da View pelo host.

## Fora de escopo
Leitura física do cartão, autorização por roles não exposta pelo contrato atual, persistência, rede e transação financeira real.

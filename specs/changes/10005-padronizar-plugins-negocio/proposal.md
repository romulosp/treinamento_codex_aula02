# Proposta: 10005-padronizar-plugins-negocio

**Autor:** Rômulo Penha

## Status
`SPEC_APROVADA`

## Problema e objetivo

Os dois componentes de negócio existentes repetem contrato, manifesto, menu e necessidade de execução manual. O objetivo é publicar uma especificação compartilhada e um template para que novos plugins sejam construídos e testados da mesma forma.

## Escopo

Regras de módulo, dependências, contrato, discovery, Compose hospedado, segurança, testes, KDoc e evidências. O padrão proíbe launcher em qualquer variante de plugin.

## Fora de escopo

Refatorar plugins existentes, alterar o protocolo binário ou arquivar Changes de componentes.

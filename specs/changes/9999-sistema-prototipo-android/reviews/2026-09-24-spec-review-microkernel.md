# Revisão da SPEC: 9999-sistema-prototipo-android — adaptação microkernel

## Escopo

A revisão substitui somente requisitos que conflitam com a plataforma já
aprovada: módulo único, `minSdk = 26`, confirmação local e negócio no host.
Preserva catálogo neutro, acessibilidade, paisagem, ausência de ativos legados,
testes e menu demonstrativo.

## REV-001 — resolvido — conflito com Change 10000

Catálogo e menu passam a `:plugin-negocio`; o host não recebe regras de
negócio. A sessão SSO é pré-condição de apresentação e nenhum segredo cruza o
contrato de plugin.

## Decisão

`SPEC_APROVADA`

A revisão é compatível com a Change 10000 e está autorizada pelo solicitante.

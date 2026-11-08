# Proposta: saque com cartão como componente

## Status
`SPEC_APROVADA` — revisão técnica r3 em reviews/2026-10-30-spec-review-r3.md.

## Objetivo e escopo
Converter o APK da [Change 10004](../10004-plugin-saque-cartao/spec.md) em componente reutilizável, invocado por plugins de negócio por intermédio do host. Preservar a jornada visual e a integração vigente com o pinpad; remover a entrada própria no menu. A 10004 e as Changes posteriores permanecem referências históricas e funcionais.

A mudança inclui contrato compartilhado, descoberta segura, hospedagem da View, retorno e consumidor demonstrativo. Não inclui autorização financeira, alteração do protocolo Go, configuração de chaves ou operação física automática.


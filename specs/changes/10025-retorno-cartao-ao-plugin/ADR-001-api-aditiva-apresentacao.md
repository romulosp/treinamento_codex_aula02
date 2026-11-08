# ADR-001 — opções de apresentação fora do pedido monetário

Status: aceito na revisão da SPEC.

Adicionar campo no data class CardPaymentRequest mudaria métodos copy/default e ABI de consumidores já compilados. Decisão: manter o tipo intacto e criar CardPaymentOptions mais facetas 1.5 que herdam os contratos existentes. Chamadas 1.4 continuam disponíveis; chamadas com personalização exigem provedor 1.5, sem fallback silencioso. O custo é dois overloads explícitos no host/provedor; o benefício é negociação de capacidade por tipo e compatibilidade sem acoplamento à bilheteria.

CAPTURED permanece captura local, não aprovação. A decisão de concluir demonstração é do consumidor de ingressos, autorizada pelo usuário e acompanhada de aviso. Não muda classificação de EMV, protocolo, autorização ou proteção de dados.

# SPEC: 066-lib-pinpad-abecs-go — Comando TLR

## Status
`SPEC_APROVADA`

## Objetivo
Transmitir um lote de registros da tabela EMV depois de `TLI` aceitar o carregamento.

## Contrato
- Receber registros já validados e limitar a quantidade a `TLR_MAX_RECORDS`.
- Preservar ordem, separadores, tamanho e codificação definidos pelo manual.
- Executar somente depois de TLI com status `000`.
- Status `020` de TLI encerra o fluxo sem TLR; não enviar registros nesse caso.
- Registrar apenas identificadores de lote, quantidade e status; nunca dados sensíveis de cartão.

## Critérios de aceite
- [ ] Lote válido é aceito pelo pinpad real.
- [ ] Lote vazio, acima do limite ou inválido é rejeitado antes do envio.
- [ ] Falha em qualquer lote interrompe TLE e informa o índice do lote sem expor registros.
- [ ] Cancelamento e retransmissão respeitam contexto e protocolo.

## Referências
`spec.md` RF-012.5, `spec-command-tli.md`, `spec-command-tle.md`; manual ABECS v2.12.

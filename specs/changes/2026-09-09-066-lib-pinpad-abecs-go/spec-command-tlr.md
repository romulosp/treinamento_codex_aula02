# SPEC: 066-lib-pinpad-abecs-go — Comando TLR

Autor: Rômulo Penha

## Status
`SPEC_APROVADA`

## Objetivo
Transmitir um lote de registros da tabela EMV depois de `TLI` aceitar o carregamento.

## Contrato
- Receber registros já validados e limitar a quantidade a `TLR_MAX_RECORDS`.
- Preservar ordem, separadores, tamanho e codificação definidos pelo manual.
- Executar depois de TLI com status `000` ou `020`.
- Status `020` de TLI inicia a carga e exige o envio de TLR/TLE.
- Registrar apenas identificadores de lote, quantidade e status; nunca dados sensíveis de cartão.
- O rastro serial registra `SPE CMD=TLR`, `PP` e `RSP CMD=TLR STATUS=...`, mas
  o frame de dados TLR é integralmente substituído por
  `**REDACTED(<n> bytes)**`; bytes de controle isolados permanecem visíveis
  conforme `spec-logging.md`.

## Critérios de aceite
- [ ] Lote válido é aceito pelo pinpad real.
- [ ] Lote vazio, acima do limite ou inválido é rejeitado antes do envio.
- [ ] Falha em qualquer lote interrompe TLE e informa o índice do lote sem expor registros.
- [ ] Cancelamento e retransmissão respeitam contexto e protocolo.
- [ ] O rastro identifica cada `CMD=TLR` sem expor registros da tabela EMV.

## Referências
`spec.md` RF-012.5, `spec-logging.md`, `spec-command-tli.md`,
`spec-command-tle.md`; manual ABECS v2.12.


## Correção normativa de 2026-09-13

TLR contém NREC N2 e os registros concatenados sem delimitador; cada registro
começa por comprimento N3 e o bloco total tem no máximo 999 bytes. O comando é
permitido depois de TLI status 000 ou 020.

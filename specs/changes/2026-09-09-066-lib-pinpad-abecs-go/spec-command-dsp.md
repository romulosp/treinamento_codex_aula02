# SPEC: 066-lib-pinpad-abecs-go — Comando DSP

## Status
`RASCUNHO`

## Objetivo
Exibir duas linhas de texto no display do pinpad por meio do comando tipado `DSP`.

## Contrato
- Entrada: `line1` e `line2` conforme limite documentado pelo manual e pelo modelo do display.
- Builder deve aplicar padding/truncamento somente quando a regra estiver comprovada; não truncar silenciosamente dados fora do contrato.
- Enviar pela fila, aguardar resposta e mapear status ABECS.
- Resposta não deve ser tratada como transação ou capacidade de display.
- Registrar `SPE CMD=DSP`, `PP` e `RSP CMD=DSP STATUS=...`; texto deve ser avaliado quanto a dados sensíveis antes do log.

## Erros e cancelamento
Validar pinpad aberto, timeout, cancelamento, NAK, CRC inválido e status de erro. O serviço deve retornar erro tipado e manter o estado consistente.

## Critérios de aceite
- [ ] Texto é exibido em pinpad real compatível.
- [ ] Limites e caracteres inválidos são rejeitados ou tratados conforme manual.
- [ ] Resposta real é parseada sem perder bytes excedentes.
- [ ] Cancelamento não deixa operação BUSY permanentemente.
- [ ] Logs não expõem conteúdo sensível.

## Referências
`spec.md` RF-012.3/RF-013; manual ABECS v2.12.

# SPEC: 066-lib-pinpad-abecs-go — Comando OPN

Autor: Rômulo Penha

## Status
`SPEC_APROVADA`

## Objetivo
Definir a abertura lógica de comunicação ABECS após a porta serial estar aberta. A abertura do handle serial e o comando ABECS `OPN` são operações distintas e devem ser documentadas separadamente.

## Escopo
- Pacotes: `internal/application/service`, `internal/domain/command`, `internal/domain/parser` e `internal/infrastructure/logging`.
- Payload lógico clássico: `OPN`.
- `Open` deve controlar estados `CLOSED -> OPEN` e impedir dois workers de iniciar abertura concorrente.
- OPN seguro/RSA/AES deve possuir SPEC complementar própria; esta SPEC não autoriza criptografia inventada.

## Contrato
- Abrir a porta em 8/N/1 antes de enviar OPN.
- Validar ACK, resposta, status e timeout.
- Fechar a porta em falha de inicialização conforme política de ciclo de vida.
- Registrar `open`, `SPE CMD=OPN`, `PP` e `RSP CMD=OPN STATUS=...` sem dados sensíveis.
- Propagar cancelamento e erro de I/O com `%w`.

## Critérios de aceite
- [ ] Porta serial real é aberta com a configuração aprovada.
- [ ] OPN básico é aceito pelo pinpad real quando o perfil exigir o comando.
- [ ] Repetição de `Open` não envia OPN duplicado para uma sessão já aberta.
- [ ] Timeout, NAK e resposta inválida deixam o estado consistente.
- [ ] OPN seguro somente é validado pela SPEC de comunicação segura aprovada.

## Referências
`spec.md` RF-002, RF-007, RF-010 e `spec-logging.md`; manual ABECS v2.12.


## Correção normativa de 2026-09-13

O payload clássico é exatamente `OPN`. O OPN seguro substitui o clássico; não
se envia um OPN clássico antes da negociação segura.

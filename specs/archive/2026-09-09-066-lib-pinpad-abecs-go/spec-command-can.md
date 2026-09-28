# SPEC: 066-lib-pinpad-abecs-go — Comando CAN

Autor: Rômulo Penha

## Status
`SPEC_APROVADA`

## Objetivo
Definir o cancelamento ABECS em baixo nível, usado pela fachada e pelo transporte quando uma operação precisa ser interrompida. `CAN` não é uma transação nem uma resposta de negócio; é um controle de sessão/operação.

## Escopo
- Pacotes: `internal/application/service`, `internal/domain/protocol` e `internal/infrastructure/serial`.
- Entrada lógica: byte `PP_CAN = 0x18`.
- O comando deve passar pelo worker único, salvo cancelamento de emergência definido pelo protocolo.
- Não expor envio hexadecimal bruto ao consumidor.

## Contrato
- Verificar contexto antes do envio.
- Registrar `SPE` sem inventar `CMD` quando CAN for controle de baixo nível.
- Aguardar e interpretar `EOT`, timeout, `NAK` ou resposta inválida conforme manual.
- Cancelamento do contexto deve ser distinguível de timeout do protocolo.
- Não duplicar CAN quando o worker já tiver finalizado a operação.

## Segurança e logging
CAN não carrega dados de cartão. Ainda assim, o rastro deve respeitar o formato SPE/PP/RSP e nunca registrar dados da operação anterior junto com o cancelamento.

## Critérios de aceite
- [ ] CAN é transmitido pela porta serial real quando a operação exigir cancelamento.
- [ ] O pinpad físico responde com o comportamento previsto no manual e o serviço retorna o estado correto.
- [ ] Cancelamento repetido é idempotente e não corrompe a próxima operação.
- [ ] Timeout, EOT e erro de porta são distintos.
- [ ] Testes unitários cobrem framing/estado; teste de aceitação usa pinpad físico real.

## Referências
`spec.md` RF-004, RF-007, RF-010 e `spec-infra-serial-cancel.md`; manual ABECS v2.12.

## Recuperação após ausência de EOT — revisão de 2026-09-14

Três tentativas de CAN sem EOT esgotam o handshake normativo. A infraestrutura
de serviço deverá então executar a reconexão controlada definida em
`spec-infra-serial-cancel.md`; isso não cria uma quarta tentativa dentro do
mesmo handshake e não altera o frame CAN. A operação original não é repetida.

- [ ] O teste físico registra as três tentativas sem EOT e a nova abertura com
  CAN/EOT e OPN válidos.
- [ ] A falha da reconexão permanece distinguível da ausência original de EOT.

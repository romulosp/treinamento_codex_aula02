# Revisão da SPEC — Change 071

## Achados

### REV-071-001 — Separação entre transporte físico e scripted

- Resultado: conforme.
- O BAT remove `PINPAD_BRIDGE_TRANSPORT` e deixa explícito que o cenário é
  pinpad físico Windows.

### REV-071-002 — Configuração de log

- Resultado: conforme.
- O contrato define `PINPAD_LOG_FILE` no mesmo arquivo usado pelo Bridge e pelo
  menu desktop.

### REV-071-003 — Segurança operacional

- Resultado: conforme.
- O BAT não contém credenciais, dados de cartão ou chaves e não executa
  comandos ABECS automaticamente.

Não há achados bloqueantes.

## Veredito

`SPEC_APROVADA`

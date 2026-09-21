# Revisão da SPEC — plugin-login

## Achados

### REV-001 — Resolvido — fronteira de login no host

- Severidade: bloqueante.
- Evidência: a tela e o ViewModel de login estavam no módulo `:app`.
- Resultado: a SPEC exige migração integral para `:plugin-login` e host apenas
  como plataforma de capacidade.

### REV-002 — Resolvido — regra de autenticação não fornecida

- Severidade: importante.
- Evidência: não há backend nem conjunto de credenciais no escopo.
- Resultado: autenticação é local demonstrativa, limitada a usuário iniciado em
  `L` e senha não vazia; backend permanece fora de escopo.

## Conclusão

`SPEC_APROVADA`

O pedido autoriza implementação, geração e validação manual previstas nesta
Change, respeitando as decisões soberanas da Change 10000.

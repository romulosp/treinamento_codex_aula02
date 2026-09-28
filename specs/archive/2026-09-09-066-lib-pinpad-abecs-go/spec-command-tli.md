# SPEC: 066-lib-pinpad-abecs-go — Comando TLI

Autor: Rômulo Penha

## Status

`SPEC_APROVADA`

## Objetivo

Iniciar a carga de tabela EMV conforme a seção 3.6.1 do manual ABECS 2.12.

## Contrato

- O payload clássico é `TLI012 + ACQUIRER(N2) + VERSION(A10)`.
- O adquirente aceita `00` para todas as tabelas ou `01..99`.
- A versão possui exatamente dez caracteres do tipo A.
- Status `000` significa que a versão informada é igual à atual e o processo de
  carga foi iniciado.
- Status `020` significa que a versão é diferente da atual e o processo de
  carga também foi iniciado.
- Em ambos os status `000` e `020`, `LoadCompleteEMVTable` prossegue com TLR e
  TLE. Outros status interrompem o fluxo.
- A fila, retransmissão e logging seguem as SPECs transversais.

## Critérios de aceite

- [ ] Teste unitário compara o payload byte a byte.
- [ ] Adquirente e versão inválidos são rejeitados antes da escrita.
- [ ] Testes de serviço comprovam que 000 e 020 enviam TLR/TLE.
- [ ] Status diferente de 000/020 interrompe o fluxo.
- [ ] O comportamento físico é registrado separadamente com pinpad real.

## Referências

`spec-conformidade-abecs-v212.md`, `spec-command-tlr.md`,
`spec-command-tle.md` e manual ABECS 2.12, seção 3.6.1.

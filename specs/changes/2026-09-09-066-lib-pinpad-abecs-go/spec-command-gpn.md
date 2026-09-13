# SPEC: 066-lib-pinpad-abecs-go — Comando GPN

Autor: Rômulo Penha

## Status
`SPEC_APROVADA`

## Objetivo
Obter PIN block por chave de terminal ou DUKPT, convertendo as funções GPN do legado para uma fachada Go tipada e segura.

## Contrato
- Suportar somente métodos explicitamente aprovados: MK/WK e DUKPT.
- Validar índice de chave, KSN, PAN, método, mensagem e limites antes do envio.
- Parser deve validar tamanho e formato binário do PIN block e KSN sem transformar bytes inválidos em texto.
- A operação deve ser exclusiva por instância e cancelável.
- O método público não deve permitir comando hexadecimal arbitrário.

## Segurança e logging
GPN exige redaction integral de SPE/PP. Nunca registrar PAN, PIN, PIN block, KSN, WKENC, chave, mensagem sensível ou payload em hexadecimal. Erros devem conter somente contexto seguro.

## Critérios de aceite
- [ ] MK/WK e DUKPT são exercitados com pinpad físico e cartão de laboratório autorizado.
- [ ] Validações rejeitam entradas inválidas antes da serialização.
- [ ] Resposta binária é validada no tamanho correto.
- [ ] Nenhum dado sensível aparece em slog, tracer, teste ou fixture.
- [ ] Cancelamento e timeout não deixam a sessão ocupada.

## Referências
`spec.md` RF-012.8/RF-013, `spec-logging.md`; manual ABECS v2.12.


## Correção normativa de 2026-09-13

O bloco GPN contém método N1, índice N2, WKENC H32, PANLEN N2, PAN A19,
ENTRIES N1=1, mínimo N2, máximo N2 e mensagem S32. DUKPT usa WKENC zerado e não
recebe KSN de entrada. A resposta exige `GPN000036 + PINBLK(H16) + KSN(H20)`.

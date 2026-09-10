# SPEC: 066-lib-pinpad-abecs-go — Comando GOX

## Status
`RASCUNHO`

## Objetivo
Definir a obtenção do PIN block e do KSN de PIN conforme o modo seguro suportado pelo pinpad.

## Contrato
- GOX é distinto de GCX e GTK.
- O payload, método de chave, tamanho e formato binário devem ser confirmados no manual e no legado antes da implementação.
- O parser deve preservar bytes binários sem conversão textual destrutiva.
- O serviço deve exigir contexto e autorização do consumidor.

## Segurança
PIN block, KSN, PAN, WKENC, KSEC e chaves nunca aparecem em logs. Respostas de erro não podem incluir dump binário. A SPEC de logging deve redigir o payload completo.

## Critérios de aceite
- [ ] Payload e resposta são confirmados documentalmente.
- [ ] Resposta real de pinpad de laboratório é parseada nos tamanhos esperados.
- [ ] Nenhum material sensível aparece em SPE, PP, RSP, slog ou erro.
- [ ] Cancelamento e fechamento limpam o estado da operação.

## Referências
`spec-command-gcx.md`, `spec-command-gpn.md`, `spec-logging.md`; manual ABECS v2.12, seção de GOX.

# SPEC: 066-lib-pinpad-abecs-go — Comando MLI

## Status
`SPEC_APROVADA`

## Objetivo
Iniciar o carregamento de um arquivo multimídia para o armazenamento do pinpad.

## Contrato
- Entrada: nome, tipo e tamanho do arquivo conforme manual.
- Validar extensão/tipo permitido, tamanho e ausência de path traversal antes do envio.
- Iniciar a sequência `MLI → MLR* → MLE`; não considerar o arquivo disponível antes de `MLE`.
- Registrar somente metadados seguros: nome validado, tamanho e status. Nunca registrar bytes do arquivo.

## Critérios de aceite
- [ ] MLI real aceita arquivo de teste permitido no pinpad físico.
- [ ] Tipo, tamanho e nome inválidos são rejeitados antes do transporte.
- [ ] Falha de MLI não permite enviar MLR/MLE.
- [ ] Timeout, cancelamento, NAK e status ABECS são retornados tipados.

## Referências
`spec.md` RF-012.4/RF-013, `spec-command-mlr.md`, `spec-command-mle.md`; manual ABECS v2.12.

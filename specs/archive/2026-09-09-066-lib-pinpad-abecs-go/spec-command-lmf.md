# SPEC: 066-lib-pinpad-abecs-go — Comando LMF

## Status
`SPEC_APROVADA`

## Objetivo
Listar os nomes dos arquivos multimídia armazenados com sucesso no pinpad.

## Contrato
- Solicitação: payload literal `LMF`.
- Resposta: status ABECS e zero ou vários campos `PP_MFNAME` (tag `0x805E`),
  um para cada arquivo armazenado.
- Ausência de arquivos é sucesso e produz lista vazia.
- Os nomes são case-insensitive no dispositivo e a API/CLI os apresenta em
  maiúsculas, preservando a ordem recebida. Espaços à direita do campo A8 são
  preenchimento e são removidos na apresentação; espaços internos não são
  válidos.
- Um nome de resposta malformado é erro de protocolo; não deve ser descartado
  silenciosamente. A validação aceita o preenchimento publicado (`SIGNALS ` e
  `PRESTO  `) e valida os bytes restantes como ASCII alfanuméricos.
- O comando é serializado pela fila existente, respeita timeout/cancelamento e
  recupera a comunicação conforme `spec-infra-serial-cancel.md`.

## Critérios de aceite
- [ ] Builder gera exatamente `LMF`.
- [ ] Parser preserva zero, um e vários campos repetidos `PP_MFNAME`.
- [ ] Nomes válidos em minúsculas são devolvidos em maiúsculas; padding à direita
  é removido; tamanho/formato inválido produz erro.
- [ ] Status ABECS e timeout são propagados sem fabricar uma lista de sucesso.
- [ ] A opção do menu exibe todos os nomes ou indica que a lista está vazia.

## Referências
Manual ABECS v2.12, seções 3.4.4 e 6.6.4, p. 100 e 206;
`spec-conformidade-abecs-v212.md`, `spec-infra-serial-cancel.md`.

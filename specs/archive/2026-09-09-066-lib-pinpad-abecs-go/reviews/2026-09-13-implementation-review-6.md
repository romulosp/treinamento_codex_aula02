# Revisão da implementação — registro local de GTK em claro

**Data:** 2026-09-13

**Estado de entrada:** `IMPLEMENTADA`

## Escopo

Foram revisados o fluxo da opção 19, `Tracer.RecordGTKClearTracks`, os testes
do CLI e do tracer e as alterações aprovadas em `spec-command-gtk.md` e
`spec-logging.md`.

Esta revisão substitui, apenas quanto à persistência das trilhas em claro, a
conclusão de redaction integral registrada na revisão 5.

## Evidências da revisão

| Requisito | Resultado verificado |
| --- | --- |
| Registrar após GTK válido em claro | `runMenu` usa a resposta validada e chama `recordGTKResult` somente após sucesso |
| Conteúdo exigido | A linha contém `GTK_CLEAR`, `TRACK1`, `TRACK2` e `TRACK3`, inclusive quando vazios |
| Integridade do arquivo | `strconv.QuoteToASCII` delimita os valores e escapa aspas, controles e quebras de linha |
| Modo criptografado | `DataMethod` preenchido impede a emissão da linha clara |
| Rastro serial | `CommandGTK` permanece classificado como sensível; SPE/PP continuam redigidos |
| Material adicional | KSN, chave e PAN separado não são passados ao método de logging |
| Persistência | A escrita usa o mesmo mutex, destino, `Sync` e propagação de erro do tracer |

Os testes usam valores sintéticos e comprovam a presença da linha clara, os
campos vazios, o escaping e a ausência no modo DUKPT. Nenhuma divergência
material foi encontrada.

## Decisão

`IMPLEMENTACAO_APROVADA`

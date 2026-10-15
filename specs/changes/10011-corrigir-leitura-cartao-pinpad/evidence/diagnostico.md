# Diagnóstico — evidências inspecionadas

Data de referência: 2026-10-11. Windows/PowerShell; leitura de arquivos locais, sem operação financeira nem instalação de chave.

## Evidências confirmadas

| ID | Evidência | Implicação |
| --- | --- | --- |
| DIAG-01 | Imagem enviada pelo solicitante: resultado indeterminado, `PINPAD_ERROR`, fase `command`, aviso de encerramento não confirmado. | É necessário preservar erro principal e cleanup separadamente. Não informa qual comando falhou. |
| DIAG-02 | `apps/desktop/libpinpadabecsgo/logs/LogPinpadAbecs.txt`: 212.763 bytes na inspeção; última modificação 2026-10-11 23:55:49. Trecho COM7#005 termina com fechamento do adaptador e `session_released`, por volta de 23:54:30. Frames redigidos. | Liberação de transporte/ownership não identifica o comando ou status de leitura nem confirma fechamento do pinpad. Não há correlação suficiente para afirmar que este trecho causou a imagem. |
| DIAG-03 | `SaqueJourney.kt`, `begin()`: condição `type in setOf("03", "05")`; `capturePin` antes de `tracks` somente para `00`. | Roteamento confirmado divergente: 05 recebe GOX indevido e 06 não recebe GOX. Ordem diverge do novo pedido. |
| DIAG-04 | `internal/domain/command/constants.go`: 05 = CTLS simulando tarja, 06 = CTLS EMV, compra = 00h, saque = 01h. Manual primário páginas 42/136 confirma elegibilidade GOX. | Correção deve usar significado normativo e teste por modo. |
| DIAG-05 | `SaqueRepository.kt`: capture envia `transactionType = 1`; GOX envia U+0001; GPN usa índice literal 8 e PAN vazio; GOX usa método/índice/WK fixos. | Divergência com compra solicitada e fonte criptográfica externa. |
| DIAG-06 | Arquivo externo indicado tem exatamente `TIPO_CHAVE`, `INDICE_CHAVE`, `WORKING_KEY`; método 3/índice 08 e marcador de WK vazia. Só nomes, estrutura e ausência lógica de WK foram registrados. | Importar todos os campos e normalizar WK vazia; não há parâmetros DATA GTK na fonte. |
| DIAG-07 | `internal/application/service/service.go`, `purchaseGCX`: política limitada de mudança/repetição CTLS já existe. `BuildFilteredGCXCommand` preserva adquirente/APPTYPE ou AARR. | Reutilizar core e validar preservação dos filtros; não duplicar fallback no Kotlin. |
| DIAG-08 | `mobile/errors.go` converte StatusError para `PINPAD_ERROR:command`, mantendo causa só no Go. | Metadados seguros no core precisam distinguir comando/status antes de perder contexto. |
| DIAG-09 | Manual primário páginas 75/201–202: uso do PAN retido com PAN vazio depende de GTK ainda não ter sido executado. Parser GCX já mantém PAN/trilhas no modelo Go. | GPN pós-GTK precisa de PAN explícito interno e contrato aditivo de sessão. |

## Pesquisa e limites

Cópia oficial ABECS 2.12 (11-abr-2019) baixada para diretório temporário externo ao repositório e extraída com pypdf: 283 páginas, download de 2.683.330 bytes. O acesso inicial pelo navegador de pesquisa falhou; a leitura direta por HTTPS funcionou. As seções pertinentes foram conferidas, sem copiar páginas ou dados de exemplo para a Change. Não se declara edição normativa mais recente nem causa física comprovada.

Catálogo [Android Skills oficial](https://developer.android.com/tools/agents/android-skills/browse) consultado. Classificação `REJECT` para incorporação nesta Change: as candidatas apresentadas não definem ABECS/DUKPT nem resolvem o roteamento observado; arquitetura/gates locais já cobrem o escopo. Nenhuma skill externa instalada, copiada ou importada; nenhum upgrade recomendado.

Não foram acessados arquivos PRD. O arquivo de propriedades, tabela e log não foram alterados. Nenhum PAN, WK não vazia, trilha, PIN block, KSN ou frame foi copiado para evidência. A correção precisa de reprodução física futura para comprovar qual comando produziu o erro da imagem.

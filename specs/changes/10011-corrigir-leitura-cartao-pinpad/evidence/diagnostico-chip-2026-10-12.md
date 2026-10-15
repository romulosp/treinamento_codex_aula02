# Diagnóstico incremental — chip e contactless EMV

Inspeção em 2026-10-12, Windows/PowerShell. Escopo: revisar o novo trecho do log e o roteamento existente. Nenhuma implementação, operação no dispositivo ou instalação de chave foi executada. Os requisitos e o status da SPEC permanecem vigentes.

## Resultado

Chip EMV (`03`) e contactless EMV (`06`) devem continuar por GOX. A verificação do portador, incluindo PIN quando exigido pelas regras de CVM, pertence ao processamento EMV. GPN é usado no ramo sem EMV: tarja (`00`) ou contactless simulando tarja (`05`), conforme RF-002/RF-005 desta Change. A aproximação física, sozinha, não distingue `05` de `06`.

O solicitante informou que o erro surge na chamada, antes da entrada de senha, e reforçou que o PIN de chip/contactless EMV deve permanecer no GOX. A hipótese principal para o trecho analisado é uma interrupção em GTK, antes de GOX. O log redigido não permite confirmar o comando nem seu status por conteúdo; a identificação abaixo é uma inferência pelo código, sequência e comprimentos.

## Código confirmado

- `SaqueJourney.kt:159–170`: captura o tipo; chama GPN somente para `00`; executa GTK; depois chama GOX somente para `03`/`05`. O chip `03` já é encaminhado a GOX se GTK concluir. O contactless EMV `06` é omitido; `05` é encaminhado indevidamente. RF-002/RF-005 já exigem corrigir para `03`/`06`.
- `SaqueRepository.kt:146–157`: GPN usa chave PIN literal; GTK usa método DATA `30` e `profile.gtkKeyIndex`; GOX usa método PIN `3` e índice literal. O índice DATA é independente do PIN. A presença de chave PIN não comprova a disponibilidade da chave DATA exigida por GTK.
- `SaqueJourney.kt:267–270` e `mobile/client.go:203–213`: cleanup chama `cancel` mesmo quando a chamada anterior já terminou. `Client.run` remove a operação ao retornar; `Cancel` sem operação retorna erro. Assim, cleanup pode reportar falha mesmo quando o `close` subsequente funciona. A confirmação no dispositivo continua necessária.

Os hashes dos fontes `internal/domain/command/advanced_contracts.go`, `internal/application/service/service.go`, `mobile/catalog.go` e `mobile/journey.go` coincidem com `sourceHashes` da proveniência AAR local. Isso vincula esses fontes ao artefato registrado; não comprova qual APK estava instalado durante o teste.

## Novo trecho do log

Fonte: `apps/desktop/libpinpadabecsgo/logs/LogPinpadAbecs.txt`, sessão serial `COM7#001`, 2026-10-12, linhas 1885–1898.

| Hora local | Evidência visível | Interpretação limitada |
| --- | --- | --- |
| 21:20:00.323 | Envio de 80 bytes; ACK; resposta em fragmentos de 1 e 179 bytes após aproximadamente 6 segundos. | Compatível com GCX filtrado e leitura do cartão. O tipo retornado está mascarado. |
| 21:20:06.435 | Envio de 30 bytes; ACK; resposta de 10 bytes. | Compatível com GTK do adapter e resposta sem bloco de dados. O status N3 está mascarado. |
| 21:20:06.587 | Envio de 42 bytes; ACK; resposta em fragmentos de 1 e 9 bytes. | Compatível com CLO de encerramento; status não observável. |
| 21:20:06.881–.882 | Fechamento serial e liberação da sessão Bridge. | Confirma liberação do transporte, não o resultado do comando no dispositivo. |

Depois do envio de 30 bytes há apenas o envio de 42 bytes e a liberação. Não há envio compatível com o GOX atual, que mede 63 bytes com timeout sintético de 30 segundos e os parâmetros do adapter. Timeout igual a um byte sujeito a escape pode aumentar esse comprimento. Tamanho de frame não identifica um comando de forma única.

O manual, páginas 84–86, permite GTK após GCX e lista falhas de chamada, chave ausente e índice inválido. Essas são hipóteses a distinguir pelo status, não diagnósticos confirmados. As páginas 136–138 definem GOX para `03`/`06`, os parâmetros de PIN online e os resultados de verificação do portador. A página 202 determina que GTK não deve afetar o contexto dos kernels EMV. Portanto, o encadeamento GCX → GTK → GOX previsto na SPEC é possível; uma falha em GTK interrompe a jornada atual antes de GOX.

Foi usada a cópia PDF local indicada pelo solicitante, com 283 páginas, extraída com pypdf 6.18.0. Não se afirma que seja a edição normativa mais recente.

## Verificação reproduzível

Ambiente: Go 1.26.5 windows/386; Python 3.14.6; pypdf 6.18.0. Códigos de saída das verificações abaixo: 0.

Na biblioteca: `go test ./internal/domain/command ./internal/domain/parser ./internal/application/service ./mobile` passou nos quatro pacotes, com resultados do cache. É baseline; não comprova implementação de RF-002/RF-005 nem homologação física.

Cálculo estrutural executado com Python, valores sintéticos, formato TLV dos builders e framing de `internal/domain/protocol/protocol.go`: header de 6 bytes, 4 bytes de framing e um byte adicional por ocorrência de `13h`, `16h` ou `17h` no payload. Resultados: GCX 80; GTK 30; GOX 63; CLO 42. Para GTK, o cálculo é `6 + (4+2) + (4+4) + (4+2) + 4 = 30`, com método `30`, seleção `0111` e índice N2. Não houve tentativa de recuperar dados redigidos.

## Consequência para a implementação

Aplicar o roteamento já aprovado `03/06 → GOX` e `00/05 → GPN`, preservando GTK e o contexto privado exigido pela SPEC. Registrar diagnóstico sanitizado com comando e status N3 no core antes da conversão para `PINPAD_ERROR:command`, para distinguir a falha anterior de uma rejeição real em GOX. Conferir separadamente o provisionamento DATA de GTK e o PIN de GOX. A causa física e o comportamento de CVM exigem reprodução após a implementação; o valor acima de um limite não garante isoladamente uma solicitação de PIN em todo cartão/kernel.

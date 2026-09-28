# Revisão da implementação — conformidade ABECS 2.12

**Data:** 2026-09-13

**Estado de entrada:** `IMPLEMENTADA`

**Fonte normativa:** *Pinpad Abecs — Protocolo de Comunicação e Funcionamento*,
versão 2.12, 11-abr-2019.

**Escopo:** `apps/desktop/libpinpadabecsgo/` e os contratos aprovados da Change
066.

## Método

A revisão comparou os achados `IMP-REV-016` a `IMP-REV-026`, cada builder,
parser e fluxo serial implementado, os testes unitários e a SPEC aprovada
`spec-conformidade-abecs-v212.md`. Os vetores publicados pelo manual foram
usados diretamente quando disponíveis. Testes que apenas repetiam o código não
foram considerados oráculo normativo.

## Evidências automatizadas

| Verificação | Código | Resultado |
| --- | ---: | --- |
| `go test ./... -count=1` | 0 | Todos os 16 pacotes aprovados sem cache. |
| `go vet ./...` | 0 | Nenhum diagnóstico. |
| `go test ./... -coverprofile=coverage-abecs212-final -count=1` | 0 | Perfil gerado; cobertura total de **81,6%**. |
| `go build -o .\bin\libpinpadabecsgo.exe .\cmd\libpinpadabecsgo` | 0 | Build Windows aprovado. |
| `GOOS=linux GOARCH=amd64 go build ./...` | 0 | Build Linux aprovado. |
| `govulncheck ./...` | 0 | Zero vulnerabilidades alcançáveis; vulnerabilidades indiretas não são chamadas pelo módulo. |
| `go test -race ./... -count=1` | 1 | Limitação objetiva: `-race is not supported on windows/386`. |
| `golint ./...` | 0 | Nenhum comentário exportado ausente; somente sugestões de nomenclatura preservada por compatibilidade. |
| `git diff --check` | 0 | Nenhum erro de whitespace. |

Ambiente: Windows, `go1.26.5`, `GOOS=windows`, `GOARCH=386`, PowerShell.

## Matriz de rastreabilidade

| Fluxo | Manual | Implementação principal | SPEC e evidência unitária | Revisão | Hardware |
| --- | --- | --- | --- | --- | --- |
| Enlace, CRC e substitution | 2.2.1–2.2.2, pp. 17–22 | `domain/protocol`, `Service.exchangeCommandWithTracePolicy` | SPEC de conformidade; ACK/NAK, três tentativas, CRC inválido, ausência de ACK de resposta válida e limites | Conforme | Pendente |
| CAN/EOT | 2.2.2.3, pp. 21–22 | `Service.cancelHandshake`, `Open`, `Reset` | bytes concorrentes ignorados, prazo de 2 s e nova tentativa | Conforme | Pendente |
| OPN/CLO/CLX e seguro | 3.2.1, 3.2.2, 3.2.6 e 5.2.2 | `Service.Open`, `OpenSecure`, `Close`, `CloseVisual`, `domain/protocol/secure.go` | SPECs OPN/CLO/CLX/seguro; vetor AES da p. 170 | Conforme | Pendente |
| GIX | 3.2.4, pp. 39–43 | parser GIX e `GetDisplayCapabilities` | mapa PP_MFSUP, LLLLCCCC e status 051 | Conforme | Open/GIX/Close já exercitado; matriz completa pendente |
| DSP/DEX | 3.3.7–3.3.8, pp. 70–71 | builders e fachada de display | vetores publicados, Latin-1 e limites S16/S160 | Conforme | Pendente |
| MNU | 3.3.13, pp. 89–90 | `BuildMNUCommand`, `ParseMNUResponse` | vetor publicado, 1..20 opções, S24, timeout X1 e PP_VALUE N2 | Conforme | Seleção física observada; repetição final pendente |
| GKY | 3.3.10, p. 80 | builder literal e parser de status | todos os status 000, 004..008 e 013 | Conforme | Pendente |
| GPN | 3.3.11, pp. 81–83 | builder/parser posicional e fachada redigida | vetor publicado da p. 83, MK/WK e DUKPT | Conforme | Pendente |
| GTK | 3.3.12, pp. 84–88 | modelo, builder e parser próprios | vetor publicado da p. 86; métodos claro, MK/WK, DUKPT e RSA | Conforme | Pendente |
| MLI/MLR/MLE/DSI | 3.4.1–3.4.6, pp. 93–102 | builders e `SendMultimediaFile` | vetores publicados, CRC16, assinaturas PNG/JPEG/GIF e SPE_DATAIN até 995 | Conforme | Pendente |
| TLI/TLR/TLE | 3.5.2–3.5.4, pp. 106–110 | builders e `LoadCompleteEMVTable` | vetor TLI; status 000/020; TLR NREC e particionamento por quantidade/tamanho | Conforme | Pendente |
| GCX | 3.7.1, pp. 129–138 | builder, parser e `PurchaseGCX` | AAMMDD, GCXOPT, campos condicionais e retentativas CTLS | Conforme | Nova execução completa pendente |
| GOX | 3.7.2, pp. 138–140 | modelo, builder, parser e fachada próprios | vetor publicado da p. 139, campos condicionais e BER-TLV | Conforme | Pendente |
| FCX | 3.7.4, pp. 141–142 | modelo, builder, parser e fachada próprios | vetor publicado da p. 141, bits RUF, PP_FCXRES e Issuer Script Results | Conforme | Pendente |
| Logging/redaction | SPEC transversal | tracer, adaptador serial e fachada | SPE/PP/RSP, falhas persistidas e dados sensíveis redigidos | Conforme | Open/GIX/Close já exercitado |
| RST | ausente no manual 2.12 | removido | SPEC formal de exclusão | Conforme | Não aplicável |

No TLR, a página 107 limita `CMD_LEN1` a `999`. O PKTDATA máximo desse comando
é `TLR` (3) + `CMD_LEN1` (3) + corpo (999) = **1005 bytes**, dentro do limite
legado de 1024 bytes. A fachada agora divide também pelo tamanho e não apenas
por `NREC`.

## Resolução dos achados anteriores

| Achado | Resolução verificada |
| --- | --- |
| IMP-REV-016 | SPECs corrigidas e novamente aprovadas em 2026-09-13. |
| IMP-REV-017 | Máquina de enlace possui ACK/NAK, três tentativas, prazos e retransmissão da resposta. |
| IMP-REV-018 | Open e cancelamento usam CAN/EOT com três tentativas. |
| IMP-REV-019 | OPN clássico/seguro, CLO S32 e proteção de CLO/CLX foram corrigidos. |
| IMP-REV-020 | GKY e GPN seguem os layouts clássicos; RST foi removido. |
| IMP-REV-021 | Multimídia usa SPE_MFNAME, SPE_MFINFO, SPE_DATAIN e CRC16. |
| IMP-REV-022 | TLI/TLR/TLE e a continuação após 000/020 foram corrigidos. |
| IMP-REV-023 | MNU, GTK, GCX, GOX e FCX validam formatos e condições de resposta. |
| IMP-REV-024 | GIX e o catálogo de status foram alinhados ao manual. |
| IMP-REV-025 | Falsos oráculos foram substituídos por vetores e limites normativos. |
| IMP-REV-026 | QR Code só informa posição não suportada quando a posição é diferente de zero. |

## Documentação, escopo e segurança

Todos os 16 pacotes possuem comentário de pacote. A inspeção automatizada não
encontrou função, método, tipo, constante ou variável exportada sem comentário.
Não há servidor HTTP/WebSocket, comando hexadecimal bruto público, segredo
persistido ou dado sensível novo em fixture. Os helpers genéricos internos sem
uso e sem contrato ABECS foram removidos para não ampliar a superfície de API.

Os avisos remanescentes do `golint` sugerem CamelCase para símbolos que espelham
nomes normativos (`PP_SYN`, `PP_DC2` etc.) e nomes menores para tipos internos.
Eles não representam erro funcional ou documental e foram mantidos para evitar
uma alteração de API sem benefício para esta correção.

## Achados desta revisão

Nenhuma divergência material nova foi encontrada no escopo automatizado.

A validação em pinpad físico continua necessária para afirmar interoperabilidade
real, comportamento de cartões e interação do operador. Essa pendência pertence
à fase de validação e não invalida a aderência estática e unitária comprovada.

## Decisão

`IMPLEMENTACAO_APROVADA`

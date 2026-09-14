# Validação — 066-lib-pinpad-abecs-go

Autor: Rômulo Penha

**Data da última evidência executada:** 2026-09-13

**Estado desta evidência:** `PENDENTE_VALIDACAO_FISICA`

**Fase atual da Change:** `IMPLEMENTACAO_APROVADA`, com validação automatizada
aprovada e matriz física integral pendente.

> As seções anteriores a “Conformidade unitária ABECS 2.12” preservam o
> histórico. A seção de 2026-09-13 contém o resultado automatizado vigente.

## Ambiente

- A biblioteca foi projetada para operar em múltiplas plataformas e arquiteturas, não somente em Windows: suporta ambientes Windows 32/64 bits, Linux e macOS, incluindo CPUs ARM e x86, conforme a compatibilidade do runtime Go e do adaptador serial utilizado.
- O uso de Go é intencional e estratégico porque oferece compilação nativa para essas plataformas e simplifica a manutenção da biblioteca em diferentes arquiteturas, sem depender de uma única runtime específica.
- Ambiente desta execução: Windows `windows/386`; Go `go1.26.5 windows/386`.
- Pinpad físico e porta serial real: indisponíveis nesta execução.
- Sonar/scanner e `govulncheck`: indisponíveis no módulo; aplicada Auditoria de Qualidade Assistida por LLM.
- Docker Desktop não está em execução. O WSL `docker-desktop` não possui Go instalado, portanto não oferece alternativa para `-race`.

## Comandos e resultados

| Comando | Código | Resultado |
| --- | ---: | --- |
| `gofmt -w ...` | 0 | Formatação concluída. |
| `go vet ./...` | 0 | Aprovado. |
| `go test ./...` | 0 | Todos os testes passaram. |
| `go test ./... -coverprofile=coverage` | 0 | Perfil de cobertura gerado. |
| `go tool cover -func=coverage` | 0 | Cobertura total aferida em **81,7%**. |
| `go build ./...` | 0 | Build aprovado. |
| `go build -o libpinpadabecsgo.exe ./cmd/libpinpadabecsgo` | 0 | Executável Windows compilado no diretório do módulo. |
| `go test -race ./...` | 2 | Não suportado em `windows/386`. |
| `docker run ... go test -race ./...` | 1 | Docker Desktop não estava em execução. |
| `wsl -d docker-desktop -- go version` | 127 | Distribuição sem Go instalado. |
| `go test ./...` após ampliação do menu local | 0 | Menu e todos os pacotes aprovados. |
| `go vet ./...` após ampliação do menu local | 0 | Aprovado. |
| `go build -o libpinpadabecsgo.exe ./cmd/libpinpadabecsgo` após ampliação do menu local | 0 | Executável atualizado. |

## Evidências VAL

- **VAL-001:** a cobertura mínima de 80% foi atendida com 81,7%.
- **VAL-002:** testes adicionais cobrem CLI local, falhas de transporte serial, escrita parcial, cópias defensivas, sessão expirada, cancelamento, resposta inválida e limites de byte stream.
- **VAL-003:** a execução de `-race` foi tentada nos ambientes disponíveis; não há ambiente compatível ativo para essa verificação.
- **VAL-004:** a validação física de comunicação, status, timeout, cancelamento e redaction continua pendente de pinpad e porta serial reais.

## Auditoria de segurança

Escopo inspecionado: módulo Go, configuração por ambiente, adaptador serial, framing, parser, fila, sessão, logging e dependência `go.bug.st/serial`.

- Nenhum segredo confirmado foi encontrado pela busca estática.
- Não foram encontrados listener de rede, API HTTP, execução de comando do sistema, SQL ou renderização HTML no módulo.
- O log aplica redaction a payloads de GCX, GTK, GOX, FCX e GPN.
- Autenticação, autorização, tenant e IDOR são não aplicáveis: a entrega é uma biblioteca local sem servidor ou identidade de usuário.

O gerador foi atualizado com a opção `--change-066`, destinada a criar `docs/security-audit/relatorio-066-lib-pinpad-abecs-go.pdf`. O PDF ainda não foi produzido porque a skill `pdf` exige a execução prévia do utilitário `container_tools/mark_artifact_operation_started.mjs`, ausente neste ambiente. O relatório histórico da Change 010 não foi reutilizado.

## Validação documental da revisão de logging — 2026-09-12

Ambiente: Windows, PowerShell, diretório
`D:\desenvolvimento\ia\lib-pinpad-abecs`.

| Comando | Código | Resultado |
| --- | ---: | --- |
| Bloco PowerShell com `Test-Path`, verificação de whitespace, tokens obrigatórios SPE/PP, caminho canônico, RF-L010/CA-L017 e comparação dos comandos de `internal/domain/command/commands.go` com `spec-logging.md` | 0 | PASS: 14 artefatos, 22 comandos e exemplos SPE/PP verificados. |
| `git diff --check -- <artefatos documentais alterados>` | 0 | Nenhum erro de whitespace; somente avisos informativos de futura normalização LF para CRLF. |

Testes Go não foram executados nesta etapa porque a solicitação alterou somente
o contrato documental. Eles voltam a ser obrigatórios após a implementação dos
itens pendentes de logging, junto dos cenários CA-L001 a CA-L017.

## Conclusão

Esta conclusão histórica foi superada pela implementação registrada na seção
seguinte. O fluxo físico GIX passou a ter evidência; a matriz física dos demais
comandos e a geração do PDF atual de segurança permanecem pendentes. Não
avançar para aprovação, arquivamento ou commit antes das etapas independentes.

## Evidências da implementação do logging — 2026-09-12

Esta seção registra evidências produzidas durante a implementação. Ela não
substitui a revisão independente nem altera o estado para `VALIDADA`.

Ambiente: Windows `windows/386`, Go `go1.26.5`, PowerShell, módulo em
`D:\desenvolvimento\ia\lib-pinpad-abecs\apps\desktop\libpinpadabecsgo` e
pinpad físico PPP100 na `COM7`, baud rate 19200.

| Comando | Código | Resultado |
| --- | ---: | --- |
| `gofmt -w <arquivos Go alterados>` | 0 | Formatação concluída. |
| `go test ./...` | 0 | Todos os pacotes aprovados. |
| `go vet ./...` | 0 | Aprovado. |
| `go test ./... -coverprofile=coverage` | 0 | Testes e perfil concluídos. |
| `go tool cover -func=coverage` | 0 | Cobertura total **80,7%**; logging **89,5%** e serial **82,4%**. |
| `go build -o .\bin\libpinpadabecsgo.exe .\cmd\libpinpadabecsgo` | 0 | Executável Windows usado pelo launcher recompilado. |
| `GOOS=linux GOARCH=amd64 go build ./...` | 0 | Build Linux amd64 aprovado. |
| `go test -race ./...` | 1 | Limitação objetiva: `-race is not supported on windows/386`. |
| `go test -coverprofile=coverage.out ./...` | 1 | O PowerShell/Go 1.26 separou `.out` como pacote; repetido com o nome simples `coverage`, com código 0. |
| `executar projeto.bat`, opção `0` | 0 | Imprimiu o caminho absoluto, entrou no menu e aumentou o arquivo canônico de 5.299 para 5.500 bytes antes do encerramento. |
| `executar projeto.bat`, sequência `Open → GIX → Close → Sair` | 0 | GIX retornou status `000`; o arquivo canônico aumentou de 5.500 para 9.878 bytes. O CLO respondeu status `011`, mas o fechamento físico foi executado e registrado. |
| `executar projeto.bat`, binário final, sequência `Open → GIX → Sair` | 0 | GIX retornou status `000`; o arquivo cresceu de 9.878 para 13.734 bytes, registrando OPN não seguro, GIX, ACKs, fragmentos PP, RSP e fechamento físico. |

Evidências observadas no arquivo canônico durante a execução física:

```text
TRACE destination=D:\desenvolvimento\ia\lib-pinpad-abecs\apps\desktop\libpinpadabecsgo\logs\LogPinpadAbecs.txt enabled
[COM7#001] SPE 16 47 49 58 30 30 30 17 7F 4A CMD=GIX
[COM7#001] PP  06
[COM7#001] RSP CMD=GIX STATUS=000
[COM7#001] close()
```

Os sufixos obrigatórios `FUNC` e `DATA_HORA` estavam presentes nas linhas
reais. O CRC físico `7F 4A` difere do valor ilustrativo `12 34` porque foi
calculado para o pacote efetivamente transmitido. A resposta GIX chegou
fragmentada e cada retorno do driver foi gravado em uma linha `PP`, antes do
parser. O antigo arquivo gerado por teste em
`cmd/libpinpadabecsgo/logs/LogPinpadAbecs.txt` foi removido; os testes agora
usam diretórios temporários e comprovam que o caminho homônimo não é recriado.

### Resultado desta etapa

- Caminho canônico absoluto: comprovado pelo launcher e pelo CLI.
- Pacote enviado `SPE`, ACK e fragmentos recebidos `PP`: comprovados byte a
  byte por teste automatizado e por execução física GIX.
- `RSP CMD=GIX STATUS=000`: comprovado após parsing completo.
- Falhas de escrita, sincronização e fechamento do tracer: retornadas e
  cobertas por teste; o CLI encerra com código não zero quando o tracer perde
  integridade.
- Redação: matriz dos 22 comandos tipados coberta; OPN seguro, MLR, TLR, GCX, GTK,
  GOX, FCX, GPN, comunicação segura e comandos marcados pelo consumidor são
  redigidos, mantendo ACK/NAK/EOT/CAN isolados visíveis.

A implementação está pronta para `implementation-review`. Esta seção não
aprova, não valida formalmente e não arquiva a Change.

## Diagnóstico do diretório consultado — 2026-09-12

Ambiente: Windows `10.0.19045.0`, PowerShell `5.1.19041.6456`, Go
`go1.26.5 windows/386`. Inspeção executada a partir da raiz do repositório;
testes executados em `apps/desktop/libpinpadabecsgo`.

| Comando | Código | Resultado |
| --- | ---: | --- |
| `Get-ChildItem -LiteralPath 'apps/desktop/libpinpadabecsgo/logs', 'logs' -Force \| Select-Object FullName,Length,LastWriteTime` | 0 | Arquivo do módulo com 17.588 bytes, modificado às 11:27:21; diretório `logs` da raiz sem arquivos. |
| `Get-Content -Encoding UTF8 -LiteralPath 'apps/desktop/libpinpadabecsgo/logs/LogPinpadAbecs.txt' -Tail 25` | 0 | Encontrados TRACE, abertura, OPN, SPE GIX, PP, RSP GIX `000` e fechamento da execução relatada às 11:26. |
| `go version` | 0 | `go version go1.26.5 windows/386`. |
| `go test ./cmd/libpinpadabecsgo ./internal/infrastructure/logging ./internal/infrastructure/serial` | 0 | Três pacotes aprovados; resultados reutilizados pelo cache do Go. |

Diagnóstico registrado em
`reviews/2026-09-12-logging-directory-diagnosis.md`: a consulta ocorreu em
outro diretório. Nenhum código foi alterado e nenhuma nova operação física
foi executada. Esta evidência confirma somente a persistência da execução
relatada e não substitui os gates pendentes da Change.

## Regressão da seleção MNU — 2026-09-12

Ambiente: Windows `windows/386`, Go `go1.26.5`, módulo em
`D:\desenvolvimento\ia\lib-pinpad-abecs\apps\desktop\libpinpadabecsgo`.

O rastro físico apresentado pelo operador contém status `000` e o valor
selecionado nos TLVs `80 4D 00 02 30 33` e `80 4D 00 02 30 31`. O erro do CLI
foi reproduzido com um executável compilado antes da atualização do parser.

| Comando | Código | Resultado |
| --- | ---: | --- |
| `gofmt -w internal/application/service/service_test.go internal/domain/parser/advanced.go internal/domain/parser/advanced_response_test.go` | 0 | Arquivos formatados. |
| `go test ./internal/domain/parser ./internal/application/service -count=1` | 0 | Parser e serviço aprovados, incluindo `MNU000006` com TLV `0x804D` e seleção `03`. |
| `go vet ./internal/domain/parser ./internal/application/service` | 0 | Nenhum problema encontrado. |
| `go build -o .\bin\libpinpadabecsgo.exe .\cmd\libpinpadabecsgo` | 0 | Executável recompilado em 2026-09-12 17:34:14. |
| `go test ./... -count=1` | 0 | Todos os pacotes aprovados sem reutilizar cache. |
| `go vet ./...` | 0 | Nenhum problema encontrado. |

A validação automatizada confirma a interpretação da resposta física. A
confirmação interativa no pinpad exige encerrar a instância iniciada às
17:30:49, abrir o executável recompilado e repetir a opção 9. Esta seção não
altera os gates da Change.

## Correção da serialização GCX — 2026-09-12

Esta seção registra as evidências produzidas durante a implementação da correção
do status `011`. Ela não substitui a validação independente nem altera o estado
da Change para `VALIDADA`.

Ambiente: Windows `windows/386`, Go `go1.26.5`, PowerShell, módulo em
`D:\desenvolvimento\ia\lib-pinpad-abecs\apps\desktop\libpinpadabecsgo`.

O manual ABECS v2.12, seção 3.7.1, páginas 129 a 134, foi confrontado com o
rastro físico apresentado. O código emitia primeiro um GCX com data e hora
`000000`, embora ambos os parâmetros sejam obrigatórios, e montava o comando de
compra por concatenação posicional. A correção removeu esse envio preliminar e
passou a serializar `SPE_AMOUNT` (`0x0013`, N12), `SPE_TRNDATE` (`0x0015`, N6),
`SPE_TRNTIME` (`0x0016`, N6) e `SPE_GCXOPT` (`0x0017`, N5) como parâmetros
ABECS com identificador e comprimento binários.

| Comando | Código | Resultado |
| --- | ---: | --- |
| `gofmt -w <arquivos Go alterados>` | 0 | Arquivos formatados. |
| `go test ./internal/domain/command ./internal/application/service -run 'TestBuildGCXCommand\|TestServiceAdditionalFacadeFlows' -count=1 -v` | 0 | Vetor GCX byte a byte, entradas inválidas e uma única escrita aprovados. |
| `go test ./internal/domain/command ./internal/application/service ./internal/domain/model ./cmd/libpinpadabecsgo -count=1` | 0 | Pacotes diretamente afetados aprovados. |
| `go test ./... -count=1` | 0 | Todos os pacotes aprovados sem reutilizar o cache. |
| `go vet ./...` | 0 | Nenhum problema encontrado. |
| `go test ./... -coverprofile=coverage-gcx` | 0 | Testes e perfil concluídos. |
| `go tool cover -func=coverage-gcx` | 0 | Cobertura total **81,0%**. |
| `go build -o .\bin\libpinpadabecsgo.exe .\cmd\libpinpadabecsgo` | 0 | Executável Windows recompilado em 2026-09-12 17:51:04; SHA-256 `D1B9E8B743A731FE833D165E50B1DEE4FEBA5779F251943B6B12FB3E0189AE43`. |
| `GOOS=linux GOARCH=amd64 go build ./...` | 0 | Build Linux amd64 aprovado. |
| `git diff --check` | 0 | Nenhum erro de whitespace; somente avisos informativos de futura normalização LF para CRLF. |

Para o vetor valor `000000010000`, data `120926`, hora `173800` e CTLS
desabilitado, o teste compara exatamente o prefixo `GCX045` e os quatro TLVs na
ordem normativa, terminando em `SPE_GCXOPT="00000"`. Com CTLS habilitado, o
valor é `"10000"`. Datas, horas, valores e bits RUF inválidos são recusados
antes de qualquer escrita.

A regressão automatizada demonstra que a causa de formação do pacote associada
ao status `011` foi corrigida. A confirmação do status retornado pelo dispositivo
continua pendente da repetição da opção 11 no pinpad físico pela COM7.

## Notificações e opções GCX — 2026-09-12

Esta seção registra evidências de implementação e não altera o estado da Change
para `VALIDADA`.

Ambiente: Windows `windows/386`, Go `go1.26.5`, PowerShell, módulo em
`D:\desenvolvimento\ia\lib-pinpad-abecs\apps\desktop\libpinpadabecsgo` e
pinpad físico PPP100 na `COM7`, baud rate 19200.

O rastro físico das 22:50 registra ACK do GCX seguido por leituras redigidas de
1 e 44 bytes, sem linha `RSP` e com status estruturado vazio. Os 45 bytes formam
o tamanho exato de uma notificação `NTM000032` enquadrada: 9 bytes de cabeçalho,
32 de mensagem, SYN, ETB e CRC de 2 bytes. O manual ABECS v2.12 confirma nas
seções 2.2.2, 2.3.3 e 6.8.6 que comandos blocantes podem enviar notificações
antes da resposta final e que a seleção da aplicação gera essa notificação.

| Comando | Código | Resultado |
| --- | ---: | --- |
| `gofmt -w <arquivos Go alterados>` | 0 | Arquivos formatados. |
| `go test ./internal/domain/parser ./internal/domain/command ./internal/application/service ./cmd/libpinpadabecsgo -count=1` | 0 | Pacotes diretamente afetados aprovados. |
| `go test ./... -count=1` | 0 | Todos os pacotes aprovados. |
| `go vet ./...` | 0 | Nenhum problema encontrado. |
| `go test ./... -coverprofile=coverage-gcx-ntm` | 0 | Testes e perfil concluídos. |
| `go tool cover -func=coverage-gcx-ntm` | 0 | Cobertura total **81,0%**. |
| `go test <pacotes afetados> -run '<regressões GCX/NTM>' -count=1 -v` | 0 | Quatro combinações GCXOPT, escolhas inválidas, parser NTM e sequência `ACK + NTM + NTM + GCX` aprovados. |
| `go build -o .\bin\libpinpadabecsgo.exe .\cmd\libpinpadabecsgo` | 0 | Executável Windows recompilado em 2026-09-12 22:59:46; SHA-256 `648F061A76B4745F68A1F9BA492CB4F9B3F5BDA39243D5C2AB0E4A4C96F2F897`. |
| `GOOS=linux GOARCH=amd64 go build ./...` | 0 | Build Linux amd64 aprovado. |

Os testes comprovam:

- `SPE_GCXOPT` igual a `00000`, `01000`, `10000` ou `11000`, conforme as
  escolhas de interface e visibilidade do valor;
- recusa de escolha inválida antes de `PurchaseGCX`;
- reconhecimento estrutural de `NTM000` com mensagem entre 0 e 32 bytes;
- consumo de duas notificações no mesmo chunk serial antes do `GCX000` final;
- somente uma escrita serial de comando durante toda a compra, pois respostas
  válidas do pinpad não recebem ACK;
- retorno final com status `000` e tipo de cartão ICC no cenário de regressão.

A causa automatizável do `invalid pinpad response` foi corrigida. A confirmação
física exige repetir a opção 11 com o executável recompilado; essa execução deve
produzir uma ou mais linhas `PP` redigidas e, ao final, `RSP CMD=GCX
STATUS=<código>`.

## Prazo efetivo do GCX — 2026-09-12

Esta seção registra evidências da correção do `context deadline exceeded`
observado na opção 11 e não altera o estado da Change para `VALIDADA`.

Ambiente: Windows `windows/386`, Go `go1.26.5`, PowerShell, módulo em
`D:\desenvolvimento\ia\lib-pinpad-abecs\apps\desktop\libpinpadabecsgo` e
pinpad físico PPP100 na `COM7`, baud rate 19200.

O rastro das 23:03 contém `SPE CMD=GCX` às 23:03:19.351, ACK às
23:03:19.360 e expiração às 23:03:21.110, sem frame de resposta. O contexto de
60 segundos havia sido criado antes das perguntas da opção 11, por volta de
23:02:21; a digitação consumiu cerca de 58 segundos e deixou somente 1,76
segundo para apresentar o cartão. Além disso, a fachada aplicava o timeout
genérico de 30 segundos sobre qualquer prazo maior fornecido pelo consumidor.

| Comando | Código | Resultado |
| --- | ---: | --- |
| `gofmt -w cmd/libpinpadabecsgo/main.go internal/application/service/service.go internal/application/service/service_test.go` | 0 | Arquivos formatados. |
| `go test ./internal/application/service ./cmd/libpinpadabecsgo -run 'TestPurchaseGCXConsumesNotificationsAndPreservesCallerDeadline\|TestReadGCXOptions\|TestRunMenuRejectsInvalidGCXOption' -count=1 -v` | 0 | Regressões GCX e opções do CLI aprovadas. |
| `go test ./... -count=1` | 0 | Todos os pacotes aprovados sem reutilizar cache. |
| `go vet ./...` | 0 | Nenhum problema encontrado. |
| `go test ./... -coverprofile=coverage-gcx-timeout -count=1` | 0 | Testes e perfil concluídos. |
| `go tool cover -func=coverage-gcx-timeout` | 0 | Cobertura total **81,2%**. |
| `GOOS=linux GOARCH=amd64 go build ./...` | 0 | Build Linux amd64 aprovado. |
| `go build -o .\bin\libpinpadabecsgo.exe .\cmd\libpinpadabecsgo` | 0 | Executável Windows recompilado em 2026-09-12 23:09:55; 4.408.832 bytes; SHA-256 `347C5CEB0F2AB25576208A3081F287664B90607FDED01D43EEF6E63203742E51`. |
| `git diff --check` | 0 | Nenhum erro de whitespace; somente avisos informativos de futura normalização LF para CRLF. |

A opção 11 agora coleta e valida modo de leitura, visibilidade, valor, data e
hora antes de criar seu contexto de 60 segundos. `SendGCXCommand` envia o
comando pela mesma fila e pelo mesmo worker, mas preserva diretamente o prazo
do consumidor em vez de aplicar o timeout genérico de `PinpadConfig`.

CA-GCX-013 usa a configuração padrão de 30 segundos e chama `PurchaseGCX` com
um contexto de 60 segundos. O fake serial registra o deadline recebido pela
leitura e o teste exige mais de 50 segundos restantes, comprovando que o prazo
não foi encurtado. A confirmação física requer executar novamente a opção 11
com o binário recompilado e apresentar o cartão dentro dos 60 segundos que
começam após a última entrada.

## Conformidade unitária ABECS 2.12 — 2026-09-13

**Estado de entrada:** `IMPLEMENTACAO_APROVADA`

**Ambiente:** Windows, PowerShell, `go1.26.5`, `GOOS=windows`, `GOARCH=386`.

**Manual:** *Pinpad Abecs — Protocolo de Comunicação e Funcionamento*, versão
2.12 de 11-abr-2019.

| Evidência | Comando/cenário | Código | Resultado |
| --- | --- | ---: | --- |
| VAL-ABECS-001 | `go test ./... -count=1` | 0 | Todos os 16 pacotes aprovados sem cache. |
| VAL-ABECS-002 | `go vet ./...` | 0 | Nenhum diagnóstico. |
| VAL-ABECS-003 | `go test ./... -coverprofile=coverage-abecs212-final -count=1` | 0 | Perfil concluído. |
| VAL-ABECS-004 | `go tool cover -func=coverage-abecs212-final` | 0 | Cobertura total **81,6%**; command 87,9%, parser 89,2% e protocol 90,6%. |
| VAL-ABECS-005 | `go build -o .\bin\libpinpadabecsgo.exe .\cmd\libpinpadabecsgo` | 0 | Build Windows aprovado; SHA-256 `E15A45C80A1250BEAD8B6ADC9052676301CD0FED51391759B58F9493F348387E`. |
| VAL-ABECS-006 | `GOOS=linux GOARCH=amd64 go build ./...` | 0 | Build Linux amd64 aprovado. |
| VAL-ABECS-007 | `go run golang.org/x/vuln/cmd/govulncheck@latest ./...` | 0 | Zero vulnerabilidades alcançáveis; foram informadas vulnerabilidades não alcançáveis em dependências. |
| VAL-ABECS-008 | `go test -race ./... -count=1` | 1 | Limitação de ambiente: `-race is not supported on windows/386`. |
| VAL-ABECS-009 | `go run golang.org/x/lint/golint@latest ./...` | 0 | Nenhum comentário exportado ausente; somente sugestões de nomes preservados. |
| VAL-ABECS-010 | `git diff --check` | 0 | Nenhum erro de whitespace; avisos LF/CRLF são informativos. |
| VAL-ABECS-011 | `go test ./internal/domain/command ./internal/domain/parser ./internal/domain/protocol ./internal/application/service -run 'ABECS212\|Published\|Exchange\|CancelHandshake\|TableLoad' -count=1 -v` | 0 | Vetores publicados, limites, enlace, CAN/EOT e TLR aprovados individualmente. |

### Cenários comprovados

- OPN clássico, OPN seguro RSA e pacote AES-CBC da página 170;
- CLO S32, CLX, GIX, DSP, DEX, MNU da página 90 e todas as teclas GKY;
- GPN da página 83, GTK da página 86, GOX da página 139 e comando FCX da
  página 141;
- MLI/MLR/MLE/DSI com CRC16 e vetores publicados;
- TLI/TLR/TLE, continuação após status 000/020 e particionamento TLR por NREC e
  `CMD_LEN1 <= 999`; o PKTDATA TLR máximo é 1005 e permanece abaixo de 1024;
- GCX com AAMMDD, quatro combinações de GCXOPT, campos condicionais de resposta
  e retentativas/mudança de interface CTLS;
- respostas ABECS com múltiplos blocos N3 e rejeição de campos obrigatórios,
  comprimentos, bits RUF, datas e BER-TLV inválidos;
- enlace com ACK, NAK, três tentativas, NAK após CRC inválido, prazo CAN/EOT de
  2 segundos e nenhuma confirmação enviada após resposta válida;
- logging SPE/PP/RSP e redação de PAN, trilhas, chaves, PIN block e KSN.

### Limite da evidência

Os testes comprovam serialização, parsing, regras condicionais, estado e fluxo
contra o manual. Eles não comprovam interoperabilidade de todos os comandos com
um equipamento, cartões, kernels e tabelas reais.

**Resultado automatizado:** `VALIDADA`

**Estado da validação integral da Change:** `PENDENTE_VALIDACAO_FISICA`

Para concluir a validação integral, ainda é necessária a matriz física dos
comandos aplicáveis. A limitação do race detector deve ser reavaliada em
`windows/amd64` ou Linux, mas não invalida a suíte unitária executada.

## Seleção de trilhas GTK no utilitário local — 2026-09-13

**Estado de entrada:** `IMPLEMENTACAO_APROVADA`

**Ambiente:** Windows, PowerShell, `go1.26.5`, `GOOS=windows`, `GOARCH=386`.

O rastro físico apresentou `GTK042` porque a opção 19 solicitava sempre o
método DUKPT `50` no índice 02. Conforme a seção 3.3.12 do manual ABECS 2.12,
o status `042` informa chave MK/DUKPT ausente. A correção permite escolher o
retorno em claro, cujo comando é `GTK000` e não depende de chave, ou preservar
o retorno criptografado com DUKPT.

| Evidência | Comando/cenário | Código | Resultado |
| --- | --- | ---: | --- |
| VAL-GTK-CLI-001 | `go test ./cmd/libpinpadabecsgo ./internal/domain/command ./internal/application/service -run 'TestReadGTKRequest\|TestGTK\|TestAdvancedFlows' -count=1 -v` | 0 | Modo claro, DUKPT, entradas inválidas e contratos GTK aprovados. |
| VAL-GTK-CLI-002 | `go test ./... -count=1` | 0 | Todos os 16 pacotes aprovados sem cache. |
| VAL-GTK-CLI-003 | `go vet ./...` | 0 | Nenhum diagnóstico. |
| VAL-GTK-CLI-004 | `go test ./... -coverprofile=coverage-abecs212-final -count=1` e `go tool cover -func=coverage-abecs212-final` | 0 | Cobertura total **81,5%**; CLI 62,4%, service 78,3%, command 87,9%, parser 89,2% e protocol 90,6%. |
| VAL-GTK-CLI-005 | `go build -o .\bin\libpinpadabecsgo.exe .\cmd\libpinpadabecsgo` | 0 | Build Windows aprovado; SHA-256 `B93505AF1F67EDCCDF91CF77B8651CD5C851E0F826CE826B2583201E571A6BD8`. |
| VAL-GTK-CLI-006 | `GOOS=linux GOARCH=386 go build -o build/libpinpadabecsgo-linux-386 ./cmd/libpinpadabecsgo` | 0 | Build Linux 386 aprovado; SHA-256 `0656930FB3681032E8EAB49D1CBD259F6D2398096E1411A58423921819AEABC3`. |

Os testes verificam byte a byte que a escolha 1 produz `GTK000`, sem índice de
chave, e que a escolha 2 produz os TLVs `SPE_MTHDDAT=50`,
`SPE_TRACKS=1111` e `SPE_KEYIDX=02`. Modos inválidos, índice não numérico e
índice acima de 99 são recusados antes da comunicação serial.

**Resultado automatizado da correção:** `VALIDADA`

**Validação física da correção:** `VALIDADA`

O fluxo físico de 20:43 registrou `GCX000`, `GTK000` em modo claro e a linha
`GTK_CLEAR`, sem o antigo status `042`. O frame serial permaneceu redigido.

## Registro das trilhas GTK em claro — 2026-09-13

**Estado de entrada:** `IMPLEMENTACAO_APROVADA`

**Ambiente:** Windows, PowerShell, `go1.26.5`, `GOOS=windows`, `GOARCH=386`.

| Evidência | Comando/cenário | Código | Resultado |
| --- | --- | ---: | --- |
| VAL-GTK-LOG-001 | `go test ./cmd/libpinpadabecsgo ./internal/infrastructure/logging -run 'TestReadGTKRequest\|TestRecordGTKResult\|TestTracerRecordsGTKClearTracks' -count=1 -v` | 0 | Seleção, conteúdo, campos vazios, escaping e ausência no modo DUKPT aprovados. |
| VAL-GTK-LOG-002 | `go test ./... -count=1` | 0 | Todos os 16 pacotes aprovados sem cache. |
| VAL-GTK-LOG-003 | `go vet ./...` | 0 | Nenhum diagnóstico após correção do cancelamento no caminho de falha do tracer. |
| VAL-GTK-LOG-004 | `go test ./... -coverprofile=coverage-abecs212-final -count=1` e `go tool cover -func=coverage-abecs212-final` | 0 | Cobertura total **81,3%**; logging **88,8%**. |
| VAL-GTK-LOG-005 | `go build -o .\bin\libpinpadabecsgo.exe .\cmd\libpinpadabecsgo` | 0 | Executável Windows aprovado; SHA-256 `36DB8CAACE5B1735943BAF85561EFC16279F4B22B22811812874CBEB22F0F4A6`. |

A linha esperada no arquivo ativo possui o formato:

```text
[COM7#001] GTK_CLEAR TRACK1="<valor>" TRACK2="<valor>" TRACK3="<valor>" FUNC=cmd.libpinpadabecsgo.GTK DATA_HORA=<RFC3339Nano>
```

Os frames `SPE` e `PP` de GTK permanecem com `**REDACTED(<n> bytes)**`. A linha
`GTK_CLEAR` só é produzida após resposta válida quando a escolha do CLI gera
`GTK000`; o modo DUKPT não produz essa linha.

**Resultado automatizado da correção:** `VALIDADA`

**Validação física do conteúdo das trilhas:** `VALIDADA`

## Decodificação BCD/nibble do GTK em claro — 2026-09-13

**Estado de entrada:** `IMPLEMENTACAO_APROVADA`

**Fonte normativa:** manual ABECS 2.12, seções 5.4.2.1 e 5.4.2.2, páginas
181–182.

| Evidência | Comando/cenário | Código | Resultado |
| --- | --- | ---: | --- |
| VAL-GTK-BCD-001 | `go test ./internal/infrastructure/logging ./cmd/libpinpadabecsgo -run 'TestDecodeGTKClearNumericTrack\|TestTracerRecordsGTKClearTracks\|TestRecordGTKResult' -count=1 -v` | 0 | Vetor físico, vetor solicitado, separador, filler, campos vazios, escaping e entradas inválidas aprovados. |
| VAL-GTK-BCD-002 | `go test ./... -count=1` | 0 | Todos os 16 pacotes aprovados sem cache. |
| VAL-GTK-BCD-003 | `go vet ./...` | 0 | Nenhum diagnóstico. |
| VAL-GTK-BCD-004 | `go test ./... -coverprofile=coverage-abecs212-final -count=1` | 0 | Cobertura total **81,4%**; logging **88,8%**. |
| VAL-GTK-BCD-005 | `go build -o .\bin\libpinpadabecsgo.exe .\cmd\libpinpadabecsgo` | 0 | Executável Windows aprovado; SHA-256 `08D46A0A73A3753493AAD13FDCEF7295337DA81C86AE94E4CC97BB9CE07B70B3`. |

O rastro físico fornecido foi usado diretamente como oráculo:

```text
54 28 20 60 97 98 40 97 D2 11 12 01 38 29 95 58 46 37 0F
=> 5428206097984097=21112013829955846370
```

O nibble `D` foi convertido no delimitador `=` e o filler `F` final foi
removido. O vetor correspondente ao formato apresentado pelo operador também
foi validado:

```text
54 28 20 60 97 98 40 97 D1 12 23 36 65 5F
=> 5428206097984097=1122336655
```

**Resultado automatizado:** `VALIDADA`

**Confirmação no arquivo físico:** `VALIDADA`

## Correção da opção 20 GOX — 2026-09-13

**Estado de entrada:** `IMPLEMENTACAO_APROVADA`

**Ambiente:** Windows, PowerShell, `go1.26.5`, `GOOS=windows`, `GOARCH=386`.

| Evidência | Comando/cenário | Código | Resultado |
| --- | --- | ---: | --- |
| VAL-GOX-CLI-001 | `go test ./cmd/libpinpadabecsgo ./internal/domain/command ./internal/application/service -run 'TestReadGOXRequest\|TestGOXAcquirerReferences\|TestGOXBuilder\|TestAdvancedFlows' -count=1 -v` | 0 | Contexto GCX, redes AID, PIN/WKENC, payload mínimo, vetor publicado e fluxo GCX→GTK→GOX aprovados. |
| VAL-GOX-CLI-002 | `TestRunMenuRejectsGOXWithoutEligibleGCXBeforeService` | 0 | Ausência de GCX elegível é recusada antes do serviço. |
| VAL-GOX-CLI-003 | `go test ./... -count=1` | 0 | Todos os 16 pacotes aprovados sem cache. |
| VAL-GOX-CLI-004 | `go vet ./...` | 0 | Nenhum diagnóstico. |
| VAL-GOX-CLI-005 | `go test ./... -coverprofile=coverage-abecs212-final -count=1` | 0 | Cobertura total **81,4%**; CLI **65,8%**, command **87,9%** e service **78,3%**. |
| VAL-GOX-CLI-006 | `go build -o .\bin\libpinpadabecsgo.exe .\cmd\libpinpadabecsgo` | 0 | Executável Windows aprovado; SHA-256 `B5F5668881A17019DAB9D04BDD18C4AA71A0877C8B936D873097EC37B7206207`. |

Para `PP_AIDTABINFO="080301"`, valor GCX `000000010000`, DUKPT TDES e índice
07, o teste exige exatamente:

```text
GOX033
  0013 000C 303030303030303130303030
  0002 0001 33
  0009 0002 3037
  0010 0002 3038
```

O payload contém `SPE_ACQREF="08"`, derivado do GCX, e não contém o antigo
valor fixo `01`. A confirmação de que o pinpad deixa de devolver `011` depende
de repetir fisicamente GCX e GOX com uma chave PIN existente no índice
escolhido.

**Resultado automatizado:** `VALIDADA`

**Confirmação física do GOX:** `VALIDADA`

## Correção da opção 21 FCX e diagnóstico do GOX047 — 2026-09-13

**Estado de entrada:** `IMPLEMENTACAO_APROVADA`

**Ambiente:** Windows, PowerShell, `go1.26.5`, `GOOS=windows`, `GOARCH=386`.

**Fonte normativa:** manual ABECS 2.12, seção 3.7.3, páginas 136–139, e seção
3.7.4, páginas 140–141.

| Evidência | Comando/cenário | Código | Resultado |
| --- | --- | ---: | --- |
| VAL-FCX-CLI-001 | `go test ./cmd/libpinpadabecsgo ./internal/domain/command ./internal/infrastructure/logging ./internal/application/service -run 'TestReadGOXRequestMatchesSuccessfulPhysicalConfig\|TestReadFCXRequest\|TestRunMenuRejectsFCX\|TestFCXBuilder\|TestTracerRecordsOnlyNonSensitiveGOXConfig\|TestGOXAndFCXRejectInvalidConditionalFields\|TestAdvancedFlows' -count=1 -v` | 0 | Vetor físico GOX, decisões FCX, ARC A2, opcionais, sequência, payloads, vetor publicado, estado e diagnóstico aprovados. |
| VAL-FCX-CLI-002 | `go test ./... -count=1` | 0 | Todos os 16 pacotes aprovados sem cache. |
| VAL-FCX-CLI-003 | `go vet ./...` | 0 | Nenhum diagnóstico. |
| VAL-FCX-CLI-004 | `go test ./... -coverprofile=coverage-abecs212-final -count=1` e `go tool cover -func=coverage-abecs212-final` | 0 | Cobertura total **81,4%**; CLI **68,0%**, command **88,4%**, parser **89,2%** e protocol **90,6%**. |
| VAL-FCX-CLI-005 | `go build -o .\bin\libpinpadabecsgo.exe .\cmd\libpinpadabecsgo` | 0 | Executável Windows com 4.525.568 bytes; SHA-256 `4F305A0246D773A2637E6370F4B3E0910A69BD7EA77D0864811D7C15CC7A02C1`. |
| VAL-FCX-CLI-006 | `GOOS=linux GOARCH=amd64 go build ./...` | 0 | Build Linux amd64 aprovado. |
| VAL-FCX-CLI-007 | `git diff --check` | 0 | Nenhum erro de whitespace; avisos LF/CRLF são informativos. |

O teste `TestReadGOXRequestMatchesSuccessfulPhysicalConfig` fixa byte a byte o
GOX que funcionou às 17:43: valor `000000010000`, `SPE_MTHDPIN=3`,
`SPE_KEYIDX=02` e `SPE_ACQREF=04`. Portanto, a montagem atual não divergiu
dessa execução comprovada.

Os testes exigem os seguintes comandos mínimos:

```text
aprovação: FCX014 001C 0002 <ARC-A2> 0019 0004 0000
negação:   FCX014 001C 0002 <ARC-A2> 0019 0004 1000
falha:     FCX008 0019 0004 2000
```

`PP_FCXRES` (`8056`) não aparece nos payloads de entrada. Após resposta FCX
válida, o CLI exibe o campo retornado pelo pinpad. A elegibilidade GOX/FCX só é
alterada depois da validação dos campos obrigatórios da resposta.

O rastro físico fornecido comprova `GOX000` às 17:43 com adquirente `04`, método
DUKPT TDES `3` e índice `02`, seguido por `FCX047`. Às 18:07 e 18:09, novas
execuções devolveram `GOX047` em poucos milissegundos. Como `047` não está na
tabela da versão 2.12 e os frames sensíveis anteriores estavam redigidos, o novo
binário passa a registrar antes do GOX:

```text
GOX_CONFIG ACQ=<N2> PIN_METHOD=<N1> KEY_INDEX=<N2>
```

Essa linha não contém chave, PIN, PIN block, KSN, PAN, trilhas ou dados EMV e
permite comparar a próxima execução com a configuração física já comprovada.

**Resultado automatizado:** `VALIDADA`

### Evidência física da correção

O teste confirmado pelo operador na COM7 gerou, no arquivo ativo, a sequência:

```text
2026-09-13T20:43:39.6199468-03:00 RSP CMD=GCX STATUS=000
2026-09-13T20:43:48.9051234-03:00 RSP CMD=GTK STATUS=000
2026-09-13T20:44:01.7505529-03:00 GOX_CONFIG ACQ=04 PIN_METHOD=3 KEY_INDEX=02
2026-09-13T20:44:06.2239606-03:00 RSP CMD=GOX STATUS=000
2026-09-13T20:44:28.6915428-03:00 RSP CMD=FCX STATUS=000
```

Os frames GOX e FCX permaneceram integralmente redigidos. A configuração GOX
coincide com o vetor físico unitário e ambos os comandos terminaram com status
`000`. O operador confirmou o funcionamento apresentado pelo CLI, o que também
comprova que o parser aceitou o `PP_FCXRES` obrigatório.

**Confirmação física do GOX e FCX corrigidos:** `VALIDADA`

A matriz física integral dos demais comandos da Change permanece pendente e
não é alterada por esta confirmação específica.

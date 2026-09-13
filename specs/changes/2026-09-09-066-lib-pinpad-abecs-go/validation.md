# Validação — 066-lib-pinpad-abecs-go

Autor: Rômulo Penha

**Data da última evidência executada:** 2026-09-12

**Estado desta evidência:** `PENDENTE_DE_REVISAO_DA_IMPLEMENTACAO`

**Fase atual da Change:** `IMPLEMENTADA`, aguardando revisão independente da
implementação antes da validação formal.

> Os resultados abaixo são históricos e não validam RF-011/CA-026 nem
> CA-L001 a CA-L017 após a revisão que identificou dois arquivos homônimos e o
> destino esperado vazio. A nova validação deverá substituir esta conclusão.

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

# SPEC: 066-lib-pinpad-abecs-go — Logging detalhado de comunicação (SPE/PP)

## Status
`RASCUNHO`

> Esta SPEC havia sido aprovada em uma versão anterior. Como o contrato foi ampliado para documentar o tracer, a separação entre `slog` e SPE/PP/RSP e os critérios de validação física, ela deve passar por nova revisão antes da implementação.

## Papel do logging no projeto

Esta SPEC define dois canais complementares: o `slog` operacional, que registra operação, duração e resultado, e o tracer bruto SPE/PP/RSP, que registra a comunicação serial suficiente para diagnosticar framing e protocolo. O tracer não é uma autorização para armazenar dados do cartão: redaction é aplicada antes da escrita e prevalece sobre a necessidade de diagnóstico.

O tracer pertence à infraestrutura. O domínio não deve importar `slog`, `os.File` ou conhecer o formato do arquivo. A serialização do comando deve fornecer apenas metadados seguros, como o tipo do comando e o status, ao tracer.

## Identificação

- `groupId`: `br.com.romulopenha`
- `artifactId`: `lib-pinpad-abecs-go`
- pacote afetado: `internal/infrastructure/logging`
- pacotes consumidores: `internal/infrastructure/serial`, `internal/application/service`, `internal/domain/command`
- Esta SPEC é complementar a `spec.md` (RF-011 e RF-012.1) da mesma Change e não substitui nenhum requisito já aprovado; ela detalha e amplia exclusivamente o comportamento de logging.

## Motivação

O log atual (`internal/infrastructure/logging/logging.go`) apenas cria um `slog.Logger` JSON genérico e não registra, byte a byte, o que é enviado ao pinpad e o que é recebido dele. Para investigar problemas de protocolo e evoluir o código com segurança, é necessário um rastro de comunicação no mesmo nível de detalhe do legado C/JNI fornecido como referência (arquivo `br_com_execucao_jbc_PinpadSerialProtocol.c`, funções `logString`, `logBytes`, `setLogDestination`, e as chamadas em `open`, `close`, `write` e `read`), sem transportar código ou comentários daquele arquivo para o repositório.

Nível de detalhe do legado, usado apenas como referência de comportamento:

- `setLogDestination(filename)`: define o arquivo de destino do log; `filename` vazio/nulo desliga o log; abre em modo *append*.
- `open(...)`: registra `open(porta,baud,databits,parity,stopbits)=>handle`.
- `close(...)`: registra `close(handle)`.
- `write(...)`: registra `[handle] SPE <bytes em hexa, separados por espaço>`.
- `read(...)`: registra `[handle] PP  <bytes em hexa, separados por espaço>`.
- Erros de sistema operacional são registrados com código e mensagem (`strerror`).

`SPE` identifica bytes enviados do software para o pinpad (direção de escrita/"Serial Para Equipamento"). `PP` identifica bytes recebidos do pinpad (direção de leitura/"Pinpad"). Esta SPEC preserva esse mesmo par de marcadores.

## Referências e dependências

- `spec.md` desta mesma Change (RF-002, RF-004, RF-007, RF-009, RF-010, RF-011, RF-012.1).
- `specs/shared/process/workflow.md`
- `specs/shared/architecture/backend-golang.md`
- `.agents/skills/golang-error-handling/SKILL.md`
- `.agents/skills/golang-security/SKILL.md`
- `.agents/skills/golang-concurrency/SKILL.md`
- Comportamento de referência do arquivo `br_com_execucao_jbc_PinpadSerialProtocol.c` (entrada de requisitos; não será copiado ao repositório).

## Contrato de formato

Cada evento deve conter um identificador lógico de sessão da porta e uma linha única. O formato hexadecimal é sempre uppercase, dois caracteres por byte e separado por espaço. O identificador não é um handle do sistema operacional e não deve expor ponteiros, descritores ou dados do processo.

Exemplos não sensíveis:

```text
[COM7#001] open(COM7,19200,8,N,1)=>OK
[COM7#001] SPE 16 47 49 58 30 30 30 17 12 34 CMD=GIX
[COM7#001] PP  06
[COM7#001] RSP CMD=GIX STATUS=000
[COM7#001] close()
```

Os exemplos acima são ilustrativos; os bytes reais devem ser derivados da transmissão observada. Para `GPN` e `GCX` sensível, o trecho hexadecimal é substituído integralmente por marcador de redaction.

## Requisitos funcionais

### RF-L001 — Destino do log configurável

Deverá existir uma função equivalente a `SetLogDestination(filename string) (bool, error)` no pacote `internal/infrastructure/logging`:

- `filename` não vazio abre (ou cria) o arquivo em modo *append* e passa a direcionar todas as mensagens de rastro de comunicação para ele;
- `filename` vazio desliga o rastro de comunicação (nenhuma mensagem `SPE`/`PP`/`open`/`close` é mais gravada);
- chamar `SetLogDestination` novamente fecha o arquivo anterior antes de abrir o novo, sem vazar descritores de arquivo;
- retorna `false`/erro quando não for possível abrir o arquivo no caminho informado (permissão, diretório inexistente etc.), preservando o destino anterior configurado;
- é thread-safe: pode ser chamada enquanto outras goroutines estão gravando ou lendo da porta serial, sem corrida de dados (`go test -race`).

Esse rastro de comunicação é independente do `slog` estruturado já definido em RF-011 de `spec.md`: um audita a operação (nível INFO/ERROR, duração, resultado); o outro é o traço bruto de bytes trocados, equivalente ao legado. Os dois poderão coexistir e ser habilitados de forma independente.

### Correlação SPE/PP com comando e status

Como a escrita na porta serial é sempre atômica (um pacote ABECS inteiro por chamada de `Write`), a linha `SPE` é gerada com o nome do comando já conhecido no momento da escrita. Como a leitura pode ser fragmentada pelo driver serial em múltiplas chamadas, cada chamada de leitura gera sua própria linha `PP` crua (fidelidade ao legado); o status ABECS só é conhecido depois que o parser reconhecer o pacote completo. Por isso, após decodificado, o serviço registra uma linha de correlação adicional, sem repetir o hexadecimal já gravado:

```text
[<id>] RSP CMD=<nome-do-comando> STATUS=<codigo-status>
```

Essa linha não duplica bytes; apenas correlaciona o comando enviado com o resultado da operação.

### RF-L002 — Registro de abertura e fechamento da porta

Ao abrir a porta serial com sucesso, deverá ser registrada uma linha equivalente a:

```text
open(<porta>,<baudrate>,<databits>,<parity>,<stopbits>)=>OK
```

Em caso de falha na abertura, deverá ser registrada uma linha equivalente a:

```text
open(<porta>,<baudrate>,<databits>,<parity>,<stopbits>)=>ERRO: <mensagem>
```

Ao fechar a porta, deverá ser registrada uma linha equivalente a `close()`. Como a biblioteca Go não expõe um handle de sistema operacional numérico estável e portátil (Windows/Linux), o identificador correlacionador entre `open`/`close`/`SPE`/`PP` deverá ser um identificador lógico de sessão de porta (por exemplo, a própria string da porta, ou um contador de sessão), documentado no código e estável durante toda a vida da conexão.

### RF-L003 — Registro de bytes enviados (SPE)

Toda escrita na porta serial deverá gerar uma linha:

```text
[<id>] SPE <hex bytes separados por espaço>
```

onde `<hex bytes>` é a representação hexadecimal maiúscula, dois dígitos por byte, exatamente dos bytes efetivamente enviados (payload já submetido a `ApplySubstitution`/`BuildPacket`), na ordem de transmissão.

### RF-L004 — Registro de bytes recebidos (PP)

Toda leitura da porta serial que retornar ao menos um byte deverá gerar uma linha:

```text
[<id>] PP  <hex bytes separados por espaço>
```

com o mesmo formato hexadecimal de RF-L003, incluindo ACK/NAK/EOT isolados e frames completos.

### RF-L005 — Identificação do comando e da direção

Além do rastro hexadecimal bruto (RF-L003/RF-L004), a linha `SPE` correspondente ao envio de um comando deverá identificar o nome funcional do comando ABECS (por exemplo `OPN`, `GIX`, `CLO`, `DSP`, `GCX`, `GPN`, `GKY`, `RST` etc., conforme catálogo de `internal/domain/command`), pois esse nome já é conhecido no momento da escrita (ver "Correlação SPE/PP com comando e status"):

```text
[<id>] SPE <hex> CMD=<nome-do-comando>
```

Quando a escrita não corresponder a um comando tipado (por exemplo, envio isolado de CAN/NAK como parte do protocolo de baixo nível), a linha deverá omitir `CMD=` e conter apenas o rastro hexadecimal, sem inventar um nome de comando. O status resultante (`RSP_STAT`) é registrado na linha de correlação `RSP`, conforme descrito acima, e não na linha `PP` bruta.

### RF-L006 — Redação de dados sensíveis

O rastro de comunicação nunca deverá expor, em texto plano ou hexadecimal, valores reais de PAN, TRACK2, PIN, PIN block, KSN, WKENC ou dados EMV sensíveis, mesmo quando esses bytes fizerem parte do payload transmitido. Para comandos classificados como sensíveis (`GPN` em qualquer direção; `GCX` e respostas que carreguem tags de trilha, PAN, PIN block ou KSN), a linha de log deverá substituir o hexadecimal completo do payload por um marcador de redação (por exemplo `**REDACTED(<n> bytes)**`), preservando os cabeçalhos `CMD=`/`STATUS=`. A redação total do payload (em vez de redação parcial por campo) é a estratégia adotada nesta SPEC por ser mais simples de auditar e por eliminar o risco de vazamento parcial por erro de cálculo de offset. Esta regra é a mesma já definida em RF-011 e RF-012.1 de `spec.md`, aplicada agora também ao rastro byte a byte.

### RF-L007 — Erros de sistema

Falhas de abertura, escrita, leitura e fechamento da porta deverão ser registradas no rastro de comunicação com o erro Go retornado (`err.Error()`), sem depender de `errno`/`strerror` (específicos de C), preservando o encadeamento de erro (`%w`) já usado no restante do módulo.

### RF-L008 — Integração não invasiva

A instrumentação de RF-L002 a RF-L007 deverá ser aplicada no adaptador real de `SerialPort` (RF-007 de `spec.md`) e, quando aplicável, no ponto único de envio/recebimento usado pela fila/worker (RF-009), sem duplicar logs para o mesmo byte em múltiplas camadas. O fake de `SerialPort` usado em testes não é obrigado a gravar no rastro de comunicação, mas deverá permitir testar a instrumentação por injeção do logger.

### RF-L009 — Desempenho e concorrência

O rastro de comunicação deverá usar exclusão mútua (`sync.Mutex` ou equivalente) para proteger a escrita no arquivo de destino, prevenindo intercalação de bytes entre goroutines. A ausência de destino configurado (RF-L001) não deverá adicionar overhead perceptível (nenhuma formatação de hexa deverá ocorrer quando o rastro estiver desligado).

## Requisitos não funcionais

- Go idiomático; nenhuma tradução literal de `FILE*`, `fopen`, `fwrite` ou `errno`.
- Sem CGO; compatível com Windows e Linux.
- Sem corrida de dados em `go test -race ./...`.
- Nenhum dado sensível real em testes ou fixtures.
- Cobertura de testes aplicável mínima de 80% para o pacote `internal/infrastructure/logging` e para a instrumentação adicionada em `internal/infrastructure/serial`.

## Regras de negócio

- O rastro de comunicação é opt-in: desabilitado por padrão até que um destino seja configurado.
- Direção de escrita é sempre `SPE`; direção de leitura é sempre `PP`.
- Nenhuma linha de rastro poderá conter PAN, TRACK2, PIN, PIN block, KSN ou WKENC em claro.
- Reconfigurar o destino do log nunca poderá deixar um arquivo aberto sem uso (vazamento de descritor).

## Cenários e critérios de aceite

- [ ] **CA-L001:** `SetLogDestination` com caminho válido cria/abre o arquivo em modo *append* e passa a registrar `open`, `close`, `SPE` e `PP`.
- [ ] **CA-L002:** `SetLogDestination("")` desliga o rastro; nenhuma linha adicional é gravada após a chamada.
- [ ] **CA-L003:** chamadas sucessivas de `SetLogDestination` fecham o arquivo anterior sem vazar descritor (verificável via teste que abre, troca de destino e verifica que o arquivo antigo pode ser removido/renomeado no SO).
- [ ] **CA-L004:** escrita de um payload conhecido gera linha `SPE` com hexadecimal exatamente igual aos bytes enviados, incluindo casos com `0x13`, `0x16`, `0x17` após `ApplySubstitution`.
- [ ] **CA-L005:** leitura de ACK, NAK, EOT e de um frame completo geram linhas `PP` com o hexadecimal correspondente.
- [ ] **CA-L006:** envio de um comando decodificável (por exemplo `OPN`) inclui `CMD=OPN` na linha `SPE`; a resposta correspondente gera uma linha `RSP CMD=OPN STATUS=<codigo>` após a decodificação completa.
- [ ] **CA-L007:** envio/recebimento de comando `GPN`/`GCX` com dados sensíveis simulados demonstra que o payload aparece totalmente redigido (`**REDACTED(<n> bytes)**`) no log, mantendo `CMD=`/`STATUS=`.
- [ ] **CA-L008:** falha simulada de abertura da porta gera linha `open(...)=>ERRO: <mensagem>` e `SetLogDestination`/estado de log preservam o destino anterior.
- [ ] **CA-L009:** `go test -race ./...` cobre gravação concorrente de múltiplas linhas de rastro sem corrida de dados.
- [ ] **CA-L010:** com o rastro desligado (nenhum destino configurado), nenhuma linha `SPE`/`PP`/`open`/`close` é produzida e nenhuma alocação de formatação hexadecimal ocorre no caminho crítico (validável por teste ou benchmark comparativo).

## Validação

Os critérios de concorrência, append, troca de destino e redaction podem ser cobertos por testes automatizados sem hardware. A confirmação de que os eventos correspondem aos bytes efetivamente trocados deve ser repetida com pinpad físico, porta serial real e dados de teste de laboratório. Logs de validação não podem ser anexados se contiverem PAN, PIN, KSN, trilhas ou chaves.

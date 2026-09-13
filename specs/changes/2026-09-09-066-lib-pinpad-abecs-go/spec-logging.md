# SPEC: 066-lib-pinpad-abecs-go — Logging detalhado de comunicação (SPE/PP)

Autor: Rômulo Penha

## Status
`SPEC_APROVADA`

> A revisão de 2026-09-12 torna o destino independente do diretório de
> trabalho, define o adaptador serial como fonte única do rastro bruto, exige
> marcador de ativação e torna falhas de persistência observáveis. A revisão
> formal registrada em `reviews/2026-09-12-spec-logging-path-review.md` aprovou
> este contrato; a implementação e a revalidação permanecem pendentes.

## Papel do logging no projeto

Esta SPEC define dois canais complementares: o `slog` operacional, que registra operação, duração e resultado, e o tracer bruto SPE/PP/RSP, que registra a comunicação serial suficiente para diagnosticar framing e protocolo. O tracer não é uma autorização geral para armazenar dados do cartão: redaction é aplicada antes da escrita e prevalece sobre a necessidade de diagnóstico. O utilitário local de laboratório possui uma exceção explícita para registrar o resultado GTK já interpretado quando o operador escolhe o modo em claro, conforme `spec-command-gtk.md`; o frame serial bruto continua redigido.

O tracer pertence à infraestrutura. O domínio não deve importar `slog`, `os.File` ou conhecer o formato do arquivo. A serialização do comando deve fornecer apenas metadados seguros, como o tipo do comando e o status, ao tracer.

## Estado atual e lacuna observada

Na data desta revisão, a execução observada deixou
`apps/desktop/libpinpadabecsgo/logs/LogPinpadAbecs.txt` com zero byte, enquanto
um arquivo homônimo em
`apps/desktop/libpinpadabecsgo/cmd/libpinpadabecsgo/logs/LogPinpadAbecs.txt`
recebeu linhas. A causa contratual é o fallback relativo `logs/...`, cujo
resultado varia com o diretório de trabalho do processo. A presença do `slog`
no console e a existência de qualquer arquivo homônimo não satisfazem esta
SPEC: deve existir um único destino canônico, explicitamente informado ao
operador, contendo o rastro serial da execução corrente.

O exemplo fornecido de cenário GCX confirma que o diagnóstico precisa manter a
ordem `SPE → PP* → close`. O conteúdo de cartão e de portador eventualmente
presente em uma resposta desse tipo é dado sensível: essa resposta não pode ser
copiada para a documentação, para fixtures nem enviada sem a redação prevista
em RF-L006.

## Identificação

- `groupId`: `br.com.romulopenha`
- `artifactId`: `lib-pinpad-abecs-go`
- pacote afetado: `internal/infrastructure/logging`
- pacotes consumidores: `internal/infrastructure/serial`, `internal/application/service`, `internal/domain/command`
- Esta SPEC é complementar a `spec.md` (RF-011 e RF-012.1) da mesma Change e não substitui nenhum requisito já aprovado; ela detalha e amplia exclusivamente o comportamento de logging.

## Motivação

O log atual (`internal/infrastructure/logging/logging.go`) apenas cria um `slog.Logger` JSON genérico e não registra, byte a byte, o que é enviado ao pinpad e o que é recebido dele. Para investigar problemas de protocolo e evoluir o código com segurança, é necessário um rastro de comunicação no mesmo nível de detalhe do legado C/JNI fornecido como referência (arquivo `br_com_execucao_jbc_PinpadSerialProtocol.c`, funções `logString`, `logBytes`, `setLogDestination`, e as chamadas em `open`, `close`, `write` e `read`), sem transportar código ou comentários daquele arquivo para o repositório.

Nível de detalhe do protocolo, usado apenas como referência de comportamento:

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

Cada evento deve conter um identificador lógico de sessão da porta e uma linha
única. O formato hexadecimal é sempre uppercase, dois caracteres por byte e
separado por espaço. O identificador não é um handle do sistema operacional e
não deve expor ponteiros, descritores ou dados do processo. Toda linha contém
também `FUNC=<origem-funcional>` e `DATA_HORA=<RFC3339Nano-com-offset>`. A origem
funcional é um identificador estável definido pela implementação, não um stack
trace nem um caminho de código-fonte.

Exemplos não sensíveis:

```text
TRACE destination=D:\...\libpinpadabecsgo\logs\LogPinpadAbecs.txt enabled FUNC=logging.Tracer.SetLogDestination DATA_HORA=2026-09-12T10:13:49.7000000-03:00
[COM7#001] open(COM7,19200,8,N,1)=>OK FUNC=serial.Adapter.Open DATA_HORA=2026-09-12T10:13:49.7100000-03:00
[COM7#001] SPE 16 47 49 58 30 30 30 17 12 34 CMD=GIX FUNC=serial.Adapter.Write DATA_HORA=2026-09-12T10:13:49.7200000-03:00
[COM7#001] PP  06 FUNC=serial.Adapter.Read DATA_HORA=2026-09-12T10:13:49.7300000-03:00
[COM7#001] RSP CMD=GIX STATUS=000 FUNC=service.ExchangeCommand DATA_HORA=2026-09-12T10:13:49.7800000-03:00
[COM7#001] close() FUNC=serial.Adapter.Close DATA_HORA=2026-09-12T10:14:10.0000000-03:00
```

Os exemplos acima são ilustrativos; os bytes reais devem ser derivados da transmissão observada. Para `GPN` e `GCX` sensível, o trecho hexadecimal é substituído integralmente por marcador de redaction.

### Regra obrigatória — pacotes enviados e recebidos

Não basta registrar a conclusão estruturada do comando, seu nome ou seu
status. Para todo comando não sensível que alcançar a porta, o arquivo deve
mostrar o pacote físico enviado e todos os bytes efetivamente recebidos. Para o
GIX usado como exemplo, o rastro contém obrigatoriamente linhas com este
formato:

```text
[COM7#001] SPE 16 47 49 58 30 30 30 17 12 34 CMD=GIX
[COM7#001] PP  06
```

Se o frame de resposta chegar em outra leitura, deverá existir outra linha
`PP` com o hexadecimal completo desse bloco. Se o driver fragmentar o frame,
cada fragmento recebido deverá aparecer em sua própria linha `PP`, na ordem
observada. `RSP CMD=GIX STATUS=000` é uma correlação adicional e nunca substitui
`SPE` ou `PP`. Os sufixos obrigatórios `FUNC` e `DATA_HORA` podem seguir o
conteúdo acima, sem remover nem alterar os bytes.

## Requisitos funcionais

### RF-L001 — Destino do log configurável

O pacote `internal/infrastructure/logging` deverá expor um `Tracer` configurável
por uma operação equivalente a `SetLogDestination(filename string) (bool, error)`:

- `filename` não vazio abre (ou cria) o arquivo em modo *append* e passa a direcionar todas as mensagens de rastro de comunicação para ele;
- a ativação só retorna sucesso depois de escrever integralmente e tornar
  legível no destino uma linha `TRACE destination=<caminho-absoluto> enabled`;
- `filename` vazio desliga o rastro de comunicação (nenhuma mensagem `SPE`/`PP`/`open`/`close` é mais gravada);
- chamar `SetLogDestination` novamente fecha o arquivo anterior antes de abrir o novo, sem vazar descritores de arquivo;
- retorna `false`/erro quando não for possível abrir o arquivo no caminho informado (permissão, diretório inexistente etc.), preservando o destino anterior configurado;
- é thread-safe: pode ser chamada enquanto outras goroutines estão gravando ou lendo da porta serial, sem corrida de dados (`go test -race`).

Esse rastro de comunicação é independente do `slog` estruturado já definido em RF-011 de `spec.md`: um audita a operação (nível INFO/ERROR, duração, resultado); o outro é o traço bruto de bytes trocados, equivalente ao legado. Os dois poderão coexistir e ser habilitados de forma independente.

### RF-L001.2 — Instância e injeção do tracer

`Tracer` é uma dependência de infraestrutura, desabilitada por padrão e segura
para concorrência. A composição do executável local deverá criar **uma única**
instância e fornecê-la tanto ao `Service` quanto ao adaptador serial real antes
de `Open`. A instância compartilhada é responsável pelo lock e pela ordenação
das linhas de uma sessão.

As APIs atuais de construção que não recebem tracer permanecem compatíveis e
usam um tracer desabilitado. Uma configuração explícita (por construtor
adicional ou setter documentado) deverá permitir associar o `Tracer` ao serviço
e ao adaptador real antes da abertura. O domínio e a porta `SerialPort` não
receberão essa dependência. Testes deverão poder criar instâncias isoladas com
arquivo temporário, sem alterar estado global nem o destino configurado por
outro teste.

### RF-L001.1 — Destino do utilitário local e coleta de evidências

A biblioteca permanece com tracer desabilitado por padrão. O executável local
`cmd/libpinpadabecsgo`, quando iniciado pelo script local, deverá configurar o
tracer com a seguinte precedência:

1. `PINPAD_LOG_FILE`, quando definida e não vazia, é o destino absoluto ou
   relativo informado pelo operador;
2. quando a variável estiver ausente ou vazia, o script local define
   temporariamente o caminho absoluto
   `<raiz-do-módulo>/logs/LogPinpadAbecs.txt`;
3. a biblioteca consumida por outro processo não infere nem cria esse caminho;
   o consumidor deve chamar a configuração de destino explicitamente.

Todo destino é normalizado e exibido como caminho absoluto antes do menu. Um
`PINPAD_LOG_FILE` relativo é resolvido contra a raiz do módulo, nunca contra o
diretório de trabalho. Na ausência da variável, a composição local encontra a
raiz do módulo pelo ancestral que contém o `go.mod` cujo `module` é
`br.com.romulopenha/lib-pinpad-abecs-go`; a busca parte tanto do diretório de
trabalho quanto do diretório do executável. Se a raiz não puder ser determinada,
o CLI encerra antes do menu e orienta o operador a definir um destino explícito.

O script local deverá criar o diretório `<raiz-do-módulo>/logs/` antes de
iniciar o executável e definir `PINPAD_LOG_FILE` com esse caminho absoluto. O
arquivo `LogPinpadAbecs.txt` usa append UTF-8 e deve ser ignorado pelo Git. O
CLI e o script devem imprimir `Log serial ativo: <caminho-absoluto>`; não podem
exibir apenas o valor relativo solicitado.
Falha ao configurar o destino deve ser exibida pelo CLI e interromper a
execução de validação local, pois sem rastro não é possível coletar evidência
de erro do pinpad.

Para suporte, o operador poderá enviar o arquivo recém-gerado em `logs/`,
desde que confirme que não contém PAN, trilhas, PIN, PIN block, KSN, chave ou
dados pessoais. Um arquivo que contenha qualquer desses dados deve ser
redigido antes do compartilhamento e sua origem deve ser preservada localmente.

### Correlação SPE/PP com comando e status

Como a escrita lógica na porta serial corresponde a um pacote ABECS inteiro, o
adaptador confirma que todos os bytes foram aceitos antes de gerar a linha
`SPE`, usando o nome do comando ativo informado pelo serviço. Como a leitura
pode ser fragmentada pelo driver serial em múltiplas chamadas, cada chamada de
leitura gera sua própria linha `PP` crua (fidelidade ao legado); o status ABECS
só é conhecido depois que o parser reconhecer o pacote completo. Por isso,
após decodificado, o serviço registra uma linha de correlação adicional, sem
repetir o hexadecimal já gravado:

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

onde `<hex bytes>` é a representação hexadecimal maiúscula, dois dígitos por
byte, exatamente dos bytes efetivamente enviados (payload já submetido a
`ApplySubstitution`/`BuildPacket`), na ordem de transmissão. O adaptador serial
real emite essa linha uma única vez, imediatamente após confirmar a escrita
integral. Antes de chamar `Write`, o serviço informa ao adaptador o comando
tipado ativo; esse metadado não altera os bytes transmitidos e existe somente
para `CMD=` e redaction.

Para comandos não sensíveis, omitir `<hex bytes>`, registrar somente a carga
lógica anterior ao framing ou substituir o pacote por mensagem textual viola
este requisito.

### RF-L004 — Registro de bytes recebidos (PP)

Toda leitura da porta serial que retornar ao menos um byte deverá gerar uma linha:

```text
[<id>] PP  <hex bytes separados por espaço>
```

com o mesmo formato hexadecimal de RF-L003, incluindo ACK/NAK/EOT isolados e frames completos.

O `PP` deve conter os bytes devolvidos pela chamada física de leitura, antes de
remoção de substitution, descarte de ACK ou parsing. O parser não pode consumir
nem normalizar bytes antes da emissão dessa linha.

### RF-L005 — Identificação do comando e da direção

comandos pertencentes ao catálogo normativo de `internal/domain/command`):

```text
[<id>] SPE <hex> CMD=<nome-do-comando>
```

Quando a escrita não corresponder a um comando tipado (por exemplo, envio isolado de CAN/NAK como parte do protocolo de baixo nível), a linha deverá omitir `CMD=` e conter apenas o rastro hexadecimal, sem inventar um nome de comando. O status resultante (`RSP_STAT`) é registrado na linha de correlação `RSP`, conforme descrito acima, e não na linha `PP` bruta.

A cobertura obrigatória inclui todos os comandos tipados do catálogo desta
Change: `CAN`, `OPN`, `CLO`, `CLX`, `GIX`, `DSP`, `DEX`, `MNU`, `DSI`, `MLI`,
`MLR`, `MLE`, `TLI`, `TLR`, `TLE`, `GKY`, `GCX`, `GTK`, `GOX`, `FCX`, `GPN` e
A operação composta gera uma sequência para cada comando realmente enviado.
enviado. Operações reservadas ou rejeitadas antes da serialização, como
`TransactionGCX` enquanto retornar `ErrNotImplemented`, não inventam uma linha
`SPE`.

### RF-L006 — Redação de dados sensíveis

Salvo a exceção local e explícita de RF-L006.1, o rastro de comunicação nunca
deverá expor, em texto plano ou hexadecimal, valores reais de PAN, TRACK2, PIN,
PIN block, KSN, WKENC, conteúdo de mídia, registros de tabela EMV, material
RSA/AES/KSEC/IV, criptogramas ou dados EMV sensíveis, mesmo quando esses bytes
fizerem parte do payload transmitido.

A linha deverá substituir o frame de dados completo por
`**REDACTED(<n> bytes)**`, preservando `CMD=` e o `STATUS=` da linha `RSP`, nos
seguintes casos:

- `GPN`, `GCX`, `GTK`, `GOX` e `FCX`, em qualquer direção;
- blocos `MLR` e registros `TLR`;
- negociação `OPN` que transporte material RSA/KSEC;
- qualquer pacote enviado ou recebido sob comunicação segura;
- DSP, DEX, MNU ou outro comando que o consumidor marque como sensível.

Bytes de controle isolados ACK, NAK, EOT e CAN não contêm payload e permanecem
visíveis em hexadecimal, mesmo durante um comando redigido. A aplicação deve
fornecer ao adaptador, junto do comando ativo, a política de redaction necessária
antes do I/O; o tracer não tenta descobrir segredo inspecionando offsets. A
redação total elimina risco de vazamento parcial e prevalece sobre o valor de
diagnóstico dos bytes.

#### RF-L006.1 — Exceção local para GTK em claro

Quando, e somente quando, o operador selecionar explicitamente “Em claro” na
opção 19 do executável local, após uma resposta GTK `000` validada o CLI deve
gravar uma linha textual `GTK_CLEAR TRACK1=<valor> TRACK2=<valor>
TRACK3=<valor>` no mesmo arquivo ativo. Os valores devem ser delimitados e
escapados para impedir quebra ou injeção de linha. Campos ausentes devem ser
registrados como strings vazias.

Antes da gravação, `TRACK1` deve ser tratada como ASCII e `TRACK2`/`TRACK3`
devem ser convertidas da codificação de um símbolo por nibble definida na seção
5.4.2.2 do manual: nibbles `0`–`9` representam dígitos, `D` representa o
separador `=` e `F` é aceito somente como filler final. Outros nibbles ou `F`
antes do final invalidam o registro.

Essa linha é produzida pelo CLI a partir de `GTKResponse`; ela não altera a
classificação sensível de `CommandGTK`, não libera os bytes `PP`, não inclui KSN
nem material de chave e não é emitida para métodos criptografados. A API da
biblioteca permanece sem logging automático desses valores.

#### RF-L006.2 — Diagnóstico não sensível do GOX

Antes de executar a opção 20, o utilitário local deve registrar uma linha
`GOX_CONFIG` contendo somente `SPE_ACQREF`, `SPE_MTHDPIN` e `SPE_KEYIDX` já
validados. A linha não pode receber nem registrar `SPE_WKENC`, PIN, PIN block,
KSN, PAN, trilhas, dados EMV ou qualquer material criptográfico. O frame `SPE`
do GOX permanece integralmente redigido.

### RF-L007 — Erros de sistema

Falhas de abertura, escrita, leitura e fechamento da porta deverão ser registradas no rastro de comunicação com o erro Go retornado (`err.Error()`), sem depender de `errno`/`strerror` (específicos de C), preservando o encadeamento de erro (`%w`) já usado no restante do módulo.

### RF-L008 — Integração não invasiva

A instrumentação de RF-L002 a RF-L007 deverá ser aplicada ao adaptador real de `SerialPort` (RF-007 de `spec.md`) e ao ponto único de envio/recebimento usado pela fila/worker (RF-009), sem duplicar logs para o mesmo byte em múltiplas camadas. O fake de `SerialPort` usado em testes não é obrigado a gravar no rastro de comunicação; a instrumentação será comprovada com uma instância isolada de `Tracer` e destinos temporários.

### RF-L008.1 — Ordem, uma única emissão e sessão de porta

- O adaptador serial real é a fonte única dos eventos `open`, `close`, `SPE`,
  `PP` e falhas de I/O; a camada de aplicação não poderá repetir os mesmos
  bytes nem os mesmos erros.
- A camada de aplicação informa o comando ativo antes de `Write` e é a fonte
  única de `RSP CMD=<tipo> STATUS=<código>`. Em falha de escrita, o adaptador
  registra somente o evento de erro; não existe `SPE` para bytes cuja escrita
  integral não foi confirmada.
- A sessão lógica deve ser incrementada somente após uma abertura bem-sucedida
  e permanecer igual até `close()`. O formato recomendado é `[COM7#001]`; um
  número de handle de C/JNI é somente referência histórica e não faz parte do
  contrato Go.
- Um ACK/NAK/EOT lido isoladamente continua sendo uma linha `PP`; ele não pode
  ser omitido nem combinado com a linha `RSP`.
- Cada linha deve terminar com `\n`, ser escrita integralmente sob o mesmo lock
  e não pode ser reordenada em relação à chamada de I/O que representa.

### RF-L009 — Desempenho e concorrência

O rastro de comunicação deverá usar exclusão mútua (`sync.Mutex` ou equivalente) para proteger a escrita no arquivo de destino, prevenindo intercalação de bytes entre goroutines. A ausência de destino configurado (RF-L001) não deverá adicionar overhead perceptível (nenhuma formatação de hexa deverá ocorrer quando o rastro estiver desligado).

### RF-L010 — Visibilidade e falhas de persistência

Cada linha deve ser integralmente entregue ao arquivo antes de a operação de
rastro retornar. Se a implementação usar buffer em espaço de usuário, deverá
executar `Flush` após cada linha; o encerramento deverá executar o flush final
antes de fechar o descritor. Uma leitura independente do arquivo durante a
execução deve observar o marcador de ativação e todos os eventos já concluídos,
sem exigir o encerramento do CLI.

Erros de escrita, flush ou fechamento não podem ser descartados. O tracer deve
retorná-los ao ponto de composição ou publicá-los por um mecanismo explícito e
testável. No utilitário de validação local, qualquer falha desse tipo deve ser
exibida em `stderr`, registrada no `slog` quando possível e encerrar a sessão
de validação com código diferente de zero. A biblioteca não deve substituir o
erro primário de comunicação; quando ambos existirem, as causas devem ser
preservadas de forma distinguível.

## Requisitos não funcionais

- Go idiomático; nenhuma tradução literal de `FILE*`, `fopen`, `fwrite` ou `errno`.
- Sem CGO; compatível com Windows e Linux.
- Sem corrida de dados em `go test -race ./...`.
- Nenhum dado sensível real em testes ou fixtures.
- Cobertura de testes aplicável mínima de 80% para o pacote `internal/infrastructure/logging` e para a instrumentação adicionada em `internal/infrastructure/serial`.

## Regras de negócio

- O rastro de comunicação é opt-in: desabilitado por padrão até que um destino seja configurado.
- Direção de escrita é sempre `SPE`; direção de leitura é sempre `PP`.
- Nenhuma linha de rastro poderá conter PAN, TRACK2, PIN, PIN block, KSN ou
  WKENC em claro, exceto `TRACK1`/`TRACK2`/`TRACK3` na linha `GTK_CLEAR`
  autorizada por RF-L006.1.
- Reconfigurar o destino do log nunca poderá deixar um arquivo aberto sem uso (vazamento de descritor).

## Cenários e critérios de aceite

- [ ] **CA-L001:** `SetLogDestination` com caminho válido cria/abre o arquivo em modo *append* e passa a registrar `open`, `close`, `SPE` e `PP`.
- [ ] **CA-L002:** `SetLogDestination("")` desliga o rastro; nenhuma linha adicional é gravada após a chamada.
- [ ] **CA-L003:** chamadas sucessivas de `SetLogDestination` fecham o arquivo anterior sem vazar descritor (verificável via teste que abre, troca de destino e verifica que o arquivo antigo pode ser removido/renomeado no SO).
- [ ] **CA-L004:** escrita de um payload conhecido gera linha `SPE` com
  hexadecimal exatamente igual ao buffer entregue e aceito integralmente pelo
  driver, incluindo framing, CRC e casos com `0x13`, `0x16`, `0x17` após
  `ApplySubstitution`; registrar apenas `CMD=` ou payload lógico reprova o teste.
- [ ] **CA-L005:** cada retorno não vazio do driver para ACK, NAK, EOT, frame
  completo ou fragmento gera uma linha `PP` cujo hexadecimal é byte a byte
  igual ao buffer recebido, antes do parser; registrar apenas status ou resposta
  resumida reprova o teste.
- [ ] **CA-L006:** envio de um comando decodificável (por exemplo `OPN`) inclui `CMD=OPN` na linha `SPE`; a resposta correspondente gera uma linha `RSP CMD=OPN STATUS=<codigo>` após a decodificação completa.
- [ ] **CA-L007:** envio/recebimento de comando `GPN`/`GCX` com dados sensíveis simulados demonstra que o payload aparece totalmente redigido (`**REDACTED(<n> bytes)**`) no log, mantendo `CMD=`/`STATUS=`.
- [ ] **CA-L008:** falha simulada de abertura da porta gera linha `open(...)=>ERRO: <mensagem>` e `SetLogDestination`/estado de log preservam o destino anterior.
- [ ] **CA-L009:** `go test -race ./...` cobre gravação concorrente de múltiplas linhas de rastro sem corrida de dados.
- [ ] **CA-L010:** com o rastro desligado (nenhum destino configurado), nenhuma linha `SPE`/`PP`/`open`/`close` é produzida e nenhuma alocação de formatação hexadecimal ocorre no caminho crítico (validável por teste ou benchmark comparativo).
- [ ] **CA-L011:** executar o script local sem `PINPAD_LOG_FILE` cria e ativa o
  caminho absoluto `<raiz-do-módulo>/logs/LogPinpadAbecs.txt`; o destino
  explícito definido pelo operador prevalece e nenhum dos dois arquivos é
  versionado.
- [ ] **CA-L012:** em um cenário GCX de laboratório, a sequência registrada é
  `SPE CMD=GCX`, um ou mais `PP`, `RSP CMD=GCX STATUS=<código>` e `close()`
  quando aplicável; o conteúdo hexadecimal de GCX/GTK/GOX/FCX/GPN permanece
  integralmente redigido. A linha textual `GTK_CLEAR` prevista em RF-L006.1
  não altera a redação do frame hexadecimal.
- [ ] **CA-L013:** duas instâncias isoladas de `Tracer`, configuradas com
  destinos temporários distintos, não misturam linhas; uma construção sem
  tracer permanece desabilitada e não cria arquivo.
- [ ] **CA-L014:** iniciar o CLI a partir da raiz do módulo e de
  `cmd/libpinpadabecsgo` resolve e exibe o mesmo caminho absoluto
  `<raiz-do-módulo>/logs/LogPinpadAbecs.txt`; nenhum arquivo homônimo é criado
  sob `cmd/`, `.bin/` ou outro diretório de trabalho.
- [ ] **CA-L015:** imediatamente após a configuração e antes de `Open`, uma
  leitura independente encontra a linha `TRACE ... enabled`; após um GIX bem-
  sucedido e antes de encerrar o processo, encontra `SPE CMD=GIX`, ao menos um
  `PP` e `RSP CMD=GIX STATUS=000`, todos com `FUNC` e `DATA_HORA` válidos. A
  evidência deve comparar os bytes da linha `SPE` com o buffer escrito e os
  bytes de cada linha `PP` com cada retorno físico de leitura.
- [ ] **CA-L016:** teste orientado a tabela percorre `CAN`, `OPN`, `CLO`, `CLX`,
  `GIX`, `DSP`, `DEX`, `MNU`, `DSI`, `MLI`, `MLR`, `MLE`, `TLI`, `TLR`, `TLE`,
  `GKY`, `GCX`, `GTK`, `GOX`, `FCX` e `GPN`, comprovando `CMD=`, ordem,
  emissão única e redaction conforme a classificação do comando.
- [ ] **CA-L017:** falhas simuladas de escrita, flush e fechamento do destino
  são observáveis e não permitem que o CLI de validação continue ou finalize
  com código zero como se o rastro estivesse íntegro.
- [ ] **CA-L018:** a opção 20 grava `GOX_CONFIG ACQ=<N2> PIN_METHOD=<N1>
  KEY_INDEX=<N2>` antes do comando e o teste confirma a ausência dos campos
  sensíveis listados em RF-L006.2.

## Validação

Os critérios de concorrência, append, troca de destino, criação do diretório e
redaction podem ser cobertos por testes automatizados sem hardware. A
confirmação de que os eventos correspondem aos bytes efetivamente trocados deve
ser repetida com pinpad físico, porta serial real e dados de teste de
laboratório. Logs de validação não podem ser anexados se contiverem PAN, PIN,
KSN, trilhas, chaves ou dados de portador.

A validação operacional deve registrar o diretório de trabalho, o caminho
absoluto exibido pelo CLI, o tamanho do arquivo antes/depois e a leitura do
arquivo ainda durante o processo. Visualizadores que não recarregam arquivos
automaticamente devem ser atualizados ou reabertos; a evidência primária é a
leitura independente do caminho absoluto informado pelo CLI.

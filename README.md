# lib-pinpad-abecs

Biblioteca Go para comunicação serial com pinpads compatíveis com o protocolo ABECS.

Este branch é dedicado exclusivamente ao projeto `lib-pinpad-abecs`. A documentação, o código e as mudanças descritas aqui devem ser interpretados como parte de uma biblioteca de comunicação com hardware físico, não como uma API Java, aplicação web ou servidor de integração.

## Objetivo

Converter para Go o comportamento relevante do projeto legado Java + JNI + C responsável por comunicação com pinpad ABECS, preservando:

- framing e controle de transmissão ABECS;
- envio e recebimento por porta serial real;
- comandos tipados e parsers de resposta;
- fila serializada para impedir operações concorrentes no mesmo pinpad;
- cancelamento, timeout, retransmissão e encerramento seguro;
- logging detalhado SPE/PP/RSP, com redaction de dados sensíveis;
- fluxos de display, multimídia, tabelas EMV, transação, teclas, PIN e reset, conforme escopo aprovado;
- comunicação segura ABECS, quando especificada e aprovada para a Change correspondente.

A conversão integral ainda deve ser considerada concluída somente após a implementação, revisão e validação dos comandos e funcionalidades do legado. A existência de builders ou constantes Go não é suficiente para declarar equivalência funcional.

## Escopo do produto

O produto desta linha é uma biblioteca Go headless e reutilizável. Ela não é um servidor e não abre endpoints de rede.

### Incluído

- Windows e Linux;
- porta serial por `go.bug.st/serial`;
- ausência de CGO no módulo Go;
- configuração por ambiente e por `PinpadConfig`;
- protocolo ABECS, CRC-16-CCITT e substitution;
- parser de resposta ABECS e BER-TLV;
- modelos de dispositivo, estado, resposta, capacidades e transação;
- catálogo individual de comandos ABECS;
- fila FIFO e worker único por instância;
- `context.Context` em operações bloqueantes;
- `SessionManager` para posse lógica do pinpad;
- logging operacional com `log/slog`;
- rastreamento bruto de comunicação configurável;
- testes unitários, concorrência e validação com hardware real.

### Fora de escopo deste módulo

- REST, HTTP, WebSocket, gRPC ou listener TCP;
- bridge de rede do legado;
- UI web, desktop ou smartphone;
- Windows Service, Linux daemon ou instalador;
- persistência de PAN, PIN, KSN ou dados EMV;
- API pública de comando hexadecimal bruto;
- tradução literal de tipos, handles, mutexes e chamadas de sistema C/C++;
- mocks como substitutos da validação final com pinpad físico.

## Protocolo ABECS

A documentação funcional desta branch utiliza o manual ABECS fornecido para a Change, além dos artefatos legados como referência comportamental.

Parâmetros seriais padrão:

| Parâmetro | Valor |
| --- | --- |
| Baud rate | `19200` |
| Dados | `8 bits` |
| Paridade | nenhuma |
| Stop bits | `1` |

Bytes de controle:

| Constante | Valor | Função |
| --- | --- | --- |
| `SYN` | `0x16` | início de frame |
| `ETB` | `0x17` | fim lógico do bloco |
| `ACK` | `0x06` | confirmação |
| `NAK` | `0x15` | rejeição/retransmissão |
| `EOT` | `0x04` | fim/cancelamento de operação |
| `CAN` | `0x18` | cancelamento |
| `DC3` | `0x13` | byte de substitution |

O frame utiliza payload com substitution, `ETB` e CRC-16-CCITT. Uma leitura da biblioteca serial pode conter ACK, NAK, EOT, um frame inteiro ou partes de vários frames; o leitor deve preservar bytes excedentes.

## Comandos cobertos

Cada comando deve possuir uma SPEC própria em [specs/changes/2026-09-09-066-lib-pinpad-abecs-go/](specs/changes/2026-09-09-066-lib-pinpad-abecs-go/), com payload, resposta, estados, timeout, cancelamento, logging, redaction e critérios de aceite.

| Grupo | Comandos |
| --- | --- |
| Ciclo de vida | `CAN`, `OPN`, `CLO`, `CLX`, `RST` |
| Informações | `GIX` |
| Display | `DSP`, `DEX`, `MNU`, `DSI` |
| Multimídia | `MLI`, `MLR`, `MLE` |
| Tabelas EMV | `TLI`, `TLR`, `TLE` |
| Teclas | `GKY` |
| Transação/cartão | `GCX`, `GTK`, `GOX`, `FCX` |
| PIN | `GPN` |
| Infraestrutura | cancelamento de leitura serial e logging SPE/PP/RSP |

Quando um comando estiver apenas parcialmente convertido, a SPEC deve declarar explicitamente o subconjunto implementado e o que permanece pendente. Não devem ser inventados campos que o manual atribui a outro comando.

## Segurança e redaction

Nenhum log, erro, fixture ou documento deve conter dados reais de cartão ou material criptográfico.

São sempre sensíveis:

- PAN;
- trilhas e dados de tarja;
- PIN e PIN block;
- KSN e WKENC;
- dados EMV sensíveis;
- chaves, KSEC, IV e material RSA/AES.

O rastro de comunicação é opt-in. Quando habilitado:

- `SPE` registra bytes enviados pelo software;
- `PP` registra bytes recebidos do pinpad;
- `RSP` correlaciona comando e status após o parse completo;
- `GPN` e respostas sensíveis de `GCX` têm payload integralmente redigido;
- abertura, fechamento e erros de I/O são registrados sem expor segredos.

## Arquitetura

```text
Consumidor Go
	|
PinpadService
	|
CommandQueue / Worker único
	|
SerialPort (porta própria)
	|
go.bug.st/serial
	|
Pinpad ABECS físico
```

Estrutura principal:

```text
apps/desktop/libpinpadabecsgo/
├── cmd/libpinpadabecsgo/        executável de validação
├── internal/domain/
│   ├── model/                   modelos e contratos
│   ├── protocol/                framing, CRC e controles ABECS
│   ├── parser/                  respostas ABECS e BER-TLV
│   ├── command/                 catálogo e builders
│   ├── queue/                   contratos de fila
│   ├── session/                 posse lógica
│   ├── state/                   status ABECS
│   └── error/                   erros tipados
├── internal/application/service/ fachada de orquestração
├── internal/infrastructure/
│   ├── serial/                  adaptador real e adaptadores de teste
│   ├── config/                  configuração
│   ├── logging/                 slog e tracer SPE/PP/RSP
│   └── worker/                  fila e worker
└── internal/utilitario/         CRC, TLV e bytes
```

## Configuração local

Variáveis:

| Variável | Obrigatória | Padrão | Descrição |
| --- | --- | --- | --- |
| `PORTA_PINPAD` | recomendada | `COM3` no modelo | porta serial do pinpad |
| `PINPAD_BAUDRATE` | não | `19200` | velocidade serial |
| `PINPAD_TIMEOUT` | não | `30s` | timeout de leitura/operação |
| `LOG_LEVEL` | não | `INFO` | nível do `slog` |

No Windows, a porta real pode ser `COM1` até `COMn`. No Linux, pode ser `/dev/ttyS*`, `/dev/ttyUSB*` ou `/dev/ttyACM*`.

Para execução local, consultar o README interno do módulo e o script [start_aplicacao.bat](apps/desktop/libpinpadabecsgo/start_aplicacao.bat), quando disponível. A configuração é temporária para o processo; não gravar credenciais ou configurações permanentes no sistema.

## Desenvolvimento e validação

Executar os comandos a partir de `apps/desktop/libpinpadabecsgo/`:

```text
gofmt -w .
go build ./...
go vet ./...
go test ./...
go test ./... -coverprofile=coverage.out

go test -race ./...
```

Os testes unitários podem usar adaptadores determinísticos para CRC, framing, parser, fila e validação de erros. Isso não substitui a validação de integração:

1. conectar um pinpad físico compatível;
2. identificar a porta serial correta;
3. executar abertura e comunicação real;
4. exercitar o comando da SPEC correspondente;
5. capturar somente logs redigidos;
6. registrar versão do dispositivo, porta, comando, resultado e código de saída em `validation.md`;
7. nunca registrar PAN, PIN, KSN, trilhas ou chaves.

Se o hardware não estiver disponível, registrar a limitação. Não marcar cenário físico como aprovado com base somente em fake ou mock.

## Processo Spec Driven

A Change principal está em [specs/changes/2026-09-09-066-lib-pinpad-abecs-go/](specs/changes/2026-09-09-066-lib-pinpad-abecs-go/).

Fluxo obrigatório:

1. inventariar o legado Java/JNI/C;
2. criar ou atualizar a SPEC individual do comando;
3. revisar e aprovar a SPEC;
4. implementar apenas o contrato aprovado;
5. executar testes unitários e verificações Go;
6. revisar a implementação contra a SPEC;
7. validar com pinpad físico quando o cenário exigir hardware;
8. registrar evidências em `reviews/` e `validation.md`;
9. aprovar, atualizar `specs/system/` e somente então arquivar/commitar.

Arquivos importantes:

| Arquivo | Finalidade |
| --- | --- |
| [AGENTS.md](AGENTS.md) | regras obrigatórias do repositório |
| [spec.md](specs/changes/2026-09-09-066-lib-pinpad-abecs-go/spec.md) | contrato geral da biblioteca |
| [DESIGN.md](specs/changes/2026-09-09-066-lib-pinpad-abecs-go/DESIGN.md) | arquitetura e decisões |
| [tasks.md](specs/changes/2026-09-09-066-lib-pinpad-abecs-go/tasks.md) | execução e pendências |
| [resumo_continuar_projeto.txt](specs/resumo_continuar_projeto.txt) | contexto consolidado para continuidade por outro modelo |
| [specs/shared/process/workflow.md](specs/shared/process/workflow.md) | etapas e gates da Change |
| [apps/desktop/libpinpadabecsgo/](apps/desktop/libpinpadabecsgo/) | implementação Go |

## Estado atual

O núcleo de framing, CRC, parte dos builders, parsers, fila, adaptador serial e fachada parcial já existe. Ainda devem ser tratados, revisados ou validados, entre outros pontos:

- conversão integral das funções Java/JNI/C;
- logging SPE/PP/RSP completo;
- parser funcional de GCX;
- GIX e capacidades de display;
- cancelamento de leitura com contexto;
- status especial de TLI;
- TransactionGCX completo, quando especificado;
- GTK, GOX e FCX;
- OPN/CLO/CLX e comunicação segura RSA/AES;
- resolução dos achados IMP-REV-001 a IMP-REV-015;
- testes de cobertura, race e validação física.

Não declarar a conversão integral concluída enquanto esses itens não estiverem implementados, formalmente excluídos ou validados conforme as SPECs aprovadas.

## Referências

- [Change 066 — lib-pinpad-abecs-go](specs/changes/2026-09-09-066-lib-pinpad-abecs-go/)
- [Resumo para continuidade](specs/resumo_continuar_projeto.txt)
- [Regras do agente](AGENTS.md)
- [Skills locais](.agents/skills/)
- [Histórico de mudanças](specs/archive/)

**Responsável:** Rômulo Penha

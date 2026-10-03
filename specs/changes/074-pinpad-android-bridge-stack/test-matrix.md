# Matriz de testes — 074 Pinpad Android Bridge Stack

## Regra

Esta matriz define evidências futuras; não declara que os testes foram
executados. O resultado atual está em validation.md.

| Área | Cenário mínimo | Tipo | Evidência esperada |
|---|---|---|---|
| ABECS framing | CRC, substitution, frame vazio, bytes 13/16/17, truncamento | unitário | vetor byte a byte e erro tipado |
| ABECS enlace | ACK, NAK, três tentativas, EOT, CAN | unitário/integridade | sequência observada e limite de 2s |
| Resposta | chunks, frames agrupados, bytes excedentes, CRC inválido | unitário | parser sem perda |
| BER-TLV | tag curta/longa, length estendido, aninhamento, truncamento | unitário | árvore e erro |
| Catálogo | builder/parser de cada comando da SPEC | unitário | tabela de entradas/saídas |
| Segurança | OPN seguro, KSEC, AES-CBC, CLO/CLX | unitário + físico | vetor sanitizado e confirmação no pinpad |
| Estado | CLOSED, OPEN, BUSY, DESYNCHRONIZED, falha de OPN | unitário | transições |
| Sessão | claim, conflito, renew, release, expiração 300s | unitário | relógio controlado |
| Fila | FIFO 100, Enqueue, Submit, Clear, Stop | unitário | ausência de execução pós-Stop |
| Serial | cancelamento durante Read, timeout, I/O, bytes excedentes | unitário + físico | ctx.Err e ausência de leak |
| PBRG | magic, versão, tipos, limites, UTF-8, partial I/O | unitário | reject antes do payload |
| EmulatorTransport | Ping, Open, Close, disconnect, close ACK/EOF | integração | sessão/reabertura e nenhum replay |
| Bridge | ownership antes da serial, erro, release e read fatal ocioso | integração | eventos e cleanup aguardado |
| Windows ownership | dois processos, mutex abandonado, reabertura | multiprocesso | exclusividade e categoria correta |
| Configuração | BAT físico e entrypoint direto com ambiente herdado/ausente/vazio/inválido; defaults não relacionados à COM; caminho com espaços | launcher + unitário/integração | ambos falham sem variável ou com vazio/inválido; não há COM fixa/fallback; porta e origem coincidem em serial/ownership |
| BAT físico 071/072 | execução de outro diretório, `setlocal`, Go/módulo ausentes, COM herdada/ausente/vazia/inválida, transporte scripted herdado, overrides válidos, configuração/log visíveis, caminho com espaços, ADB ausente, bind ocupado e saída do Bridge | launcher Windows | comando, ambiente sanitizado, mensagem e exit code real; sem COM fixa, sem encerrar processo concorrente e sem declarar hardware validado |
| ADB | zero/um/vários/offline/seleção explícita | helper | reverse confirmado ou emulator_not_prepared |
| Logging | destino único, append, eventos, persistência e redaction | unitário/integridade | conteúdo seguro após cada evento |
| Mobile | operationID, cancelamento, panic boundary, summaries | unitário | API gomobile admissível |
| AAR | bind, consumo Kotlin, proveniência, hash | build Android | AAR/APK correspondentes |
| UI Android | estado, erro visível, acessibilidade, catálogo | JVM/Compose/instrumentado | asserções sem cor como única pista |
| REST | DTOs, rotas, métodos, status, alias reset | httptest | ErrorBody correlacionado |
| Scripted | Ping → Open → GIX → DSP → Close → Open | integração | log real scripted, sem declarar físico |
| Hardware | OPN/CLO, comandos, timeout, recovery, DSI visual | físico | confirmação do operador e log sanitizado |

## Gates

1. Unitários e vet devem passar antes de integração.
2. Integração de processo e Android exige artefatos correspondentes ao build.
3. Race detector deve rodar em ambiente suportado; indisponibilidade é
   limitação, não sucesso.
4. Scripted e físico devem ser relatados em blocos separados.
5. DSI só é aceito com status do firmware e observação visual.

# Diagnóstico de baseline — 072

## Evidências confirmadas

Inspeção somente leitura em 27/09/2026, Windows 10, PowerShell, repositório
`D:\desenvolvimento\ia\lib-pinpad-abecs`.

| ID | Evidência | Consequência demonstrável |
|---|---|---|
| DIA-001 | `testar_bridge_pinpad.bat:7` atribui uma porta fixa incondicionalmente | outra PORTA_PINPAD herdada seria ignorada |
| DIA-002 | `DiagnosticViewModel.kt:87` marca OPEN após Ping | sucesso de transporte aparenta abertura ABECS |
| DIA-003 | `DiagnosticViewModel.kt:114` marca OPEN após qualquer ação salvo Close | consulta que retorna CLOSED habilita ações de sessão |
| DIA-004 | `DiagnosticScreen.kt:197` renderiza erro após catálogo iniciado em :162 | erro de Abrir não aparece na área de estado inicial |
| DIA-005 | `bridge/server.go:76` descarta erro de handle | falhas sem diagnóstico de sessão |
| DIA-006 | `bridge/server.go:110` faz ownership antes de Open em :118 | falha do lock não gera rastro serial de Open |
| DIA-007 | `bridge/server.go:165` consulta serialErr só após frame TCP | falha serial pode não despertar peer ocioso |
| DIA-008 | `ownership_windows.go:39-43` adquire/libera mutex sem worker com afinidade de thread; switch não trata abandono | contrato de lifecycle Windows insuficiente |
| DIA-009 | tracer do entrypoint é associado aos transportes; Adapter.Open registra sucesso/falha | arquivo com só ativação não comprova defeito de escrita do tracer |

Arquivos Kotlin ficam em
`apps/frontend/smartphone/diagnosticopinpad/app/src/main/java/br/com/romulopenha/diagnosticopinpad/ui/`.
Bridge/ownership ficam em `apps/desktop/libpinpadabecsgo/internal/infrastructure/`.
Linhas são referência do baseline, não promessa de posição após a correção.

## Observações de runtime e anexos

- As evidências do usuário mostram uma porta enumerada e `PORTA_PINPAD` de usuário; o valor deve sempre ser lido do ambiente.
- Imagens mostram Open com ERROR, consulta Estado com OPEN e posterior
  `pinpad is closed`/resultado CLOSED. O código DIA-003 explica o falso estado
  da UI; não comprova a causa inicial de Open.
- Tail do arquivo Windows contém somente duas ativações do tracer
  (21:01:56 e 22:03:43), sem Open/Read/Write. Pode significar que nenhuma
  tentativa alcançou a serial ou que uma fase anterior falhou sem evento.
- Na consulta anterior havia listener PID 24620. Na consulta posterior esse
  processo/listener não existia e ADB não listava reverse.
- Rechecagem em `2026-09-27T22:11:26-03:00`: sem listener 39100 listado e
  sem reverse em `emulator-5554`. ADB reconhecia o emulador na consulta anterior.

Não concluir que o processo ausente foi a causa da primeira falha, nem que
COM14 está disponível para abertura, só com essas observações.

## Hipóteses ainda sem reprodução conclusiva

| Hipótese | Evidência necessária |
|---|---|
| Processo host encerrou/conflito de porta ou build | exit code, PID e evento de bind do novo launcher |
| Reverse perdido após reinício/host localhost inacessível | reverse por serial e Ping end-to-end |
| COM14 ocupada/driver indisponível | causa sanitizada de ownership/Open físico |
| OPN falhou após abertura serial | timeline CAN/OPN redigida e estado final do core |
| Falha no lock/thread/abandono | teste multiprocesso Windows e eventos de release |
| Falha de escrita do log | erro de persistência e teste com tracer real |

Não se implementa correção exclusiva para uma hipótese deixando os defeitos
confirmados sem cobertura. Primeiro teste de implementação deve reproduzir
Ping com sessão fechada e Estado CLOSED que hoje viram OPEN.

# DESIGN — 072 Corrigir abertura, estado e log Android/Bridge

## Decisão arquitetural

Manter composição no entrypoint Go, Bridge/ownership/logging na infraestrutura,
ABECS no Service e estado de tela no ViewModel. A mudança corrige contratos
existentes; não introduz backend HTTP, parser Kotlin ou módulos novos.

## Componentes e responsabilidade

| Componente | Alteração prevista |
|---|---|
| BAT do módulo | preservar ambiente; checar pré-requisitos; preparar reverse; preservar exit code |
| Helper PowerShell do módulo | descobrir/selecionar emulador e preparar reverse de modo verificável |
| `cmd/libpinpadabecsgo-bridge` | configuração/origem, tracer único, startup/erro/shutdown |
| `bridge.Server` | readiness real, eventos de sessão, erro correlacionado, cleanup completo |
| `ownership` | posse Windows correta e liberação idempotente |
| `logging.Tracer` | eventos técnicos seguros no mesmo destino, append e persistência |
| `serial.Adapter` / scripted | preservar rastro, bytes, redação e falhas de driver |
| `EmulatorTransport` / `mobile.Client` | preflight, classificação de erro, desconexão e estado real |
| Repository/ViewModel/Compose | separar sessão de ação; erro visível; botões coerentes |

## Sequência normal

1. BAT valida ambiente e prepara reverse se emulador estiver selecionável.
2. Entrypoint prepara log e dependências e solicita startup do servidor.
3. Após bind bem-sucedido, servidor registra `bridge_listening`.
4. Abrir no Android inicia Ping controlado, sem COM, se cliente fechado.
5. Open faz HELLO/ACQUIRE, ownership e abertura serial no Windows.
6. Service Android realiza CAN/OPN pelo transporte existente.
7. Somente sucesso do Service autoriza estado de sessão aberta na UI.
8. GIX/DSP atravessam o mesmo stream; Fechar libera serial e ownership.

Os passos 4–7 compartilham o prazo total da ação. Não se dobra o timeout ao
adicionar preflight. ID derivado para Ping, se necessário, deve ser rastreável
ao ID pai e caber no limite do envelope. Não se inicia Ping paralelo em uma
sessão com operação ativa.

## Observabilidade

Tracer é injetado explicitamente; sem logger global ou segundo arquivo de
rastro. Eventos de ciclo de vida recebem campos controlados e passam pelo
mesmo mecanismo de serialização e limite de mensagem. Log de infraestrutura
não deve fabricar `open()=>OK`, respostas ABECS ou bytes TX/RX.

Ready deve ser emitido por callback/evento do servidor após listen. Criar
goroutine ou imprimir a intenção antes do bind não serve como readiness.
Startup deverá permitir cleanup com `defer`; erro termina com exit code após
fechar recursos, em vez de usar `log.Fatal` onde ele impediria cleanup.

Bridge registra causas internas sanitizadas, envia código/mensagem pública e
preserva correlationId. Limites/redação incluem caminhos, controles e textos
de terceiros. Integração testa rastro sobre chunks fragmentados e agregados,
incluindo dados sintéticos sensíveis; se política atual não garantir redação,
usar somente contagens para chunks desconhecidos. Não adicionar interpretação
de comando ao servidor para enriquecer logs.

## Estado e erros Android

Estado de operação e estado da sessão são campos distintos. `ERROR` não
equivale automaticamente a sessão fechada, nem `PONG` a sessão aberta. Após
ações relevantes, Repository obtém estado real pela fachada; após queda
confirmada, invalida o cliente/transporte. Apenas GetState local sem verificação
de conectividade não comprova saúde remota.

Fachada traduz erros para categorias estáveis; Repository não recebe causas
arbitrárias do driver. ViewModel guarda último ID/erro seguro/duração e não
perde esses campos ao zerar a operação ativa. Compose usa cartão/aviso antes do
catálogo e aviso acessível ou deslocamento de scroll para erros fora da área
visível. Estado não depende de analisar texto de resultado como `"CLOSED"`
quando a fachada pode fornecer o estado explicitamente.

## Ownership Windows e workers

A API Windows associa mutex à thread adquirente. O código atual não estabelece
afinidade entre `WaitForSingleObject` e `ReleaseMutex`; isso é uma deficiência
de contrato, mas não prova que causou a falha específica das imagens.

Para manter mutex nomeado, usar worker que chama `runtime.LockOSThread`,
adquire o mutex e permanece vivo até receber pedido de release. Liberar/fechar
o handle nessa thread, publicar resultado e então fazer `UnlockOSThread`.
O callback de release será idempotente; timeout/cancel durante aquisição não
deixa handle ou worker vivo. Resultado de abandono libera posse e retorna
falha controlada com evento, exigindo tentativa explícita posterior.

Referências oficiais consultadas em 27/09/2026:

- [Microsoft: Mutex Objects](https://learn.microsoft.com/en-us/windows/win32/sync/mutex-objects).
- [Microsoft: ReleaseMutex](https://learn.microsoft.com/en-us/windows/win32/api/synchapi/nf-synchapi-releasemutex).

Canal de erro serial deve despertar o supervisor de sessão e interromper a
leitura TCP ociosa; finalizar todos os workers antes de soltar o transporte
para a próxima conexão. Uma única serialização de saída TCP impede intercalar
frames DATA/PONG/ERROR. Não compartilhar prazos de leitura/escrita de modo que
uma operação apague o deadline da outra. Testes de concorrência verificam isso.

## Compatibilidade e riscos

Layout e limites PBRG v1 permanecem; categorias novas são somente representação
de erros locais/da UI. APIs adicionais da fachada, se indispensáveis, exigem
GoDoc, testes e prova gomobile; sem API raw/genérica. Código Android alterado
exige rebuild/instalação APK; mudança na fachada exige rebuild AAR antes do APK.

O caminho sem hardware testa disponibilidade e regressão de estado, mas não
valida firmware/baudrate/OPN físicos. Falha de log não pode ocultar a causa
primária nem justificar registro de payload. Porta COM enumerada ou listener
aberto não são critérios de sucesso do pinpad.

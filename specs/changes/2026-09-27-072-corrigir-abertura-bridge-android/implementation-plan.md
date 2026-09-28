# Implementation plan — 072

Preparado após decisão `SPEC_APROVADA` de 27/09/2026. Documento preparatório;
não altera o contrato nem registra implementação já executada.

## Ordem de implementação

1. Criar testes que reproduzem falso OPEN após Ping e consulta CLOSED, erro
   oculto e falha ownership sem registro. Usar fake Repository/transport,
   tracer real em diretório temporário e dependências de comando controladas.
2. Corrigir launcher/configuração/reverse. `testar_bridge_pinpad.bat` preserva
   variável herdada, default condicionado, caminhos, código de saída e
   foreground. Criar helper no módulo Go com argumentos documentados,
   descoberta de ADB e seleção de emulador. Não matar listener em conflito.
3. Adicionar API pequena de evento técnico ao tracer e composição explícita
   no entrypoint/Server. Registrar startup pronto só após bind. Testar
   persistência/append/erros por fase com tracer e listener reais.
4. Corrigir lifecycle Bridge/ownership: supervisor de sessão, wakeup em read
   fatal, escrita TCP serializada, prazos independentes e espera de workers
   antes de fechar/liberar transporte. Corrigir mutex Windows com worker de
   thread vinculada, tratamento de abandono e release idempotente.
5. Classificar erros na fachada/transporte preservando código/causa segura e
   ID. Implementar preflight que usa o prazo total da ação e não adquire COM.
   Mudança pública mínima deve ser comprovada pelo binding gomobile real.
6. Corrigir Repository/ViewModel/Compose com estado da sessão separado, ações
   dependentes bloqueadas e mensagem visível/acessível. Manter ID final e
   limpar resultado antigo. Atualizar testes Go/Kotlin/Compose pertinentes.
7. Atualizar READMEs, GoDoc/KDoc e exemplos operacionais. Executar checks,
   gerar AAR quando aplicável e APK, instalar no emulador selecionado e provar
   scripted. Repetir o fluxo físico com COM14 e o log compartilhado.

## Arquivos de impacto provável

- `apps/desktop/libpinpadabecsgo/testar_bridge_pinpad.bat` e novo helper `.ps1`.
- `cmd/libpinpadabecsgo-bridge/main.go` e testes.
- `internal/infrastructure/bridge/server.go`, testes de sessão/integrados.
- `internal/infrastructure/ownership/ownership*.go`, testes Windows.
- `internal/infrastructure/logging/logging.go`, testes de eventos/redação.
- `internal/infrastructure/serial/serial.go`, scripted e testes, se necessário
  para persistência/política de rastro; preservar bytes/protocolo existentes.
- `internal/infrastructure/transport/emulator`, `mobile` e respectivos testes.
- `DiagnosticRepository.kt`, `DiagnosticViewModel.kt`, `DiagnosticScreen.kt`
  e testes Android do projeto `diagnosticopinpad`.
- READMEs dos módulos e evidências da própria change.

Não alterar o core ABECS para compensar erro de UI, reverse ou startup. Se a
reprodução confirmar defeito adicional de protocolo fora desta SPEC, registrar
achado e avaliar contrato específico antes de expandir a implementação.

## Testes e evidências

Mapear cada arquivo de produção alterado ao teste relacionado conforme
`specs/shared/testing/golang-testing.md`. Unitários cobrem estado, configuração,
classificação e falhas. Integração cobre launcher/helper, framing/concorrência,
eventos reais, liberação e dois processos Windows. Testes do helper usam ADB
substituto, sem modificar dispositivos reais para cenários de falha.

Matriz VAL-072-01 a 14 é referência obrigatória. Executar Go test/vet/build,
cobertura e race quando suportado, depois Android unit/lint/assemble e Compose
instrumentado. Usar harness/teste com sincronização explícita, sem sleeps como
prova de cleanup. Do mesmo modo, verificar timeout com contexto determinístico
ou prazo controlado, e não esperar minutos em testes unitários.

Regressão física só declara sucesso com porta efetiva, log alimentado e
confirmação do pinpad para Open/GIX/DSP/Close/reabertura. Se hardware não
responder, registrar categoria, fase, ID e timeline redigida; não declarar o
gate aprovado pelo simples fato de o erro estar diagnosticado.

## Segurança e riscos de execução

Revisar redaction de eventos/erros/chunks; não imprimir ambiente inteiro,
parâmetros de PIN/EMV ou mensagens arbitrárias de driver. Não serializar
payload em novo evento. Manter loopback, PBRG v1 e controle exclusivo da COM.
Erro de logger não pode encobrir causa primária. A auditoria de implementação
ocorre sobre artefatos novos e suas evidências.

Worktree contém mudanças prévias: editar somente o necessário, sem reset,
staging geral ou limpeza de arquivos do usuário. Validar que APK/AAR em uso
correspondem ao build corrigido; reinício/reverse requer evidência atual.
Não arquivar/commitar nesta fase; seguir gates formais depois dos testes.

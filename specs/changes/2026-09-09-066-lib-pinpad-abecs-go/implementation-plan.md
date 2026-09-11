# Plano de implementação — 066-lib-pinpad-abecs-go

## Fatos observados

- O módulo Go já existe em `apps/desktop/libpinpadabecsgo/`, mas a conversão integral ainda não foi concluída.
- A Change define uma biblioteca desktop/headless, sem servidor externo, HTTP, WebSocket ou UI. A fachada interna (`PinpadService`) faz parte do escopo como componente de biblioteca.
- O módulo será criado em `apps/desktop/libpinpadabecsgo/` com `go.mod` `br.com.romulopenha/lib-pinpad-abecs-go`.
- O ambiente disponível informa Go `1.26.5` em Windows 386.
- A dependência serial aprovada é `go.bug.st/serial` e deve permanecer isolada no adaptador de infraestrutura.
- O framing, CRC, parte dos builders/parsers, fila, worker e fachada parcial já estão presentes; os checks de `tasks.md` precisam ser confrontados com o código real.
- A matriz de SPECs individuais está em construção e todas as novas/alteradas SPECs devem retornar a `SPEC_APROVADA` somente após revisão formal.

## Impactos prováveis

- Novo módulo completo sob `apps/desktop/libpinpadabecsgo/`.
- Novos pacotes de domínio para modelos, protocolo, parsers, comandos, fila, sessão, estados e erros.
- Adaptador serial real e fake sob infraestrutura.
- Configuração por ambiente, logging `slog` e worker.
- Fachada de aplicação (`internal/application/service`) equivalente a `PinpadService`, cobrindo ciclo de vida, display, imagens/QR Code, EMV, GCX (subconjunto), GKY, RST e GPN.
- Builders, parsers e fluxos para DSP, DEX, MNU, DSI, MLI, MLR, MLE, TLI, TLR, TLE, GKY, GPN, GCX (subconjunto) e RST, além das SPECs pendentes de CAN, OPN, CLO/CLX, GTK, GOX, FCX e comunicação segura.
- Fila `CommandQueue` com `Enqueue` não bloqueante, `Submit` bloqueante e `SessionManager` com relógio injectável.
- Executável de validação em `cmd/libpinpadabecsgo`.
- README operacional do módulo.
- Nenhuma API HTTP, persistência, frontend, listener de rede ou alteração em aplicações existentes.
- `TransactionGCX` com parâmetros completos existirá apenas como stub `ErrNotImplemented` nesta Change.

## Estratégia de implementação

1. Inventariar o legado Java/JNI/C e classificar cada função como convertida, parcial, ausente ou fora de escopo.
2. Manter a matriz de SPEC individual por comando e obter aprovação formal antes de implementar cada lote.
3. Consolidar modelos, constantes, erros e catálogo de status/comandos.
4. Validar CRC, substitution, framing, leitura de resposta e parsers ABECS/BER-TLV com limites defensivos.
5. Ajustar `SerialPort` para propagação de contexto e cancelamento efetivo.
6. Implementar logging com redaction e tracer SPE/PP/RSP sem duplicação,
   injetando a mesma instância no serviço e no adaptador serial; o script local
   cria `logs/LogPinpadAbecs.txt` ou respeita `PINPAD_LOG_FILE`.
7. Revisar `SessionManager`, fila FIFO de capacidade 100, worker único, cancelamento e shutdown.
8. Implementar e validar individualmente CAN/OPN/GIX/CLO/CLX/RST.
9. Implementar e validar display, multimídia, tabelas EMV, GKY, GCX, GTK, GOX, FCX e GPN conforme suas SPECs.
10. Implementar a comunicação segura RSA/AES somente após aprovação de `spec-protocolo-seguro.md`.
11. Manter `TransactionGCX` como `ErrNotImplemented` enquanto sua tabela de parâmetros completos não estiver especificada e aprovada.
12. Documentar todo código novo, convertido ou gerado conforme `.agents/skills/golang-documentation/SKILL.md`, incluindo comentários de pacote, símbolos exportados, fluxos internos complexos e exemplos executáveis aplicáveis.
13. Executar revisão de implementação, inspeção documental, testes automatizados e validação com pinpad físico real por comando.

## Testes, cobertura e qualidade

- Testes unitários orientados a tabela para CRC, bytes, substitution, framing, parsers, status, erros e estados.
- Testes do fake serial para CAN/EOT, OPN/ACK, GIX/ACK+payload, CLO/ACK, NAK, timeout e CRC inválido.
- Testes de sessão com relógio controlável, incluindo conflito e expiração de 300 segundos.
- Testes de fila para FIFO, capacidade 100, `ErrQueueFull` em `Enqueue`, `Submit` bloqueante com cancelamento de contexto, `Clear` e `Stop` sem execução pós-shutdown.
- Testes de builders/parsers de display, multimídia, EMV, GKY, GCX (subconjunto) e GPN, incluindo redaction de PAN/PIN/KSN.
- Testes da fachada cobrindo ciclo de vida, `GetInfo`/`GetInfoRaw`, `DisplayQRCode` (com fake `QRCodeGenerator`, geração ausente e limites inválidos), `LoadCompleteEMVTable` (incluindo `StatusTableVersionDifferent`) e stub `ErrNotImplemented` de `TransactionGCX`.
- Cobertura mínima: 80% da produção aplicável, aferida por `go test ./... -coverprofile=coverage.out` e `go tool cover -func=coverage.out`.
- Qualidade: `gofmt`, `go vet ./...`, `go test ./...`, `go test -race ./...` e build para o ambiente disponível.
- O inventário de todos os arquivos `.go` e a associação com testes serão registrados em `validation.md`.
- A inspeção de documentação Go deverá verificar comentários de pacote, símbolos exportados, código gerado, exemplos executáveis e documentação de fluxos complexos; as lacunas serão registradas em `validation.md`.
- Como não há configuração Sonar existente no módulo, planejar Auditoria de Qualidade Assistida por LLM caso Sonar/scanner não esteja disponível; não declarar métrica não aferida.
- Atualizar o README sem documentar servidor ou contratos fora do escopo.

## Auditoria de segurança

- Escopo: dependência serial, configuração por ambiente, leitura/escrita de bytes, limites de payload, cancelamento, concorrência, logs e dados sensíveis.
- Verificar ausência de PAN, trilhas, PIN, KSN ou EMV sensível em logs e fixtures.
- Executar `go test -race ./...`, `go vet ./...` e verificações de dependências disponíveis.
- Aplicar `security-audit` na validação por existir dependência, configuração e integração serial; registrar o resultado atual em `validation.md` e gerar relatório conforme o procedimento vigente, sem segredos.
- Se houver achado corrigível dentro da SPEC, corrigir e repetir revisão, validação e auditoria. Bloquear se depender de alteração de contrato ou ação externa.

## Riscos, dúvidas e decisões necessárias

- A execução usa Windows 386; a compatibilidade Linux deverá ser comprovada por build/teste em ambiente Linux disponível ou registrada como limitação objetiva.
- `PinpadConfig` inicia com `COM7`; `PORTA_PINPAD` definida e não vazia tem precedência, e ausência usa o default. Essa política deve ser coberta por testes e refletida no script local.
- A API atual `SerialPort.Read()` ainda precisa ser alinhada à SPEC de cancelamento para receber contexto e retornar `context.Canceled`/`context.DeadlineExceeded` corretamente.
- A biblioteca serial poderá exigir download de dependência; falha de rede será registrada como bloqueio de ambiente, não contornada com implementação inventada.
- A validação com hardware físico não está disponível automaticamente; o fake comprovará somente componentes puros e a validação física deverá ser registrada separadamente. Sem essa evidência, o comando permanece não validado.
- Nenhuma dependência concreta de geração de QR Code será adicionada nesta Change; `DisplayQRCode` depende de `QRCodeGenerator` injetado pelo consumidor.
- `TransactionGCX` com parâmetros completos permanece como stub `ErrNotImplemented` até uma Change futura especificar a tabela de tags GCX completa.

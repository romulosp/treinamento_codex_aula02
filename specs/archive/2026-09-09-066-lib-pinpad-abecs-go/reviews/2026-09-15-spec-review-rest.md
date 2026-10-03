# Revisão da SPEC — API RESTful

**Data:** 2026-09-15  
**Change:** `2026-09-09-066-lib-pinpad-abecs-go`  
**Resultado:** `SPEC_APROVADA`

## Escopo revisado

Foram revisados `proposal.md`, `spec.md`, `DESIGN.md`, `tasks.md`, `implementation-plan.md` e as regras compartilhadas de arquitetura, REST, testes e segurança Go.

## Achados

### REV-REST-001 — Contradição entre biblioteca sem rede e servidor requerido

**Severidade:** alta. A redação original proibia HTTP e servidor, enquanto o aditivo exigia WebSocket. O contrato foi corrigido para manter domínio e aplicação independentes e incluir `internal/api` como adaptador RESTful.

### REV-REST-002 — Transporte e rotas indefinidos

**Severidade:** alta. O envelope por `action` foi substituído por recursos HTTP versionados. A SPEC agora define método e rota para os 25 comandos, códigos HTTP, JSON, correlação, limites e timeouts.

### REV-REST-003 — Exposição de rede sem autenticação

**Severidade:** alta. Como autenticação, TLS e rate limit permanecem fora desta entrega, o listener padrão foi limitado a `127.0.0.1:8080`. Alterar o bind para interface externa exigirá novo requisito de segurança.

### REV-REST-004 — Contratos de transporte

**Severidade:** média. As 25 duplas Input/Output foram mantidas e realocadas conceitualmente para `internal/api/dto`; `ErrorBody` recebeu `correlationId`. Respostas de sucesso serializam o Output DTO correspondente.

## Veredito

As contradições materiais foram resolvidas e os critérios `CA-REST-001` a `CA-REST-007` são verificáveis. A implementação REST ainda não está concluída e deverá passar por revisão de implementação e validação com `httptest`, testes completos, auditoria de segurança e chamadas `curl`.

`SPEC_APROVADA`

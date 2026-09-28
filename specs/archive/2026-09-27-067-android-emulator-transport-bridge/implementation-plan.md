# Plano de implementação — 067 Android Emulator Transport Bridge

Este plano foi criado após `SPEC_APROVADA`, conforme o workflow do projeto.
Ele não altera o contrato da SPEC e não autoriza a implementação da Change 068.

## Sequência

1. Criar `internal/application/port.Transport` e adaptar
   `PinpadService`, `serial.Adapter` e fakes, preservando os testes da Change
   066.
2. Criar encoder/decoder do BridgeEnvelope com testes de limites, stream
   fragmentado/agregado, partial write, truncamento e versão inválida.
3. Implementar `EmulatorTransport` com dial, handshake, deadlines, leitura
   contínua, cancelamento e encerramento idempotente.
4. Implementar ownership cross-process do Windows e um fake para testes.
5. Implementar Bridge foreground com fake serial, controle de sessão e
   encaminhamento byte a byte.
6. Criar package `mobile` mínimo e executar Gate 1 em ambiente equipado.
7. Executar Gates 2–5 em ordem; qualquer falha estrutural retorna para SPEC ou
   ADR antes de avançar.

## Testes e qualidade

- unitários: contrato, envelope, configuração, ownership, cancelamento,
  panic boundary e erros;
- integração: Core → fake transport, EmulatorTransport → fake Bridge,
  Bridge → fake serial e fragmentação TCP;
- comandos: `gofmt`, `go test ./...`, cobertura aplicável, `go vet ./...`,
  `go test -race ./...` quando o toolchain suportar, build desktop e Gate 1;
- segurança: loopback default, limites, ausência de payload sensível em logs,
  dependências e concorrência cross-process;
- documentação: GoDoc em símbolos exportados e comentários nos fluxos de
  framing, lifecycle, ownership, cancelamento e redaction.

## Artefatos de validação

Registrar em `validation.md` o ambiente, comando, código de saída, resultado e
artefatos de cada gate. AAR, logs e evidências de hardware não devem ser
versionados como fonte.

# Tasks — 067 Android Emulator Transport Bridge

## Especificação e revisão

- [x] Separar a fundação de transporte do aplicativo `diagnosticopinpad`.
- [x] Registrar baseline real do módulo Go e limitações do ambiente.
- [x] Definir porta Core → Transport independente de tecnologia.
- [x] Definir envelope Bridge versionado, limite e tipos de controle.
- [x] Definir ownership por sessão e coordenação entre processos.
- [x] Definir host, porta, `adb reverse` e fallback `10.0.2.2`.
- [x] Definir Mobile Facade e fronteira de tipos.
- [x] Definir gates 1–5 e fallback formal de binding.
- [x] Obter revisão formal `SPEC_APROVADA` registrada em `reviews/2026-09-27-spec-review.md`.

## Implementação posterior, somente após SPEC_APROVADA

- [x] Criar `internal/application/port.Transport` e adaptar o serviço sem
  regressão.
- [x] Implementar encoder/decoder do `BridgeEnvelope` com limites defensivos.
- [x] Implementar `EmulatorTransport` com conexão persistente, deadlines e
  cancelamento.
- [x] Implementar Bridge Windows, lifecycle foreground e shutdown seguro.
- [x] Implementar ownership cross-process da COM e resposta `BUSY`.
- [x] Implementar pacote público `mobile` com panic/error boundary.
- [x] Propagar `correlationId` em `DATA` e manter a correlação no fluxo serial.
- [x] Implementar `PING/PONG` sem aquisição da COM para diagnóstico de conectividade.
- [x] Disponibilizar transporte serial roteirizado para testes sem hardware.
- [x] Fazer o Bridge consumir `PORTA_PINPAD` como fonte da porta física e
  reutilizar o valor no adapter e no ownership.
- [ ] Executar Gate 1 em ambiente com Go Mobile, Java, SDK/NDK e ADB.
- [ ] Executar Gates 2–5, registrando evidências em `validation.md`.
- [ ] Revisar implementação e segurança antes da Change 068.

## Change 068 — dependência posterior

- [ ] Criar o projeto `apps/frontend/smartphone/diagnosticopinpad`.
- [ ] Importar somente o AAR aprovado e usar Compose/ViewModel/UDF.
- [ ] Implementar o laboratório com comandos classificados a partir do código.
- [ ] Executar Gate 6 e validação física com pinpad real.

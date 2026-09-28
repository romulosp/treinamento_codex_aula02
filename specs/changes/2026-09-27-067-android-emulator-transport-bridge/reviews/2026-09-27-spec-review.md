# Revisão da SPEC — 067 Android Emulator Transport Bridge

Data: 2026-09-27  
Revisor: Codex  
Skill: `spec-review`

## Escopo revisado

- `proposal.md`
- `spec.md`
- `DESIGN.md`
- `tasks.md`
- `validation.md`
- `adr-001-decomposicao.md`
- `specs/shared/process/workflow.md`
- `specs/shared/process/evidence-conventions.md`
- `specs/shared/architecture/backend-golang.md`
- `specs/shared/testing/golang-testing.md`
- `specs/shared/security/golang-security.md`
- baseline real em `apps/desktop/libpinpadabecsgo`
- especificação de referência `spec_transporte_android.md`

## Achados

### REV-001 — Separação de Change

- Severidade: importante, resolvido.
- Evidência: a especificação de referência mistura Bridge, binding e UI
  Android na mesma entrega; o repositório ainda não possui o app Android.
- Impacto: uma falha de toolchain ou transporte poderia ser confundida com
  falha de UI e lifecycle.
- Recomendação: separar fundação e laboratório, com dependência explícita.
- Resolução: `adr-001-decomposicao.md`, `proposal.md` e `spec.md` definem
  Change 067 para transporte e Change 068 para `diagnosticopinpad`.

### REV-002 — Fronteira Core → Transport

- Severidade: importante, resolvido.
- Evidência: o código atual possui `service.SerialPort`, embora o novo
  cenário também exija TCP e Android.
- Impacto: manter o nome serial na fachada aumentaria o acoplamento e
  dificultaria a substituição futura por USB.
- Recomendação: contrato de aplicação neutro, sem driver ou rede.
- Resolução: RF-001 e D-001 definem `internal/application/port.Transport`.

### REV-003 — Framing do stream

- Severidade: importante, resolvido.
- Evidência: TCP é stream e a especificação de referência exigia partial
  read/write, fragmentação, agregação e limite de frame.
- Impacto: sem envelope versionado haveria perda de fronteira ou alocação
  descontrolada.
- Recomendação: header fixo, big-endian, limite e tipos de controle.
- Resolução: RF-005 fixa o envelope `PBRG`, versão 1 e payload máximo de 1 MiB.

### REV-004 — Configuração e rede do Emulator

- Severidade: importante, resolvido.
- Evidência: `localhost` global não representa automaticamente o Windows
  Host dentro do Android Emulator.
- Impacto: substituir silenciosamente por `10.0.2.2` quebraria o default
  existente e ocultaria a origem da configuração.
- Recomendação: `adb reverse` como fluxo padrão e `10.0.2.2` somente como
  override explícito.
- Resolução: RF-007 e D-005 fixam host `localhost`, porta `39100`, `adb
  reverse` e origem observável.

### REV-005 — Viabilidade do gomobile

- Severidade: importante, resolvido como gate.
- Evidência: `GOOS=android GOARCH=arm64 go list -deps ./...` passou, mas o
  ambiente não tem `gomobile`, Java, Android SDK, ADB ou Gradle.
- Impacto: não é possível declarar AAR ou Kotlin → Go validado por análise
  estática.
- Recomendação: package público mínimo e Gate 1 antes dos demais gates.
- Resolução: RF-008, seção 4 da SPEC e `VAL-006` exigem prova reproduzível;
  `validation.md` registra a limitação atual.

### REV-006 — Ownership cross-process

- Severidade: importante, resolvido.
- Evidência: mutex em memória não protege Bridge contra CLI/API em outro
  processo.
- Impacto: bytes de sessões diferentes poderiam ser intercalados ou a COM
  poderia ser aberta simultaneamente.
- Recomendação: ownership por sessão com mecanismo Windows nomeado e fake em
  testes.
- Resolução: RF-006 e D-004 fixam a granularidade e o comportamento `BUSY`.

### REV-007 — Escopo de segurança remota

- Severidade: melhoria, resolvido por exclusão.
- Evidência: o Bridge é ferramenta de desenvolvimento e não deve escutar em
  LAN por padrão.
- Impacto: adicionar LAN sem autenticação ampliaria a superfície de risco.
- Recomendação: loopback apenas nesta Change; autenticação/TLS em Change
  futura específica.
- Resolução: RF-004, RF-011 e CA-007.

## Verificação de consistência

- O objetivo, escopo e fora de escopo estão alinhados entre proposal, SPEC e
  DESIGN.
- Os critérios CA-001 a CA-016 são verificáveis e apontam para testes/gates.
- A UI Android não é requisito de implementação da Change 067; sua dependência
  está explicitamente registrada para a Change 068.
- O contrato não expõe comando ABECS bruto nem tipos incompatíveis como
  `context.Context`.
- A SPEC preserva a configuração existente da Change 066 e não altera
  `localhost` global.
- As limitações de ambiente foram registradas sem serem convertidas em falsa
  aprovação de AAR ou hardware.

## Veredito

`SPEC_APROVADA`

A Change 067 está suficientemente definida para implementação posterior,
desde que o `implementation-plan.md` seja mantido compatível com este contrato
e o Gate 1 seja executado antes dos Gates 2–5. Esta aprovação não aprova a
Change 068, não autoriza o aplicativo Android nesta execução e não substitui a
revisão da implementação, a validação ou a aprovação final da mudança.

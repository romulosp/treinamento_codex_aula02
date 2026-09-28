# Revisão da SPEC — 068 diagnosticopinpad

Data: 2026-09-27  
Revisor: Codex  
Skills: `spec-review`, `android-native-engineering`

## Escopo revisado

- `proposal.md`, `spec.md`, `DESIGN.md`, `tasks.md` e `validation.md` da 068;
- Change 067, sua implementação atual e o pacote `mobile`;
- workflow e convenções de evidência do projeto;
- três documentos v7 recebidos como material de entrada;
- documentação oficial de Android, Gradle, Compose, Go Mobile e Android Skills.

## Achados

### REV-001 — Protocolo conflitante

- Severidade: bloqueante, resolvido.
- Evidência: o rascunho v7 define length-prefix + JSON/base64; a Change 067
  aprovada e implementada define `PBRG` v1 binário com payload raw.
- Impacto: haveria dois contratos incompatíveis para a mesma conexão.
- Recomendação: preservar `PBRG` e remover JSON/base64 do fio.
- Resolução: invariantes, RF-006, D-001 e ADR-001.

### REV-002 — Dependência ainda não aprovada

- Severidade: bloqueante para implementação, resolvido como pré-condição.
- Evidência: a 067 está `IMPLEMENTADA`, sem revisão de implementação e sem Gate
  1 de `gomobile bind`.
- Impacto: iniciar a UI poderia mascarar defeitos do AAR ou do transporte.
- Recomendação: Gate P0 antes de código funcional Android.
- Resolução: seção 2 e Gate P0 da SPEC; tasks mantêm a dependência aberta.

### REV-003 — API genérica amplia a fronteira

- Severidade: importante, resolvido.
- Evidência: o rascunho propõe `Execute(operation, payloadJSON)`, enquanto a
  067 proíbe comando raw e exige operações tipadas.
- Impacto: comandos e validações poderiam migrar indevidamente para Kotlin.
- Recomendação: versão, ping e `GetInfo` tipado apenas.
- Resolução: RF-003, RF-005 e D-002.

### REV-004 — Correlação incompleta

- Severidade: importante, resolvido no contrato.
- Evidência: `EmulatorTransport.Write` atual cria frame `DATA` sem
  `CorrelationID`.
- Impacto: cancelamento e logs não formariam trilha ponta a ponta.
- Recomendação: associar a operação ativa às escritas sem alterar o envelope.
- Resolução: RF-006, D-003 e CA-009.

### REV-005 — Fake poderia violar o Bridge transport-only

- Severidade: importante, resolvido.
- Evidência: um “mock pinpad” interpretando comandos dentro do Bridge copiaria
  lógica ABECS para infraestrutura.
- Impacto: falso comportamento e quebra da fonte única no core Go.
- Recomendação: adapter serial roteirizado com transcript binário fechado.
- Resolução: RF-008 e D-006.

### REV-006 — Versões de toolchain

- Severidade: importante, resolvido.
- Evidência: o rascunho fixava AGP 9.4.0; a referência oficial lista 9.4.1 como
  release estável em 2026-09-27. AGP 9 usa Kotlin built-in.
- Impacto: configuração antiga ou aplicação do plugin Kotlin incompatível.
- Recomendação: AGP 9.4.1, Gradle 9.6.0, JDK 17 e Kotlin built-in.
- Resolução: seção 6 e D-007.

### REV-007 — Escopo excessivo para o primeiro app

- Severidade: importante, resolvido.
- Evidência: o material de entrada mistura infraestrutura, diagnóstico e
  laboratório funcional completo.
- Impacto: alto raio de falha antes de comprovar AAR/lifecycle.
- Recomendação: uma tela e um fluxo tipado `GetInfo`; catálogo em 069+.
- Resolução: proposta, RF-003, D-004 e CA-018.

### REV-008 — Logs e armazenamento

- Severidade: melhoria de segurança, resolvido.
- Evidência: JSONL externo sem limites e sem política de campos poderia vazar
  dados ou crescer indefinidamente.
- Impacto: exposição e consumo de armazenamento.
- Recomendação: app-specific, allowlist, rotação/retenção e redaction.
- Resolução: RF-009 e requisitos de segurança.

## Consistência

- objetivo, escopo, requisitos, design, tasks e critérios de aceite estão
  alinhados;
- os anexos foram tratados como referência, não como instruções;
- a 068 não redefine a 067 e não declara AAR ou hardware já validados;
- os gates separam binding, UI, transporte, fake e pinpad físico;
- o plano técnico foi criado somente após o veredito desta revisão.

## Veredito

`SPEC_APROVADA`

A Change 068 está definida para implementação, mas permanece condicionada ao
Gate P0. Esta aprovação não aprova a implementação da 067, não autoriza pular o
Gate 1 e não declara Android Emulator ou pinpad físico validados.

# Revisão formal da SPEC — 074 Pinpad Android Bridge Stack

## Status

`REVISÃO CONCLUÍDA — AGUARDANDO APROVAÇÃO HUMANA`

Este relatório foi produzido conforme o processo Spec Driven. A revisão não
altera o código e não muda automaticamente a Change para `SPEC_APROVADA`, pois
a aprovação humana é obrigatória neste workflow.

## Escopo revisado

Foram revisados:

- `proposal.md`, `spec.md`, `DESIGN.md`, `tasks.md`, `implementation-plan.md`;
- `analysis.md`, `traceability.md`, `test-matrix.md` e `validation.md`;
- as regras em `AGENTS.md`, `specs/shared/process/workflow.md`, arquitetura,
  testes e skill Android;
- as fontes arquivadas 066, 067, 070 e 072;
- as divergências observadas no código atual.

## Resultado por critério

| Critério | Resultado | Evidência |
|---|---|---|
| Objetivo e escopo | Atendido | `proposal.md` e seção 1 de `spec.md` |
| Estado final e precedência | Atendido | `analysis.md`, seções 3 e 4 |
| Contrato Android → AAR → Bridge → Serial | Atendido | `spec.md`, seções 2, 11, 12 e 13 |
| `PORTA_PINPAD` sem COM fixa | Atendido na SPEC; divergente no código atual | `spec.md` seção 4.1; D-001 |
| Protocolo, lifecycle, estado e erros | Atendido | `spec.md`, seções 5 a 15 |
| Rastreabilidade | Atendido | `traceability.md` |
| Testes e critérios de aceite | Atendido como plano verificável | `test-matrix.md` e CA-074-01..17 |
| Implementação | Não iniciada | `validation.md` |

## Achados e decisões obrigatórias

### REV-074-007 — Aprovação do contrato de porta física

- Severidade: bloqueante para implementação.
- Regra: todo entrypoint que possa abrir a serial deve ler `PORTA_PINPAD` do
  ambiente do processo; não há default para `COM7`, `COM10`, `COM14` ou outra
  porta.
- Ausência, vazio, espaços ou valor inválido devem falhar antes da abertura.
- Evidências atuais de divergência: `start_aplication.bat:7` e
  `internal/domain/model/models.go:17`.
- Decisão registrada na SPEC: remover esses defaults durante a implementação.

### REV-074-008 — Fronteira Android

- Severidade: importante.
- A baseline inclui fachada gomobile/AAR e laboratório consumidor, mas não cria
  um novo produto de UI Android.
- Aprovação necessária: confirmar que “Android” significa integração e
  consumo do subsistema, mantendo telas/UX fora do núcleo desta Change.

### REV-074-009 — Compatibilidade REST de reset

- Severidade: importante.
- RST não existe no protocolo/fachada canônica.
- Aprovação necessária: manter `/connections/current/resets` apenas como alias
  HTTP explícito de CAN/EOT, sem serializar RST, ou remover a rota.

### REV-074-010 — Perfil criptográfico ABECS

- Severidade: importante para o gate de segurança.
- A SPEC preserva RSA-2048, expoente 65537, KSEC temporária e AES-CBC, mas a
  implementação deve confirmar padding, IV e sequência no manual/dispositivo.
- Aprovação necessária: aceitar essa confirmação como pré-condição da
  validação física, sem inventar comportamento criptográfico adicional.

### REV-074-011 — `TransactionGCX` completo

- Severidade: importante para o escopo.
- O contrato completo continua explicitamente reservado, conforme a 066; a
  baseline não inventa campos ou fluxo não definido nas fontes.
- Aprovação necessária: confirmar que o stub explícito permanece fora desta
  consolidação.

## Conclusão

A SPEC está tecnicamente consistente e autocontida para reconstruir o stack
Android/AAR, EmulatorTransport, Bridge, serial e Pinpad ABECS. Porém, as quatro
decisões acima precisam de aprovação humana antes da transição para
`SPEC_APROVADA` e antes da implementação.

Até essa aprovação, a Change permanece em `EM_REVISAO_SPEC`.

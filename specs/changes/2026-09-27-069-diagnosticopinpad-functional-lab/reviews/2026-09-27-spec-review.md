# Revisão da SPEC — 069 diagnosticopinpad Functional Lab

Revisor: Codex
Data: 2026-09-27

## Resultado

`SPEC_APROVADA`

## Escopo revisado

- `proposal.md`;
- `spec.md`;
- `DESIGN.md`;
- `tasks.md`;
- `implementation-plan.md`;
- workflow Spec Driven e referências Android/Go aplicáveis;
- inventário do menu desktop e das APIs existentes no core Go.

## Achados

### REV-069-001 — Opções indisponíveis e reservadas

- Severidade: informativo;
- Evidência: opções 6 e 25 do menu local;
- Impacto: expor botão sem contrato poderia simular uma capacidade inexistente;
- Decisão: SPEC exige que ambas permaneçam visíveis e desabilitadas com motivo;
- Estado: resolvido na SPEC.

### REV-069-002 — Dados sensíveis de GTK/GPN/GCX/GOX

- Severidade: importante mitigado;
- Evidência: modelos Go contêm trilhas, PAN, PIN block, KSN e EMV;
- Impacto: transportar esses campos para Compose ou logs criaria exposição;
- Decisão: fachada retorna somente summaries allowlist e descarta os campos
  sensíveis após o uso;
- Estado: requisito verificável em CA-069-06 e tasks de redaction.

### REV-069-003 — Fronteira ABECS

- Severidade: importante mitigado;
- Evidência: Change 067/068 definem PBRG binário e core Go como fonte única;
- Impacto: JSON de comando ou parser Kotlin quebraria o desenho aprovado;
- Decisão: métodos gomobile nomeados, tipos simples e nenhum executor raw;
- Estado: resolvido em RF/CA-069-02.

### REV-069-004 — Configuração física

- Severidade: informativo;
- Evidência: requisito do usuário `PORTA_PINPAD=COM14`;
- Impacto: duplicar COM no Android causaria divergência de ownership;
- Decisão: Android conhece apenas host/porta TCP; Bridge Windows consome
  `PORTA_PINPAD`;
- Estado: resolvido em CA-069-07.

## Conclusão

Não há bloqueio material para a implementação. A SPEC é aprovada com a
limitação explícita de que hardware, tabelas EMV e comandos sensíveis exigem
evidência real; ausência de hardware será registrada como não executada.

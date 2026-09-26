# Revisão da SPEC — baseline do prompt2

## Escopo

- Artefatos: `proposal.md`, `spec.md`, `DESIGN.md`, especificações
  complementares, `tasks.md`, `implementation-plan.md`,
  `compatibility-matrix.md`, ADR-001, fontes e validação.
- Fontes temporais consultadas em 2026-09-19: documentação oficial do AGP
  9.4.0, Android 17, Compose BOM, Compose Compiler e Google Play.

## Achados

### REV-006 — Resolvido — verificação incompleta do AGP

- Severidade original: bloqueante.
- Evidência: a revisão anterior verificou apenas AGP 9.0.1. O AGP 9.4.0
  suporta oficialmente API 37 e define Gradle 9.6.0, JDK 17 e KGP 2.2.10.
- Impacto: o bloqueio registrado anteriormente era falso para o baseline atual.
- Resultado: resolvido na matriz de compatibilidade e no ADR-001.

### REV-007 — Resolvido — minSdk

- Severidade original: bloqueante.
- Evidência: Compose aceita API 21 ou superior; a Change é interna, não exige
  APIs posteriores à 26 e já possui validação mínima planejada na API 26.
- Impacto: sem a decisão, configuração e matriz de dispositivos seriam ambíguas.
- Resultado: `minSdk = 26` confirmado e rastreado.

### REV-008 — Resolvido — contratos contraditórios

- Severidade original: importante.
- Evidência: os documentos misturavam SDK 36/BOM 2026.06.01 com SDK 37/BOM
  2026.09.00 e mantinham simultaneamente `BLOCKED` e `SPEC_APROVADA`.
- Impacto: a implementação não teria uma fonte de verdade inequívoca.
- Resultado: os artefatos vigentes foram consolidados no baseline da matriz.

## Conclusão

`SPEC_APROVADA`

Não restam pendências materiais na especificação. O plano técnico pode seguir
com o stack registrado em `compatibility-matrix.md` e com as skills acionadas
sob demanda conforme `AGENTS.md` e `implementation-plan.md`. A incorporação e
conferência física do material de origem é a primeira tarefa de execução e não
autoriza dependência de caminhos externos no código, build ou testes.

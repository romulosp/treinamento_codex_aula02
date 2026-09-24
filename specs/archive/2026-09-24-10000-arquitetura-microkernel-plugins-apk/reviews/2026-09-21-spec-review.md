# Revisão da SPEC — arquitetura microkernel com plugins APK

## Escopo revisado

- `proposal.md`, `spec.md`, `DESIGN.md`, `tasks.md` e plano de implementação;
- matriz de compatibilidade, modelo de ameaças, migração, validação e ADRs;
- processo em `specs/shared/process/workflow.md`;
- documentação oficial Android citada em `sources-and-decisions.md`.

## Achados

Nenhum achado bloqueante ou importante permaneceu após a consolidação da SPEC.

### REV-001 — Resolvido — isolamento presumido por classloader

- Severidade original: bloqueante.
- Evidência: o requisito de origem propunha plugins APK dinâmicos sem delimitar
  as permissões compartilhadas com o host.
- Impacto: poderia levar à aceitação de plugin não confiável como se estivesse
  em sandbox.
- Resultado: a SPEC limita v1 a plugins internos assinados, exige validação em
  área privada e registra que terceiros exigem aplicativo/processo separado.

### REV-002 — Resolvido — hot reload sem ciclo de vida seguro

- Severidade original: importante.
- Evidência: a substituição de classes e recursos já carregados não possui
  contrato seguro para este runtime Android.
- Impacto: risco de UI inconsistente e referências antigas após atualização.
- Resultado: nova versão de plugin ativo fica `PENDING_RESTART`; ativação sem
  reinício limita-se a artefato ainda não carregado.

### REV-003 — Resolvido — pendências externas de entrega/publicação

- Severidade original: importante.
- Evidência: endpoint Maven corporativo e canal produtivo de distribuição não
  foram fornecidos pelo requisito.
- Impacto: não era possível documentá-los sem inventar autoridade externa.
- Resultado: a Change usa `build/local-maven` e staging manual somente para
  desenvolvimento/teste; distribuição produtiva ficou fora de escopo.

## Conclusão

`SPEC_APROVADA`

O objetivo, escopo, exclusões, contratos, requisitos de segurança, riscos,
ADRs, migração e critérios de aceite são verificáveis. A implementação pode ser
iniciada somente mediante solicitação explícita, seguindo `implementation-plan.md`
e os quality gates da skill `android-native-engineering`.

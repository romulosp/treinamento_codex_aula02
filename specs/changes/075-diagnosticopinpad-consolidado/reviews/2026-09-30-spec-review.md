# Revisão da SPEC — 075 diagnosticopinpad consolidado

> Revisão histórica da primeira versão. Superada pela re-revisão
> `2026-09-30-spec-rereview-072-074.md` após a descoberta de consolidação
> incompleta da 072/074.

Revisor: Codex  
Data: 2026-09-30  
Skill aplicada: `spec-review`

## Escopo revisado

Foram confrontados `proposal.md`, `spec.md`, `DESIGN.md`, `tasks.md`,
`implementation-plan.md`, `validation.md`, o workflow do repositório e os
artefatos arquivados das Changes 068 e 069.

## Achados

### REV-075-001 — Escopo mínimo versus catálogo completo

- Severidade: importante, resolvido.
- Evidência: a 068 limitava o laboratório a seis ações; a 069 especificava 28
  opções e a fachada nomeada.
- Impacto: manter a lista mínima como contrato final perderia o estado acumulado.
- Decisão: a SPEC 075 preserva as seis ações e incorpora as 28 opções, com 6 e
  25 explicitamente desabilitadas.

### REV-075-002 — Perfil arquitetural

- Severidade: importante, resolvido.
- Evidência: a 068 usava `SIMPLE`; a 069 classificava o laboratório como
  `STANDARD` por causa dos fluxos e formulários.
- Impacto: manter `SIMPLE` ocultaria a necessidade de seções, diálogos e estado
  coordenado.
- Decisão: `STANDARD` em um único módulo `app`, sem modularização artificial.

### REV-075-003 — Protocolo e fronteira ABECS

- Severidade: bloqueante se alterado, resolvido.
- Evidência: ambas as fontes preservam PBRG v1, payload raw e core Go como fonte
  única; JSON é apenas saída nomeada.
- Impacto: JSON/base64 no fio ou `Execute` genérico criaria segundo protocolo e
  exporia a lógica ABECS ao Kotlin.
- Decisão: contrato consolidado mantém PBRG v1 e métodos nomeados.

### REV-075-004 — Segurança e dados sensíveis

- Severidade: importante, resolvido.
- Evidência: as fontes exigem summaries allowlist, redaction e exclusão de PAN,
  trilhas, PIN block, KSN, chaves, EMV bruto e payload raw.
- Impacto: transportar esses dados ao Compose ou aos logs violaria o contrato.
- Decisão: os invariantes e critérios de aceite 075-09 são normativos.

### REV-075-005 — Scripted versus pinpad físico

- Severidade: importante, resolvido.
- Evidência: as validações arquivadas registram que hardware real depende de
  ambiente compatível e não pode ser simulado.
- Impacto: declarar Gate 5 por transcript produziria falso positivo.
- Decisão: Gate 5 permanece separado e `não executado` é resultado válido.

### REV-075-006 — Implementação durante a consolidação

- Severidade: bloqueante para o escopo solicitado, resolvido.
- Evidência: o pedido exclui implementação; o prompt indicado executa até
  `IMPLEMENTADA`.
- Impacto: executar o prompt agora produziria código fora do objetivo.
- Decisão: preparar o caminho de implementação e deixar todos os gates
  funcionais pendentes.

## Verificação de completude

- objetivo, escopo e fora de escopo: presentes;
- requisitos funcionais, segurança, privacidade, lifecycle e acessibilidade:
  presentes;
- arquitetura, módulos, package name, SDKs herdados e artefatos: presentes;
- riscos, dependências, testes e critérios verificáveis: presentes;
- tarefas de implementação e validação: não marcadas como concluídas;
- evidências históricas: separadas das evidências futuras de 075.

## Veredito

`SPEC_APROVADA`

A Change está apta a aguardar implementação. A aprovação não autoriza declarar
build, testes, validação física ou implementação concluídos.

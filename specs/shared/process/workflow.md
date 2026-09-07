# Workflow Spec Driven

## Fonte oficial

Este documento é a regra canônica de processo. `AGENTS.md` impõe as regras gerais; Skills descrevem a execução de cada etapa; documentos em `changes/` mantêm a evidência de uma mudança específica.

## Ciclo de vida e gates

| Fase | Entrada obrigatória | Saída obrigatória | Próximo status |
| --- | --- | --- | --- |
| Especificação | `proposal.md`, `spec.md`, `DESIGN.md`, `tasks.md` | contrato completo | `EM_REVISAO_SPEC` |
| Revisão da SPEC | contrato completo | relatório `REV-*` | `SPEC_APROVADA` ou `REPROVADA` |
| Implementação | `SPEC_APROVADA` | código, configuração e testes previstos | `IMPLEMENTADA` ou `BLOQUEADA` |
| Revisão da implementação | implementação e SPEC | relatório `IMP-REV-*` | `IMPLEMENTACAO_APROVADA` ou `REPROVADA` |
| Validação | implementação aprovada | evidências `VAL-*` | `VALIDADA` ou `REPROVADA` |
| Aprovação | relatórios sem pendências materiais | decisão formal | `APROVADA` ou `REPROVADA` |
| Encerramento | mudança aprovada | atualização de `system/`, mudança pronta em `archive/`, commit | `ARQUIVADA` |

## Planejamento técnico preparatório

Após a Change ficar `SPEC_APROVADA` e antes da implementação, registre `implementation-plan.md` na pasta da Change. Esse artefato descreve impactos prováveis, estratégia, testes unitários e de integração, qualidade, segurança, riscos e decisões técnicas. Ele não cria uma nova fase, não altera os gates acima e não modifica `proposal.md`, `spec.md` ou `DESIGN.md`.

## Regras de retorno

- Falha de clareza retorna para especificação.
- Divergência de código retorna para implementação.
- Falha de teste retorna para implementação ou para a SPEC, conforme a causa.
- A aprovação e o commit nunca corrigem código ou requisitos.

## Estrutura obrigatória de uma mudança

```text
specs/changes/<id>-<nome>/
├── proposal.md
├── spec.md
├── DESIGN.md
├── tasks.md
├── implementation-plan.md
├── validation.md
└── reviews/
```

`proposal.md` registra objetivo e escopo. `spec.md` define comportamento verificável. `DESIGN.md` registra como a solução será estruturada. `tasks.md` permite acompanhar a execução. `implementation-plan.md` registra o plano técnico preparatório sem alterar o contrato aprovado. `validation.md` contém evidências objetivas.

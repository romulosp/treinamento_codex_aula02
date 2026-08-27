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
├── validation.md
└── reviews/
```

`proposal.md` registra objetivo e escopo. `spec.md` define comportamento verificável. `DESIGN.md` registra como a solução será estruturada. `tasks.md` permite acompanhar a execução. `validation.md` contém evidências objetivas.
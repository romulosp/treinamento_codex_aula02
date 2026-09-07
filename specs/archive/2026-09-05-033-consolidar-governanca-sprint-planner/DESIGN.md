# Design: 033-consolidar-governanca-sprint-planner

## Contexto

O processo Spec Driven é a fonte de verdade de uma Change. O Sprint Planner deve organizar apenas a ordem, os riscos e o acompanhamento operacional, usando evidências existentes em vez de criar um fluxo paralelo.

## Referências

- `specs/shared/process/workflow.md`
- `specs/sprint/templates/template-sprint.md`
- `specs/sprint/prompts/`

## Decisões

1. Criar `specs/sprint/README.md` como guia de descoberta, nomenclatura e mapeamento de estados.
2. Persistir o plano técnico em `implementation-plan.md` dentro da Change, porque resposta de chat não é evidência rastreável.
3. Tratar a auditoria de qualidade assistida como fallback operacional, nunca como resultado artificial de Sonar ou cobertura.
4. Tratar auditoria de segurança como parte da validação para os artefatos aplicáveis, sem criar uma etapa 07.
5. Preservar PDFs de segurança somente quando representarem a auditoria atual; Change documental registra não aplicabilidade em `validation.md`.

## Arquitetura e componentes

```text
specs/
├── changes/<change>/
│   ├── implementation-plan.md
│   ├── validation.md
│   └── reviews/
├── sprint/
│   ├── README.md
│   ├── templates/template-sprint.md
│   └── prompts/
└── shared/process/workflow.md
```

## Alternativas e consequências

- Não criar o plano técnico persistente foi descartado, pois elimina a rastreabilidade entre SPEC e implementação.
- Exigir Sonar e cobertura inexistentes foi descartado, pois bloquearia módulos sem infraestrutura. O fallback mantém evidências, mas não substitui ferramentas quando disponíveis.
- Exigir PDF de segurança para documentação pura foi descartado, pois não há artefato de frontend/backend no escopo a auditar.

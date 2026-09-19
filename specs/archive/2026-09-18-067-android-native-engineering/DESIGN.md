# DESIGN: Android Native Engineering

## Decisão

Será criada uma única skill orquestradora no primeiro incremento. Skills oficiais Android continuam responsáveis por fluxos especializados. Novas skills locais só serão extraídas quando uso real demonstrar que arquitetura, segurança, testes ou release precisam de contexto independente.

## Estrutura

```text
.agents/skills/android-native-engineering/
├── SKILL.md
├── agents/openai.yaml
├── references/
│   ├── architecture-and-profiles.md
│   ├── kdoc-guidelines.md
│   ├── quality-gates.md
│   ├── sources-and-decisions.md
│   └── spec-driven-workflow.md
└── scripts/
    └── validate_android_project.py
```

`SKILL.md` faz o roteamento. Referências concentram detalhes condicionais, incluindo a política obrigatória de KDoc para código Kotlin. O script executa apenas verificações locais e read-only; build e testes continuam a cargo do Gradle Wrapper do projeto analisado.

## Compatibilidade

A skill segue o formato de Agent Skills aceito pelo Codex e pelas Android Skills oficiais. `agents/openai.yaml` fornece somente metadados de interface, mantendo invocação implícita habilitada.

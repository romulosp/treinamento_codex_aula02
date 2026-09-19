---
name: android-native-engineering
description: Planejar, implementar, revisar ou validar aplicações Android nativas em Kotlin com processo Spec Driven, arquitetura oficial adaptada à complexidade e evidências reproduzíveis. Use para projetos Android nativos; não use para React Native, Flutter ou backend isolado.
---

# Android Native Engineering

Construa Android nativo com decisões rastreáveis e o menor nível de complexidade que atende ao produto.

## Antes de alterar código

1. Leia `AGENTS.md` e a Change ativa. Código de produção exige `SPEC_APROVADA`.
2. Confirme package name, módulos, perfil, min/target SDK, distribuição, integrações, dados sensíveis e critérios de aceite. Consulte fontes oficiais para versões atuais; não use versões lembradas pelo modelo.
3. Verifique se uma [Android Skill oficial](https://developer.android.com/tools/agents/android-skills/browse) cobre a tarefa. Durante pesquisa, trate-a apenas como candidata: não instale nem copie automaticamente. Classifique-a como `REUSE`, `EXTEND`, `CREATE` ou `REJECT` usando [references/sources-and-decisions.md](references/sources-and-decisions.md). Somente `REUSE` ou `EXTEND` aprovadas pela SPEC podem ser incorporadas.
4. Classifique afirmações como `REQUIRED`, `RECOMMENDED`, `OPTIONAL`, `CONTEXTUAL`, `EXPERIMENTAL` ou `RESEARCH_CANDIDATE`. Use `NEEDS_EVIDENCE` e `NEEDS_DECISION` quando necessário.

Leia [references/spec-driven-workflow.md](references/spec-driven-workflow.md) ao criar ou alterar uma Change. Leia [references/architecture-and-profiles.md](references/architecture-and-profiles.md) ao decidir estrutura, camadas, módulos ou stack. Leia [references/quality-gates.md](references/quality-gates.md) ao implementar, revisar ou validar. Consulte [references/sources-and-decisions.md](references/sources-and-decisions.md) antes de afirmar práticas ou versões Android atuais.

Ao criar, alterar ou revisar código Kotlin, leia e aplique obrigatoriamente [references/kdoc-guidelines.md](references/kdoc-guidelines.md).

## Regras de implementação

- Prefira Kotlin e Compose em novas UIs, salvo contrato aprovado em contrário.
- Mantenha UI e data layers explícitas. A UI observa estado imutável e envia eventos; repositories mediam fontes de dados. Use domain layer somente quando simplificar lógica reutilizada ou complexa.
- Use coroutines/Flow e APIs lifecycle-aware. Tipos que fazem trabalho bloqueante devem ser main-safe.
- Comece com um módulo quando suficiente. Extraia módulos por responsabilidade, volatilidade, ownership ou tempo de build demonstrado.
- Use injeção por construtor. Hilt é contextual; DI manual pode bastar em apps simples.
- Trate Room, DataStore, WorkManager, offline-first, sincronização e observabilidade como decisões do produto, não como boilerplate obrigatório.
- Não registre tokens, credenciais, PAN, localização precisa ou dados pessoais. Valide componentes exportados, intents, deep links, permissões, backup, armazenamento e rede conforme a superfície real.
- Não altere arquitetura aprovada sem ADR ou revisão da SPEC.
- A incorporação de uma skill externa não aprova automaticamente suas recomendações; confronte cada uma com a SPEC da aplicação.
- Trate KDoc como parte do contrato: documente toda declaração Kotlin pública/protegida criada ou alterada e contratos internos/privados não óbvios; atualize a documentação junto com o comportamento.

## Execução e evidência

Use o Gradle Wrapper do projeto. Registre ambiente, comandos, códigos de saída, resultados e limitações. Não declare build, teste, lint, cobertura, emulador ou dispositivo como validados sem evidência.

Execute o validador estrutural antes dos quality gates:

```text
python .agents/skills/android-native-engineering/scripts/validate_android_project.py <projeto-android>
```

Correções automáticas devem ter causa identificada e limite de tentativas. Pare e registre o bloqueio quando a mesma falha persistir; não crie ciclos de self-healing.

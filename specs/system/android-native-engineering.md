# Android Native Engineering vigente

As skills Kotlin/Compose subordinadas ficam em
`.agents/skills/android-native-engineering/kotlin/`, conforme o índice local;
a skill-base prevalece sobre essas especializações.

## Skill principal

- Skill: `.agents/skills/android-native-engineering/SKILL.md`.
- Referências: arquitetura e perfis, KDoc, quality gates, processo Spec Driven e fontes/decisões.
- Validador estrutural: `.agents/skills/android-native-engineering/scripts/validate_android_project.py`.
- Invocação implícita permanece habilitada por `.agents/skills/android-native-engineering/agents/openai.yaml`.

## Governança

Changes Android devem possuir `proposal.md`, `spec.md`, `DESIGN.md` e `tasks.md` completos e aprovados antes de alterar código de produção. A skill escolhe o perfil mínimo entre `SIMPLE`, `STANDARD`, `ENTERPRISE` e `HIGH_ASSURANCE`, consulta fontes oficiais temporais e compõe Android Skills especializadas sob demanda, sem importação automática.

## Arquitetura e qualidade

O baseline é Kotlin, Compose para novas UIs, UI/data layers, repositories, estado imutável, UDF, coroutines/Flow, coleta lifecycle-aware e dependências por construtor. Hilt, Room, DataStore, WorkManager, Navigation 3, modularização e offline-first são decisões contextuais.

Quality gates incluem Wrapper, build, testes, lint, inventário de código, revisão de segurança, acessibilidade, adaptive UI, desempenho e validação em dispositivo quando aplicável. Cobertura elegível tem mínimo de 80% e alvo de 90%, salvo regra mais forte da SPEC.

## KDoc obrigatório

Código Kotlin público/protegido criado ou alterado deve possuir KDoc em português do Brasil. Declarações internas ou privadas com regra de negócio, contrato arquitetural, concorrência, segurança, efeitos colaterais ou comportamento não óbvio também exigem KDoc. Overrides sem contrato novo, código gerado e implementações privadas triviais podem ser excluídos com justificativa na validação.

Cada Change Kotlin deve inventariar arquivos `.kt`, declarações documentadas e exclusões. Dokka é executado quando já configurado ou exigido pela SPEC consumidora; a skill não instala Dokka automaticamente.

## Segurança e evidências

Não versionar segredos, tokens, credenciais, PAN, localização precisa ou dados pessoais. Revisar componentes exportados, Intents, deep links, WebView, armazenamento, backup, rede e permissões conforme a superfície real. Toda execução registra ambiente, comando, código de saída, resultado e limitações.

Esta especificação vigente foi consolidada após a aprovação formal da Change `2026-09-18-067-android-native-engineering`.

## Microkernel Android com plugins dinâmicos

O host Android pode observar `<externalFilesDir>/plugins/inbox` como staging não
confiável. Plugins internos devem ser entregues como APK separado, assinado com
o mesmo certificado permitido pelo host, copiado para quarentena privada,
validados por digest, pacote, manifesto JSON, API compartilhada e capacidade, e
promovidos para `files/plugins/verified/<sha256>.apk` somente leitura antes do
uso de `DexClassLoader`.

O `FileObserver` somente agenda trabalho; o processamento ocorre em executor
serial com debounce e varredura de boot. Atualizações de uma capacidade ativa
ficam em `PENDING_RESTART`. Falhas de rejeição e falhas recuperáveis de carga ou
callback devem ser auditadas sem segredos e convergir para `REJECTED` ou
`ERROR`, `onDetach` de melhor esforço e fallback do host.

O primeiro fluxo vigente dessa arquitetura é a Change
`10002-microkernel-pasta-dinamica`, arquivada após validação no AVD e auditoria
de segurança sem achados confirmados.

# Instruções do projeto

## Processo obrigatório

1. Toda alteração começa em `specs/changes/<id>-<nome>/` e segue `specs/shared/process/workflow.md`.
2. Não implemente uma mudança cujo `spec.md` e `proposal.md` não estejam em `SPEC_APROVADA`.
3. Registre revisão, validação, aprovação e evidências em `reviews/` e `validation.md`.
4. Antes de concluir, execute os testes aplicáveis e registre ambiente, comando, resultado e código de saída.
5. A mudança só pode ser arquivada depois da aprovação formal, atualização de `specs/system/` e preparação do commit que registra ambos.
6. Prompts legados são material didático; Skills são o mecanismo operacional do projeto.

## Gerenciamento de Skills (Ecossistema Android)

Para esta Change e para qualquer tarefa de Android nativo, a skill base
`.agents/skills/android-native-engineering` é soberana sobre o processo Spec
Driven, arquitetura, segurança, qualidade, KDoc e evidências.

As sub-skills em `.agents/skills/android-native-engineering/kotlin/` são
especializações subordinadas à skill base e devem ser lidas sob demanda,
somente quando o gatilho correspondente aparecer. O índice completo está em
`.agents/skills/android-native-engineering/kotlin/INDEX.md`.

### Gatilhos de acionamento

| Gatilho na tarefa | Sub-skill a acionar |
| --- | --- |
| Animação, transição, gesto ou motion | `kotlin/compose-animations` |
| Criação, refatoração ou estilização de componente Compose | `kotlin/compose-component-design` |
| Foco, teclado, acessibilidade, deep link ou navegação | `kotlin/compose-focus-navigation` |
| Estado de UI, efeitos colaterais, `remember` ou `LaunchedEffect` | `kotlin/compose-state-and-effects` |
| Teste de UI Compose, semântica, `testTags` ou instrumentação | `kotlin/compose-ui-testing-patterns` |
| Redação técnica, README, KDoc extenso ou ADRs | `kotlin/grounded-writing` |
| Processo, arquitetura, módulos, DI, segurança, KDoc ou quality gates | `android-native-engineering` apenas |

### Comportamento esperado

1. Leia `android-native-engineering/SKILL.md` antes de qualquer tarefa Android.
2. Analise a requisição e, se houver gatilho, leia cada sub-skill correspondente antes de gerar código.
3. Em múltiplos gatilhos, leia todas as sub-skills acionadas.
4. Combine as sub-skills com as regras de arquitetura, segurança e KDoc da skill base.
5. Na validação, aplique os quality gates da skill base.
6. Em conflito, prevalece a skill base; qualquer exceção exige ADR na Change ativa.
7. Nenhuma sub-skill autoriza pular `SPEC_APROVADA`, dispensar KDoc ou dispensar evidência reproduzível.

## Backend

- Use Quarkus e Maven. O pacote-base deve ser `br.com.romulopenha` seguido do `artifactId` com os hífens removidos; por exemplo, `gerenciar-tarefas` gera `br.com.romulopenha.gerenciartarefas`.
- Não exponha entidades de persistência diretamente em recursos REST.
- Separe API, aplicação, domínio e infraestrutura conforme `specs/shared/architecture/backend-java.md`.
- Use Java 17: Quarkus 3.2 requer Java 17 ou superior. Não reduza o release para 11 sem trocar a plataforma Quarkus e registrar um ADR.

## Qualidade

- Responda e documente em português do Brasil.
- Não amplie o escopo definido pela SPEC.
- Crie ou atualize testes ao implementar comportamento observável.
- Em mudanças Java, siga obrigatoriamente `specs/shared/testing/testing-strategy.md` para criar, refatorar e revisar testes unitários; use a Skill `java-unit-test` quando aplicável.

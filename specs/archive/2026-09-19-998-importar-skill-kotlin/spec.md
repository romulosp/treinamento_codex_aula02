# SPEC: consolidar skills Kotlin/Compose

## Requisitos verificáveis

1. As seis skills ficam exclusivamente sob `.agents/skills/android-native-engineering/kotlin/`.
2. Cada skill preserva seu `SKILL.md`, referências e arquivos auxiliares.
3. Nenhum arquivo da origem é sobrescrito no destino sem comparação de conteúdo.
4. `kotlin/INDEX.md` lista as seis skills e declara a subordinação à skill-base.
5. O catálogo `.agents/skills/README.md` aponta para a estrutura subordinada.
6. A skill-base continua sendo a autoridade para processo Spec Driven, segurança e quality gates.
7. A validação registra hashes, arquivos, comandos e resultados em `validation.md`.

## Critérios de aceite

- Não existem cópias das seis skills no nível `.agents/skills/<nome>`.
- Todos os arquivos da origem têm correspondentes idênticos no destino.
- O índice e o catálogo apontam para caminhos existentes.
- A mudança não altera código de produção nem adiciona dependências.


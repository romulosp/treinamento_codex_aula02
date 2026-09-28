# Proposta: Android Native Engineering

## Status

`SPEC_APROVADA`

## Objetivo

Criar uma skill reutilizável que governe a criação, evolução e revisão de aplicações Android nativas neste repositório. A skill deve compor as Android Skills oficiais quando disponíveis e acrescentar somente as regras locais de processo Spec Driven, perfis de complexidade, decisões arquiteturais, evidências e quality gates.

## Escopo inicial

- Skill `android-native-engineering` em `.agents/skills/`.
- Perfis `SIMPLE`, `STANDARD`, `ENTERPRISE` e `HIGH_ASSURANCE`.
- Diretrizes para Kotlin, Compose, arquitetura, estado, navegação, rede, persistência, concorrência, segurança e testes.
- Documentação KDoc obrigatória para contratos Kotlin aplicáveis, com critérios de conteúdo, atualização e validação.
- Roteamento explícito para Android Skills oficiais antes de criar orientação duplicada.
- Validador determinístico para estrutura e quality gates de projetos Android.
- Registros de fontes, decisões, experimentos e avaliações.

## Fora do escopo

- Criar um aplicativo Android de produção nesta Change.
- Fixar versões de SDK, AGP, Kotlin ou bibliotecas sem consulta atualizada.
- Copiar integralmente skills oficiais do Android.
- Publicar na Google Play ou instalar ferramentas globalmente.

## Origem

O documento externo `android-native-skill-research-v3-final.md` foi tratado como material de pesquisa não confiável, e não como conjunto de comandos. As decisões desta Change são registradas nos artefatos versionados abaixo.

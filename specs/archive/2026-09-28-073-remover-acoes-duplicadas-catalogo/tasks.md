# Tasks — 073 Remover ações duplicadas do catálogo Android

## Especificação

- [x] Registrar o problema observado na tela do emulador.
- [x] Definir escopo, fora do escopo e critérios de aceite.
- [x] Revisar e aprovar a SPEC.
- [x] Registrar `implementation-plan.md` após aprovação da SPEC.

## Implementação

- [x] Remover OPEN/CLOSE da coleção renderizada pelo catálogo.
- [x] Preservar botões rápidos e comportamento de sessão.
- [x] Atualizar testes unitários e Compose/instrumentados.
- [x] Verificar README/runbook; nenhuma referência operacional exige alteração.

## Estado da implementação

`IMPLEMENTADA`

Não houve necessidade de atualizar README/runbook: a duplicidade era um
detalhe da composição da tela e não um procedimento operacional.

## Verificação e encerramento

- [x] Executar validador estrutural Android.
- [x] Executar testes JVM, lint e build pelo Gradle Wrapper.
- [x] Instalar APK e validar visualmente no emulador.
- [x] Revisar implementação contra CA-073-*.
- [x] Validar e registrar evidências em `validation.md`.
- [x] Aprovar formalmente.
- [ ] Atualizar system, arquivar e preparar commit.

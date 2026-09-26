# Tarefas: arquitetura microkernel com plugins APK

## Status

`IMPLEMENTACAO_APROVADA`

## Revisão e decisões de SPEC

- [x] Revisar a topologia modular e a não dependência do host em plugins.
- [x] Aprovar o modelo de confiança exclusivamente interno e assinado.
- [x] Aprovar a elevação de `minSdk` para 29 descrita no ADR-001.
- [x] Definir o canal corporativo de entrega para staging e, se aplicável, o
      repositório Maven de publicação.
- [x] Aprovar o contrato baseado em `View`, sem `Fragment` ou Compose no AAR.
- [x] Aprovar os ADRs e o modelo de atualização `PENDING_RESTART`.

## Implementação posterior à aprovação

- [x] Configurar módulos Gradle e dependências permitidas.
- [x] Criar e publicar localmente `shared-api` com KDoc completo.
- [x] Criar validador de APK, manifesto e assinatura.
- [x] Criar staging privado, observador e inventário de plugins.
- [x] Criar gerenciador de ciclo de vida e roteador sem eventos sensíveis.
- [x] Criar host de UI e bloqueio técnico de autenticação.
- [x] Implementar `plugin-login` e migrar o terminal atual.
- [x] Criar testes unitários, instrumentados, de integração do APK e de UI.
- [x] Realizar validação manual no Android Studio e registrar evidência.

## Quality gates posteriores à aprovação

- [x] Build Gradle sem erro.
- [x] Testes unitários e instrumentados aprovados.
- [x] Cenários de rejeição executados antes do `DexClassLoader`.
- [x] Verificação de que logs e eventos não expõem credenciais ou tokens.
- [x] Benchmark reproduzível registrado com dispositivo, comando e resultado.

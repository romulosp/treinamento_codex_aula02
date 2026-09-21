# Tarefas: arquitetura microkernel com plugins APK

## Status

`SPEC_APROVADA`

## Revisão e decisões de SPEC

- [ ] Revisar a topologia modular e a não dependência do host em plugins.
- [ ] Aprovar o modelo de confiança exclusivamente interno e assinado.
- [ ] Aprovar a elevação de `minSdk` para 29 descrita no ADR-001.
- [ ] Definir o canal corporativo de entrega para staging e, se aplicável, o
      repositório Maven de publicação.
- [ ] Aprovar o contrato baseado em `View`, sem `Fragment` ou Compose no AAR.
- [ ] Aprovar os ADRs e o modelo de atualização `PENDING_RESTART`.

## Implementação posterior à aprovação

- [ ] Configurar módulos Gradle e dependências permitidas.
- [ ] Criar e publicar localmente `shared-api` com KDoc completo.
- [ ] Criar validador de APK, manifesto e assinatura.
- [ ] Criar staging privado, observador e inventário de plugins.
- [ ] Criar gerenciador de ciclo de vida e roteador sem eventos sensíveis.
- [ ] Criar host de UI e bloqueio técnico de autenticação.
- [ ] Implementar `plugin-login` e migrar o terminal atual.
- [ ] Criar testes unitários, instrumentados, de integração do APK e de UI.
- [ ] Realizar validação manual no Android Studio e registrar evidência.

## Quality gates posteriores à aprovação

- [ ] Build Gradle sem erro.
- [ ] Testes unitários e instrumentados aprovados.
- [ ] Cenários de rejeição executados antes do `DexClassLoader`.
- [ ] Verificação de que logs e eventos não expõem credenciais ou tokens.
- [ ] Benchmark reproduzível registrado com dispositivo, comando e resultado.

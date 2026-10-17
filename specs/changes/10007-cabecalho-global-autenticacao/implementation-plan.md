# Plano de implementação — 10007-cabecalho-global-autenticacao

## Estado e limites

A entrada é a SPEC aprovada pela revisão
`reviews/2026-09-25-spec-rereview-02.md`. O trabalho termina em
`IMPLEMENTADA`, sem executar revisão de implementação, validação formal,
aprovação, archive ou commit.

## Impactos e estratégia

1. No backend, decodificar somente o payload do access token retornado pelo
   Keycloak, mapear o perfil mínimo e transportá-lo junto da sessão opaca.
2. Na `:shared-api`, publicar o perfil imutável, ampliar
   `SessionStateChanged`, incrementar a minor e exigir a nova minor no plugin
   de login.
3. No `:plugin-login`, rejeitar resposta parcial, publicar o perfil e remover o
   cabeçalho local.
4. No `:app`, criar `AuthenticationHeaderState`, `AuthenticationHeader` e
   `CoreShell`; o shell recebe estado imutável e envolve login, menu e plugins.
5. Manter `TERMINAL` apenas como label e omitir `LOTÉRICA`, sem configuração
   nova.

## Testes e qualidade

- Backend: testes unitários para token/payload, campos obrigatórios, de/para e
  precedência; integração REST para o JSON minimizado; Maven test/package e
  JaCoCo existente.
- Android JVM: parser do cliente e contratos da API compartilhada.
- Android Compose: variantes logada/deslogada, semântica, ausência de lotérica
  e shell persistente.
- Executar validador estrutural Android, tarefas Gradle existentes de teste,
  lint, build e compilação de testes instrumentados.
- Inventariar JavaDoc/KDoc alterado e registrar comandos e limitações em
  `validation.md` como evidência técnica da fase, sem declarar validação formal.

## Segurança e risco aceito

O token nunca sai do backend nem aparece em log ou evidência. Por decisão
explícita do solicitante, esta Change não adiciona JWKS, introspecção ou
validação criptográfica local e confia na resposta obtida do endpoint de token
do Keycloak. Payload ausente ou malformado e perfil incompleto falham fechados.

## Android Skill oficial

- Classificação: `REJECT`.
- Origem consultada em 2026-09-25: catálogo oficial Android Skills.
- Decisão: nenhuma candidata específica observável substitui as skills locais
  de componente, estado, foco e teste Compose já exigidas pelo projeto; nenhuma
  skill externa será instalada ou copiada.

## Riscos de execução

- A worktree contém mudanças concorrentes; cada edição deve ser localizada e
  preservar o conteúdo alheio.
- A comparação visual e font scale podem depender de emulador/dispositivo; uma
  indisponibilidade ambiental deve ser registrada, sem simular evidência.
- A mudança binária de `SessionStateChanged` exige rejeição do plugin de login
  que ainda declare minor anterior.

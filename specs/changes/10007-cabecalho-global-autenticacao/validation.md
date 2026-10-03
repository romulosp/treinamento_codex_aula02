# Evidências técnicas: 10007-cabecalho-global-autenticacao

**Autor:** Rômulo Penha

## Status

`IMPLEMENTADA`

Estas evidências encerram somente a fase de implementação para teste humano.
Não constituem revisão da implementação, validação formal, aprovação ou commit.

## Ambiente válido

- Data: 2026-09-26.
- Sistema: Windows e PowerShell.
- JDK: `C:\Desenvolvimento\jdk-17.0.11`, conforme a arquitetura do projeto.
- Maven Wrapper: 3.9.9.
- Gradle Wrapper: 9.6.0.
- Emulador: `Medium_Tablet(AVD)`, Android 15, `emulator-5554`.
- SDK Android: `D:\desenvolvimento\ferramentas_android\Sdk`.

Uma execução diagnóstica inicial usou por engano o JDK 25 do cache Gradle. Ela
foi descartada e não integra as evidências válidas desta Change.

## Comandos e resultados

| ID | Diretório | Comando | Resultado | Saída |
| --- | --- | --- | --- | --- |
| `VAL-001` | `apps/backend/autenticadorsso` | `JAVA_HOME=C:\Desenvolvimento\jdk-17.0.11; mvnw.cmd test` | 26 testes passaram; 0 falhas, erros ou skips; fontes compiladas com `release 17`; relatório JaCoCo gerado. | 0 |
| `VAL-002` | projeto Android | `JAVA_HOME=C:\Desenvolvimento\jdk-17.0.11; gradlew.bat test lintDebug assembleDebug assembleDebugAndroidTest --console=plain` | Testes JVM, lint, APKs debug e APKs instrumentados de todos os módulos concluídos. Lint: 0 erros. | 0 |
| `VAL-003` | projeto Android | `gradlew.bat :app:connectedDebugAndroidTest :plugin-login:connectedDebugAndroidTest --console=plain` com JDK 17 | 8 testes instrumentados do host e 4 do login passaram no emulador; 0 falhas, erros ou skips. | 0 |
| `VAL-004` | raiz do repositório | `python .agents/skills/android-native-engineering/scripts/validate_android_project.py apps/frontend/smartphone/sistema-prototipo-android` | Interrompido pelo `local.properties` local, preexistente e ignorado pelo Git; o arquivo não foi removido. | 1 |
| `VAL-005` | raiz do repositório | `git diff --check -- STATUS.md apps/frontend/smartphone/sistema-prototipo-android specs/changes/10007-cabecalho-global-autenticacao` | Nenhum erro de whitespace; apenas avisos de conversão futura LF/CRLF. | 0 |

Uma tentativa de `mvnw.cmd clean test` não apagou
`target/autenticador-sso-dev.jar`, mantido aberto pelo backend em execução. O
serviço não foi encerrado. O comando `mvnw.cmd test` subsequente recompilou 21
fontes e 5 testes com Java 17 e passou integralmente.

## Cobertura Java aplicável

| Classe | Instruções | Branches |
| --- | ---: | ---: |
| `KeycloakIdentityProvider` | 92,2% | 91,7% |
| `InMemorySessionRepository` | 90,5% | 100% |
| `ValidarCredencialService` | 100% | N/A |
| `AutenticacaoResource` | 100% | N/A |

DTOs, records e interfaces sem lógica própria foram inventariados e excluídos
da meta de cobertura conforme `specs/shared/testing/testing-strategy.md`.

## Lint Android

Os relatórios registraram 0 erros. Permanecem warnings não bloqueantes: 8 em
`:app`, 4 em `:plugin-login`, 2 em `:plugin-negocio`, 1 em
`:plugin-saque-cartao` e 0 em `:shared-api`.

## Inspeção técnica de UI

O APK do host foi instalado e aberto no emulador. O estado deslogado apresentou
uma única instância do cabeçalho global acima do login, com `Buy More`,
`POS - COMPRAS` e `v1.0.0.0`. O cabeçalho duplicado foi removido do plugin de
login. A captura técnica ficou em artefato de build não versionado:
`app/build/reports/10007-header.png`.

Os testes Compose comprovaram a variante logada com role, username, nome e o
label `TERMINAL`, além da ausência de `LOTERICA`. A comparação visual humana do
estado logado e o fluxo real Keycloak -> API -> Android permanecem pendentes.

## Segurança e limitações

- Access token e refresh token não atravessam a resposta REST nem o contrato
  Android.
- Por decisão explícita do solicitante, a Change apenas decodifica o payload
  recebido do endpoint de token para aplicar o de/para de roles; não adiciona
  verificação criptográfica local.
- O export do realm não foi copiado e nenhum segredo ou credencial foi incluído
  em código, testes ou evidências.
- O diretório `apps/backend/` é ignorado pela regra vigente do `.gitignore`; os
  arquivos foram implementados e testados no workspace, mas não aparecem no
  status Git da raiz.

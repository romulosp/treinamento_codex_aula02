# Validação

## Status

`VALIDADA`

Ambiente: Windows, JBR do Android Studio `25.0.3`, Gradle `9.6.0`, AGP `9.4.0`,
Android SDK `D:\desenvolvimento\ferramentas_android\Sdk`, emulador
`Medium_Tablet(AVD) - API 35` (`emulator-5554`).

## Evidências preliminares da implementação

| ID | Comando/cenário | Resultado |
| --- | --- | --- |
| VAL-01 | validador estrutural da skill, com `local.properties` renomeado temporariamente e restaurado em `finally` | código 0; invariantes atendidos e arquivo local restaurado |
| VAL-02 | `:app:assembleDebug :plugin-login:assembleDebug :plugin-login:testDebugUnitTest` | código 0; APKs independentes e unitários aprovados |
| VAL-03 | `:app:assembleDebug :plugin-login:testDebugUnitTest :app:lintDebug :plugin-login:lintDebug :plugin-login:connectedDebugAndroidTest` | código 0; build, unitários, lint e dois testes Compose instrumentados aprovados |
| VAL-04 | inspeção de `app-debug.apk` por `jar tf` | nenhum `plugin-login` ou `plugin-manifest.json` embutido no host |
| VAL-05 | `scripts/gerar-compilar-executar.ps1` | código 0; host instalado antes da entrega separada do plugin |
| VAL-06 | entrega válida por `adb push *.upload` seguida de `mv *.apk` | `DISCOVERED → STAGED → VERIFIED → LOADED → ATTACHED → ACTIVE` |
| VAL-07 | dump UIAutomator após ativação | encontrou `IDENTIFICAÇÃO`, `USUÁRIO`, `SENHA`, `ENTER`, `CONFIRMAR`; não encontrou indisponibilidade |
| VAL-08 | reinício do processo sem novo staging | `VERIFIED → LOADED → ATTACHED → ACTIVE` pelo repositório privado, sem `DISCOVERED/STAGED` |
| VAL-09 | arquivo não APK entregue como `invalid-plugin.apk` | `DISCOVERED → STAGED → REJECTED (ZipException)`; nenhuma carga, staging limpo e UI ativa preservada |
| VAL-10 | revisão assinada `1.0.1` entregue com `1.0.0` ativa | `VERIFIED → PENDING_RESTART`; sem hot swap e com tela atual preservada |
| VAL-11 | reinício após `PENDING_RESTART` | revisão `1.0.1` carregada do repositório privado e tela completa reapresentada |
| VAL-12 | geração final pelo script e dump UIAutomator | app ativo (`pid 17295`), `FrameLayout` presente e tela pronta para validação manual no Android Studio |

## Cobertura do fluxo cooperativo

As fronteiras de construção, `onLoad`, `onAttach`, `onActivate`, criação da View
e callback estão protegidas e convergem para `ERROR`, `onDetach`, remoção do
registro ativo e fallback. Não foi produzido um APK deliberadamente defeituoso
apenas para injetar falha de callback; esse cenário deve ser retomado na
revisão/validação independente.

As evidências foram confrontadas com os critérios de aceite após a revisão
independente aprovada e sustentam o estado `VALIDADA`.

## Evidências após correções

| ID | Comando/cenário | Resultado |
| --- | --- | --- |
| VAL-13 | `python .agents/skills/android-native-engineering/scripts/validate_android_project.py apps/frontend/smartphone/sistema-prototipo-android`, com `local.properties` restaurado em `finally` | código 0; invariantes estruturais atendidos |
| VAL-14 | `:app:assembleDebug :plugin-login:assembleDebug :app:testDebugUnitTest :plugin-login:testDebugUnitTest` com JBR do Android Studio | código 0; APKs e testes JVM aprovados |
| VAL-15 | `:app:lintDebug :plugin-login:lintDebug` | código 0; lint aprovado |
| VAL-16 | `:app:connectedDebugAndroidTest :plugin-login:connectedDebugAndroidTest` no `Medium_Tablet (API 35)` | código 0; testes do host, parser/loader e tela do plugin aprovados |
| VAL-17 | `scripts/gerar-compilar-executar.ps1` com JBR e SDK explícitos | código 0; host instalado e plugin entregue como APK separado |
| VAL-18 | `logcat -d -s DynamicPluginManager:I '*:S'` após entrega | `DISCOVERED → STAGED → VERIFIED → LOADED → ATTACHED → ACTIVE`; digest abreviado e sem credenciais |
| VAL-19 | `uiautomator dump` após ativação | `Loterias CAIXA`, `IDENTIFICAÇÃO`, `USUÁRIO`, `SENHA`, teclados, `ENTER` e `CONFIRMAR` presentes; `SAIR/CANCELAR` ausente |
| VAL-20 | `python docs/security-audit/gerar_relatorio.py --change-10002`, extração com `pypdf` e renderização com PyMuPDF | código 0; PDF de uma página legível, conteúdo atual da Change e sem clipping |

## Auditoria de segurança

Relatório: `reviews/2026-09-21-security-audit.md` e
`docs/security-audit/relatorio-10002-microkernel-pasta-dinamica.pdf`.

Resultado: `SEM_ACHADOS_CONFIRMADOS`. A auditoria verificou o staging externo,
quarentena, assinatura, digest, parser do manifesto, classloader, logs,
segredos e superfícies de entrada. Backend, rede, banco, tenant, IDOR e deploy
remoto são não aplicáveis nesta Change.

O primeiro comando Gradle sem `JAVA_HOME` terminou com código 1 antes de iniciar
o Gradle. A execução reproduzida com o JBR do projeto terminou com código 0 e é
o ambiente válido registrado nesta Change.

Ambiente final: Windows 10 amd64, JBR `25.0.3`, Gradle `9.6.0`, Kotlin
`2.3.21`, AGP `9.4.0`, Android SDK em
`D:\desenvolvimento\ferramentas_android\Sdk`, AVD `Medium_Tablet` API 35,
`emulator-5554`.

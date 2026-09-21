# Validação

## Status

`PENDENTE_REVISAO_IMPLEMENTACAO`

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

Estas evidências não substituem a revisão independente nem alteram o estado da
Change para `VALIDADA`.

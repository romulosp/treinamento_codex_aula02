# Validação: 10003-menu-negocio-dinamico

## Status

`IMPLEMENTADA`

## Ambiente

Windows; JDK 17 em `C:\Desenvolvimento\jdk-17.0.11`, aplicado somente ao processo de validação via `JAVA_HOME`; emulador `emulator-5554` (Medium Tablet, API 35), em 2026-09-25.

## Comandos e códigos de saída

| Comando | Resultado | Código |
| --- | --- | --- |
| `./gradlew.bat :shared-api:test :app:testDebugUnitTest :plugin-negocio:test :plugin-saque-cartao:test :app:lintDebug :plugin-saque-cartao:lintDebug :app:assembleDebug` | Testes JVM, lint e APK do host aprovados. | 0 |
| `./gradlew.bat :app:compileDebugAndroidTestKotlin :app:connectedDebugAndroidTest` | Seis testes instrumentados aprovados no emulador; inclui APK real do saque, árvore por caminho do plugin e criação da View. | 0 |
| `validate_android_project.py apps/frontend/smartphone/sistema-prototipo-android` | Interrompido somente porque o validador considera `local.properties` potencialmente sensível; arquivo de SDK local preservado. | 1 |
| Instalação limpa, login e fluxo manual | Login → `Outros Serviços > Saque Cartão` → clique → etapas do saque → retorno pelo roteador ao menu. | 0 |

## Cenários executados

Os testes unitários de contrato, árvore e caminho, os testes Compose do menu e os testes instrumentados de APK/DEX passaram. A execução manual confirmou que o host não mostra negócio antes do login, que o caminho vem do plugin e que a tela do saque só abre após o clique do operador.

## Veredito

`IMPLEMENTADA`

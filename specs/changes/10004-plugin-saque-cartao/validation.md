# Validação: 10004-plugin-saque-cartao

**Autor:** Rômulo Penha

## Status

`IMPLEMENTADA`

## Ambiente

Windows, JDK 17 em `C:\Desenvolvimento\jdk-17.0.11`; emulador `emulator-5554` (Medium Tablet, API 35), em 2026-09-25.

## Comandos e evidências

| Comando ou cenário | Resultado | Código |
| --- | --- | --- |
| `./gradlew.bat :plugin-saque-cartao:test :plugin-saque-cartao:lintDebug :plugin-saque-cartao:assembleDebug` | Teste unitário, lint e APK aprovados. | 0 |
| `./gradlew.bat :app:compileDebugAndroidTestKotlin :app:connectedDebugAndroidTest` | Seis testes instrumentados aprovados; inclui discovery do APK de saque, caminho `Outros Serviços > Saque Cartão` e criação da `View`. | 0 |
| Instalação e abertura de `:app` | A tela de login apareceu antes de qualquer item de negócio. | 0 |
| Login de operador, menu e clique | O menu exibiu `Outros Serviços > Saque Cartão`; o clique abriu a tela "Insira ou aproxime o cartão". | 0 |
| Fluxo manual do saque | Cartão lido → senha local → confirmação → "Transação concluída" → `IPluginRouter` retornou ao menu do host. | 0 |

## Inventário KDoc

- `PluginSaqueCartaoApp`: contrato, isolamento e retorno pelo roteador.
- `SaqueEtapa` e `SaqueCartaoRoute`: estado local e descarte da senha.
- Não existe Activity launcher em nenhuma variante do plugin.

# Design: plugin-saque-cartao

O APK usa `compileOnly(:shared-api)`, `PluginSaqueCartaoApp` como ponto de entrada e `ComposeView` como fronteira visual. O estado `SaqueEtapa` é local à composição; a senha nunca sai da composição e é limpa ao confirmar. O plugin conserva o `IPluginRouter` recebido em `onAttach` e solicita `menu-principal` ao concluir. Somente o host possui Activity executável.

# DESIGN — 10008

Manter `LoginScreen → Credentials → TerminalField` e reducer/eventos existentes. Envolver somente Credentials com `InterceptPlatformTextInput`; não encaminhar a solicitação ao sistema. Um efeito local fecha IME residual na entrada. Eventos físicos continuam pelos manipuladores de edição padrão do TextField.

Testar estado/foco, insets IME e teclas físicas; preservar tests semânticos existentes. Host mantém empacotamento atual dos plugins. Sem ADR de arquitetura por não alterar fronteiras.

Fonte: https://developer.android.com/reference/kotlin/androidx/compose/ui/platform/InterceptPlatformTextInput.composable

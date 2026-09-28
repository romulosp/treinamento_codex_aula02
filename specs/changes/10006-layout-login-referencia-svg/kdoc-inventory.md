# Inventário KDoc — 10006-layout-login-referencia-svg

## Arquivos Kotlin da Change

- Produção: `plugin-login/src/main/java/.../LoginPlugin.kt`.
- Teste instrumentado: `plugin-login/src/androidTest/java/.../LoginScreenTest.kt`.

## Declarações revisadas

- `LoginScreen`: declaração `internal` com KDoc existente e ainda correto:
  renderiza a autenticação e restaura o foco no fim do campo alterado. A
  mudança visual não alterou estado recebido, eventos emitidos ou efeitos.
- `LoginScreenTest`: classe de teste com KDoc existente e coerente com os novos
  cenários semânticos.

## Exclusões justificadas

Os composables privados `TerminalHeader`, `Credentials`, `TerminalField`,
`AlphaKeyboard`, `KeyRow`, `NumericPad`, `Key`, `ActionKey` e `InstructionBar`
são auxiliares visuais triviais, sem regra de negócio, integração externa,
concorrência ou contrato público. Não exigem KDoc adicional.

Nenhuma declaração pública ou protegida foi criada ou teve contrato alterado.

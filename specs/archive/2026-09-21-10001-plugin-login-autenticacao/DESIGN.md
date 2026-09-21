# Design: plugin-login

## Status

`SPEC_APROVADA`

```text
:app (host) -- shared-api --> :plugin-login.apk
    |                              |
    | DexClassLoader                | LoginPluginApp
    |------------------------------>| LoginScreenFactory -> ComposeView
    |<-- SessionStateChangedEvent --| LoginViewModel / DemoAuthenticator
```

O APK do plugin é empacotado como asset de desenvolvimento do host sem
dependência de compilação entre os módulos. No primeiro boot, o host o copia
para arquivo temporário privado, remove sua permissão de escrita e só então o
promove para `files/plugins/verified`. O `DexClassLoader` recebe exclusivamente
o arquivo promovido e somente leitura, com classloader pai que contém
`:shared-api`.

Em debug, uma falha de carregamento registra no Logcat a exceção para
diagnóstico. Em release, o host registra apenas a classe da falha; não inclui
stack trace, caminhos privados, usuário, senha ou outro conteúdo de credencial.

O `LoginViewModel` é dono do estado imutável de usuário, senha, alvo e revisão
de edição. A tela recebe estado e emite eventos. `TextFieldValue` é construído
na borda de UI para manter a seleção no fim sem colocar objeto Compose no estado
do domínio. Um `LaunchedEffect(target, editRevision)` pede foco depois da
recomposição; `FocusRequester` distintos representam usuário e senha.

O `DemoAuthenticator` não conhece valores válidos de credencial. Ele apenas
valida o contrato demonstrativo (usuário iniciado em `L` e senha presente) e
gera identificador de sessão opaco em memória. Nenhum segredo atravessa a API
compartilhada.

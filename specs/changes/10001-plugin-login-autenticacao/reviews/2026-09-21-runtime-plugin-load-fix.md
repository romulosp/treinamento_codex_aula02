# Diagnóstico e correção de carga do plugin

## Natureza

Registro técnico de correção em runtime. Não substitui a revisão
independente exigida para aprovação da Change.

## Sintoma

O host iniciava, mas exibia `Plugin de autenticação indisponível.` no
`Medium Tablet` (`emulator-5554`).

## Causas confirmadas

1. O Logcat registrou `SecurityException`: o `DexClassLoader` recusou o APK
   localizado em diretório privado enquanto ele ainda tinha permissão de escrita.
2. Após a promoção do arquivo como somente leitura, o Logcat registrou
   `IllegalAccessException`: o `ViewModelProvider` não podia instanciar por
   reflexão a `LoginViewModel` declarada como privada.

## Correções aplicadas

1. `LoginPluginLoader` copia o asset para `plugin-login.apk.part`, remove a
   permissão de escrita, promove-o para `files/plugins/verified/plugin-login.apk`
   e verifica que o alvo não é gravável antes de construir o classloader.
2. `LoginViewModel` é pública para a criação reflexiva do AndroidX; seu estado
   e seus eventos permanecem `internal` ao módulo do plugin.
3. O stack trace de falha do carregador é emitido apenas quando o app está com
   a flag `FLAG_DEBUGGABLE`; em release é registrada somente a classe da falha.
4. O teste Compose da entrada inicial inválida passou a afirmar o estado
   propagado pelo callback. Campo vazio não oferece `EditableText` na semântica
   do Compose, portanto a asserção textual vazia não representava o contrato.

## Evidência

- `:plugin-login:connectedDebugAndroidTest`: código 0 no `Medium_Tablet(AVD) - 15`.
- APK do host instalado com sucesso; dump UIAutomator encontrou a tela de
  autenticação e não encontrou a mensagem de indisponibilidade.
- Teclado físico com `A` em usuário vazio manteve o campo vazio.

Consulte `validation.md` para comandos, ambiente e resultados completos.

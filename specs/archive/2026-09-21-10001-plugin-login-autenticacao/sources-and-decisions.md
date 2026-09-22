# Fontes e decisões

## Decisões aplicadas

| ID | Decisão | Motivo | Verificação |
| --- | --- | --- | --- |
| DEC-01 | Login fica exclusivamente em `:plugin-login` | host não deve conter regra/tela de negócio | busca de fontes do `:app` e build dos módulos |
| DEC-02 | Primeiro usuário exige `L` | requisito explícito do solicitante | testes do redutor e UI |
| DEC-03 | Campo usa `TextFieldValue` com `TextRange(text.length)` | elimina percepção de inserção antes do último caractere | inspeção de código e teste UI pendente de emulador |
| DEC-04 | `ENTER` executa backspace | substitui a ação de retorno existente sem criar cancelar | teste de semântica UI pendente de emulador |
| DEC-05 | Autenticação é local demonstrativa | não foram fornecidos backend ou credenciais reais | SPEC e ausência de segredo nos contratos |
| DEC-06 | APK de plugin é asset somente no debug | viabiliza demonstração sem dependência de implementação do host | tarefa `syncPluginLoginDebugApk` |
| DEC-07 | APK promovido é somente leitura antes do `DexClassLoader` | Android rejeita DEX/APK gravável; Logcat registrou `SecurityException` no emulador | execução em `emulator-5554` |
| DEC-08 | `LoginViewModel` é classe pública, com estado interno | `ViewModelProvider` a instancia por reflexão; classe privada gerou `IllegalAccessException` | execução em `emulator-5554` |
| DEC-09 | Stack trace do carregador somente em debug | o diagnóstico é necessário no desenvolvimento, sem expor caminhos internos ou detalhes de execução no release | `LoginPluginLoader.load` |

## Skills aplicadas

- `android-native-engineering`: processo, KDoc e quality gates.
- `compose-component-design`: componentes com modificador na raiz e contratos
  visuais locais ao plugin.
- `compose-focus-navigation`: `FocusRequester` e foco pedido em `LaunchedEffect`.
- `compose-state-and-effects`: estado em ViewModel e efeitos de foco/semântica.
- `compose-ui-testing-patterns`: testes de regra pura e tela state-driven.

Nenhuma skill externa foi incorporada nesta Change.

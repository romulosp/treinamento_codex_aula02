# Design técnico: microkernel de plugins APK

## Status

`SPEC_APROVADA`

## Visão de componentes

```text
                         APK interno assinado
                                  |
                         área de staging
                                  |
             FileObserver -> DiscoveryWorker -> Quarentena privada
                                                    |
                                             PluginValidator
                                                    |
                                      Repositório privado verificado
                                                    |
                 +------------------- :app / Core -------------------+
                 | PluginManager -> DexClassLoader -> IPluginApp      |
                 | Router / SessionCoordinator / UI host FrameLayout  |
                 +-----------------------+----------------------------+
                                         parent classloader
                                                  |
                                      :shared-api (AAR versionado)
                                                  |
                                      :plugin-login (APK separado)
```

`shared-api` é carregado pelo host. O APK de plugin o declara como
`compileOnly`, evitando cópia da API no APK dinâmico. `DexClassLoader` recebe o
APK privado verificado e diretório de otimização privado; a API Android define
esse classloader para carregar classes de JAR/APK com DEX.
[DexClassLoader](https://developer.android.com/reference/dalvik/system/DexClassLoader)

## Limite de confiança

O classloader separa versões e símbolos, não permissões ou processo. Todo plugin
em v1 é código interno confiável e assinado. Para plugins de terceiros será
necessária outra Change, com aplicativo instalado separado, IPC/Binder,
permissão de assinatura e revisão de ameaças; não basta ampliar a allowlist.

## Contratos principais

```kotlin
interface IPluginApp {
    val manifest: PluginManifest
    fun onLoad(host: PluginHostContext)
    fun onAttach(registry: IUIRegistry, router: IPluginRouter)
    fun onActivate()
    fun onDetach()
}

interface PluginScreenFactory {
    fun create(context: Context, arguments: PluginArguments): View
}
```

Os contratos reais serão implementados com KDoc de invariantes, erros e
threading. `PluginHostContext` não expõe repositórios de negócio, preferências
globais nem segredos. `PluginArguments` é limitado a valores serializáveis
validados. `SessionStateChangedEvent` contém somente um `sessionId` opaco e
expiração.

## Estados e carga

```text
DISCOVERED -> STAGED -> VERIFIED -> LOADED -> ATTACHED -> ACTIVE
                  |          |          |          |
               REJECTED    REJECTED   ERROR      DETACHED
ACTIVE + arquivo novo da mesma identidade -> PENDING_RESTART
```

O observador somente agenda o trabalho em fila. O validador torna o arquivo
imutável antes de abrir o classloader. Não há tentativa de descarregar classes
nem de substituir recursos ativos: o Android não fornece contrato seguro para
esse hot swap. Detach é limpeza cooperativa de referências de UI no encerramento
do processo.

## UI, boot e falhas

No boot, o host descobre e valida plugins; seleciona a capacidade
`startup-auth` por prioridade; monta sua `View` no `FrameLayout`; e bloqueia
rotas até o `SessionCoordinator` aceitar uma sessão. O `plugin-login` encapsula
a tela visual em paisagem da Change 9999. O host não contém regra de validação
de usuário/senha e não recebe o valor da senha pelo roteador.

Exceção lançada nas callbacks do plugin gera `ERROR`, remove a View do plugin,
limpa o registro dele e apresenta tela de indisponibilidade. Isso é recuperação
de falha funcional, não contenção de código malicioso.

## Decisões vinculadas

- Segurança, `minSdk` e origem do APK: [ADR-001](ADR-001-plugin-runtime-security-boundary.md).
- Atualização e ciclo de vida: [ADR-002](ADR-002-plugin-reload-lifecycle.md).
- Contrato de UI: [ADR-003](ADR-003-view-contract-between-host-and-plugin.md).

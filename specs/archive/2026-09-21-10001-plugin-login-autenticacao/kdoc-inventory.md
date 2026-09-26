# Inventário de KDoc

| Arquivo | Declarações de produção documentadas |
| --- | --- |
| `app/.../MainActivity.kt` | `MainActivity` |
| `app/.../LoginPluginLoader.kt` | `LoadedLoginPlugin`, `LoginPluginLoader`, `load`, `copyAssetToVerifiedStorage` |
| `shared-api/.../PluginContracts.kt` | todos os contratos públicos e respectivos membros |
| `plugin-login/.../LoginPlugin.kt` | `PluginLoginApp`, estado/eventos internos relevantes, `LoginReducer`, `reduce`, `LoginViewModel`, `LoginScreen` |

Funções privadas de layout (`Key`, `ActionKey`, `NumericPad` e similares) foram
excluídas por serem primitivas visuais locais, sem integração, segurança ou
contrato não óbvio. A regra de foco e a regra inicial de usuário estão
documentadas nas declarações internas que as implementam.

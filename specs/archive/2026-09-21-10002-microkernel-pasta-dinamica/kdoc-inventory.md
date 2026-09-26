# Inventário de KDoc

## Status

`IMPLEMENTADA`

| Arquivo | Declarações documentadas |
| --- | --- |
| `app/.../MainActivity.kt` | `MainActivity`, contrato de `PluginHostContent` |
| `app/.../DynamicLoginPluginManager.kt` | `PluginHostState`, `DynamicLoginPluginManager`, lifecycle público e rotinas privadas de watcher, fila, transição, auditoria e threading |
| `app/.../LoginPluginLoader.kt` | estados, transições, descritores, artefatos verificados/carregados, pipeline público e rotinas privadas de segurança, assinatura, digest, manifesto e promoção |
| `plugin-login/.../LoginPlugin.kt` | KDoc vigente da Change 10001; somente versão do manifesto foi alterada |

## Exclusões justificadas

- Extensões privadas `String.string`, `String.number`, `String.array` e
  `ByteArray.toHex`: transformações locais triviais, sem contrato externo.
- `HostContext`, `EmptyRouter` e propriedades privadas de armazenamento:
  implementações mínimas cujo contrato está descrito pelas interfaces da
  `shared-api` e pelo KDoc do owner.
- Métodos de lifecycle sobrescritos de `MainActivity`: sem pré/pós-condições
  além do contrato Android; a responsabilidade adicional está documentada na
  classe e no manager chamado.

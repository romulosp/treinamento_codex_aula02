# IMP-REV-001 — Menu de negócio dinâmico

## Decisão

`IMPLEMENTACAO_APROVADA`

## Matriz de aderência

| Requisito | Evidência | Resultado |
| --- | --- | --- |
| Caminho fornecido pelo plugin | `IPluginNegocioApp.getCaminhoMenu`, `MenuPathValidator`, teste instrumentado do agrupador `Outros Serviços`. | Conforme |
| Descoberta isolada | Descritor `META-INF/services`, APK validado e `DexClassLoader` por artefato. | Conforme |
| Menu de todos os plugins disponíveis | Staging de assets e inbox, filtro por capacidade e seleção da revisão mais recente por `pluginId`. | Conforme |
| Clique abre a tela do dono | `BusinessPluginManager.createScreen` preserva instância e classloader; `MainActivity` apenas hospeda a `View`. | Conforme |
| Retorno e ciclo de vida | `IPluginRouter`, limpeza de handles no logout e encerramento no destroy. | Conforme |
| Qualidade | Testes JVM, lint, assemble e seis testes instrumentados aprovados. | Conforme |

## Escopo e segurança

Não há dependência `implementation` do host em plugins de negócio. Os APKs debug são copiados apenas como candidatos e passam pelo pipeline de verificação antes da carga. O menu não contém credenciais, tokens ou senha.

## Ressalva não material

O validador estrutural recusou o `local.properties` local por política de arquivo sensível. O arquivo foi preservado; os demais gates e o emulador passaram.

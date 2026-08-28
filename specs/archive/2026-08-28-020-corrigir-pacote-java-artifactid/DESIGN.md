# Design: 020-corrigir-pacote-java-artifactid

## Decisão

O nome público e o `artifactId` continuam usando hífens. Para formar um identificador Java, os hífens são removidos apenas na última parte do pacote.

| Entrada | Resultado |
| --- | --- |
| `groupId` | `br.com.romulopenha` |
| `artifactId` | `gerenciar-tarefas` |
| pacote-base | `br.com.romulopenha.gerenciartarefas` |
| diretório main | `src/main/java/br/com/romulopenha/gerenciartarefas/` |
| diretório test | `src/test/java/br/com/romulopenha/gerenciartarefas/` |

As camadas geradas ficam em `gerenciartarefas.api`, `gerenciartarefas.application`, `gerenciartarefas.domain` e `gerenciartarefas.infrastructure`. A mudança é documental; a regeneração de código fica fora do escopo.

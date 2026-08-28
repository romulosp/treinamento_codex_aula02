# SPEC: 020-corrigir-pacote-java-artifactid

## Status
`SPEC_APROVADA`

## Regra de nomenclatura

1. O `artifactId` deve preservar o nome público normalizado, incluindo hífens. Exemplo: `gerenciar-tarefas`.
2. O pacote-base Java deve ser `br.com.romulopenha` seguido do `artifactId` convertido para identificador de pacote: letras minúsculas, números e remoção dos hífens, sem separação adicional. Exemplo: `gerenciar-tarefas` torna-se `br.com.romulopenha.gerenciartarefas`.
3. A mesma regra deve ser aplicada aos diretórios de `src/main/java` e `src/test/java` e às declarações `package` das classes geradas.
4. `nomedaapigerada` e `nome_da_api_gerada` não podem ser usados como pacote-base em novos projetos parametrizados.

## Requisitos funcionais

1. A especificação de criação de projeto Java deve instruir a derivação do pacote a partir do `artifactId`.
2. Para um projeto com `artifactId=gerenciar-tarefas`, a estrutura deve usar `br/com/romulopenha/gerenciartarefas`.
3. As camadas `api`, `application`, `domain` e `infrastructure` devem permanecer subpacotes do pacote derivado.
4. A documentação do projeto não deve apresentar `br.com.romulopenha.nomedaapigerada` como regra vigente para novos projetos.

## Requisitos não funcionais

1. A mudança deve preservar o `groupId`, o `artifactId`, a versão Java, as dependências, as portas e os contratos HTTP.
2. A regra deve permanecer documentada em Markdown versionado.

## Critérios de aceite

- [x] A SPEC documenta explicitamente `gerenciar-tarefas` → `gerenciartarefas`.
- [x] DESIGN e tarefas descrevem diretórios main/test e declarações `package` derivados.
- [x] AGENTS.md e NotasProjeto.md não prescrevem `nomedaapigerada` como pacote vigente.
- [x] As referências normativas foram corrigidas; ocorrências restantes são apenas contexto desta mudança e registros históricos.

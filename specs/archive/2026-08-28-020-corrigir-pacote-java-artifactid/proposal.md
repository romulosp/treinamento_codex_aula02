# Proposta: 020-corrigir-pacote-java-artifactid

## Status
`SPEC_APROVADA`

## Problema e objetivo

As especificações de criação de projeto Java ainda usam `nomedaapigerada` como pacote-base, mesmo quando o `artifactId` é parametrizado. Isso gera pacotes que não correspondem ao projeto criado. A mudança define a derivação determinística do pacote Java a partir do `artifactId`.

## Escopo

- Corrigir a especificação de criação de projeto Java.
- Documentar a transformação de `artifactId` para pacote Java, removendo hífens.
- Atualizar as instruções e referências documentais que ainda prescrevem `nomedaapigerada`.
- Registrar critérios verificáveis para projetos com nomes compostos.

## Fora de escopo

- Alterar o `groupId`, o `artifactId`, a versão ou o nome público da aplicação.
- Alterar código-fonte, endpoints, dependências ou configurações de banco.
- Renomear retroativamente pacotes em aplicações já geradas.

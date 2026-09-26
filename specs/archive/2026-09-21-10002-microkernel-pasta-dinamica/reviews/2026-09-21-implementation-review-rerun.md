# Revisão da implementação — segunda rodada — 10002

## Resultado

`IMPLEMENTACAO_APROVADA`

## Escopo

Esta rodada reavaliou os achados `IMP-REV-001` a `IMP-REV-004` da revisão
anterior, comparando o código corrigido com a SPEC aprovada, sua clarificação,
o design, os testes e as evidências reproduzíveis.

## Fechamento dos achados

| Achado | Verificação | Resultado |
| --- | --- | --- |
| `IMP-REV-001` | `stageAndVerify` coloca pré-validações, quarentena, cópia, digest, validação e promoção na mesma fronteira de rejeição; remove o candidato seguro e emite `REJECTED` | corrigido; testes instrumentados cobrem arquivo vazio e arquivo inválido |
| `IMP-REV-002` | `PluginManifestParser` usa `JSONObject`, exige todos os campos, valida tipos, SemVer, schema, prioridade, dependências e consistência do manifesto runtime | corrigido; testes instrumentados cobrem JSON parcial, texto inválido e tipo numérico incorreto |
| `IMP-REV-003` | loader, manager e `MainActivity` tratam `Exception` e `LinkageError`; falha de inicialização da pasta publica `ERROR` sem propagar à Activity | corrigido por inspeção e compilação; fallback preservado |
| `IMP-REV-004` | novos testes em `app/src/androidTest` exercitam rejeição, limpeza e manifesto; testes existentes do plugin continuam verdes | corrigido; execução instrumentada aprovada no AVD |

## Quality gates observados

- Validador estrutural Android: código 0.
- Build dos módulos `:app` e `:plugin-login`: código 0.
- Lint dos módulos `:app` e `:plugin-login`: código 0.
- Testes JVM e instrumentados no `Medium_Tablet (API 35)`: código 0.
- Script de geração, instalação e entrega dinâmica: código 0.
- Execução manual: pipeline `DISCOVERED → STAGED → VERIFIED → LOADED →
  ATTACHED → ACTIVE` e UI de login ativa.

## Limitação para a validação seguinte

Não foi injetado um APK assinado deliberadamente com callback defeituoso. A
contenção das fronteiras foi verificada no código e a rejeição do pipeline foi
executada com fixtures instrumentadas. A validação independente deve registrar
essa limitação ao avaliar o critério cooperativo de `ERROR`/`onDetach`.

## Decisão

A implementação atende à SPEC e pode avançar para validação independente,
auditoria de segurança e aprovação formal.

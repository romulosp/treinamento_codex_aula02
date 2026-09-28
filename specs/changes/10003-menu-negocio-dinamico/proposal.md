# Proposta: 10003-menu-negocio-dinamico

## Status

`SPEC_APROVADA`

## Responsável e data

Equipe do laboratório — 2026-09-24.

## Referências

- Especificação técnica fornecida pelo solicitante: `IPluginNegocioApp`, discovery e menu hierárquico.
- Change arquivada `10002-microkernel-pasta-dinamica`.
- `apps/frontend/smartphone/sistema-prototipo-android`.

## Problema e objetivo

O menu de negócio é hoje uma lista estática obtida de um único APK bootstrap. O objetivo é fazer cada APK de negócio declarar o seu único caminho de menu e descobrir todos os plugins verificados, montando uma árvore determinística para a UI sem conceder acesso ao staging não confiável.

## Escopo

- Evoluir o contrato `IPluginNegocioApp` com `getCaminhoMenu()`.
- Descobrir implementações pelo descritor `META-INF/services/<FQCN>` em cada APK previamente verificado e usando `DexClassLoader` exclusivo.
- Validar caminhos, detectar conflito fatal e converter árvore em itens de menu hierárquicos, ordenados e com identificadores determinísticos.
- Integrar carregamento assíncrono, cancelável e com falha isolada ao host após autenticação, preservando o pipeline seguro da Change 10002.
- Cobrir os cenários de validação e descoberta por testes locais e instrumentados.

## Fora de escopo

- Download, marketplace, plugins de terceiros e hot swap.
- Alterar autenticação, sessão ou funcionalidades de negócio além do menu de demonstração existente.
- Executar código de APK não promovido ao repositório privado verificado.

## Impactos e riscos

`BusinessMenuItem` atual não representa filhos. Para cumprir a exigência de que agrupadores também sejam itens de menu, a API compartilhada terá uma árvore opcional de filhos e preservará o construtor binário de quatro campos da API 1.1. Plugins de negócio ainda precisam declarar `getCaminhoMenu()` para participar da montagem dinâmica.

## Critérios para aprovação da SPEC

- Contrato, gramática do caminho, regras de conflito e tratamento de falhas são verificáveis.
- A fronteira de confiança e o ciclo de vida do classloader permanecem claros.
- A evolução incompatível da API é aceita explicitamente.

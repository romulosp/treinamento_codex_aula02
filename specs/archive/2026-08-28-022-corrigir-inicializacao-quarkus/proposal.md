# Proposta: 022-corrigir-inicializacao-quarkus

## Status
`IMPLEMENTADA`

## Problema e objetivo

O backend `gerenciar-categorias` conclui `mvn quarkus:dev` com sucesso, mas não inicia o servidor. O log informa que o goal foi ignorado porque o `quarkus-maven-plugin` não possui o goal `build` configurado. Corrigir a configuração do plugin para que o script de inicialização realmente suba o Quarkus.

## Escopo

- Configurar a execução `build` do `quarkus-maven-plugin` no POM do projeto `gerenciar-categorias`.
- Atualizar a especificação-base e o prompt de geração para preservar essa configuração em novos projetos.
- Executar testes Maven e validar a inicialização em modo dev.

## Fora de escopo

- Alterar endpoints, contratos HTTP, dependências de negócio ou estrutura de pacotes.
- Alterar a pasta do projeto ou gerar `gerenciar-tarefas`.

## Critérios para aprovação

- O POM contém a execução do goal `build` do plugin Quarkus.
- `mvn test` continua aprovado.
- `mvn quarkus:dev` permanece em execução e expõe a aplicação, em vez de terminar imediatamente com o warning observado.

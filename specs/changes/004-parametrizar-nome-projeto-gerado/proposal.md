# Proposta: 004-parametrizar-nome-projeto-gerado

## Status
`RASCUNHO`

## Problema e objetivo

O backend criado anteriormente ainda utiliza os placeholders `nome_da_api_gerada`, `nome_api_projeto` e `NOME_SCHEMA`. Tornar a nomenclatura do projeto determinística a partir do nome do modelo da mudança, evitando referências genéricas nos artefatos gerados.

## Escopo

- Definir a regra de derivação do nome do projeto a partir do diretório da mudança.
- Aplicar a regra ao projeto atual da mudança `003-gerenciar-categorias`.
- Atualizar o prompt de execução para exigir a substituição nos futuros projetos gerados.
- Ajustar `pom.xml` e `application.properties` apenas nos campos definidos pela SPEC.
- Criar testes ou verificações automatizadas para os valores configurados.

## Fora de escopo

- Alterar package Java, versão, dependências, portas, configurações de DB2 ou comportamento dos endpoints.
- Alterar documentos e evidências históricas das mudanças 001, 002 ou 003.

## Riscos

- Hífens são válidos no nome público do projeto, mas não em identificadores SQL sem aspas; o schema de teste deve usar forma normalizada.
- A renomeação do `artifactId` pode alterar o nome final do artefato Maven.
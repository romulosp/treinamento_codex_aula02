# Proposta: 004-parametrizar-nome-projeto-gerado

## Status
`IMPLEMENTADA`

## Problema e objetivo

O backend criado anteriormente ainda utiliza os placeholders `nome_da_api_gerada`, `nome_api_projeto` e `NOME_SCHEMA`. Tornar a nomenclatura do projeto determinística a partir do nome do modelo da mudança, evitando referências genéricas nos artefatos gerados. Também tornar o prompt de execução capaz de conduzir automaticamente as fases aprovadas de uma mudança, sem solicitações manuais entre os gates.

## Escopo

- Definir a regra de derivação do nome do projeto e da pasta `apps/backend/<artifactId-sem-hifens>/` a partir do diretório da mudança.
- Aplicar a regra ao projeto atual da mudança `003-gerenciar-categorias`.
- Atualizar o prompt de execução para exigir a substituição nos futuros projetos gerados.
- Ajustar `pom.xml` e `application.properties` apenas nos campos definidos pela SPEC.
- Criar testes ou verificações automatizadas para os valores configurados.
- Criar o script `apps/backend/start_aplicacao.bat` para iniciar o backend com as versões locais aprovadas de Java e Maven.
- Orquestrar no prompt a sequência de revisão da SPEC, implementação, revisão da implementação, validação, aprovação e encerramento, interrompendo o fluxo em gates reprovados ou bloqueados.

## Fora de escopo

- Alterar package Java, versão, dependências, portas, configurações de DB2 ou comportamento dos endpoints.
- Alterar documentos e evidências históricas das mudanças 001, 002 ou 003.
- Alterar variáveis permanentes de ambiente, configurar o sistema operacional ou incluir versões de Java e Maven no repositório.
- Ignorar, rebaixar ou contornar gates obrigatórios do processo Spec Driven.

## Riscos

- Hífens são válidos no nome público do projeto, mas não em identificadores SQL sem aspas; o schema de teste deve usar forma normalizada.
- A renomeação do `artifactId` pode alterar o nome final do artefato Maven.
- O script depende dos caminhos locais configurados para Java 17.0.11 e Maven 3.8.8; os caminhos devem permanecer explícitos para permitir sua revisão.

# Proposal: 067-compilar-projeto-go

## Resumo

Adicionar um atalho batch na raiz do projeto para compilar o módulo Go de forma repetível e segura.

## Motivação

O projeto já exige validações manuais de build e o processo atual depende do operador lembrar o caminho e os comandos corretos. Um script em `.bat` reduz ruído operacional e padroniza a execução.

## Escopo proposto

- criar `compilar projeto.bat` na raiz do repositório;
- localizar automaticamente `apps/desktop/libpinpadabecsgo`;
- compilar o módulo com `go build ./...`;
- retornar mensagem adequada para sucesso e falha.

## Não escopo

- alterar a arquitetura do módulo Go;
- alterar a lógica de negócio da biblioteca ABECS;
- instalar ou modificar o ambiente Go no sistema.

## Resultado esperado

Um operador consegue compilar o projeto Go com um único clique, sem depender de linha de comando manual.

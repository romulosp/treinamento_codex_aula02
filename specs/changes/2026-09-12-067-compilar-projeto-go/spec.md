# SPEC: 067-compilar-projeto-go

Autor: Rômulo Penha

## Status
`SPEC_PROPOSTA`

## Descrição executiva

Esta Change define um atalho operacional para compilar o projeto Go do módulo `apps/desktop/libpinpadabecsgo` a partir da raiz do repositório, sem exigir que o operador execute comandos manuais pelo terminal.

O objetivo é reduzir o risco de erro humano, padronizar a compilação do módulo e cumprir a convenção do projeto de manter scripts locais de suporte na raiz.

## Identificação

- `groupId`: `br.com.romulopenha`
- `artifactId`: `lib-pinpad-abecs-go`
- módulo: `apps/desktop/libpinpadabecsgo/`
- artefato operacional: `compilar projeto.bat`

## Escopo

A solução deve oferecer um arquivo `.bat` na raiz do projeto que:

1. entra na pasta do módulo Go;
2. verifica se a ferramenta `go` está disponível no `PATH`;
3. executa `go build ./...` no diretório correto;
4. preserva mensagens claras de sucesso ou falha;
5. encerra com código de saída compatível com o shell do Windows;
6. não altera configuração permanente do sistema operacional.

## Requisitos funcionais

### RF-001 — Ponto de entrada

O arquivo `compilar projeto.bat` deve ficar na raiz do repositório e ser executado diretamente no Windows.

### RF-002 — Diretório de trabalho

O script deve localizar o diretório do projeto Go em `apps/desktop/libpinpadabecsgo` e mudar para esse diretório antes da compilação.

### RF-003 — Validação do ambiente

Se `go` não estiver disponível no `PATH`, o script deve exibir uma mensagem clara e encerrar com falha.

### RF-004 — Compilação

O script deve executar a compilação do módulo com:

```bat
go build ./...
```

### RF-005 — Resultado

- em caso de sucesso, o script deve informar que o build foi concluído com sucesso;
- em caso de falha, deve informar o erro e retornar um código de saída diferente de zero;
- a janela deve permanecer aberta para leitura do usuário quando executada manualmente.

### RF-006 — Segurança operacional

O script deve ser não invasivo: não deve persistir configurações de sistema, não deve alterar variáveis de ambiente de forma global e não deve depender de privilégios administrativos.

## Critérios de aceite

- [ ] O arquivo `.bat` existe na raiz do projeto.
- [ ] O script encontra o módulo Go corretamente.
- [ ] Ao executar com ambiente Go válido, a compilação do projeto termina com sucesso.
- [ ] Quando `go` não está configurado, o erro é informado ao usuário.
- [ ] O código de saída final reflete o resultado da compilação.

## Observações

Esta Change é operacional e não altera a lógica da biblioteca Go em si. Ela reduz esforço manual e padroniza a validação do build para o módulo atual.

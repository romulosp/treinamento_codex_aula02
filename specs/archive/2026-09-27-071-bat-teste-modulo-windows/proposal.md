# Proposta — 071 BAT de teste do módulo Windows

Autor: Rômulo Penha

## Status

`SPEC_APROVADA`

## Objetivo

Criar um script `.bat` documentado para iniciar o módulo Windows
`libpinpadabecsgo-bridge` usando um pinpad físico e a configuração operacional
do ambiente.

## Escopo

- gerar um BAT na raiz do módulo Go;
- consumir `PORTA_PINPAD` herdada e aplicar defaults documentados de baudrate/timeout;
- remover `PINPAD_BRIDGE_TRANSPORT` para garantir transporte físico;
- iniciar `go run .\cmd\libpinpadabecsgo-bridge`;
- exibir o arquivo de log ativo e manter a janela aberta após o encerramento.

## Fora do escopo

- alteração do protocolo ABECS;
- alteração da porta padrão ou das regras de configuração do Bridge;
- inclusão de credenciais ou dados sensíveis;
- execução automática de operações no pinpad.

## Critérios de aceite

- CA-071-01: o BAT consome `PORTA_PINPAD` herdada, sem sobrescrita por COM fixa;
- CA-071-02: define baudrate `19200` e timeout `30` por padrão;
- CA-071-03: remove `PINPAD_BRIDGE_TRANSPORT` antes de iniciar;
- CA-071-04: inicia o Bridge no diretório correto do módulo;
- CA-071-05: documenta que o BAT é para pinpad físico Windows, não Emulator.

## Retificação rastreada

A primeira implementação fixou COM14. A correção e seus gates pertencem à
[Change 072](../../archive/2026-09-27-072-corrigir-abertura-bridge-android/proposal.md),
incluindo reverse opcional para o app Android que usa esse Bridge Windows.
O script continua executado no host, mesmo quando o cliente é o Emulator.
Os resultados anteriores não comprovam o contrato retificado.

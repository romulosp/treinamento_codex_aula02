# Proposta — 068 diagnosticopinpad

Autor: Rômulo Penha

## Status

`SPEC_APROVADA`

## Objetivo

Criar um aplicativo Android nativo de diagnóstico que prove, de forma
reproduzível, o caminho Kotlin → binding Go → `EmulatorTransport` → Bridge
Windows → transporte serial, sem duplicar no Android o protocolo ABECS.

Esta Change entrega o menor laboratório útil: verificação da versão do binding,
conexão, cancelamento, consulta tipada de informações do pinpad e diagnóstico
de erros. O catálogo completo de comandos continuará em Change posterior.

## Contexto observado

- a Change 067 implementou a fundação Go, o Bridge e uma fachada `mobile`, mas
  ainda está em `IMPLEMENTADA`, sem revisão/validação/aprovação formal;
- o Gate de `gomobile bind` da Change 067 ainda não foi executado;
- não existe projeto Android em `apps/frontend/smartphone/diagnosticopinpad`;
- o contrato vigente do Bridge é o envelope binário `PBRG` versão 1, com
  payload raw, e não JSON/base64;
- os documentos v7 recebidos são insumos de análise. Eles não substituem o
  workflow, a Change 067 ou as decisões registradas neste repositório.

## Escopo

- projeto Android nativo em Kotlin e Jetpack Compose, com um módulo `app`;
- geração reproduzível do AAR via `gomobile bind` e consumo local pelo app;
- extensão mínima e tipada da fachada Go para versão, ping e `GetInfo`;
- propagação de `operationID` pelos frames `PBRG DATA` e pelos logs;
- tela única de configuração, estado, ações e resultado de diagnóstico;
- cancelamento por operação e lifecycle seguro;
- Bridge com transporte serial roteirizado para testes sem hardware;
- consumo da porta serial física parametrizada por `PORTA_PINPAD` no processo
  Windows do Bridge, sem duplicar a configuração da COM no Android;
- testes unitários, instrumentados e de integração por gates;
- documentação KDoc/GoDoc dos contratos públicos e dos fluxos não triviais.

## Fora do escopo

- catálogo completo de comandos ABECS ou um executor genérico de comandos;
- `Execute(operation, payloadJSON)`, `SendRawCommand` ou edição de bytes raw;
- Android USB Host, OTG ou comunicação USB direta;
- cópia de parser, CRC, máquina de estados ou comandos ABECS para Kotlin;
- REST ou WebSocket como fronteira Android → Windows;
- publicação em loja, analytics, backend, autenticação remota ou acesso LAN;
- persistência de dados de negócio;
- alterações não relacionadas nas Changes 066 e 067.

## Dependência de implementação

A implementação será incremental. O esqueleto Android, os testes de estado e
os scripts de preparação podem ser criados enquanto a Change 067 é corrigida.
O código que consome classes do AAR, os testes Kotlin de binding e os Gates 3–6
somente poderão avançar depois da revisão da 067 e do Gate 1 do AAR. Qualquer
divergência encontrada na 067 deve ser corrigida ou registrada em revisão antes
de ser consumida pelo aplicativo.

## Resultado esperado

O operador conseguirá executar no Android Emulator:

```text
diagnosticopinpad → AAR Go → PBRG/localhost:39100 → Bridge Windows
                  → serial roteirizada ou COM real → pinpad
```

O aplicativo exibirá estado, duração e resultado sanitizado, permitirá cancelar
a operação ativa e produzirá evidências dos gates sem expor dados sensíveis.

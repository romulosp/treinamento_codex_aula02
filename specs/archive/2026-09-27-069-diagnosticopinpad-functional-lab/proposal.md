# Proposta — 069 diagnosticopinpad Functional Lab

Autor: Rômulo Penha

## Status

`SPEC_APROVADA`

## Objetivo

Estender o `diagnosticopinpad` para oferecer no Android o catálogo funcional
correspondente ao menu local da biblioteca Go ABECS, preservando o core Go como
única fonte de verdade dos comandos e permitindo testes com `PORTA_PINPAD` no
Bridge Windows.

## Contexto

A Change 068 entregou o laboratório mínimo: versão, ping, abertura, `GetInfo`,
fechamento e cancelamento. O executável desktop existente já expõe as opções de
display, menu, transação, EMV, PIN e multimídia, mas o binding Go e a tela
Android ainda não as disponibilizam.

As imagens `menu_opcao.png` e `tela_diagnostico_atual_V1.png` são referências
visuais fornecidas pelo usuário. Elas não substituem as SPECs, o workflow ou os
contratos ABECS existentes.

## Escopo

- fachada gomobile tipada para as operações já implementadas no core Go;
- tela Android organizada por grupos funcionais, sem executor raw genérico;
- ações 1–5, 7–24 e 26–27 do menu local;
- opção 6 visível como indisponível conforme ABECS 2.12;
- opção 25 visível como reservada e desabilitada;
- opção 28 como encerramento seguro do laboratório;
- formulários validados para parâmetros simples e avançados;
- resultados resumidos e sanitizados, sem PAN, trilhas, PIN block, KSN,
  chaves, EMV bruto ou payload ABECS;
- testes JVM, Go, binding, lint, build e teste funcional no Emulator.

## Fora do escopo

- novos comandos ABECS no core que não existam no contrato vigente;
- comandos raw, JSON de comando, CRC, framing ou parser em Kotlin;
- USB Host/OTG, publicação em loja, backend, LAN ou persistência de negócio;
- implementação da transação completa reservada da opção 25;
- exibição ou armazenamento de dados sensíveis retornados por GTK, GPN, GCX ou
  GOX;
- alteração do significado de `PORTA_PINPAD`: ela continua sendo consumida
  somente pelo processo Windows que inicia o Bridge.

## Dependências

- Change 067: transporte, Bridge, protocolo PBRG e transporte serial;
- Change 068: projeto Android, AAR, lifecycle e tela base;
- comandos e parsers existentes em `apps/desktop/libpinpadabecsgo`.

## Resultado esperado

O operador poderá selecionar no Android as mesmas capacidades funcionais do
menu desktop, informar os parâmetros necessários, acompanhar estado/duração e
receber resultado sanitizado. No cenário físico, o Bridge continuará sendo
iniciado com `PORTA_PINPAD` definida no ambiente (ou outra porta efetiva) e o Android usará
somente o endpoint TCP configurado.

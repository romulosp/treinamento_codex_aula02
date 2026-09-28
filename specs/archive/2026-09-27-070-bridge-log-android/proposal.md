# Proposta — 070 Bridge log Android

Autor: Rômulo Penha

## Status

`SPEC_APROVADA`

## Objetivo

Garantir que o Bridge Windows usado pelo `diagnosticopinpad` use o mesmo
`LogPinpadAbecs.txt` já utilizado pelo menu desktop e pelo transporte serial,
mantendo a configuração explícita por `PINPAD_LOG_FILE`.

## Contexto

O aplicativo Android registra apenas eventos técnicos locais, mas o processo
Bridge iniciado por `cmd/libpinpadabecsgo-bridge` cria o adaptador serial sem
configurar o `logging.Tracer`. O caminho Android/Bridge, portanto, não grava no
mesmo arquivo de rastro usado pelo menu desktop. A correção deve centralizar o
registro no Bridge; não haverá um segundo arquivo Android para o rastro ABECS.

## Escopo

- inicializar o tracer único no comando Bridge;
- usar `PINPAD_LOG_FILE` quando definido;
- usar como padrão `<raiz-do-módulo>/logs/LogPinpadAbecs.txt`;
- cobrir criação do arquivo, diretório, append e erro de configuração;
- documentar o comportamento para teste com Android e `PORTA_PINPAD`.

## Fora do escopo

- criar um arquivo de rastro ABECS separado no Android;
- registrar PAN, PIN, chaves, KSN ou payload sensível em claro;
- alterar o protocolo PBRG ou comandos ABECS;
- alterar o logger privado do Android;
- mudar a semântica de `PORTA_PINPAD`.

## Critérios de aceite

- CA-070-01: o Bridge cria o arquivo padrão antes de aceitar operações;
- CA-070-02: `PINPAD_LOG_FILE` substitui o caminho padrão;
- CA-070-03: Android → Bridge → serial grava o rastro no arquivo configurado;
- CA-070-04: falha ao preparar o log impede o Bridge de iniciar com erro claro;
- CA-070-05: testes Go existentes e novos passam sem dados sensíveis reais;
- CA-070-06: README e instruções de execução documentam o arquivo.

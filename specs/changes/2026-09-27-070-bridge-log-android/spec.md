# SPEC — 070 Bridge log Android

## 1. Status

`SPEC_APROVADA`

## 2. Contrato

O entrypoint `cmd/libpinpadabecsgo-bridge` deve criar um único
`logging.Tracer`, configurar seu destino antes de chamar `bridge.Server.Serve`
e associá-lo ao transporte serial físico por `SetTracer`. Esse é o mesmo
tracer/arquivo conceitual utilizado pelo menu desktop; o Android não cria nem
acessa diretamente o arquivo Windows.

O único destino compartilhado será resolvido nesta ordem:

1. `PINPAD_LOG_FILE`, quando não vazio;
2. `logs/LogPinpadAbecs.txt` relativo à raiz do módulo Go.

O diretório pai deve ser criado quando necessário. O tracer deve operar em
append e manter as políticas de redaction já existentes. O transporte
`scripted` também deve receber o mesmo tracer para que o fluxo de diagnóstico
tenha o mesmo comportamento observável, sem alterar bytes ABECS.

## 3. Segurança

- não registrar parâmetros Android sensíveis nem payloads fora das políticas
  existentes;
- não criar um segundo arquivo Android para o rastro ABECS;
- não aceitar caminho vazio como destino válido;
- manter o Bridge limitado a loopback;
- retornar erro de inicialização se o destino não puder ser preparado.

## 4. Cenários

- padrão sem `PINPAD_LOG_FILE`;
- caminho explícito em arquivo temporário;
- diretório inexistente;
- caminho inválido ou não gravável;
- Bridge scripted iniciado e encerrado após uma sessão.

## 5. Critérios verificáveis

Os critérios CA-070-01 a CA-070-06 da proposta serão demonstrados por testes
unitários, integração do entrypoint e teste funcional Android já disponível na
Change 069.

## 6. Complemento corretivo — Change 072

A [SPEC 072](../2026-09-27-072-corrigir-abertura-bridge-android/spec.md)
complementa CA-070-03/04: criar e ativar o arquivo não comprova alimentação.
O mesmo destino deve receber eventos reais de startup, Ping, ownership,
falhas de sessão e abertura/fechamento. Falha anterior ao Open serial deve
deixar evento técnico, sem fabricar linhas serial/TX/RX.

Se o Bridge estiver ausente, a tentativa Android não pode escrever no arquivo
de um processo Windows que não está executando. Se o próprio log não puder
ser aberto, informar a falha no stderr. Os testes CA-072-06/12/13 substituem a
inferência de sucesso físico baseada somente em criação do arquivo.

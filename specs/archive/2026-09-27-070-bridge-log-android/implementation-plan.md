# Implementation plan — 070 Bridge log Android

## Impactos

- entrypoint `cmd/libpinpadabecsgo-bridge`;
- adaptadores de transporte serial;
- testes do comando e documentação do Bridge;
- nenhum contrato ABECS ou código Kotlin.

## Estratégia

Reutilizar o tracer existente, resolver o destino em uma função testável,
configurar o tracer antes do servidor e manter o fluxo scripted coberto.

## Testes

- unitários para destino padrão, override e falha;
- integração do Bridge scripted;
- `go test ./...`, `go vet ./...` e `go build`;
- teste Android já existente para confirmar o caminho de execução.

## Segurança

Inspecionar redaction do tracer, ausência de segredos e erro explícito quando o
arquivo não puder ser preparado.

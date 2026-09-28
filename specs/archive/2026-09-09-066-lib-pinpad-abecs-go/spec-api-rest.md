# SPEC: 066-lib-pinpad-abecs-go — Adaptador RESTful HTTP e Especificação OpenAPI (Swagger)

Autor: Rômulo Penha

## Status
`SPEC_APROVADA`

## Descrição executiva

Esta SPEC define o adaptador RESTful HTTP/JSON e a documentação contratual OpenAPI 3 (Swagger) para a biblioteca `lib-pinpad-abecs-go`. O adaptador reside exclusivamente na camada `internal/api` e no executável servidor `cmd/libpinpadabecsgo-api/`, preservando o domínio (`internal/domain`) e a fachada de aplicação (`internal/application/service`) totalmente desacoplados de protocolos de rede e frameworks HTTP.

O serviço HTTP não utiliza WebSocket, não expõe comandos hexadecimais brutos e não abre portas para interfaces públicas de rede por padrão, limitando-se ao bind em `127.0.0.1:8080`.

## Identificação

- `groupId`: `br.com.romulopenha`
- `artifactId`: `lib-pinpad-abecs-go`
- módulo: `br.com.romulopenha/lib-pinpad-abecs-go`
- pacote da API: `internal/api`
- pacote de DTOs: `internal/api/dto`
- executável do servidor: `cmd/libpinpadabecsgo-api/`
- contrato OpenAPI: `specs/changes/2026-09-09-066-lib-pinpad-abecs-go/openapi.yaml`

## Requisitos funcionais da API RESTful

### RF-REST-001 — Isolamento arquitetural
A camada `internal/api` é um adaptador de entrada (Driving Adapter) que valida payloads JSON, mapeia-os para DTOs tipados, invoca a fachada `PinpadService` e converte o retorno em respostas HTTP padronizadas. O domínio e o serviço não conhecem `net/http`, roteadores, cabeçalhos ou status codes.

### RF-REST-002 — Catálogo de rotas e métodos

A API expõe rotas canônicas sob `/api/v1/...` e rotas de conveniência sob `/api/...` para permitir automação via runners e scripts de laboratório:

| Comando | Método HTTP | Rota canônica | Rota de conveniência |
|---|---|---|---|
| OPN | `POST` | `/api/v1/connections` | `/api/opn` |
| CLO | `DELETE` / `POST` | `/api/v1/connections/current` | `/api/clo` |
| CAN | `POST` | `/api/v1/connections/current/cancellations` | `/api/can` |
| RST | `POST` | `/api/v1/connections/current/resets` | `/api/rst` |
| GIX | `GET` | `/api/v1/pinpad` | `/api/gix` |
| DSP | `PUT` / `POST` | `/api/v1/display/message` | `/api/dsp` |
| DEX | `POST` | `/api/v1/display/exchanges` | `/api/dex` |
| MNU | `POST` | `/api/v1/display/menus` | `/api/mnu` |
| CLX | `POST` | `/api/v1/display/clear` | `/api/clx` |
| DSI | `POST` | `/api/v1/display/images` | `/api/dsi` |
| QRCODE | `POST` | `/api/v1/media/qrcodes` | `/api/qrcode` |
| LMF | `GET` | `/api/v1/media` | `/api/lmf` |
| DMF | `DELETE` / `POST` | `/api/v1/media` | `/api/dmf` |
| MLI | `POST` | `/api/v1/media-loads` | `/api/mli` |
| MLR | `POST` | `/api/v1/media-loads/current/blocks` | `/api/mlr` |
| MLE | `POST` | `/api/v1/media-loads/current/commit` | `/api/mle` |
| TLI | `POST` | `/api/v1/table-loads` | `/api/tli` |
| TLR | `POST` | `/api/v1/table-loads/current/records` | `/api/tlr` |
| TLE | `POST` | `/api/v1/table-loads/current/commit` | `/api/tle` |
| GCX | `POST` | `/api/v1/card-captures` | `/api/gcx` |
| GTK | `POST` | `/api/v1/card-tracks` | `/api/gtk` |
| GOX | `POST` | `/api/v1/emv/continuations` | `/api/gox` |
| FCX | `POST` | `/api/v1/emv/finalizations` | `/api/fcx` |
| GKY | `POST` | `/api/v1/key-captures` | `/api/gky` |
| GPN | `POST` | `/api/v1/pin-captures` | `/api/gpn` |

### RF-REST-003 — Tratamento de erros e correlação

1. Todas as respostas de erro HTTP retornam estrutura `ErrorResponse` com `Content-Type: application/json; charset=utf-8`:
```json
{
  "error": {
    "code": "ERR_PINPAD_CLOSED",
    "module": "lib-pinpad-abecs-go",
    "message": "pinpad is closed",
    "details": "detalhes sanitizados",
    "correlationId": "req-1726390000000-1-abcd1234"
  }
}
```
2. O identificador de correlação (`correlationId`) é propagado a partir do cabeçalho `X-Correlation-ID` ou `X-Request-ID`. Caso ausente na requisição, um novo identificador único é gerado no middleware e devolvido no cabeçalho de resposta `X-Correlation-ID`.
3. Mapeamento de status HTTP:
   - `400 Bad Request`: JSON malformado, campos obrigatórios ausentes ou payload inválido.
   - `404 Not Found`: Rota inexistente.
   - `405 Method Not Allowed`: Método HTTP não permitido na rota, acompanhado do cabeçalho `Allow` com os métodos suportados.
   - `409 Conflict`: Pinpad fechado (`ErrPinpadClosed`) ou ocupado (`ErrPinpadBusy`).
   - `501 Not Implemented`: Funcionalidade não implementada (`ErrNotImplemented` ou `ErrQRCodeGeneratorNotConfigured`).
   - `503 Service Unavailable`: Fila cheia (`ErrQueueFull`) ou porta serial indisponível (`ErrPortUnavailable`).
   - `504 Gateway Timeout`: Timeout de comunicação ou cancelamento por contexto.
   - `500 Internal Server Error`: Falha interna não mapeada.

### RF-REST-004 — Segurança e restrição de acesso
1. O servidor HTTP escuta por padrão exclusivamente no endereço de loopback `127.0.0.1:8080`.
2. Tentativas de handshake WebSocket com cabeçalho `Upgrade: websocket` são imediatamente rejeitadas com status `400 Bad Request`.
3. Dados sensíveis (PAN, PIN Block, KSN, chaves criptográficas) sofrem redaction e nunca são expostos em mensagens de erro ou logs estruturados.

## Critérios de aceite

- [x] **CA-REST-001:** O servidor inicia em `127.0.0.1:8080` por padrão e suporta configuração via `PINPAD_HTTP_HOST` e `PINPAD_HTTP_PORT`/`PORT`.
- [x] **CA-REST-002:** Rotas válidas utilizam DTOs específicos em `internal/api/dto`.
- [x] **CA-REST-003:** Respostas utilizam status HTTP e payloads JSON tipados.
- [x] **CA-REST-004:** Erros são redigidos e incluem `correlationId`.
- [x] **CA-REST-005:** Testes com `httptest` cobrem conexão, display, JSON inválido, rota inexistente, timeout e WebSocket.
- [x] **CA-REST-006:** O executável servidor `cmd/libpinpadabecsgo-api` é um daemon autônomo sem menu interativo.
- [x] **CA-REST-007:** Métodos diferentes dos permitidos retornam código `405 Method Not Allowed` com cabeçalho `Allow`.

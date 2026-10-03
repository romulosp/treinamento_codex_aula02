# Rastreabilidade — 074 Pinpad Android Bridge Stack

## Regra

Os documentos arquivados são fontes históricas preservadas. Os requisitos
normativos futuros estão em spec.md; esta matriz aponta a origem e o tratamento
consolidado. Nenhuma linha significa que a implementação deve executar outra
Change.

## Fontes

| Código | Fonte |
|---|---|
| SRC-066 | specs/archive/2026-09-09-066-lib-pinpad-abecs-go/ |
| SRC-067 | specs/archive/2026-09-27-067-android-emulator-transport-bridge/ |
| SRC-070 | specs/archive/2026-09-27-070-bridge-log-android/ |
| SRC-071 | specs/archive/2026-09-27-071-bat-teste-modulo-windows/ |
| SRC-072 | specs/archive/2026-09-27-072-corrigir-abertura-bridge-android/ |

## Matriz de requisitos

| Requisito consolidado | Origem principal | Origem complementar | Tratamento |
|---|---|---|---|
| Arquitetura Go, módulo, Clean/Ports and Adapters | SRC-066 RF-001 | SRC-067 RF-001 | Mantido em SPEC seção 2. |
| Configuração serial/defaults | SRC-066 RF-002 | SRC-072 RF-072-01 + aprovação 074 | A fonte 072 distingue BAT/entrypoint; a decisão humana da 074 supersede o fallback e exige `PORTA_PINPAD` em todo processo físico. |
| Estados ABECS e modelos | SRC-066 RF-003 | SRC-072 RF-072-06 | Mantido; Ping/Version nunca promovem a OPEN. |
| CRC/substitution/framing | SRC-066 RF-004, conformidade v2.12 | SRC-067 RF-002 | Mantido na seção 5. |
| Parsers ABECS/BER-TLV | SRC-066 RF-005 | SRC-067 RF-003 | Mantido; Bridge continua opaco. |
| Status e erros Go | SRC-066 RF-006 | SRC-072 RF-072-05 | Mantido; categorias mobile são allowlist posterior. |
| Porta serial contextual | SRC-066 RF-007 + spec-infra-serial-cancel | SRC-067 RF-001/002 | Mantido na seção 10. |
| SessionManager 300s | SRC-066 RF-008/014 | SRC-067 RF-006 | Mantido; ownership físico fica no Bridge. |
| FIFO/worker/capacidade 100 | SRC-066 RF-009/014 | SRC-072 RF-072-07 | Mantido com cleanup aguardado. |
| Serviço e lifecycle ABECS | SRC-066 RF-010/013 | SRC-072 RF-072-06/07 | Mantido; RST removido e CAN/EOT prevalece. |
| Logging SPE/PP/RSP | SRC-066 RF-011/spec-logging | SRC-070 contrato, SRC-072 RF-072-04 | 070/072 acrescentam tracer único e eventos reais. |
| Catálogo de comandos | SRC-066 RF-012 e specs individuais | SRC-072 preserva | Consolidado na tabela da seção 8; RST obsoleto. |
| Comunicação segura | SRC-066 RF-018/spec-protocolo-seguro | SRC-072 não altera formato | Mantida com gate físico e redaction. |
| API REST | SRC-066 RF-015/spec-api-rest | SRC-067 fora da fronteira Android | Mantida como adaptador local, sem contaminar Bridge. |
| Launcher físico Windows | SRC-071 contrato, CA-071-01–05 | SRC-066 RF-016, SRC-072 RF-072-01/02/03 | Seção 4.1.1 e CA-074-18: BAT autocontido, COM herdada, setlocal, modo físico, diretório do módulo, configuração visível, ADB opcional e exit code real; 072 prevalece sobre COM fixa e readiness. |
| GoDoc/documentação | SRC-066 RF-017 | SRC-072 qualidade | Mantido como critério de aceite. |
| Transport neutro | SRC-067 RF-001/002 | SRC-066 serial | Mantido na seção 10. |
| EmulatorTransport TCP persistente | SRC-067 RF-003 | SRC-072 preflight/cleanup | Mantido; sem retry/replay. |
| Bridge Windows | SRC-067 RF-004 | SRC-072 RF-072-02/07 | 072 prevalece em startup, thread/cleanup e read fatal. |
| Envelope PBRG v1 | SRC-067 RF-005 | SRC-072 preserva layout | Mantido na seção 11. |
| Ownership COM | SRC-067 RF-006 | SRC-072 RF-072-07 | Mantido; abandono é erro controlado, não BUSY. |
| Host/porta/reverse | SRC-067 RF-007 | SRC-072 RF-072-03 | Mantido; localhost default e 10.0.2.2 somente override. |
| Fachada gomobile | SRC-067 RF-008/009/010 | SRC-072 RF-072-05/06 | Mantida; operationID, categorias e panic boundary. |
| AAR/gate mobile | SRC-067 seção 4 | SRC-072 CA-072-12/14 | Mantido como requisito de implementação/validação. |
| Arquivo de log compartilhado | SRC-070 seção 2 | SRC-072 RF-072-04 | Destino único, absoluto, append, eventos reais. |
| Readiness/exit code | SRC-072 RF-072-02 | SRC-070 segurança | 072 substitui inferência por arquivo criado. |
| Estado Android fiel | SRC-072 RF-072-06 | SRC-067 RF-010 | 072 prevalece. |
| Erros visíveis/correlacionados | SRC-072 RF-072-05/06 | SRC-067 RF-009/011 | 072 prevalece; sem mensagens livres. |
| Reabertura/cleanup | SRC-072 RF-072-07 | SRC-067 RF-010 | 072 acrescenta espera de workers e mutex Windows. |

## Matriz de critérios de aceite

| Critérios de origem | Critério consolidado |
|---|---|
| 066 CA-001–CA-026, CA-REST-001–007 e critérios por comando | CA-074-01–05, CA-14–16; seção 8 e seção 17 |
| 067 CA-001–CA-016 | CA-074-05–12 e seção 11/12 |
| 070 CA-070-01–06 | CA-074-13 e seção 14 |
| 071 CA-071-01–05 | CA-074-09/18 e seções 4.1.1/17.3; correção da COM e launcher da 072 prevalece |
| 072 CA-072-01–15 | CA-074-07–17, com 072 prevalecendo em configuração, estado, logging e cleanup |

## Tratamentos explícitos

| Origem | Decisão |
|---|---|
| 066 texto inicial “sem rede/servidor” | Mantido para domínio; superseded pelos adaptadores REST/Bridge. |
| 066 RST | Removido do protocolo/fachada; eventual REST é alias CAN/EOT. |
| 066 GCX preliminar | Removido; PurchaseGCX envia uma única iniciação. |
| 067 app Android fora do escopo | Preservado para UI/produto; binding/AAR e laboratório consumidor entram na baseline de integração. |
| 070 arquivo criado | Insuficiente; 072 exige eventos reais. |
| 071 atribuição original de COM fixa | Superseded por 072 no BAT físico e pela decisão C-003 da 074 em todo entrypoint físico. |
| 072 comportamento de porta | Consolidado com supersessão explícita: a 072 preservava fallback direto; a aprovação humana da 074 removeu esse fallback da baseline canônica. |
| 072 evidências arquivadas | Permanecem históricas; a futura implementação deve gerar evidência própria. |

## Rastreamento da implementação atual

| Divergência | Requisitos afetados | Evidência |
|---|---|---|
| Configuração sem fallback | RF de configuração/CA-074-09 | `config.Load()` exige `PORTA_PINPAD` e `DefaultConfig()` não contém porta; implementação conforme C-003, validação formal pendente. |
| RST ainda aparece na API | catálogo/CA-074-03 e CA-074-14 | internal/api/handler.go, internal/api/dto/dto.go |
| TransactionGCX retorna ErrNotImplemented | seção 8/L-004 | internal/application/service/service.go:189 |
| AAR/proveniência local | CA-074-11/15 | mobile.aar, build do laboratório e script documentado no README |
| Validador Android bloqueado por local.properties | CA-074-15 | validate_android_project.py em 2026-09-29 |
| Falha de integração no close legacy_eof | CA-074-04/15 | go test -tags=integration ./... |
| Race indisponível no ambiente | CA-074-15 | go test -race ./... em Windows/386 |

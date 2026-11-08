# SPEC: 10017-logging-semantico-abecs — Logging semântico ABECS — V.2.0.0

**Título:** Logging detalhado e interpretação semântica dos comandos ABECS
**SPEC de origem:** `066-lib-pinpad-abecs-go` (arquivada em `specs/archive/2026-09-09-066-lib-pinpad-abecs-go/`)
**Versão:** `2.0.0`
**Status:** SPEC_APROVADA (revisão `2026-10-15-spec-review-2.md`)
**Tipo:** Evolução observacional de componente existente
**Linguagem:** Go
**Protocolo:** ABECS 2.12 (PDF `docs/Pinpad-Abecs-Protocolo-de-Comunicacao-e-Funcionamento-v212190411212/`)

> Esta V.2.0.0 substitui a V.0.0.0.1 após a revisão técnica da proposta original.
> Ela foi **rebaseada** no código existente e no PDF ABECS 2.12. O inventário
> completo do protocolo está em
> [abecs-212-referencia-protocolo.md](../../../docs/Pinpad-Abecs-Protocolo-de-Comunicacao-e-Funcionamento-v212190411212/abecs-212-referencia-protocolo.md).

---

## 1. Contexto e baseline (o que já existe)

A biblioteca já implementa, em `apps/desktop/libpinpadabecsgo/`, a maior parte da
infraestrutura necessária. **Esta Change não cria um segundo parser**; ela
**estende e decora** o que existe.

| Conceito | Já existe em |
|---|---|
| Catálogo de comandos (24 tipos) | `internal/domain/command/commands.go` |
| Catálogo de parâmetros `SPE_xxx` (0x0001–0x0026) | `internal/domain/command/constants.go` |
| Catálogo de resposta `RSP_DATID` | `internal/domain/protocol/constants.go` + `parser/abecs.go` |
| Códigos de retorno `RSP_STAT` (000–102) | `internal/domain/state/status.go` |
| Enquadramento SYN/ETB/CRC + substituição DC3 | `internal/domain/protocol/protocol.go` |
| CRC-16-CCITT | `internal/utilitario/crc/crc.go` |
| Conversão HEX | `internal/utilitario/bytes/hex.go` |
| Builder de comando (TLV) | `internal/domain/command/abecs.go` |
| Parser de resposta (TLV + NTM) | `internal/domain/parser/abecs.go` |
| Sessão segura (DC2/AES) | `internal/domain/protocol/secure.go` |
| Logger + redação (`REDACTED`) | `internal/infrastructure/logging/logging.go` |
| Correlação de resposta/redação | `internal/application/service/service.go` |

**Conclusão da revisão:** a SPEC original propunha "criar catálogo" e
"reconstruir mensagens/identificar comando a partir dos bytes no logger", o que
**duplicaria** o parser e a máquina de estados ACK/NAK/retransmissão que já vivem
na camada de serviço. A direção correta é **consumir** os metadados já conhecidos
(`kind`, `status`, flag de redação, sessão segura) e **decorar** o frame.

---

## 2. Problema (corrigido)

O log atual (`LogPinpadAbecs.txt`) **já** registra, por frame serial:
- porta/sessão (`[COM7#001]`), direção (`SPE`/`PP`), `FUNC`, `DATA_HORA`;
- `CMD=<tipo>` no SPE (quando o tipo é conhecido) e `RSP CMD=<tipo> STATUS=<código>`;
- `GOX_CONFIG`, `GTK_CLEAR` para metadados não sensíveis;
- redação integral (`**REDACTED(n bytes)**`) para comandos sensíveis e sessão segura.

**A lacuna real é mais restrita** do que a SPEC original afirmava:

1. Não há camada **ASCII/textual por campo** — só HEX em bloco.
2. Não há **interpretação campo a campo** dos comandos **não sensíveis**.
3. Não há **validação estrutural** (offsets, comprimentos, CRC) exposta no log.
4. Não há **distinção explícita** entre bytes de controle, substituição DC3 e dados.
5. O frame **criptografado** (sessão segura, `DC2`) não é sinalizado como tal.

**Não é objetivo** (e seria incorreto/ilegal) decodificar o conteúdo de comandos
sensíveis ou criptografados.

---

## 3. Escopo

### 3.1 Incluído
- Decoração semântica dos frames **não sensíveis e não criptografados**.
- HEX integral + ASCII/texto + interpretação por campo (para campos permitidos).
- Validação estrutural e de CRC (diagnóstico, sem alterar comunicação).
- Sinalização de substituição DC3 e de sessão segura (DC2).
- Catálogo declarativo **estendido** (não novo), com metadados por campo.
- Matriz de cobertura (46 comandos × sensibilidade × implementado).

### 3.2 Excluído
- Alterar protocolo, builders, parsers funcionais, comunicação ou cálculo de CRC.
- Decodificar/interpretar o **payload** de comandos sensíveis ou criptografados.
- Expor PIN, chaves, KSN, trilhas, PAN, dados EMV, bloco criptográfico.
- Criar segundo parser ou reimplementar a máquina de estados do serviço.

---

## 4. Modelo de classificação (obrigatório antes de formatar)

Todo frame, **antes** de qualquer formatação, é classificado em uma de três classes:

| Classe | Critério | Tratamento no log |
|---|---|---|
| **A — Visível** | comando não sensível E sem sessão segura | HEX + ASCII/texto + interpretação por campo (campos permitidos) |
| **B — Sensível** | `kind ∈ {MLR, TLR, GCX, GTK, GOX, FCX, GPN}` OU política de redação ativa | envelope (comando/status/tamanho) + `[REDACTED]` no payload |
| **C — Criptografado** | payload inicia com `DC2 (12h)` OU sessão segura ativa | envelope + `OPACO/CRIPTOGRAFADO`; **nunca** tentar decodificar |

A classificação **reutiliza** a política existente: `logging.isSensitive` /
`RedactPayload` e `service.setTraceCommand` (`redact = forceRedaction || secureSession != nil || traceSensitive[kind]`).

**Regra de falha segura:** se a classe não puder ser determinada com confiança,
o frame é tratado como **Classe B** (redigido).

---

## 5. Requisitos funcionais

### RF-001 — Consumir metadados já conhecidos
O logger recebe, de quem já os conhece (`service`/`serial`), o `kind`
(`command.Type`), o `status` (`RSP_STAT`) e as flags de redação/sessão segura.
**É proibido** re-derivar o comando varrendo o payload atrás de três letras.

### RF-002 — Representação hexadecimal (Classe A)
- HEX uppercase, 2 dígitos/byte, separado por espaço, em linhas de N bytes
  (configurável, padrão 16), com offset hexadecimal.
- Sem perda, sem truncamento silencioso, sem representação DECIMAL byte a byte.

### RF-003 — Representação ASCII/textual (Classe A)
- Bytes imprimíveis → caractere; não imprimíveis → `.`.
- Texto interpretado somente quando a codificação for determinada pelo catálogo
  (ex.: `SPE_DSPMSG` é `S` com quebras `0Dh`; `C7 C3` = "ÇÃ" em ISO-8859-1).
- Codificação não determinada → `CODIFICACAO: DESCONHECIDA`, `TEXTO: NAO_INTERPRETADO`.
- **Nota (REV-002):** o PDF não declara explicitamente o encoding dos campos `S`.
  `ISO-8859-1` é assumido **provisoriamente** para campos de 8 bits e deve ser
  confirmado com o dispositivo. O encoding é um atributo **por campo** no catálogo,
  nunca global.

### RF-004 — Interpretação por campo (Classe A, campos permitidos)
Para cada campo estruturalmente identificável, emitir:
`NOME`, `OFFSET_INICIAL`, `OFFSET_FINAL`, `TIPO`, `COMPRIMENTO_DECLARADO`,
`COMPRIMENTO_EFETIVO`, `HEX`, `ASCII/TEXTO`, `VALOR_INTERPRETADO`, `VALIDACAO`.

Os nomes vêm do **catálogo do PDF** (seções 3.1.3.1/3.1.3.2), não de
"confirmar no manual": ex. `0013h = SPE_AMOUNT`, `0014h = SPE_CASHBACK`,
`001Bh = SPE_DSPMSG`.

### RF-005 — Catálogo estendido (não novo)
Estender `command`/`protocol` com metadados **de exibição** por campo
(`nome`, `descrição`, `tipo`, `encoding`, `sensibilidade`, `referência PDF`).
Um novo comando deve ser adicionável **sem** alterar o formatador genérico.

### RF-006 — Formatos suportados
ASCII, texto (encoding documentado), inteiro textual, inteiro binário, BCD,
binário opaco, campos fixos/variáveis, TLV, estruturas aninhadas. A interpretação
é escolhida pelo **esquema do comando** (não há TLV universal — `DSP` é fixo `S32`).

### RF-007 — Respostas
Para Classe A: identificar comando associado, decodificar campos, interpretar
`RSP_STAT` (via `state.GetStatusDescription`), validar enquadramento/CRC.
Para B/C: somente `RSP_ID` + `RSP_STAT` + `[REDACTED]`/`OPACO`.

### RF-008 — Bytes de controle (tabela corrigida)
| HEX | Nome | Papel |
|---|---|---|
| `04` | EOT | Resposta ao CAN |
| `06` | ACK | Pacote válido |
| `12` | DC2 | Prefixo de pacote criptografado (sessão segura) |
| `13` | DC3 | Byte de substituição |
| `15` | NAK | Retransmissão |
| `16` | SYN | Início de pacote |
| `17` | ETB | Fim de pacote |
| `18` | CAN | Cancelar comando blocante |

Nunca interpretar um byte interno de payload como controle sem considerar o enquadramento.

### RF-009 — Validação de CRC (diagnóstico)
Usar `crc.CRC16CCITT` existente; exibir `CRC_RECEBIDO`, `CRC_CALCULADO`,
`CRC_STATUS`. Não alterar a comunicação para produzir o log.

### RF-010 — Substituição DC3
O formatador deve distinguir bytes **na linha** (wire) de bytes **lógicos**
(pós `RemoveSubstitution`), indicando escapes `13h 33/36/37`.

### RF-011 — Sessão segura (DC2)
Payload iniciado por `12h` → classe C; exibir apenas envelope + `OPACO/CRIPTOGRAFADO`.
Jamais decodificar KSEC/CRKSEC/claro protegido.

### RF-012 — Fragmentação
Distinguir fragmento serial, mensagem completa/incompleta/inválida. Consolidar
somente com estado **por sessão e direção**, respeitando limites de frame. Não
criar IDs de correlação artificiais. **A máquina de estados ACK/NAK/retransmissão
permanece no serviço**; o logger não a reimplementa.

### RF-013 — Campos desconhecidos
`NOME: DESCONHECIDO`, `TIPO: NAO_DETERMINADO`; preservar bytes/offset/comprimento
autorizados; não inventar significado. Em conteúdo não classificável → Classe B.

### RF-014 — Preservação de REDACTED
Manter `RedactPayload`/`isSensitive`. Não registrar PIN, chaves, KSN, trilhas,
PAN, EMV sensível, blocos criptográficos. O modo detalhado **não** contorna a redação.

### RF-015 — Compatibilidade com o logging existente
Preservar porta, sessão, direção, `FUNC`, `DATA_HORA`, eventos do Bridge,
`correlationId`, destino do arquivo. Formato detalhado configurável; `LEGACY` = atual.

---

## 6. Exemplos de saída esperada

### 6.1 DSP (Classe A — campo fixo S32)
```
ABECS | SPE -> PP | DSP (clássico, não-blocante)
HEX:  16 44 53 50 30 33 32 <32 bytes> 17 <CRC>
COMANDO: DSP
  CMD_LEN1: "032" (fixo)
  DSP_MSG: S32 = "<32 caracteres, padding preservado>"  (encoding ISO-8859-1)
  CONTROLE: SYN(16) ... ETB(17)
  CRC: RECEBIDO=0x.... CALCULADO=0x.... STATUS=VALIDO
```

### 6.2 GOX (Classe B — sensível)
```
ABECS | SPE -> PP | GOX (ABECS, blocante)  [SENSIVEL]
COMANDO: GOX
  CMD_LEN1: "116" (comprimento do bloco)
  PAYLOAD: [REDACTED(116 bytes)]
  (não decodificar SPE_AMOUNT/SPE_CASHBACK/SPE_WKENC/PP_PINBLK/PP_EMVDATA)
```
Os campos não sensíveis e já expostos hoje continuam via `GOX_CONFIG`
(`ACQ`, `PIN_METHOD`, `KEY_INDEX`) — sem ampliar a exposição.

### 6.3 Resposta criptografada (Classe C)
```
ABECS | PP -> SPE | <CMD>  [CRIPTOGRAFADO]
COMANDO: <CMD>
  DC2: PRESENTE
  PAYLOAD: OPACO/CRIPTOGRAFADO
  (sem tentativa de decodificação)
```

---

## 7. Arquitetura

O pipeline observacional é **decorativo**, consumindo o estado que o serviço já possui:

```
serial.Adapter.Write/Read (bytes + kind + redact)
        |
        v
CLASSIFICAÇÃO (A/B/C)  ← reutiliza isSensitive + secureSession
        |
        v
DECODIFICAÇÃO DE ENQUADRAMENTO (SYN/ETB/DC3/CRC)  ← reutiliza protocol.go
        |
        v
DECODIFICAÇÃO SEMÂNTICA (somente Classe A)  ← reutiliza catálogo estendido
        |
        v
FORMATADOR HEX/ASCII/DETALHES
        |
        v
TRACER EXISTENTE (logging.Tracer)
```

- **Não** há reconstrução de mensagens nem identificação de comando no logger.
- O processamento pesado de Classe A pode ser assíncrono, mas **sempre** após a
  classificação de sensibilidade (nunca copiar payload sensível para fila de diagnóstico).

---

## 8. Matriz de cobertura (46 comandos)

Estados: `DOCUMENTADO_E_IMPLEMENTADO`, `DOCUMENTADO_NAO_IMPLEMENTADO`,
`EXTENSAO_SEM_DOCUMENTACAO`, `NAO_IDENTIFICADO`. + dimensão de sensibilidade.

| Comando | § | Impl. | Sensível | Obs. |
|---|---|---|---|---|
| OPN (clássico) | 3.2.1 | ✅ | — | |
| OPN (seguro) | 3.2.2 | ✅ | — | |
| GIN | 3.2.3 | ❌ | | obsoleto |
| GIX | 3.2.4 | ✅ | — | |
| DWK | 3.2.5 | ❌ | | |
| CLO | 3.2.6 | ✅ | — | |
| CLX | 3.2.7 | ✅ | — | |
| CEX | 3.3.1 | ❌ | | |
| CHP | 3.3.2 | ❌ | | |
| CKE | 3.3.3 | ❌ | | |
| DEX | 3.3.4 | ✅ | — | |
| DSP | 3.3.5 | ✅ | — | |
| EBX | 3.3.6 | ❌ | | |
| ENB | 3.3.7 | ❌ | | obsoleto |
| GCD | 3.3.8 | ❌ | | |
| GDU | 3.3.9 | ❌ | | |
| GKY | 3.3.10 | ✅ | — | |
| GPN | 3.3.11 | ✅ | ✅ | |
| GTK | 3.3.12 | ✅ | ✅ | |
| MNU | 3.3.13 | ✅ | — | |
| RMC | 3.3.14 | ❌ | | |
| MLI | 3.4.1 | ✅ | — | |
| MLR | 3.4.2 | ✅ | ✅ | |
| MLE | 3.4.3 | ✅ | — | |
| LMF | 3.4.4 | ✅ | — | |
| DMF | 3.4.5 | ✅ | — | |
| DSI | 3.4.6 | ✅ | — | |
| GTS | 3.5.1 | ❌ | | |
| TLI | 3.5.2 | ✅ | — | |
| TLR | 3.5.3 | ✅ | ✅ | |
| TLE | 3.5.4 | ✅ | — | |
| GCR | 3.6.1 | ❌ | | obsoleto |
| CNG | 3.6.2 | ❌ | | obsoleto |
| GOC | 3.6.3 | ❌ | | obsoleto |
| FNC | 3.6.4 | ❌ | | obsoleto |
| GCX | 3.7.1 | ✅ | ✅ | |
| GED | 3.7.2 | ❌ | | |
| GOX | 3.7.3 | ✅ | ✅ | blocante |
| FCX | 3.7.4 | ✅ | ✅ | |
| GEN/02/K3 | 3.8.1 | ❌ | | |
| GEN/04/01 | 3.8.2 | ❌ | | |
| GEN/04/02 | 3.8.3 | ❌ | | |
| GEN/04/03 | 3.8.4 | ❌ | | |
| GEN/04/04 | 3.8.5 | ❌ | | |
| GEN/03/02 | 3.8.6 | ❌ | | |
| GEN/03/03 | 3.8.7 | ❌ | | |

**Critério de aceite de cobertura:** a matriz deve listar os 46 comandos e marcar,
para cada um, implementação e sensibilidade. Não declarar "cobertura universal"
enquanto houver comando documentado sem entrada no catálogo.

---

## 9. Segurança

- RF-014 preserva `REDACTED`; falha de classificação → Classe B.
- Campos `SPE_WKENC`, `SPE_PBKMOD/SPE_PBKEXP`, `PP_PINBLK`, `PP_KSN`, `PP_TRK1INC`,
  `PP_TRK2INC`, `PP_TRK3INC`, `PP_EMVDATA`, `PP_PAN*` são **sempre** sensíveis.
- A política de divulgação é centralizada e aplicada **antes** de qualquer buffer
  de diagnóstico; sem persistir representações intermediárias com dados protegidos.

---

## 10. Configuração

Proposta (reconciliar com `internal/infrastructure/config/config.go`, que hoje só
trata `PORTA_PINPAD`/`PINPAD_BAUDRATE`/`PINPAD_TIMEOUT`):

```
PINPAD_LOG_FORMAT=LEGACY|DETAILED      (default LEGACY)
PINPAD_LOG_DECODE_ABECS=true|false
PINPAD_LOG_BYTES_PER_LINE=16
```

`DETAILED` não é habilitado automaticamente em produção.

---

## 11. Requisitos não funcionais

- **RNF-001 Transparência:** não alterar dados transmitidos/recebidos.
- **RNF-002 Desempenho:** medir overhead; hoje `writeLine` faz `Sync()` por linha
  ([logging.go](../../../apps/desktop/libpinpadabecsgo/internal/infrastructure/logging/logging.go));
  a decodificação de Classe A não pode bloquear a thread serial.
- **RNF-003 Resiliência:** falha do logger não interrompe o pinpad.
- **RNF-004 Determinismo:** mesma entrada + catálogo = mesma saída.
- **RNF-005 Compatibilidade:** preservar legado e contratos.
- **RNF-006 Extensibilidade:** novos comandos sem reescrever o formatador.
- **RNF-007 Rastreabilidade:** preservar sessão/direção/data/hora/função/correlação.

---

### Riscos e mitigação (REV-004)

| Risco | Mitigação |
|---|---|
| Encoding de campos `S` não confirmado no PDF | atributo por campo no catálogo + fallback `DESCONHECIDA` |
| Divergência de nomes no código (0024h/0025h, 8032h/8033h) | usar nomes do PDF; documentar discrepâncias |
| Vazamento de dados sensíveis | classificação A/B/C + campos sensíveis sempre `[REDACTED]` |
| Overhead de `Sync()` por linha + decodificação | decodificar só Classe A; medir; processamento assíncrono se necessário |
| Fragmentação (frames incompletos) | decodificar só frame completo; fragmento = HEX puro |

---

## 12. Plano de implementação

- **Fase 0 — Diagnóstico:** já concluído nesta revisão (baseline + PDF + memória).
- **Fase 1 — Catálogo estendido:** metadados de exibição por campo (nome/tipo/encoding/sensibilidade/§PDF).
- **Fase 2 — Classificador A/B/C:** integrar `isSensitive` + `secureSession` + `DC2`.
- **Fase 3 — Formatador:** HEX + ASCII + offset (Classe A) + sinalização DC3/DC2.
- **Fase 4 — Interpretador semântico:** campos dos comandos **não sensíveis** implementados.
- **Fase 5 — Integração:** plugar no `logging.Tracer` (modo `DETAILED`).
- **Fase 6 — Segurança:** validar redação e falha segura.
- **Fase 7 — Testes e documentação.**

---

## 13. Testes obrigatórios

- Unitários: HEX/ASCII/offset; encoding; campos fixos/variáveis/TLV; desconhecidos;
  comprimentos inválidos; CRC válido/inválido; DC3; DC2 (classe C); redação; falha segura.
- Integração: SPE→PP, ACK/NAK/retransmissão, fragmentação, sessões, Bridge, pinpad físico.
- Regressão: testes da Change 066.
- Segurança: dados protegidos ausentes em HEX/ASCII/valor; detalhado não desabilita `REDACTED`.

---

## 14. Critérios de aceite

- [ ] Classificação A/B/C aplicada antes de formatar.
- [ ] Tabela de controles correta (SYN/ETB/DC3/DC2, sem STX/ETX).
- [ ] DC3 e DC2 sinalizados.
- [ ] Campos de comandos não sensíveis interpretados com nomes do PDF.
- [ ] DSP (S32 fixo) e GOX (sensível, redigido) validados.
- [ ] Matriz de cobertura com os 46 comandos.
- [ ] Nenhum payload sensível/criptografado decodificado.
- [ ] `REDACTED` preservado; `LEGACY` funcional.
- [ ] Sem linha DECIMAL byte a byte.
- [ ] Sem regressão funcional; testes aprovados; evidências registradas.

---

## 15. Definition of Done

1. Catálogo estendido documentado (referências ao PDF).
2. Classificador A/B/C integrado e testado.
3. Comandos não sensíveis implementados interpretados por campo.
4. HEX/ASCII/DETALHES presentes; DECIMAL ausente.
5. Comunicação funcional inalterada; dados sensíveis protegidos.
6. Matriz de cobertura (46 comandos) atualizada.
7. Testes unitários/integração/regressão/segurança aprovados.
8. Documentação e evidências registradas; aprovação conforme workflow.

---

## 16. Referências

- PDF ABECS 2.12: `docs/Pinpad-Abecs-Protocolo-de-Comunicacao-e-Funcionamento-v212190411212/Pinpad-Abecs-Protocolo-de-Comunicacao-e-Funcionamento-v212190411212.pdf`
- Memória do protocolo: `docs/.../abecs-212-referencia-protocolo.md`
- SPEC 066 arquivada: `specs/archive/2026-09-09-066-lib-pinpad-abecs-go/` (inclui `spec-logging.md` e `spec-conformidade-abecs-v212.md`)
- Workflow: `specs/shared/process/workflow.md`

**FIM DA SPEC — V.2.0.0**

## Revisão 3 — integração ao caminho BAT/Bridge (2026-10-15)

Status: `SPEC_APROVADA` pela revisão 3, após pedido explícito de concluir o fluxo real.

RF-016: testar_bridge_pinpad.bat habilita DETAILED por padrão, preservando override LEGACY. O Bridge lê e valida PINPAD_LOG_FORMAT e registra o formato efetivo na inicialização.
RF-017: manter o transporte e o rastro serial bruto opacos. Acrescentar decorador observacional de transporte, com buffers independentes SPE/PP limitados a 2*2049+4 bytes, recomposição somente de chunks do mesmo pacote (sem recompor blocos ABECS, ACK/retry ou lógica de negócio). Nenhum byte encaminhado é alterado.
RF-018: detalhar somente pacote SYN/ETB íntegro, CRC/substituição válidos, comando em catálogo e não sensível, e esquema interpretado sem campos sensíveis/desconhecidos. Resposta exige comando correspondente a uma requisição válida na sessão. Desconhecido, truncado, excedente, DC2 ou erro de decodificação falham fechados. Sem ressincronização em SYN interno de pacote inválido. Limpar buffers ao fechar/reabrir e apagar cópias após uso.
RF-019: pacote sensível permanece REDACTED no rastro bruto; observador nunca gera HEX/ASCII dele. Observador não instala chaves, não altera GTK/PIN, Android ou protocolo Bridge.
RF-020: demonstrar integração sem hardware pelo mesmo executável Bridge em modo scripted e cliente TCP, incluindo fragmentação, SPE/PP, proteção, LEGACY, validação de configuração e ausência de alteração do fluxo.

Aceite adicional: ao iniciar o BAT sem override, próximas operações não sensíveis produzem HEX/COMANDO/CRC no LogPinpadAbecs.txt. Não converter retroativamente o log antigo. Reiniciar somente Bridge sem sessão ativa; não executar compra automaticamente. Esta revisão substitui a restrição anterior de não integrar o Bridge apenas na superfície de observabilidade descrita no ADR-001.
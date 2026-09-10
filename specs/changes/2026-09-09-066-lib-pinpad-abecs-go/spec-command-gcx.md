# SPEC: 066-lib-pinpad-abecs-go — Comando GCX (transação/captura de cartão)

## Status
`RASCUNHO`

## Papel do comando no projeto

`GCX` é o comando de captura/obtenção de dados de cartão no fluxo transacional ABECS. Nesta Change ele deve ser implementado somente com o payload, status e campos de resposta comprovados pelo manual e pelo legado analisado. A fachada Go não pode transformar a resposta em uma transação completa inventando dados que o dispositivo só fornece por outros comandos.

## Identificação

- pacote afetado: `internal/domain/parser` (`GCXResponseFromResponse`), `internal/domain/model` (`GCXResponse`)
- Esta SPEC é complementar a `spec.md` (RF-003, RF-012.7, RF-013 seção "Transação GCX") e não substitui nenhum RF já aprovado; ela detalha e corrige exclusivamente a interpretação da resposta do comando `GCX`.

## Motivação

**IMP-REV-008** — `GCXResponseFromResponse` preserva somente dados brutos (`RawData`/`Tags`); os campos funcionais do modelo `GCXResponse` (tipo de cartão, PAN, dados do portador, EMV) não são preenchidos a partir das tags da resposta, contrariando RF-003 e RF-012.7 de `spec.md`.

Conforme o manual ABECS ("Pinpad Abecs - Protocolo de Comunicação e Funcionamento", v2.12, seção 3.7.1), a resposta do comando `GCX` contém exclusivamente os campos abaixo; ela **não** contém trilhas completas, KSN de trilha, PAN cifrado, PIN block ou resultado de scripts do emissor — esses dados pertencem, respectivamente, aos comandos `GTK` (trilhas completas/KSN, fora do escopo desta SPEC), `GOX` (PIN block/KSN de PIN) e `FCX` (Issuer Script Results). Esta SPEC não deve propor o preenchimento desses campos a partir de `GCX`.

## Referências e dependências

- `spec.md` RF-003 (modelo `GCXResponse`), RF-012.7 (parser GCX), RF-013 (fachada transacional GCX).
- Tags de resposta reais do comando `GCX`, conforme manual ABECS §3.7.1: `PP_CARDTYPE`, `PP_ICCSTAT` (somente quando `PP_CARDTYPE` = "00", cartão magnético), `PP_AIDTABINFO` (somente quando `PP_CARDTYPE` ≠ "00"), `PP_PAN`, `PP_PANSEQNO`, `PP_TRK1INC`/`PP_TRK2INC`/`PP_TRK3INC` (trilhas incompletas, já truncadas/mascaradas pelo pinpad — ver seção 5.4.1/6.3.4.1 do manual), `PP_CHNAME`, `PP_LABEL`, `PP_ISSCNTRY`, `PP_CARDEXP`, `PP_EMVDATA` (somente se solicitado via `SPE_TAGLIST`), `PP_DEVTYPE` (somente para CTLS).
- Regra de redação de dados sensíveis já vigente em RF-011/RF-012.7 de `spec.md`.

## Contrato de transporte e sequência

- O comando lógico é `GCX` e o payload deve ser construído por builder tipado.
- A operação deve passar pelo worker único da instância para impedir interleaving com outro comando.
- A leitura deve aceitar ACK, NTM, frame único, resposta multipartes e bytes excedentes.
- `NAK`, timeout, cancelamento, EOT, CRC inválido e status ABECS devem ser distinguíveis.
- O serviço deve registrar `SPE CMD=GCX`, `PP` e `RSP CMD=GCX STATUS=...`; o payload e as respostas sensíveis devem ser redigidos integralmente conforme `spec-logging.md`.
- `GTK`, `GOX` e `FCX` são comandos distintos. A implementação de `GCXResponseFromResponse` não deve chamar esses comandos implicitamente nem preencher seus campos.

## Requisitos funcionais

### RF-GCX-001 — Preenchimento do modelo a partir das tags reais de `GCX`

`GCXResponseFromResponse(r *model.Response) *model.GCXResponse` deverá interpretar `r.Tags` e preencher, quando presentes na resposta, exclusivamente os seguintes campos (fiéis à seção 3.7.1 do manual ABECS):

- tipo de cartão (`PP_CARDTYPE`);
- status da última leitura de ICC (`PP_ICCSTAT`), presente apenas quando `PP_CARDTYPE` = "00" (magnético, possível fallback de chip);
- informações da(s) Tabela(s) de AID usada(s) no processamento (`PP_AIDTABINFO`), presente apenas quando `PP_CARDTYPE` ≠ "00";
- PAN do cartão (`PP_PAN`) e número de sequência (`PP_PANSEQNO`) — copiados exatamente como recebidos do pinpad (já mascarados conforme `SPE_PANMASK`, se solicitado), sem des-mascarar nem re-mascarar;
- trilhas incompletas (`PP_TRK1INC`, `PP_TRK2INC`, `PP_TRK3INC`), quando presentes — estas já vêm truncadas/mascaradas pelo próprio pinpad conforme a criptografia "End-to-End" do manual (não são as trilhas completas, que só são obtidas via comando `GTK`, fora do escopo desta SPEC);
- nome do portador (`PP_CHNAME`);
- etiqueta da aplicação processada (`PP_LABEL`);
- código do país emissor (`PP_ISSCNTRY`);
- data de validade do cartão (`PP_CARDEXP`);
- tipo de dispositivo CTLS (`PP_DEVTYPE`), quando presente;
- dados EMV genéricos (`PP_EMVDATA`), preservados como `[]byte`, apenas quando o comando `GCX` foi executado com `SPE_TAGLIST` e o pinpad devolveu o bloco correspondente.

Esta SPEC não deve preencher no `GCXResponse` trilhas completas, KSN de trilha, PAN cifrado, KSN de PAN, KSN genérico, PIN block ou resultado de scripts do emissor — esses campos não existem na resposta de `GCX` segundo o manual ABECS e pertencem a outros comandos (`GTK`, `GOX`, `FCX`).

### RF-GCX-002 — Campos ausentes

Tags ausentes na resposta deverão deixar o campo correspondente no valor zero do tipo Go (string vazia, slice nula, ou struct zero), sem erro e sem valor inventado.

### RF-GCX-003 — Preservação de dados brutos

`RawData` e `Tags` continuam preenchidos como hoje (comportamento já existente e aprovado), garantindo que consumidores que precisem de acesso bruto não percam informação.

### RF-GCX-004 — Redação em logs

Nenhum campo do `GCXResponse` preenchido por esta SPEC que seja considerado sensível (PAN, trilhas incompletas, nome do portador) poderá ser exposto por `slog` ou pelo rastro de comunicação definido em `spec-logging.md`; esta SPEC não altera a regra de redação já definida, apenas garante que o modelo passe a ter esses campos preenchidos para uso pelo código autorizado (nunca para logging).

## Requisitos não funcionais

- A validação desta SPEC é feita com pinpad físico real via porta serial real (nunca com resposta simulada/mock); se for necessário usar cartão de teste, deve ser um cartão de teste de laboratório sem dados de portador reais.
- Cobertura de testes aplicável mínima de 80% para as novas ramificações de `GCXResponseFromResponse`, medida sobre os cenários executados contra o pinpad real.

## Regras de negócio

- O parser nunca infere ou deriva um campo sensível a partir de outro; cada campo do modelo é preenchido somente a partir de sua tag correspondente.

## Cenários e critérios de aceite

Todos os cenários abaixo são validados por comunicação serial real com um pinpad físico conectado, executando uma transação/captura real de cartão (nunca por resposta simulada/mock).

- [ ] **CA-GCX-001:** resposta `GCX` real do pinpad para cartão magnético contendo `PP_CARDTYPE` = "00" e `PP_ICCSTAT` preenche os campos correspondentes do modelo.
- [ ] **CA-GCX-002:** resposta `GCX` real do pinpad para cartão ICC/CTLS contendo `PP_CARDTYPE` ≠ "00", `PP_AIDTABINFO`, `PP_PAN` e `PP_PANSEQNO` preenche os campos correspondentes.
- [ ] **CA-GCX-003:** resposta `GCX` real do pinpad contendo `PP_TRK1INC`/`PP_TRK2INC`/`PP_TRK3INC` preenche as trilhas incompletas correspondentes, sem tentar reconstituir trilhas completas.
- [ ] **CA-GCX-004:** resposta `GCX` real do pinpad contendo `PP_CHNAME`, `PP_LABEL`, `PP_ISSCNTRY` e `PP_CARDEXP` preenche todos os campos correspondentes.
- [ ] **CA-GCX-005:** resposta `GCX` real do pinpad para CTLS contendo `PP_DEVTYPE` preenche o campo correspondente; ausência do campo resulta em valor zero do tipo, sem erro.
- [ ] **CA-GCX-006:** `GCX` executado com `SPE_TAGLIST` preenche `PP_EMVDATA` como `[]byte` idêntico ao bloco devolvido pelo pinpad; sem `SPE_TAGLIST`, o campo permanece vazio.
- [ ] **CA-GCX-007:** teste demonstra que nenhum campo sensível preenchido por esta SPEC (PAN, trilhas incompletas, nome do portador) aparece em texto claro em uma chamada de `slog` capturada durante a execução real contra o pinpad.

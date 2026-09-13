# SPEC: 066-lib-pinpad-abecs-go — Comando GCX (transação/captura de cartão)

Autor: Rômulo Penha

## Status
`SPEC_APROVADA`

## Papel do comando no projeto

`GCX` é o comando de captura/obtenção de dados de cartão no fluxo transacional ABECS. Nesta Change ele deve ser implementado somente com o payload, status e campos de resposta comprovados pelo manual e pelo legado analisado. A fachada Go não pode transformar a resposta em uma transação completa inventando dados que o dispositivo só fornece por outros comandos.

## Identificação

- pacotes afetados: `internal/domain/command` (`BuildGCXCommand`),
  `internal/application/service` (`SendGCXCommand`/`PurchaseGCX`),
  `internal/domain/parser` (`ParseNotification`/`GCXResponseFromResponse`),
  `internal/domain/model` (`GCXResponse`/`PinpadConfig`) e
  `cmd/libpinpadabecsgo` (escolha do modo de leitura)
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
- Após o ACK do comando, a leitura deve aceitar zero ou mais respostas
  intermediárias `NTM` e continuar até a resposta final `GCX`; frame único,
  resposta multipartes, fragmentação serial e bytes excedentes permanecem
  suportados.
- `NAK`, timeout, cancelamento, EOT, CRC inválido e status ABECS devem ser distinguíveis.
- O serviço deve registrar `SPE CMD=GCX`, `PP` e `RSP CMD=GCX STATUS=...`; o payload e as respostas sensíveis devem ser redigidos integralmente conforme `spec-logging.md`.
- `GTK`, `GOX` e `FCX` são comandos distintos. A implementação de `GCXResponseFromResponse` não deve chamar esses comandos implicitamente nem preencher seus campos.

## Requisitos funcionais

### RF-GCX-000 — Envelope e parâmetros do comando

`BuildGCXCommand` deverá produzir o formato ABECS da seção 3.7.1:
`CMD_ID="GCX"`, comprimento decimal N3 e parâmetros com identificador e
comprimento binários. O subconjunto desta Change contém:

- `SPE_AMOUNT` (`0x0013`): exatamente 12 dígitos ASCII em centavos;
- `SPE_TRNDATE` (`0x0015`): exatamente 6 dígitos ASCII `AAMMDD`, com data válida;
- `SPE_TRNTIME` (`0x0016`): exatamente 6 dígitos ASCII `HHMMSS`, com hora válida;
- `SPE_GCXOPT` (`0x0017`): exatamente 5 dígitos ASCII, sendo `00000` para
  magnético/ICC e `10000` quando CTLS estiver habilitado. O segundo bit pode
  ser `1` para ocultar o valor; os três últimos permanecem `000` por serem RUF.

Concatenar opção, data, hora e valor como texto posicional sem os identificadores
de parâmetro viola este requisito.

### RF-GCX-000.1 — Uma única iniciação transacional

O próprio comando `GCX` inicia a transação. `PurchaseGCX` deverá enviar
exatamente um GCX com valor, data, hora e opção recebidos. Não deverá enviar
antes outro GCX com valor, data ou hora zerados. A API não exporá
`SendGCXInitialization`, `skipInit`, `UseGCXInitialization` nem
`GCXInitTimeout`.

### RF-GCX-000.2 — Escolha das opções GCX no utilitário local

Antes de executar a opção 11, o utilitário local deverá perguntar qual modo de
leitura será habilitado:

1. chip ou tarja, sem contactless, serializado como `SPE_GCXOPT="00000"`;
2. chip, tarja ou contactless, serializado como `SPE_GCXOPT="10000"`.

Em seguida, deverá perguntar se o valor será mostrado no display durante a
espera pelo cartão. Mostrar o valor preserva o segundo bit em `0`; ocultá-lo usa
o segundo bit em `1`. As combinações possíveis são `00000`, `01000`, `10000` e
`11000`, mantendo os três bits RUF em zero.

Qualquer resposta diferente das opções apresentadas deverá ser informada como
inválida e não deverá iniciar o GCX. O protocolo não oferece uma opção somente
contactless. `PurchaseGCX` receberá as duas escolhas separadamente e as
converterá para a máscara tipada do builder.

O prazo da operação GCX deverá começar somente depois que o operador informar
modo de leitura, visibilidade, valor, data e hora. O tempo gasto preenchendo
esses dados não poderá consumir o prazo destinado à resposta do pinpad.

### RF-GCX-000.3 — Notificações intermediárias de comando blocante

Cada frame válido com `RSP_ID="NTM"`, `RSP_STAT="000"` e `NTM_MSG` de até 32
bytes deverá ser reconhecido como notificação intermediária, sem interpretação
TLV e sem encerrar o GCX. O serviço deverá continuar lendo a mesma operação até
receber `RSP_ID="GCX"`. Conforme a seção 2.2.2.2 do manual, uma resposta válida
do pinpad não recebe ACK; portanto, tratar `NTM` não poderá gerar nova escrita
serial. Notificação malformada deverá retornar erro distinguível de resposta
final inválida.

### RF-GCX-000.4 — Prazo do comando blocante

O GCX deverá preservar o prazo definido pelo consumidor em `context.Context`.
O timeout genérico de `PinpadConfig` não poderá encurtar esse prazo. No
utilitário local, a opção 11 criará um contexto novo de 60 segundos após a
coleta e validação de todas as entradas.

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

- Builder, validação de entrada, escolha do modo, notificações intermediárias e
  quantidade de envios da fachada serão comprovados por testes unitários
  determinísticos, sem hardware.
- O aceite do comportamento do dispositivo permanece dependente de pinpad
  físico real; se for necessário usar cartão, deverá ser um cartão de teste de
  laboratório sem dados de portador reais.
- Cobertura de testes aplicável mínima de 80% para as novas ramificações.

## Regras de negócio

- O parser nunca infere ou deriva um campo sensível a partir de outro; cada campo do modelo é preenchido somente a partir de sua tag correspondente.

## Cenários e critérios de aceite

Os cenários CA-GCX-001 a CA-GCX-007 são validados por comunicação serial real
com pinpad físico. Os cenários CA-GCX-008 a CA-GCX-013 são automatizados.

- [ ] **CA-GCX-001:** resposta `GCX` real do pinpad para cartão magnético contendo `PP_CARDTYPE` = "00" e `PP_ICCSTAT` preenche os campos correspondentes do modelo.
- [ ] **CA-GCX-002:** resposta `GCX` real do pinpad para cartão ICC/CTLS contendo `PP_CARDTYPE` ≠ "00", `PP_AIDTABINFO`, `PP_PAN` e `PP_PANSEQNO` preenche os campos correspondentes.
- [ ] **CA-GCX-003:** resposta `GCX` real do pinpad contendo `PP_TRK1INC`/`PP_TRK2INC`/`PP_TRK3INC` preenche as trilhas incompletas correspondentes, sem tentar reconstituir trilhas completas.
- [ ] **CA-GCX-004:** resposta `GCX` real do pinpad contendo `PP_CHNAME`, `PP_LABEL`, `PP_ISSCNTRY` e `PP_CARDEXP` preenche todos os campos correspondentes.
- [ ] **CA-GCX-005:** resposta `GCX` real do pinpad para CTLS contendo `PP_DEVTYPE` preenche o campo correspondente; ausência do campo resulta em valor zero do tipo, sem erro.
- [ ] **CA-GCX-006:** `GCX` executado com `SPE_TAGLIST` preenche `PP_EMVDATA` como `[]byte` idêntico ao bloco devolvido pelo pinpad; sem `SPE_TAGLIST`, o campo permanece vazio.
- [ ] **CA-GCX-007:** teste demonstra que nenhum campo sensível preenchido por esta SPEC (PAN, trilhas incompletas, nome do portador) aparece em texto claro em uma chamada de `slog` capturada durante a execução real contra o pinpad.
- [ ] **CA-GCX-008:** para valor `000000010000`, data `120926`, hora `173800`
  e CTLS desabilitado, teste unitário compara byte a byte `GCX045` seguido dos
  parâmetros `0013/N12`, `0015/N6`, `0016/N6` e `0017/N5="00000"`.
- [ ] **CA-GCX-009:** teste unitário da fachada comprova que `PurchaseGCX`
  realiza uma única escrita GCX e não envia uma inicialização com data/hora
  `000000`.
- [ ] **CA-GCX-010:** valores, datas, horas ou bits RUF inválidos são rejeitados
  antes de qualquer escrita serial.
- [ ] **CA-GCX-011:** testes comprovam que as escolhas de interface e
  visibilidade geram `00000`, `01000`, `10000` e `11000`; uma escolha inválida
  no utilitário local não inicia a operação.
- [ ] **CA-GCX-012:** teste da fachada recebe `ACK`, duas notificações válidas
  `NTM000032` e a resposta final `GCX000...`, inclusive com frames no mesmo
  chunk serial, e comprova sucesso, preservação da resposta final e ausência de
  escrita adicional para as notificações.
- [ ] **CA-GCX-013:** teste da fachada configura timeout genérico de 30 segundos,
  chama `PurchaseGCX` com contexto de 60 segundos e comprova que o prazo que
  chega à leitura serial permanece próximo de 60 segundos.


## Correção normativa de data de 2026-09-13

`SPE_TRNDATE` usa seis dígitos em `AAMMDD`. Para 12 de setembro de 2026, o valor
correto é `260912`; `120926` representa 26 de setembro de 2012 e não pode ser
usado como oráculo de teste.

## Campos condicionais e repetição CTLS

Uma resposta `000` exige `PP_CARDTYPE`. Magnético exige `PP_ICCSTAT`;
ICC/CTLS exige `PP_AIDTABINFO` e `PP_LABEL`; ICC EMV e CTLS EMV também exigem
`PP_PAN` e `PP_PANSEQNO`. O parser rejeita a ausência desses campos.

Com CTLS habilitado, `ST_CTLSCOMMERR` repete uma vez com CTLS e muda para
ICC/tarja na segunda ocorrência. `ST_CTLSMULTIPLE` e `ST_CTLSEXTCVM` repetem
uma vez com CTLS. `ST_CTLSINVALIDAT`, `ST_CTLSPROBLEMS`, `ST_CTLSAPPNAV`,
`ST_CTLSAPPNAUT` e `ST_CTLSIFCHG` mudam imediatamente para ICC/tarja.

# SPEC: 066-lib-pinpad-abecs-go — Comando GIX (informações do dispositivo)

## Status
`RASCUNHO`

## Papel do comando no projeto

`GIX` é o comando de descoberta do pinpad. Ele fornece a resposta usada pela biblioteca para montar `DeviceInfo` e `DisplayCapabilities`; não inicia transação, não carrega tabela EMV e não deve ser usado como fonte de capacidades que o manual não define. A implementação Go deve manter a separação entre transporte (`SPE`/`PP`), resposta ABECS e modelos de aplicação.

## Identificação

- pacote afetado: `internal/application/service` (`GetInfo`, `GetDisplayCapabilities`), `internal/domain/parser`
- Esta SPEC é complementar a `spec.md` (RF-010, RF-013 seção "Comandos básicos" e "Display e capacidades") e não substitui nenhum RF já aprovado; ela detalha e corrige exclusivamente o comportamento do comando `GIX`.

## Motivação

A revisão de implementação registrou dois achados relacionados a `GIX`:

- **IMP-REV-007** — `GetDisplayCapabilities` retorna `DisplayCapabilities{}` vazio, sem executar `GIX` nem interpretar as tags de capacidade previstas em RF-010/RF-013.
- **IMP-REV-013** — RF-013 especifica `GetInfo` e `GetInfoRaw`; a fachada só implementa `GetInfo`.

## Referências e dependências

- `spec.md` RF-010 (comando GIX), RF-013 (`GetInfo`/`GetInfoRaw`, `GetDisplayCapabilities`).
- `internal/domain/model` (`DeviceInfo`, `DisplayCapabilities`).
- `internal/domain/parser` (`ParseAbecsResponse`, `DeviceInfoFromResponse`).
- Tags `PP_MODEL`, `PP_MNNAME`, `PP_CAPAB`, `PP_DSPTXTSZ`, `PP_DSPGRSZ`, `PP_MFSUP` do catálogo ABECS já definido em `internal/domain/command/constants.go`.
- Fonte normativa: "Pinpad Abecs - Protocolo de Comunicação e Funcionamento", versão 2.12, seção 3.1.3.2 (definição de `PP_CAPAB`, `PP_DSPTXTSZ`, `PP_DSPGRSZ` e `PP_MFSUP`).

## Contrato de transporte

- Comando lógico: `GIX`.
- Payload sem parâmetros: `GIX000` antes do framing.
- O frame deve ser montado pelo protocolo comum ABECS, com `SYN`, substitution, `ETB` e CRC.
- A resposta pode chegar em múltiplas leituras e deve preservar bytes excedentes.
- `NAK`, timeout, cancelamento e CRC inválido devem retornar os erros tipados da biblioteca.
- O rastro deve registrar `SPE CMD=GIX`, `PP` e `RSP CMD=GIX STATUS=...`, sem incluir dados que o parser classifique como sensíveis.

## Requisitos funcionais

### RF-GIX-001 — `GetInfoRaw`

Deverá existir `GetInfoRaw(ctx context.Context) (*model.Response, error)` na fachada (`Service`), executando o comando `GIX` e devolvendo a resposta ABECS decodificada (`Response`), sem conversão para `DeviceInfo`. `GetInfo` deverá ser reescrito para chamar `GetInfoRaw` internamente e aplicar `parser.DeviceInfoFromResponse` sobre o resultado, eliminando duplicação de envio do comando.

### RF-GIX-002 — `GetDisplayCapabilities` real

`GetDisplayCapabilities(ctx context.Context) (*model.DisplayCapabilities, error)` deverá:

- executar o comando `GIX` (reaproveitando `GetInfoRaw`, sem enviar o comando duas vezes quando chamado isoladamente);
- interpretar as tags `PP_MODEL` e `PP_MNNAME` para preencher os campos de modelo e fabricante de `DisplayCapabilities`;
- interpretar `PP_CAPAB` (formato `A10`, conforme manual ABECS §3.1.3.2) estritamente pelos dois primeiros caracteres documentados, sem inventar bits adicionais:
  - 1º caractere (`0`/`1`): suporte a CTLS (cartão sem contato);
  - 2º caractere (`0`/`1`/`2`): tipo de display — `0` = sem display gráfico, `1` = display gráfico monocromático, `2` = display gráfico colorido;
  - os demais caracteres de `PP_CAPAB` são RUF (reservados) e não devem ser interpretados nesta SPEC;
- interpretar `PP_DSPTXTSZ` (formato `N4`, "LLCC") como número máximo de linhas e colunas do display em modo texto;
- interpretar `PP_DSPGRSZ` (formato `N8`, "LLLLCCCC", em pixels), quando presente, como dimensão máxima do display gráfico;
- interpretar `PP_MFSUP` (formato `A..20`), quando presente, como os formatos de arquivo multimídia suportados (PNG/JPG/GIF, conforme os bits definidos no manual — 1º caractere PNG, 2º caractere JPG, 3º caractere GIF); este dado é independente de `PP_CAPAB` e não deve ser fundido com o tipo de display;
- quando uma tag esperada estiver ausente na resposta, o campo correspondente deverá permanecer no valor zero/`false` do tipo, sem erro, mas o método não deverá inventar um valor não presente na resposta.

Esta SPEC não deve atribuir a `DisplayCapabilities` nenhuma noção de suporte a ICC ou tarja magnética a partir de `PP_CAPAB`: o manual ABECS não define esses bits nesse campo, e qualquer inferência desse tipo seria uma invenção não documentada.

### RF-GIX-003 — Erros

Falhas de transporte, timeout, NAK ou resposta inválida durante `GIX` deverão propagar o mesmo erro tipado já usado por `GetInfo` (`ErrTimeout`, `ErrNakReceived`, `ErrInvalidResponse` etc.), sem novo tipo de erro.

## Requisitos não funcionais

- Nenhuma tag decodificada poderá conter PAN, TRACK2, PIN ou dados sensíveis (GIX não carrega esses campos, mas a implementação não deve assumir isso sem checagem defensiva do parser existente).
- Sem alocação de nova conexão/envio duplicado de `GIX` quando `GetDisplayCapabilities` for chamado após `GetInfo` na mesma operação lógica (ambos continuam sendo chamadas independentes do consumidor; não há cache obrigatório nesta SPEC).

## Regras de negócio

- `GetInfo` e `GetInfoRaw` e `GetDisplayCapabilities` exigem o pinpad no estado `OPEN` (mesma regra já aplicada a outros comandos via `SendCommand`).

## Cenários e critérios de aceite

Todos os cenários abaixo são validados por comunicação serial real com um pinpad físico conectado (nunca por resposta simulada/mock).

- [ ] **CA-GIX-001:** `GetInfoRaw`, executado contra um pinpad real via porta serial real, retorna a `*model.Response` completa do `GIX`, incluindo `RawData` e `Tags`.
- [ ] **CA-GIX-002:** `GetInfo` usa `GetInfoRaw` internamente (sem enviar `GIX` duas vezes ao pinpad) e devolve `DeviceInfo` preenchido a partir da mesma resposta real.
- [ ] **CA-GIX-003:** `GetDisplayCapabilities`, executado contra um pinpad real cuja resposta `GIX` contenha `PP_MODEL`, `PP_MNNAME`, `PP_CAPAB`, `PP_DSPTXTSZ`, `PP_DSPGRSZ` e `PP_MFSUP`, preenche todos os campos correspondentes de `DisplayCapabilities`, incluindo suporte a CTLS e tipo de display (sem/mono/cor) extraídos exclusivamente dos dois primeiros caracteres de `PP_CAPAB`.
- [ ] **CA-GIX-004:** `GetDisplayCapabilities`, executado contra um pinpad real cuja resposta `GIX` não traga `PP_MFSUP`, devolve o campo de multimídia no valor zero do tipo, sem erro.
- [ ] **CA-GIX-005:** uma falha real de timeout/NAK reportada pelo pinpad/porta serial durante `GIX` propaga o erro tipado correspondente em `GetInfo`, `GetInfoRaw` e `GetDisplayCapabilities`.

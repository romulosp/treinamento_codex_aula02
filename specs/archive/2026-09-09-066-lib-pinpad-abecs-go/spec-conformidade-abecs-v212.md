# SPEC: conformidade normativa ABECS 2.12

Autor: Rômulo Penha

## Status

`SPEC_APROVADA`

## Fonte normativa e precedência

Esta SPEC corrige os contratos técnicos da Change 066 após a revisão integral do
manual `Pinpad-Abecs-Protocolo-de-Comunicacao-e-Funcionamento-v212190411212.pdf`,
revisão 2.12 de 11-abr-2019. Os formatos, limites e sequências abaixo prevalecem
sobre descrições genéricas ou contraditórias nas demais SPECs desta Change.

Nenhum teste unitário pode usar um payload inventado como oráculo. Builders,
parsers e fluxos devem ser comparados byte a byte com este contrato e, quando o
manual publicar um exemplo completo, com o vetor publicado.

## Transporte

- O pacote possui `SYN + PKTDATA substituído + ETB + CRC16`; o `PKTDATA`
  original, antes das substituições, chega a 2049 bytes. Dados claros de um
  “Comando Abecs” podem ocupar até 2044 bytes; o encapsulamento seguro pode
  produzir `DC2 + criptograma` com 2049 bytes. Nos comandos clássicos, o limite
  de `PKTDATA` é 1024 bytes.
- Comandos e respostas ABECS podem conter vários blocos, cada um iniciado por
  comprimento decimal `N3` de até 999 bytes. Cada parâmetro TLV permanece
  inteiro dentro do bloco; o parser preserva todos os blocos e rejeita sobra.
- Depois de cada envio, o host aguarda `ACK` ou `NAK` por no máximo 2 segundos.
  `NAK` ou ausência de confirmação retransmite o mesmo pacote, até três
  tentativas no total.
- CRC ou framing inválido na resposta faz o host enviar `NAK` e aguardar a
  retransmissão, até três tentativas. Resposta válida não recebe ACK do host.
- Comando não bloqueante aguarda a resposta final por no máximo 10 segundos.
  Comando bloqueante aguarda até resposta, cancelamento do contexto ou timeout
  explícito do próprio comando.
- Antes de iniciar uma comunicação, o host transmite o byte isolado `CAN`
  (`0x18`), aguarda `EOT` por 2 segundos e repete até três vezes. Bytes diferentes
  de EOT são ignorados durante essa espera.
- Cancelar um comando bloqueante executa o mesmo handshake `CAN`/`EOT` antes de
  devolver o erro original do contexto.

## Ciclo de vida e comunicação segura

- OPN clássico é o payload literal `OPN`; sua resposta de sucesso é `OPN000`.
- OPN seguro substitui o OPN clássico e é enviado em claro como `OPN + N3 +
  OPMODE(0) + MODLEN(256) + MODULUS(H512) + EXPLEN(N1) + EXPONENT(H2..H6)`.
- CLO é `CLO032` seguido de uma mensagem `S32`; sua resposta é `CLO000`.
- Com KSEC ativa, todos os comandos posteriores ao OPN são protegidos, inclusive
  CLO e CLX. Todas as respostas são protegidas, exceto as respostas de CLO e
  CLX, que chegam em claro.
- O bloco seguro é `DATALEN(X2) + DATACRC(X2) + CLRDATA + zeros`, completado para
  múltiplo de 16 e cifrado por AES-CBC com KSEC de 16 bytes e IV zerado; PKTDATA
  contém somente DC2 seguido do criptograma.

## Texto, display e teclas

- Campos `A` e `S` usam um byte por caractere. A biblioteca converte Latin-1 e
  rejeita runes fora de `U+0000..U+00FF`; não trunca UTF-8 silenciosamente.
- DSP é `DSP032` seguido de exatamente 32 bytes, duas linhas de 16.
- DEX contém o parâmetro de mensagem `N3 + S1..160` em um bloco ABECS.
- MNU aceita 1..20 opções de 1..24 bytes, título opcional de até 128 bytes e
  timeout `X1` de 0..255. A resposta selecionada está em `PP_VALUE` (`0x804D`)
  como `N2` entre `01` e `20`.
- GKY é o payload literal `GKY`. A tecla é o próprio status: `000` OK,
  `004..007` F1..F4, `008` CLEAR e `013` CANCEL.

## PIN

GPN é um comando clássico bloqueante com o bloco, nesta ordem: método `N1`
(`0` MK/WK DES, `1` MK/WK TDES, `2` DUKPT DES, `3` DUKPT TDES), índice `N2`,
WKENC `H32` (zeros em DUKPT), PANLEN `N2`, PAN `A19` alinhado à esquerda,
ENTRIES `N1=1`, mínimo `N2>=04`, máximo `N2>=mínimo` e mensagem `S32`.
A resposta de sucesso é `GPN000036 + PINBLK(H16) + KSN(H20)`, convertida para
8 e 10 bytes. PAN, chaves, PIN block e KSN nunca são registrados.

## Multimídia

- Nome de mídia é `A8`, exatamente oito caracteres alfanuméricos.
- MLI contém `SPE_MFNAME` e `SPE_MFINFO(B10)`: tamanho `X4` big-endian,
  CRC16 `B2`, tipo `B1` (`1` PNG, `2` JPG, `3` GIF) e três bytes RUF zerados.
- MLR contém um ou mais `SPE_DATAIN`, cada um com até 995 bytes.
- MLE é o payload literal `MLE`. DSI contém `SPE_MFNAME`.
- LMF é o payload literal `LMF`; a resposta pode conter zero ou vários campos
  `PP_MFNAME` (tag `0x805E`). Lista vazia é sucesso; os nomes retornados pelo
  dispositivo são apresentados em maiúsculas.
- DMF contém um ou mais campos `SPE_MFNAME` (`0x001E`), cada um com nome A8.
  Nomes desconhecidos ou inválidos são ignorados pelo dispositivo; ausência de
  qualquer nome é `ST_MANDAT`.
- Quando a fachada recebe somente bytes, o tipo é identificado pela assinatura
  PNG, JPEG ou GIF; conteúdo sem assinatura suportada usa `B1=00h` (RUF) e é
  transferido sem crítica de formato em MLI, conforme a seção 6.6.1. Se o pinpad
  não suportar o tipo, a crítica ocorre ao usar DSI.

## Tabelas EMV

- TLI clássico é `TLI012 + ACQUIRER(N2) + VERSION(A10)`.
- Status TLI `000` informa versão igual e `020` versão diferente; ambos iniciam
  a carga e devem prosseguir com TLR/TLE. Outros status encerram o fluxo.
- TLR é `TLR + N3 + NREC(N2) + registros`. Cada registro começa por seu próprio
  comprimento `N3`; o `CMD_LEN1` tem no máximo `999`, como determina a página
  107 do manual, e não usa delimitador. `1024` é o limite legado do `PKTDATA`
  completo: `TLR` + `CMD_LEN1` + corpo chegam a no máximo 1005 bytes. O valor
  1024 não cabe no campo `N3` e não deve ser usado como limite do corpo TLR.
- TLE é o payload literal `TLE`.

## Transação

- GCX usa valor `N12`, data `AAMMDD`, hora `HHMMSS` e opções `N5`. O primeiro
  bit habilita CTLS, o segundo oculta o valor e os três últimos são zero.
- Em sucesso, GCX exige `PP_CARDTYPE`. Cartão magnético exige `PP_ICCSTAT`;
  ICC/CTLS exige `PP_AIDTABINFO` e `PP_LABEL`; ICC EMV e CTLS EMV também exigem
  `PP_PAN` e `PP_PANSEQNO`.
- Com CTLS habilitado, `ST_CTLSCOMMERR` repete uma vez com CTLS e na segunda
  ocorrência muda para ICC/tarja; `ST_CTLSMULTIPLE` e `ST_CTLSEXTCVM` repetem
  uma vez com CTLS; `ST_CTLSINVALIDAT`, `ST_CTLSPROBLEMS`, `ST_CTLSAPPNAV`,
  `ST_CTLSAPPNAUT` e `ST_CTLSIFCHG` mudam imediatamente para ICC/tarja.
- GTK aceita DataMethod ausente para devolver trilhas em claro ou `00,01,10,11,30,40,50,51,
  90,91`. IV é opcional em CBC e vale zero quando ausente. Índice e material de
  chave são condicionais ao método, conforme seção 3.3.12 do manual.
- GOX exige adquirente `N2`, método de PIN `N1` e índice `N2`; WKENC é
  condicional. Os campos opcionais respeitam os tamanhos da seção 3.7.3. A
  resposta exige `PP_GOXRES(N6)` e exige PIN block/KSN e EMV conforme a
  solicitação.
- FCX usa opções `N4` (decisão `0`, `1` ou `2` e três zeros), ARC `A2`
  obrigatório para decisões `0` e `1`, EMV de até 512 bytes, tag list de até
  128 e timeout `B1`. A resposta exige `PP_FCXRES(N3)`.
- Data inválida, campo obrigatório ausente ou condição de resposta violada gera
  erro; o parser não fabrica valor zero para dado obrigatório.

## Informações e catálogo

- `PP_MFSUP` é uma cadeia de bits: posições 0, 1 e 2 indicam PNG, JPG e GIF.
- `PP_DSPGRSZ` é `LLLLCCCC`: quatro dígitos de linhas/altura e quatro de
  colunas/largura.
- O catálogo inclui status `051` (`ST_NOSAM`) e não inclui o status não
  documentado `047`.
- O manual 2.12 não define comando RST. RST é removido do catálogo, da fachada,
  do menu e dos testes desta Change; reset de operação usa CAN.

## Critérios de aceite automatizados

- [x] Cada builder possui teste byte a byte de caso válido e tabela de entradas
  inválidas baseada nos tipos e limites do manual.
- [x] Cada parser possui exemplos válidos, campos obrigatórios ausentes,
  comprimentos inválidos e valores condicionais inválidos.
- [x] Transporte cobre ACK, NAK, timeout de 2 s, três tentativas, NAK de resposta
  corrompida, ausência de ACK para resposta válida e CAN/EOT.
- [x] Vetores publicados de MNU, GPN e comunicação segura são reproduzidos sem
  alteração no teste unitário.
- [ ] Vetores publicados de LMF (campos `PP_MFNAME` repetidos) e DMF
  (múltiplos `SPE_MFNAME`) são reproduzidos byte a byte, incluindo lista vazia.
- [ ] Timeout não bloqueante e resposta fora de sequência recuperam CAN/EOT;
  após três CAN sem EOT, a instância protege a fila e executa uma única
  reconexão controlada com CAN/EOT inicial e OPN, sem reenviar o comando
  original.
- [ ] A validação física de DSI distingue status `000` de confirmação visual;
  ambos são necessários para declarar que a imagem foi exibida.
- [x] `go test ./...`, cobertura, `go vet ./...` e `go build ./...` terminam com
  código zero; limitações do race detector e hardware são registradas.

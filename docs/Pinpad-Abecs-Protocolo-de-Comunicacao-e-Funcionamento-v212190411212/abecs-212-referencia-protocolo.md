# Referência ABECS 2.12 — memória de protocolo para agentes de IA

> **Fonte canônica:** `Pinpad-Abecs-Protocolo-de-Comunicacao-e-Funcionamento-v212190411212.pdf` (versão 2.12, 11-abr-19, 283 páginas).
> **Derivação:** texto integral extraído via PyMuPDF em [abecs212.txt](abecs212.txt) (extração integral mantida no projeto). Este arquivo é uma **síntese fiel** para consulta por agentes; em caso de dúvida, volte ao PDF.
> **Escopo:** protocolo de comunicação e funcionamento do Pinpad ABECS (SPE ⇄ pinpad).

---

## 1. Nível de enlace (seção 2.2)

### 1.1 Bytes de controle (seção 2.2)

| Nome | Valor | Papel |
|---|---|---|
| EOT | `04h` | Resposta do pinpad ao receber `CAN`. |
| ACK | `06h` | Pinpad → SPE ao receber pacote válido. |
| DC3 | `13h` | Byte de substituição (escape) dentro do corpo. |
| NAK | `15h` | Devolvido ao lado que enviou pacote inválido (retransmissão). |
| SYN | `16h` | Início de pacote (PKTSTART). |
| ETB | `17h` | Fim de pacote (PKTSTOP). |
| CAN | `18h` | SPE → pinpad para cancelar comando blocante. |

> **Não existem** `STX (02h)` nem `ETX (03h)` neste protocolo. O enquadramento é `SYN … ETB + CRC`.

### 1.2 Formato do pacote (seção 2.2.1)

```
PKTSTART (SYN 16h) + PKTDATA (com substituição) + PKTSTOP (ETB 17h) + PKTCRC (X2)
```

- Regra de substituição em PKTDATA:
  - `13h` → `13h 33h`
  - `16h` → `13h 36h`
  - `17h` → `13h 37h`
- `PKTCRC` = CRC-16 dos dados **originais** (sem substituição) + PKTSTOP (ver algoritmo na seção 7.2 do PDF).
- PKTDATA original máximo: **2049 bytes** (compatibilidade legada: >1024 bytes só em "Comando Abecs").

### 1.3 Fluxo de comunicação (seção 2.2.2)

- O SPE inicia sempre; o pinpad é passivo.
- Comando não-blocante → SPE aguarda até **10 s**.
- Comando blocante → SPE aguarda **indefinidamente**; permite **mensagens de notificação** (`NTM`).
- ACK/NAK: pinpad valida CRC → `ACK` (06h) se ok, `NAK` (15h) se CRC/formato inválido; SPE aguarda até **2 s**.

---

## 2. Nível de aplicação (seções 2.3 e 3.1.3)

### 2.1 Formato do comando (3.1.3.1)

```
CMD_ID (A3) + CMD_LEN1 (N3, ≤999) + CMD_BLK1{ CMD_PARID(X2) + CMD_PARLEN(X2) + CMD_PAR } + [CMD_LEN2 + CMD_BLK2 …]
```

- Parâmetros podem vir em **qualquer ordem**; divididos em blocos de até 999 bytes.
- `CMD_PARLEN` máximo: **995** (`03E3h`).
- Presença: `M` (mandatório → senão `ST_MANDAT`), `MD` (mandatório condicional), `O` (opcional).
- O pinpad **despreza** parâmetros desconhecidos.

### 2.2 Formato da resposta (3.1.3.2)

```
RSP_ID (A3) + RSP_STAT (N3) + RSP_LEN1 (N3) + RSP_BLK1{ RSP_DATID(X2) + RSP_DATLEN(X2) + RSP_DAT } + [RSP_LEN2 + RSP_BLK2 …]
```

- Presença: `M`, `MD`, `MR` (mandatório se requerido pelo SPE), `O`.

---

## 3. Catálogo de parâmetros de comando — `SPE_xxx` (seção 3.1.3.1)

| ID | Nome | Formato | Descrição |
|---|---|---|---|
| `0001h` | SPE_IDLIST | B..128 | Lista de até 64 identificadores de retorno. |
| `0002h` | SPE_MTHDPIN | N1 | Método criptografia PIN (`0`=MK/WK:DES:PIN, `1`=MK/WK:TDES:PIN, `2`=DUKPT:DES:PIN, `3`=DUKPT:TDES:PIN). |
| `0003h` | SPE_MTHDDAT | N2 | Método criptografia dados (`00/01/10/11/30/50/51`). |
| `0004h` | SPE_TAGLIST | B..128 | Tags EMV requeridos. |
| `0005h` | SPE_EMVDATA | B..512 | Objetos EMV (TLV) enviados ao pinpad. |
| `0006h` | SPE_CEXOPT | A6 | Opções do comando CEX. |
| `0007h` | SPE_TRACKS | N4 | Trilhas a devolver no GTK. |
| `0008h` | SPE_OPNDIG | N1 | Dígitos "em claro" nas trilhas (`0/2/4/6/8`). |
| `0009h` | SPE_KEYIDX | N2 | Índice da chave MK/DUKPT (`00`–`99`). |
| `000Ah` | SPE_WKENC | B8/B16 | Working Key criptografada (MK:DES=8, MK:TDES=16). |
| `000Bh` | SPE_MSGIDX | X2 | Índice da mensagem. |
| `000Ch` | SPE_TIMEOUT | X1 | Tempo de inatividade do portador (s, ≤255). |
| `000Dh` | SPE_MINDIG | X1 | Mínimo de dígitos (0–32). |
| `000Eh` | SPE_MAXDIG | X1 | Máximo de dígitos (1–32). |
| `000Fh` | SPE_DATAIN | B..995 | Dados genéricos. |
| `0010h` | SPE_ACQREF | N2 | Rede Credenciadora (`01`–`99`). |
| `0011h` | SPE_APPTYPE | N..20 | Tipos de aplicação (`01`–`98`, 1–10 ids). |
| `0012h` | SPE_AIDLIST | A..512 | Registros AID ("AARR", até 128). |
| `0013h` | SPE_AMOUNT | N12 | Valor da transação em **centavos**. |
| `0014h` | SPE_CASHBACK | N12 | Valor saque/troco (cashback) em centavos. |
| `0015h` | SPE_TRNDATE | N6 | Data ("AAMMDD"). |
| `0016h` | SPE_TRNTIME | N6 | Hora ("HHMMSS"). |
| `0017h` | SPE_GCXOPT | N5 | Opções GCX. |
| `0018h` | SPE_GOXOPT | N5 | Opções GOX. |
| `0019h` | SPE_FCXOPT | N4 | Opções FCX. |
| `001Ah` | SPE_TRMPAR | B10 | Terminal Risk Management (floor limit, percentages). |
| `001Bh` | SPE_DSPMSG | S..128 | Mensagem display (quebras `0Dh`). |
| `001Ch` | SPE_ARC | A2 | Authorization Response Code. |
| `001Dh` | SPE_IVCBC | B8 | IV para criptografia CBC. |
| `001Eh` | SPE_MFNAME | A8 | Nome do arquivo multimídia. |
| `001Fh` | SPE_MFINFO | B10 | Info do arquivo (tamanho X4 + CRC B2 + tipo B1 + RUF B3). |
| `0020h` | SPE_MNUOPT | S..24 | Texto de uma opção de menu. |
| `0021h` | SPE_TRNTYPE | B1 | Tipo de transação (`00`=compra, `01`=saque, `09`=cashback, `20`=refund, `30`=saldo). |
| `0022h` | SPE_TRNCURR | N3 | Código da moeda (ex.: `986`=Real). |
| `0023h` | SPE_PANMASK | N4 | Mascaramento PAN ("eedd"). |
| `0024h` | SPE_PBKMOD | B256 | Módulo de chave pública RSA 2048 bits. |
| `0025h` | SPE_PBKEXP | B..3 | Expoente de chave pública RSA. |

> ⚠️ No código Go, `0024h`/`0025h` estão nomeados como `SPEPinBlockMode`/`SPEPinBlockExponent`, mas o PDF define `SPE_PBKMOD`/`SPE_PBKEXP` = **módulo/expoente de chave pública RSA** (não "Pin Block"). Reconciliar no catálogo semântico.

---

## 4. Catálogo de dados de resposta — `RSP_DATID` / `PP_xxx` (seção 3.1.3.2)

| ID | Nome | Formato | Descrição |
|---|---|---|---|
| `8001h` | PP_SERNUM | A..32 | Número de série. |
| `8002h` | PP_PARTNBR | A..32 | Part Number. |
| `8003h` | PP_MODEL | A..20 | Modelo/versão de hardware. |
| `8004h` | PP_MNNAME | A..20 | Fabricante. |
| `8005h` | PP_CAPAB | A10 | Capacidades (CTLS/display). |
| `8006h` | PP_SOVER | A..20 | Versão software/OS. |
| `8007h` | PP_SPECVER | A4 | Versão da especificação ("2.12"). |
| `8008h` | PP_MANVERS | A16 | Versão app Gerenciadora. |
| `8009h` | PP_APPVERS | A16 | Versão app Abecs. |
| `800Ah` | PP_GENVERS | A16 | Versão app Extensão. |
| `8010h` | PP_KRNLVER | A..20 | Versão Kernel EMV ICC. |
| `8011h` | PP_CTLSVER | A..20 | Versão Kernel CTLS. |
| `8012h` | PP_MCTLSVER | A..20 | Kernel MasterCard PayPass. |
| `8013h` | PP_VCTLSVER | A..20 | Kernel VISA PayWave. |
| `8014h` | PP_AECTLSVER | A..20 | Kernel Amex. |
| `8015h` | PP_DPCTLSVER | A..20 | Kernel Discover. |
| `8016h` | PP_PUREVER | A..20 | Kernel Pure. |
| `8020h` | PP_DSPTXTSZ | N4 | Display texto "LLCC". |
| `8021h` | PP_DSPGRSZ | N8 | Display gráfico "LLLLCCCC". |
| `8022h` | PP_MFSUP | A..20 | Formatos multimídia suportados. |
| `8030h` | PP_MKDESP | A100 | Mapa chaves MK:DES:PIN. |
| `8031h` | PP_MKDESD | A100 | Mapa chaves MK:DES:DAT. |
| `8032h` | PP_MKTDESP | A100 | Mapa chaves MK:TDES:PIN. |
| `8033h` | PP_MKTDESD | A100 | Mapa chaves MK:TDES:DAT. |
| `8034h` | PP_DKPTDESP | A100 | Mapa chaves DUKPT:DES:PIN. |
| `8035h` | PP_DKPTTDESP | A100 | Mapa chaves DUKPT:TDES:PIN. |
| `8036h` | PP_DKPTTDESD | A100 | Mapa chaves DUKPT:TDES:DAT. |
| `8040h` | PP_EVENT | A2 | Evento detectado no CEX (teclas/cartão/CTLS). |
| `8041h` | PP_TRK1INC | A..60 | Trilha 1 incompleta. |
| `8042h` | PP_TRK2INC | A..30 | Trilha 2 incompleta. |
| `8043h` | PP_TRK3INC | A..30 | Trilha 3 incompleta. |
| `8044h` | PP_TRACK1 | — | Trilha 1. |
| `8045h` | PP_TRACK2 | — | Trilha 2. |
| `8046h` | PP_TRACK3 | — | Trilha 3. |
| `8047h` | PP_TRK1KSN | — | KSN trilha 1. |
| `8048h` | PP_TRK2KSN | — | KSN trilha 2. |
| `8049h` | PP_TRK3KSN | — | KSN trilha 3. |
| `804Ah` | PP_PAN | — | PAN criptografado. |
| `804Bh` | PP_PANKSN | — | KSN do PAN criptografado. |
| `804Ch` | PP_KSN | — | KSN. |
| `804Dh` | PP_VALUE | — | Valor. |
| `804Eh` | PP_DATAOUT | — | Dados de saída. |
| `804Fh` | PP_CARDTYPE | — | Tipo de cartão. |
| `8050h` | PP_ICCSTAT | — | Status ICC. |
| `8051h` | PP_AIDTBLINF | — | Info tabela AID. |
| `8052h` | PP_PAN | — | PAN. |
| `8053h` | PP_PANSEQ | — | Sequência do PAN. |
| `8054h` | PP_EMVDATA | — | Dados EMV (TLV). |
| `8055h` | PP_CARDHNAME | — | Nome do portador. |
| `8056h` | PP_GOXRES | — | Resultado GOX. |
| `8057h` | PP_PINBLK | — | Bloco de PIN criptografado. |
| `8058h` | PP_FCXRES | — | Resultado FCX. |
| `8059h` | PP_ISRES | — | Issuer Script Results. |
| `805Ah` | PP_BIGRAND | — | Grande aleatório. |
| `805Bh` | PP_LABEL | — | Rótulo. |
| `805Ch` | PP_ISSCTRY | — | País do emissor. |
| `805Dh` | PP_CARDEXP | — | Expiração do cartão. |
| `805Eh` | PP_MFNAME | — | Nome do arquivo multimídia. |
| `8060h` | PP_DEVTYPE | — | Tipo de dispositivo. |
| `8062h` | PP_TLRMEM | — | Memória TLR. |
| `8063h` | PP_ENCRAND | — | Aleatório criptografado. |
| `8064h` | PP_BATTINF | — | Info de bateria. |
| `8065h` | PP_COMMINF | — | Info de comunicação. |

> ⚠️ Discrepância no código Go (`internal/domain/protocol/constants.go`): `8032h`/`8033h` estão nomeados `TagMerchantDescription`/`TagMerchantData`, mas o PDF define `PP_MKTDESP`/`PP_MKTDESD` (mapas de chave MK:TDES). O interpretador semântico deve usar os nomes do PDF.

---

## 5. Códigos de retorno — `RSP_STAT` (seção 3.1.1)

| Valor | Nome | Descrição |
|---|---|---|
| `000` | ST_OK | Sucesso. |
| `003` | ST_NOSEC | Comunicação Segura não estabelecida. |
| `004`–`007` | ST_F1–ST_F4 | Teclas de função. |
| `008` | ST_BACKSP | Tecla LIMPA. |
| `009` | ST_ERRPKTSEC | Erro decodificação segura / comando em claro com sessão segura. |
| `010` | ST_INVCALL | Chamada inválida / comando desconhecido. |
| `011` | ST_INVPARM | Parâmetro inválido. |
| `012` | ST_TIMEOUT | Tempo esgotado. |
| `013` | ST_CANCEL | Cancelado pelo portador. |
| `019` | ST_MANDAT | Parâmetro mandatório ausente. |
| `020` | ST_TABVERDIF | Versão de Tabelas EMV difere. |
| `021` | ST_TABERR | Erro ao gravar tabelas. |
| `040` | ST_INTERR | Erro interno. |
| `041` | ST_MCDATAERR | Erro leitura cartão magnético. |
| `042` | ST_ERRKEY | MK/DUKPT não presente. |
| `043` | ST_NOCARD | Sem ICC/CTLS. |
| `044` | ST_PINBUSY | Captura PIN temporariamente indisponível. |
| `045` | ST_RSPOVRFL | Resposta excede tamanho máximo. |
| `046` | ST_ERRCRYPT | Erro criptográfico genérico. |
| `051` | ST_NOSAM | SAM ausente. |
| `060` | ST_DUMBCARD | ICC "mudo". |
| `061` | ST_ERRCARD | Erro comunicação ICC/CTLS. |
| `067` | ST_CARDINVALIDAT | Cartão invalidado. |
| `068` | ST_CARDPROBLEMS | ICC com problemas. |
| `069` | ST_CARDINVDATA | ICC com dados inválidos. |
| `070` | ST_CARDAPPNAV | ICC sem aplicação disponível. |
| `071` | ST_CARDAPPNAUT | Aplicação ICC não autorizada. |
| `076` | ST_ERRFALLBACK | Erro passível de fallback para tarja. |
| `077` | ST_INVAMOUNT | Valor inválido. |
| `078` | ST_ERRMAXAID | Excesso de AIDs candidatos. |
| `079` | ST_CARDBLOCKED | Cartão bloqueado. |
| `080` | ST_CTLSMULTIPLE | Múltiplos CTLS. |
| `081` | ST_CTLSCOMMERR | Erro comunicação CTLS. |
| `082` | ST_CTLSINVALIDAT | CTLS invalidado. |
| `083` | ST_CTLSPROBLEMS | CTLS com problemas. |
| `084` | ST_CTLSAPPNAV | CTLS sem aplicação. |
| `085` | ST_CTLSAPPNAUT | Aplicação CTLS não autorizada. |
| `086` | ST_CTLSEXTCVM | Verificar no dispositivo do portador. |
| `087` | ST_CTLSIFCHG | Mudança de interface. |
| `100` | ST_MFNFOUND | Arquivo multimídia inexistente. |
| `101` | ST_MFERRFMT | Erro formato multimídia. |
| `102` | ST_MFERR | Erro carga multimídia. |

> O código Go já espelha integralmente esta tabela em `internal/domain/state/status.go` (com descrições em português e `GetStatusDescription`).

---

## 6. Catálogo de comandos (Capítulo 3)

Legenda: **Impl.** = implementado em `command.Type`; **Sensível** = redigido pelo tracer (`isSensitive`); **Obs.** = obsoleto.

| Comando | § | Propósito | Impl. | Sensível | Obs. |
|---|---|---|---|---|---|
| OPN (clássico) | 3.2.1 | Aloca recursos de hardware/software. | ✅ | — | |
| OPN (seguro) | 3.2.2 | Idem + estabelece Comunicação Segura. | ✅ | — | |
| GIN | 3.2.3 | Informações gerais (predecessor do GIX). | ❌ | | obsoleto |
| GIX | 3.2.4 | Informações gerais (serial, modelo, capacidades). | ✅ | — | |
| DWK | 3.2.5 | Habilita modo PAN Criptografado. | ❌ | | |
| CLO | 3.2.6 | Libera recursos. | ✅ | — | |
| CLX | 3.2.7 | Equivalente ao CLO, com mensagem. | ✅ | — | |
| CEX | 3.3.1 | Aguarda evento (tecla/cartão/CTLS). | ❌ | | |
| CHP | 3.3.2 | Acesso direto a ICC/SAM (APDU). | ❌ | | |
| CKE | 3.3.3 | Aguarda evento (versão antiga). | ❌ | | |
| DEX | 3.3.4 | Mensagem display, formato livre. | ✅ | — | |
| DSP | 3.3.5 | Mensagem display 2×16 (fixo S32). | ✅ | — | |
| EBX | 3.3.6 | Criptografa bloco de dados (até 256 B). | ❌ | | |
| ENB | 3.3.7 | Criptografa bloco de 8 B. | ❌ | | obsoleto |
| GCD | 3.3.8 | Captura dados em claro pelo teclado. | ❌ | | |
| GDU | 3.3.9 | Obtém KSN corrente. | ❌ | | |
| GKY | 3.3.10 | Aguarda pressionamento de tecla. | ✅ | — | |
| GPN | 3.3.11 | Captura PIN (bloco). | ✅ | ✅ | |
| GTK | 3.3.12 | Obtém trilhas completas. | ✅ | ✅ | |
| MNU | 3.3.13 | Menu de até 20 opções. | ✅ | — | |
| RMC | 3.3.14 | Aguarda remoção do ICC. | ❌ | | |
| MLI | 3.4.1 | Inicia carga de arquivo multimídia. | ✅ | — | |
| MLR | 3.4.2 | Carrega bloco de arquivo multimídia. | ✅ | ✅ | |
| MLE | 3.4.3 | Finaliza carga de arquivo. | ✅ | — | |
| LMF | 3.4.4 | Lista nomes de arquivos. | ✅ | — | |
| DMF | 3.4.5 | Exclui arquivos multimídia. | ✅ | — | |
| DSI | 3.4.6 | Apresenta imagem. | ✅ | — | |
| GTS | 3.5.1 | Obtém versão das Tabelas EMV. | ❌ | | |
| TLI | 3.5.2 | Inicia carga de tabelas. | ✅ | — | |
| TLR | 3.5.3 | Carrega registros das Tabelas EMV. | ✅ | ✅ | |
| TLE | 3.5.4 | Finaliza carga de tabelas. | ✅ | — | |
| GCR | 3.6.1 | Inicia transação com cartão. | ❌ | | obsoleto |
| CNG | 3.6.2 | Abastece parâmetros EMV. | ❌ | | obsoleto |
| GOC | 3.6.3 | Continua processamento chip. | ❌ | | obsoleto |
| FNC | 3.6.4 | Finaliza processamento chip. | ❌ | | obsoleto |
| GCX | 3.7.1 | Inicia transação com cartão (ABECS). | ✅ | ✅ | |
| GED | 3.7.2 | Obtém dados EMV. | ❌ | | |
| GOX | 3.7.3 | Continua processamento chip (ABECS). | ✅ | ✅ | blocante |
| FCX | 3.7.4 | Finaliza processamento chip (ABECS). | ✅ | ✅ | |
| GEN/02/K3 | 3.8.1 | Pesquisa chaves de criptografia. | ❌ | | |
| GEN/04/01 | 3.8.2 | Controla leitor de ICC. | ❌ | | |
| GEN/04/02 | 3.8.3 | Troca APDU com ICC. | ❌ | | |
| GEN/04/03 | 3.8.4 | Captura de PIN. | ❌ | | |
| GEN/04/04 | 3.8.5 | Recupera elementos de dados. | ❌ | | |
| GEN/03/02 | 3.8.6 | Recupera elementos de dados. | ❌ | | |
| GEN/03/03 | 3.8.7 | Criptografa bloco (DUKPT:TDES:DAT#1). | ❌ | | |

**Totais:** 46 comandos documentados; 23 implementados; 7 sensíveis (MLR, TLR, GCX, GTK, GOX, FCX, GPN); ~8 obsoletos (GIN, ENB, GCR, CNG, GOC, FNC + genéricos legados).

---

## 7. Segurança (Capítulo 5)

- **Mapeamento de chaves** (5.1): DUKPT:DES e DUKPT:TDES; chaves DES removidas na 2.10; tamanho mínimo de chave elevado para 32.
- **Comunicação Segura** (5.2): OPN seguro estabelece sessão; pacotes criptografados prefixados com **`DC2` (12h)**; AES-128-CBC com IV zero, contendo `DATALEN + DATACRC + CLRDATA` (ver `internal/domain/protocol/secure.go`).
- **PAN Criptografado** (5.3): habilitação via comando DWK; codificação/decodificação do PAN.
- **End-to-End** (5.4): criptografia de trilhas, mascaramento.

> No código Go, `service.go` redige **qualquer** frame quando há sessão segura ativa (`setTraceCommand` → `secureSession != nil`).

---

## 8. Correspondência com o código Go existente

| Conceito do PDF | Implementação Go |
|---|---|
| Bytes de controle | `internal/domain/protocol/protocol.go` (SYN/ETB/ACK/NAK/EOT/CAN/DC3/DC2) |
| Enquadramento + CRC | `protocol.BuildPacket` / `ValidatePacket`; `internal/utilitario/crc/crc.go` (CRC16CCITT) |
| Substituição DC3 | `protocol.ApplySubstitution` / `RemoveSubstitution` |
| Formato comando (TLV) | `command.BuildABECSPayload`; `protocol.BuildAbecsPayload` |
| Formato resposta (TLV) | `parser.ParseAbecsResponse`; `parser.ParseNotification` (NTM) |
| `SPE_xxx` | `command/constants.go` (`SPEParameter`) |
| `RSP_DATID` | `protocol/constants.go` (`ResponseTag`); `parser/abecs.go` (`tagNames`) |
| `RSP_STAT` | `state/status.go` (com descrições) |
| Comandos | `command/commands.go` (`Type`, 24 constantes) |
| Sessão segura | `protocol/secure.go` (`SecureSession.Protect/Unprotect`) |
| Logging + redação | `logging/logging.go` (`Tracer`, `isSensitive`, `RedactPayload`) |

### Discrepâncias a reconciliar no interpretador semântico
1. `0024h`/`0025h`: código usa `PinBlockMode/PinBlockExponent`; PDF = `SPE_PBKMOD/PBKEXP` (chave pública RSA).
2. `8032h`/`8033h`: código usa `MerchantDescription/MerchantData`; PDF = `PP_MKTDESP/PP_MKTDESD` (mapas de chave).
3. Comandos do PDF ainda não implementados (22): ver seção 6.

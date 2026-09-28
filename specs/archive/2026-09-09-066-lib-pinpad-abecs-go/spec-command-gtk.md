# SPEC: 066-lib-pinpad-abecs-go — Comando GTK

Autor: Rômulo Penha

## Status
`SPEC_APROVADA`

## Objetivo
Definir a obtenção das trilhas completas e dos KSNs relacionados, quando permitidos pelo perfil ABECS e pelo legado convertido.

## Contrato

- `GTK` só pode ser chamado uma vez após `CEX` ou `GCX` concluído com sucesso;
  nova chamada ou ausência dessa pré-condição deve resultar em `ST_INVCALL`.
- `SPE_TRACKS` seleciona PAN e trilhas 1, 2 e 3; ausente, solicita todas as
  informações conhecidas pelo pinpad. PAN após `GCX` somente é elegível para
  cartão com chip quando o objeto equivalente estiver disponível.
- `SPE_MTHDDAT` seleciona retorno em claro, MK/WK, DUKPT ou chave aleatória,
  conforme valores permitidos pelo manual. `SPE_IVCBC` só é aceito nos modos
  CBC; `SPE_OPNDIG` preserva quantidade par de dígitos iniciais em claro.
- `SPE_KEYIDX` é obrigatório para método diferente de chave aleatória;
  `SPE_WKENC` é obrigatório para os modos MK/WK; `SPE_PBKMOD` e `SPE_PBKEXP`
  são obrigatórios para método com chave pública. O builder deve rejeitar
  combinações inconsistentes antes de escrever na serial.
- A resposta contém `RSP_ID=GTK`, `RSP_STAT` e, quando disponíveis e
  solicitados, `PP_ENCPAN`, `PP_TRACK1`, `PP_TRACK2`, `PP_TRACK3` e os KSNs
  correspondentes. `PP_ENCKRAND` só é aplicável ao método de chave aleatória.
- `ST_OK` sem dados de cartão é resposta válida quando a leitura magnética
  anterior não disponibilizou trilhas. GTK é separado de GCX; seus dados não
  devem preencher `GCXResponse`.
- O comando só pode ser exposto por método tipado e autorizado; não criar
  `SendRawCommand`.

## Segurança
Por padrão, nunca registrar trilhas, KSN, PAN, PIN block ou chaves em texto,
hexadecimal, erro ou métrica. Redaction integral deve ocorrer no tracer. A
única exceção é o modo em claro escolhido explicitamente na opção 19 do
utilitário local de laboratório, conforme CA-GTK-CLI-004; essa exceção não
remove a redaction do rastro serial bruto e não se aplica ao modo criptografado
nem à biblioteca usada por outros consumidores.

## Critérios de aceite

- [ ] Builder valida pré-condição, seleção de trilhas, método, índice, IV,
  WKENC e chave pública conforme aplicável.
- [ ] Parser mapeia resposta vazia válida, PAN/trilhas e KSNs sem conversão
  textual destrutiva ou atribuição a GCX.
- [ ] Dados sensíveis não aparecem em logs capturados fora da linha
  `GTK_CLEAR` autorizada pelo operador no utilitário local.
- [ ] Timeout, cancelamento, NAK e status são distinguíveis.
- [ ] Validação usa pinpad físico e cartão de laboratório.

## Referências
`spec-command-gcx.md`, `spec-logging.md`, `spec-protocolo-seguro.md`; manual
ABECS v2.12, seção 3.3.12.


## Complemento normativo de 2026-09-13

DataMethod pode estar ausente para devolver as trilhas em claro ou ser 00, 01,
10, 11, 30, 40, 50, 51, 90 ou 91. O método 40 representa DUKPT:TDES:DAT
variante 2 em ECB e aparece no vetor publicado da página 86. IV é opcional em
CBC e vale zero quando ausente. Índice, WKENC e chave pública RSA são exigidos
conforme o método da seção 3.3.12.

## Seleção no utilitário local — 2026-09-13

A opção 19 deve perguntar se as trilhas serão obtidas em claro ou
criptografadas antes de executar GTK:

- em claro, o utilitário envia o payload literal `GTK000`, sem `SPE_TRACKS`,
  `SPE_MTHDDAT` ou `SPE_KEYIDX`; a ausência dos campos solicita todas as
  informações conhecidas e retorno em claro, conforme as páginas 84–85;
- criptografado, o utilitário usa DUKPT TDES DAT#3 em ECB (`SPE_MTHDDAT=50`),
  solicita todas as informações (`SPE_TRACKS=1111`) e pergunta o índice N2 da
  chave DUKPT previamente carregada no pinpad;
- escolha ou índice inválido deve ser rejeitado antes de escrever na serial;
- `ST_ERRKEY` (`042`) na opção criptografada deve continuar sendo devolvido,
  pois indica que a chave MK/DUKPT não existe no índice informado.

### Critérios de aceite do utilitário

- [x] CA-GTK-CLI-001: escolha “em claro” produz `GTK000` e não pergunta índice.
- [x] CA-GTK-CLI-002: escolha “criptografado” produz método 50, seleção 1111 e
  índice N2 informado pelo operador.
- [x] CA-GTK-CLI-003: modo diferente de 1/2 e índice fora de 00..99 são
  recusados antes de `GetTracks`.
- [x] CA-GTK-CLI-004: após GTK `000` no modo em claro, o utilitário grava no
  arquivo ativo uma linha `GTK_CLEAR` com `TRACK1`, `TRACK2` e `TRACK3`,
  incluindo campos vazios; no modo criptografado, esses campos não são
  gravados.
- [x] CA-GTK-CLI-005: na linha `GTK_CLEAR`, `TRACK1` é interpretada como ASCII
  e `TRACK2`/`TRACK3` são decodificadas nibble a nibble conforme a seção
  5.4.2.2: `0`–`9` viram dígitos, `D` vira `=` e somente `F` ao final é
  removido como filler. Nibble inválido deve causar erro em vez de produzir uma
  trilha ambígua.

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
Nunca registrar trilhas, KSN, PAN, PIN block ou chaves em texto, hexadecimal, erro ou métrica. Redaction integral deve ocorrer no tracer.

## Critérios de aceite

- [ ] Builder valida pré-condição, seleção de trilhas, método, índice, IV,
  WKENC e chave pública conforme aplicável.
- [ ] Parser mapeia resposta vazia válida, PAN/trilhas e KSNs sem conversão
  textual destrutiva ou atribuição a GCX.
- [ ] Dados sensíveis não aparecem em logs capturados.
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

# SPEC: 066-lib-pinpad-abecs-go — Comando FCX

## Status
`SPEC_APROVADA`

## Objetivo
Definir a finalização da transação e a obtenção dos resultados de scripts do emissor, quando o perfil ABECS exigir `FCX`.

## Contrato

- `FCX` finaliza o processamento após GOX. É obrigatório quando `PP_GOXRES`
  requer autorização online; após aprovação ou negação offline, só pode ser
  usado quando a regra da rede credenciadora exigir manutenção por Issuer
  Scripts.
- `SPE_FCXOPT` é obrigatório e representa a resposta da rede credenciadora.
  `SPE_ARC` é obrigatório para aprovação ou negação; `SPE_EMVDATA` pode conter
  TLV de autenticação e scripts do emissor; `SPE_TAGLIST` define os objetos
  EMV solicitados; `SPE_TIMEOUT` limita a reapresentação necessária em CTLS.
- A resposta contém `RSP_ID=FCX`, `RSP_STAT` e `PP_FCXRES`. Quando
  `SPE_TAGLIST` foi enviado, `PP_EMVDATA` é obrigatório inclusive se vazio.
  `PP_ISRESULTS` somente é retornado quando o comando recebeu Issuer Scripts.
- O parser preserva TLV e resultados de scripts no modelo próprio de FCX;
  Issuer Script Results não podem preencher `GCXResponse` nem a resposta GOX.
- A reapresentação CTLS pode tornar a operação bloqueante e deve respeitar
  contexto, timeout e cancelamento.

## Segurança
EMV, PAN, trilhas, scripts e respostas criptográficas devem ser classificados por campo. O tracer deve aplicar redaction integral quando houver qualquer campo sensível.

## Critérios de aceite

- [ ] Builder valida `SPE_FCXOPT`, obrigatoriedade condicional de `SPE_ARC`,
  TLV, lista de tags e timeout.
- [ ] Parser cobre resultado aprovado, negado, EMV solicitado vazio e Issuer
  Script Results somente quando aplicável.
- [ ] FCX real é executado em cartão de laboratório compatível.
- [ ] Issuer Script Results aparecem somente no modelo próprio de FCX.
- [ ] GCXResponse permanece sem campos de FCX.
- [ ] Timeout, cancelamento e status são registrados sem vazamento.

## Referências
`spec-command-gcx.md`, `spec-command-gox.md`, `spec-logging.md`; manual
ABECS v2.12, seção 3.7.4.

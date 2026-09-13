# SPEC: 066-lib-pinpad-abecs-go — Comando FCX

Autor: Rômulo Penha

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


## Complemento normativo de 2026-09-13

FCX usa opções N4 com decisão 0, 1 ou 2 e três zeros. ARC A2 é obrigatório nas
decisões 0 e 1. A resposta exige PP_FCXRES N3 e PP_EMVDATA quando solicitado.

## Correção do fluxo FCX no utilitário local — 2026-09-13

Depois de um `GOX` válido, a opção 21 deve conservar `PP_GOXRES` e perguntar o
resultado efetivamente obtido na comunicação com a Rede Credenciadora. A
escolha é convertida sem valores fixos:

| Resultado informado | `SPE_FCXOPT` | `SPE_ARC` |
| --- | --- | --- |
| Transação aprovada pela rede | `0000` | obrigatório, `A2` |
| Transação negada pela rede | `1000` | obrigatório, `A2` |
| Comunicação malsucedida ou sem resposta válida | `2000` | ausente |

O utilitário também permite informar, em hexadecimal, `SPE_EMVDATA` e
`SPE_TAGLIST`, além do `SPE_TIMEOUT` binário entre 1 e 255 segundos. Entradas
hexadecimais inválidas, ARC diferente de dois caracteres ASCII entre 20h e 7Eh,
timeout fora do intervalo e execução sem um GOX válido devem ser recusados
antes da comunicação serial. O contexto de 60 segundos começa somente depois
da coleta das entradas.

`PP_FCXRES` é campo de resposta obrigatório do pinpad. Ele não é perguntado ao
operador nem enviado no comando e deve ser exibido após a finalização válida.
O status `047` observado no equipamento não consta do catálogo de status da
versão 2.12; o código bruto deve ser preservado como retorno desconhecido, sem
atribuir significado normativo.

### Critérios adicionais de aceite

- [x] A opção 21 exige um GOX válido anterior e conserva seu `PP_GOXRES`.
- [x] Aprovação, negação e falha de comunicação produzem, respectivamente,
  `SPE_FCXOPT` igual a `0000`, `1000` e `2000`.
- [x] `SPE_ARC` é coletado somente para aprovação ou negação e tem `A2`.
- [x] Os campos opcionais são decodificados e validados antes de `FinalizeEMV`.
- [x] A resposta válida exibe `PP_FCXRES` e encerra o contexto FCX conservado.
- [x] Testes byte a byte comprovam a ausência de `SPE_ARC` na falha de rede e
  que `PP_FCXRES` nunca é serializado como entrada.

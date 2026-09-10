# SPEC: 066-lib-pinpad-abecs-go — Comando FCX

## Status
`RASCUNHO`

## Objetivo
Definir a finalização da transação e a obtenção dos resultados de scripts do emissor, quando o perfil ABECS exigir `FCX`.

## Contrato
- FCX é separado de GCX; Issuer Script Results não devem ser preenchidos pelo parser de GCX.
- Definir sequência com os comandos anteriores e condições de envio conforme manual.
- Parser deve preservar status, tags e dados brutos permitidos.
- Falha de FCX deve ser distinguível de falha da captura GCX.

## Segurança
EMV, PAN, trilhas, scripts e respostas criptográficas devem ser classificados por campo. O tracer deve aplicar redaction integral quando houver qualquer campo sensível.

## Critérios de aceite
- [ ] Sequência FCX é confirmada pelo manual e pelo legado.
- [ ] FCX real é executado em cartão de laboratório compatível.
- [ ] Issuer Script Results aparecem somente no modelo próprio de FCX.
- [ ] GCXResponse permanece sem campos de FCX.
- [ ] Timeout, cancelamento e status são registrados sem vazamento.

## Referências
`spec-command-gcx.md`, `spec-logging.md`; manual ABECS v2.12, seção de FCX.

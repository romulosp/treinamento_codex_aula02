# Perfil de laboratório — provisionado em 2026-10-11

O modelo pendente abaixo permanece histórico. Após a escolha explícita APPROVE
pelo solicitante, o perfil foi salvo em
`D:\desenvolvimento\chave_des\perfil_saque_cartao_des.json` e provisionado no
host debug como `files/saque-cartao/perfil.json`, junto à tabela `emv.json`.
Hashes locais/remotos conferidos. Perfil efetivo:

```json
{
  "schemaVersion": 1,
  "environment": "DES",
  "host": "localhost",
  "port": 39100,
  "timeoutMillis": 30000,
  "timeoutSeconds": 30,
  "tableAcquirer": "04",
  "tableVersion": "DES873D94F",
  "maxBaseCents": 876117049236,
  "gtkDataMethod": "30",
  "gtkKeyIndex": 8,
  "scenario": "APPROVE"
}
```

Versão derivada do hash da tabela; limite técnico garante N12 com a maior taxa,
sem estabelecer política comercial. DATA 08 segue a indicação do solicitante;
chave instalada ainda depende do teste físico. APPROVE é simulação somente
quando GOX=2, sem forçar aprovação de resultados offline ou falhas reais.

## Modelo anterior à escolha

Modelo para completar; não é executável com campos null. Nenhum arquivo foi
provisionado no Android. Porta serial será lida de PORTA_PINPAD. A conexão
localhost:39100 usa o reverse já verificado. Adquirente 04 vem da tabela DES.
Modalidade/valor vêm da tela 1; filtros não são campos de configuração.
DATA 08 é indicação provisória do solicitante; não confirma a chave instalada.
Versão, limite, prazos e cenário precisam de valores aprovados.

```json
{
  "schemaVersion": 1,
  "environment": "DES",
  "host": "localhost",
  "port": 39100,
  "timeoutMillis": null,
  "timeoutSeconds": null,
  "tableAcquirer": "04",
  "tableVersion": null,
  "maxBaseCents": null,
  "gtkDataMethod": "30",
  "gtkKeyIndex": 8,
  "scenario": null
}
```

PIN: método 3, índice 08, working key vazia, conforme arquivo externo recebido;
nenhum material criptográfico deve ser colocado neste modelo.

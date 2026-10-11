# Correções de conformidade — REV-011

Data: 2026-10-11. Decisão: `SPEC_APROVADA` para corrigir IMP-REV-001/002/003,
dentro dos requisitos já aprovados RF-001 e REV-010, na continuação solicitada.

ADR-10004-02: expor a função gomobile estática
`ValidateFormattedEMVTableJSON(acquirer, version, recordsJSON)`.
É validação local sem cliente, transporte ou I/O; reutiliza parser de JSON,
BuildTLICommand, ValidateFormattedEMVRecords e BuildFormattedTLRCommand do Go.
O adapter executa essa validação antes de publicar configReady; JSON inválido,
array vazio, adquirente divergente e registro inválido geram
CONFIGURATION/preflight. Não duplicar regras ABECS em Kotlin. A validação
durante a carga permanece; não há novo endpoint, comando ou mudança do Bridge.

Entrada monetária aceita dígitos e separadores da máscara, bloqueando sinais,
texto e excesso de dígitos sem truncar/converter em outra compra. O cálculo
continua exato em centavos, crédito percentual e débito com R$ 1,00 fixo.
Consolidar contrato, KDoc e métrica atual 94,2% da REV-010.

Validar Go, unidade/estado, adapter Android com arquivos sintéticos, UI, lint,
cobertura, AAR e carregamento nativo. Não aprovar hardware ou FCX pela revisão.
Índice DATA 08 indicado pelo solicitante de forma provisória; não comprova
provisionamento da chave. Versão da carga e cenário FCX permanecem pendentes.

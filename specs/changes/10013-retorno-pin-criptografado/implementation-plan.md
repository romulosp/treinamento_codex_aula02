# Plano de implementação — 10013

Após SPEC_APROVADA: core aditivo CapturePaymentPINResult (buffers8/10); wrapper
antigo limpa. Mobile PaymentResult com Result/PINBlock/KSN/Clear e APIs
CapturePaymentPINResult/ContinuePaymentEMVResult; ParsePaymentPINConfiguration
com Method/KeyIndex/WorkingKey. Nenhuma assinatura nova recebe PAN.

Kotlin PaymentPinConfiguration(method,keyIndex,workingKey) importa três getters;
SaquePerfil ganha boolean opcional logPinResults. Adapter consome handle novo,
limpa em finally e registra apenas par completo no logger privado bounded,
debug/DES/noBackup. UI/porta de jornada seguem decisão/Unit existentes.

Go: testes result getters/clear/concorrência, parser tipado, integração Bridge
com GPN e GOX online/offline; manter testes API anterior. Android: preflight
boolean estrito, logger com sintéticos e opt-in/desabilitado/partial/bound.
Go test/vet/coverage, AAR bind/proveniência e JVM/lint/build/instrumentados.
Race pode continuar indisponível sem compiladorC. Nenhum hardware transaction
repetido para produzir amostra; habilitar somente flag no perfil externo DES e
provisionar host com script10012 após gates. Não imprimir log criptográfico real.

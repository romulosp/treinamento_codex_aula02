# Expansão da bilheteria

Status: `SPEC_APROVADA`

Aprovação: solicitação explícita do usuário em 2026-11-08: PLEASE IMPLEMENT THIS PLAN.

Cinema: 180 posições A–L, 1–15, blocos 5+5+5. A1/A3/A13/A15 PCD; A2/A4/A12/A14 acompanhantes independentes. L1–L2 e L14–L15 namoro inseparável, preço de dois ingressos. Mapa ampliável, rolagem e toque mínimo 48dp. Catálogo Cinema/Teatro/Shows/Todos com apresentação fixa, data/local/horário/preço informativos. Pedido de uma apresentação combina assentos e pista, total Long em centavos limitado a 999999999999. Pedido congelado; vendas atômicas e idempotentes somente APPROVED/CAPTURED + cleanup. Sucesso demonstrativo por 3s; estoque em memória preservado no novo atendimento. Sem backend, persistência, desconto ou autorização financeira real.

Baseline: módulo plugin-ingressos, package existente, Compose, compileSdk 37/minSdk 29/targetSdk 36 e dependências atuais sem atualização. Contrato SharedApi 1.5 inalterado. Critérios verificáveis: geometria, especiais, seleção múltipla, pista/capacidade, isolamento, callbacks, cancelamento e retorno; JVM, Compose, integração, lint, APK e cobertura ≥80%.

# Expansão da bilheteria

Status: `SPEC_APROVADA`

Aprovação: solicitação explícita do usuário em 2026-11-08: PLEASE IMPLEMENT THIS PLAN.

Cinema: 180 posições A–L, 1–15, blocos 5+5+5. A1/A3/A13/A15 PCD; A2/A4/A12/A14 acompanhantes independentes. L1–L2 e L14–L15 namoro inseparável, preço de dois ingressos. Mapa ampliável, rolagem e toque mínimo 48dp. Catálogo Cinema/Teatro/Shows/Todos com apresentação fixa, data/local/horário/preço informativos. Pedido de uma apresentação combina assentos e pista, total Long em centavos limitado a 999999999999. Pedido congelado; vendas atômicas e idempotentes somente APPROVED/CAPTURED + cleanup. Sucesso demonstrativo por 3s; estoque em memória preservado no novo atendimento. Sem backend, persistência, desconto ou autorização financeira real.

Baseline: módulo plugin-ingressos, package existente, Compose, compileSdk 37/minSdk 29/targetSdk 36 e dependências atuais sem atualização. Contrato SharedApi 1.5 inalterado. Critérios verificáveis: geometria, especiais, seleção múltipla, pista/capacidade, isolamento, callbacks, cancelamento e retorno; JVM, Compose, integração, lint, APK e cobertura ≥80%.

## Critérios de aceite
- CA-01: cinema gera 180 posições únicas A1–L15 e corredores após 5/10; PCD, acompanhantes e pares namoro conforme plano aprovado.
- CA-02: seleção/desseleção de um casal altera dois ingressos; PCD e acompanhante permanecem independentes.
- CA-03: categorias Todos/Cinema/Teatro/Shows, carrossel animado, apresentação fixa e dados informativos sem edição de horário.
- CA-04: teatro demonstrativo com 60 lugares; show com 40 lugares, Pista 100 a R$ 50 e VIP 40 a R$ 120, assento a R$ 90. Dados fictícios.
- CA-05: um pedido da mesma apresentação combina lugares e quantidades, congela linhas/preços/total/ID, respeita ocupações e estoque.
- CA-06: retorno APPROVED/CAPTURED com cleanup confirma todos os itens uma vez; falhas/cancelamento não vendem; após 3s novo atendimento mantém estoque.
- CA-07: mapa com numeração, legenda, semântica de tipo/estado e vínculos, alvos 48dp, rolagem e ampliação; acesso compacto e fonte ampliada.
- CA-08: testes JVM, Compose, integração sintética e APK; lint sem erros e cobertura elegível mínima 80%. Registrar limitações do validador sem ocultá-las.

## Compatibilidade
Plugin 1.2.0/versionCode 3; ID e menu existentes preservados; SharedApi permanece 1.5. Modelos Cinema* internos continuam como ponto de compatibilidade do módulo, representando também outros eventos. Nenhuma instalação de dependência, importação de skill externa ou mudança de SDK.

# Modalidade escolhida na jornada — REV-012

Data: 2026-10-11. Decisão: `SPEC_APROVADA`, pela orientação explícita do
solicitante: os filtros vêm da modalidade selecionada na tela 1, assim como
o valor da compra é digitado nessa tela; não são campos do operador no perfil.
O solicitante confirmou explicitamente os valores: Crédito = 1 e Débito = 2,
formatados no protocolo como `01` e `02`.

O manual local ABECS 2.12, seção 4.1.1, define T1_APPTYPE como N2 configurado
pela tabela (01..98); seção 3.7.1 relaciona SPE_APPTYPE a T1_APPTYPE. Não há
equivalência universal crédito=01/débito=02 no manual.

Na tabela DES recebida (SHA-256 873d94fa1ec3be17d5a58d130f1ab07b82367eee01c05b4745218c14ebad0ab2),
os 15 registros AID contêm somente os tipos 01 (8 registros) e 02 (7 registros).
Os rótulos explícitos de modalidade associam 01 a crédito (2 rótulos; outros
6 não classificam modalidade) e 02 a débito (7 rótulos), sem associação
contraditória. Essa correspondência é específica deste laboratório, não dedução
por bandeira ou regra universal do protocolo.

O domínio SaqueModalidade declara essa correspondência DES. O parser gera os
filtros internos a partir do domínio; creditApplicationTypes/debitApplicationTypes
legados no JSON são ignorados e deixam de ser exigidos pelo launcher.
O resumo congela a seleção e o adapter usa seu filtro no GCX. O valor/total
continua vindo da tela, respeitando o limite técnico/operacional configurado.
Testes demonstram seleção→01/02, total N12 e parser independente dos filtros
externos. AAR/Bridge e o contrato da validação local não mudam nesta revisão.

Conferência direta do PDF local solicitada em 2026-10-11: página 35 define
SPE_APPTYPE (`0011h`) como N..20, composto por 1 a 10 identificadores de dois
dígitos (01..98). A seção 3.7.1, página 129, recebe os valores de T1_APPTYPE;
a seção 6.9.1, página 243, filtra os registros candidatos por esses valores.
SPE_AIDLIST tem precedência sobre os filtros; a jornada envia a lista vazia,
portanto SPE_ACQREF e SPE_APPTYPE são utilizados. SPE_TRNTYPE é outro campo:
X1, tipo da operação, independente da escolha crédito/débito. A jornada
aprovada atualmente mantém saque (01h); a modalidade altera somente APPTYPE.
`go test ./internal/domain/command -run 'TestFilteredGCX' -count=1`: PASS,
exit 0. O teste confere IDs, tamanhos e bytes dos campos no payload.

Versão A10, limite, prazos e cenário FCX não constam dos arquivos recebidos;
não preencher como se fossem parâmetros aprovados. Índice DATA 08 permanece
indicação provisória do solicitante. Não comprova chaves, PIN ou hardware.

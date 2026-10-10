# Contrato executável de homologação — 10004

## Decisão arquitetural (ADR-10004-01)

A jornada usa o Go da 074. Esta Change inclui duas extensões aditivas nesse
componente, necessárias aos requisitos originais de modalidade e carga da
tabela indicada. Bridge/PBRG, APIs existentes e `TransactionGCX` reservado
permanecem com seus contratos. Não há novo endpoint nem comando raw Kotlin.

### Captura filtrada

`PurchaseGCXFilteredSummaryJSON(operationID, amount, date, clock,
enableCTLS, hideAmount, transactionType, acquirerReference,
applicationTypes, aidReferences, timeoutSeconds)`.

Tipos gomobile: strings, bools e int. A jornada envia transactionType inteiro
1 (saque), não string hexadecimal. Campos financeiros N12, data AAMMDD e
hora HHMMSS; timeout de inatividade 1..255 segundos. O core valida antes de
I/O, serializa SPE_TRNTYPE como byte e preserva parser, elegibilidade de
GTK/GOX/FCX e fallback normativo existente em todas as tentativas.

Usar uma estratégia: adquirente N2 01..99 + tipos de aplicação concatenados
N2 01..98 (1..10); ou referências AARR (até 128, adquirente 01..99,
índice de registro 01..ZZ). Rejeitar mistura, vazio, duplicatas e dados
inválidos. As referências pertencem à tabela, não são AIDs hexadecimais.
O kernel pesquisa AID base/estendido dentre os candidatos. A seleção crédito
ou débito usa filtros distintos fornecidos pelo perfil, nunca inferidos pelo
nome da bandeira. Retorno seguro igual ao GCX resumido existente.

### Importação da tabela

`LoadFormattedEMVTableJSON(operationID, acquirer, version, recordsJSON)`:
array JSON de strings; aceitar comentários somente em linhas inteiras com
`//`, conforme arquivo externo. Validar integralmente antes de TLI: Latin-1,
TAB_LEN N3 incluindo o prefixo, TAB_ID 1/2, adquirente correspondente, índice
de registro e unicidade TAB_ID/TAB_ACQ/TAB_RECIDX; rejeitar vazio, formatos
inválidos e registros maiores que um bloco. Versão A10 alfanumérica.
TLR novo preserva cada registro completo byte a byte, prefixando somente
NREC N2; dividir em blocos de até 999 bytes incluindo NREC, máximo 99
registros. TLI/TLR/TLE usa o serviço/fila existente; falha aborta sem GCX.
Não alterar semanticamente a API antiga de carga nem corrigir framing Kotlin.

## Perfil externo e provisionamento

Arquivo JSON UTF-8 `saque-cartao/perfil.json` no armazenamento privado do
processo host, e tabela `saque-cartao/emv.json` no mesmo diretório. Provisionar
somente no build debug por launcher/ADB `run-as`, com nomes fixos validados,
sem conteúdo no console, sem tabela no APK/repositório. Ler pelo Context host
fornecido à View, mantendo tabelas fora de estado UI. Perfil ausente/inválido
bloqueia comandos físicos e informa configuração de homologação incompleta.

Schema versão 1: `environment="DES"`, `host` local (localhost/127.0.0.1 ou
override 10.0.2.2), `port` 1..65535, `timeoutMillis` 100..120000,
`timeoutSeconds` 1..120, `tableAcquirer` N2 01..99, `tableVersion` A10,
`creditApplicationTypes`, `debitApplicationTypes` nos formatos acima,
`feePercent` decimal 0..100, `maxBaseCents` inteiro positivo até N12,
`gtkDataMethod` e `gtkKeyIndex` definidos separadamente do PIN,
`scenario` APPROVE/DENY/COMMUNICATION_ERROR. Validar filtros não vazios e
diferentes. Configuração de PIN fixa: método 3, índice 8, WK vazia.
O formato GTK será restrito ao método DUKPT TDES DAT#1 ECB `30` do manual local,
sem PAN/trilhas em claro, openDigits 0 e sem material WK/RSA/IV externo.
O índice DATA deve ser explicitamente informado; não deduzir índice 8.

Taxa é parâmetro obrigatório, sem tarifa de produção presumida. Cálculo:
tarifa = base × taxa / 100, HALF_UP em centavos; total = base + tarifa.
Validar limite configurado e N12 também após somar tarifa. Crédito 1..12,
débito 1 parcela nesta homologação; sem alegação de juros/condições reais.

## Sequência física e resultados

NewClient → Ping → Open (sessão clássica suportada; E2E da seção 5.4 não
significa obrigatoriamente OpenSecure) → consulta capacidades → carga EMV →
resumo pinpad → confirmação real → GCX filtrado → processamento → cleanup.

Display de pelo menos 4 linhas e largura suficiente: DEX com total, parcelas,
tarifa e instrução OK/CANCELA. Displays pequenos: DSP mostra três páginas,
com GKY OK para cada uma (total; quantidade; tarifa); somente o último OK
confirma o resumo completo. Cancelar em qualquer página cancela jornada;
outras teclas encerram com erro de confirmação, sem avançar. Valores devem
caber, sem truncamento silencioso.

Tarja: GCX → GPN DUKPT(PAN vazio) → GTK criptografado resumido. CTLS simulando
tarja: GTK, sem impor GPN. ICC/CTLS EMV: GTK resumido → GOX → FCX somente
se GOX requer autorização online (resultado 2). Todos usam
o mesmo resumo congelado; não há retentativa de jornada automática.
GOX existente recebe pinMethod3/index8/WKvazia, transactionType byte U+0001,
amount total N12, moeda da tabela (campo currencyHex ausente), cashback vazio,
options01000 (impedir aprovação offline em ICC; não garante online em CTLS),
demais blobs opcionais vazios.
UI GOX: “Siga as instruções no pinpad; digite a senha se solicitada”. A API
resumida não comprova instante de solicitação PIN, portanto não simular evento.

FCX cenário de teste explicitamente configurado: APPROVE opções0000/ARC00;
DENY opções1000/ARC05; COMMUNICATION_ERROR opções2000/ARC vazio. Códigos são
respostas simuladas de homologação, nunca autorização de adquirente.
Se GOX retorna 1, concluir negado sem autorização simulada; se retorna 0,
concluir decisão offline de homologação, sem FCX de aprovação online; se
retorna 2, executar FCX no cenário configurado. Mapear decisão FCX real
(primeiro dígito0/1), não
ausência de exception. Não EMV termina “Captura de homologação concluída”,
sem declarar aprovação financeira. Todas as conclusões dizem homologação e
não autorizam dinheiro. Resultado desconhecido/timeout/desconexão após envio
é indeterminado; cancelamento confirmado é cancelado. Erro pré-envio é erro.

## Entrega para teste humano

Implementar, executar testes técnicos e parar em IMPLEMENTADA. Teste físico,
revisão da implementação, validação formal, aprovação, archive e commit
ficam para continuação solicitada após avaliação humana. A falta de evidência
física não impede aprovar este contrato; impede declarar hardware validado.

Referência normativa: [manual local fornecido pelo solicitante](../../../docs/Pinpad-Abecs-Protocolo-de-Comunicacao-e-Funcionamento-v212190411212/Pinpad-Abecs-Protocolo-de-Comunicacao-e-Funcionamento-v212190411212.pdf)
e [ABECS 2.12](https://api.abecs.org.br/wp-content/uploads/2021/08/Pinpad-Abecs-Protocolo-de-Comunica%C3%A7%C3%A3o-e-Funcionamento-v212190411.pdf),
seções 3.7.1, 3.5.2/3.5.3, 3.3.11 e 5.4. Evidências da revisão anterior
permanecem históricas; este contrato resolve o escopo das extensões.

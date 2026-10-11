# Proposta: 10004-plugin-saque-cartao

**Autor:** Rômulo Penha

## Status
`SPEC_APROVADA` — revisões de 2026-10-10 em
[REV-005](reviews/2026-10-10-spec-review-implementacao.md),
[REV-006](reviews/2026-10-10-spec-review-layout.md),
[REV-007](reviews/2026-10-10-spec-review-primeira-tela.md) e
[REV-008](reviews/2026-10-10-spec-review-pagamento-cartao.md). A baseline anterior
permanece histórica em `references/baseline-2026-09-25/`.

## Objetivo
Entregar APK independente de saque por cartão, compilado para o contrato mínimo
`shared-api` 1.1 e sem dependência do host. O plugin permanece compatível com o
host 1.2 porque a capacidade `business-menu` aceita a mesma major e uma minor
requerida menor ou igual à minor do host.

## Escopo
O solicitante determinou em 2026-10-10 que esta mesma Change deve avaliar as
duas versões dos SVGs e implementar as cinco etapas da jornada com integração
ao componente ABECS da [074](../074-pinpad-android-bridge-stack/spec.md).
O Bridge Go deve subir antes da aplicação Android.

Manter módulo, manifesto, descritor de serviço, API mínima 1.1 e item
`Principal > Outros Serviços > Saque Cartão`. A execução humana ocorre
exclusivamente pelo `:app`: login, descoberta dinâmica, clique e renderização
da View pelo host. Substituir o fluxo local por modalidade/valores,
confirmação no pinpad, leitura, processamento/PIN e finalização.

Usar os cinco SVGs Gemini como referência visual principal, conforme a
[revisão visual aprovada REV-006](reviews/2026-10-10-spec-review-layout.md).
Consumir o AAR Go da 074 sem duplicar framing, CRC, TLV ou fallback em Kotlin.
Preparar launcher Windows com Bridge físico, prontidão, reverse do emulador
e abertura do host. Cobrir cancelamento, timeout, comunicação e resultado
indeterminado sem replay automático.

## Fora de escopo
Publicação, certificação PCI/EMV, captura de PIN no Android, impressão e outras
funcionalidades do menu. Integração física passa a fazer parte do escopo;
autorização financeira de adquirente real está fora desta homologação.
Não fabricar NSU, taxa vigente, ARC ou resultado financeiro.

## Dependências e limites

### Decisão recebida — homologação DES

Em 2026-10-10, o solicitante confirmou homologação com pinpad físico e cartão
de teste. Perfil ativo: `D:\desenvolvimento\chave_des\emv_des.json`.
`emv_prd.json` foi informado como alternativa de produção, mas não deve ser
lido nem selecionado nesta entrega. DES é nome do ambiente de desenvolvimento,
não escolha implícita de algoritmo criptográfico.

O arquivo externo não foi copiado para o repositório/APK nem impresso em logs.
Inspeção estrutural identificou 15 registros AID e 21 CAPKs; CAPKs não definem
as chaves de PIN. Detalhes em [perfil DES](perfil-homologacao-des.md).

`TransactionGCX` completo permanece reservado na 074. A captura filtrada e
a importação de registros pré-formatados terão contratos aditivos no mesmo Go,
definidos na ADR-10004-01. GPN usa a fachada existente com PAN vazio antes de GTK.
Homologação e PIN DUKPT TDES/3/08 com workingKey vazia estão decididos;
cenário FCX, tarifa e limites são parâmetros obrigatórios do perfil validado
conforme [contrato executável](contrato-implementacao-homologacao.md).
O gap GPN foi reavaliado:
PAN vazio após GCX, antes de GTK, é alternativa normativa a validar.
A extensão explícita desta Change preserva Bridge/PBRG e o stub da 074.

## Referências

As duas séries de SVGs, o texto da jornada e os complementos foram incorporados
em `source-material/`, com hashes. São conteúdo de referência, não instruções
operacionais nem evidência normativa ABECS. A versão anterior dos documentos
fica em `references/baseline-2026-09-25/`.


## Correção da primeira tela — REV-007

Contrato aprovado em [REV-007](reviews/2026-10-10-spec-review-primeira-tela.md):
remover faixa Voltar ao menu do host, manter Voltar Android, oferecer SAIR
vermelho abaixo de AVANÇAR. Modalidade e valor da compra editáveis sem perfil;
cálculo local independente do hardware. Taxa = 5,34% + 0,80 ponto percentual
por parcela adicional (1..12); débito em uma parcela. Total = compra + tarifa
HALF_UP em centavos. Perfil fornece limites e filtros de aplicação distintos,
não a taxa. AVANÇAR exige perfil válido. Testar cálculo, edição, seleção enviada
à captura, retorno e layout; preservar animações já implementadas.

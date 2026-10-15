# SPEC — 10015

Status: `SPEC_APROVADA` pela [REV-001](reviews/2026-10-14-spec-review.md).

## Requisitos REQUIRED

RF-01: perfil privado DES define explicitamente `gtkDataMethod`: string vazia
`""` seleciona GTK em claro; `"30"` seleciona DUKPT TDES DATA#1 ECB.
Campo ausente/null/número/outro método é inválido; nunca assumir em claro
para compensar erro. `gtkKeyIndex` continua obrigatório inteiro0..99 vindo
do perfil; preservado para mudança posterior, não enviado no modo em claro.

RF-02: em claro, omitir SPE_MTHDDAT, SPE_KEYIDX, WK e demais parâmetros
criptográficos; continuar SPE_TRACKS=0111. Em30 enviar método e índice da fonte.
Manual local ABECS2.12p85 define retorno em claro pela ausência de SPE_MTHDDAT;
`00` significa MK/WK DES e NÃO retorno em claro. Manter ABI gomobile existente.

RF-03: alterar o perfil externo fornecido pelo usuário para gtkDataMethod vazio,
preservando gtkKeyIndex08 e todos os demais campos. PIN continua três campos do
properties (3/08/WK vazia) e retorno PINblock/KSN criptografado; não condicionar
PIN ao modo GTK nem substituir parâmetros. Mudança futura para30 requer
reprovisionar perfil e abrir nova jornada; nunca alterar operação em andamento.

RF-04: manter 03/06 GCX→GTK→GOX e 00/05 GCX→GTK→GPN. Preservar objeto Go da10014,
bytes retornados em claro ou criptografados; não fabricar KSN DATA quando GTK
é em claro. Snapshot inclui identificação do modo DATA e índice efetivamente
enviado (ausente/-1 em claro) para interpretar os bytes, além dos dados retidos.
Índice do perfil permanece reservado para uso posterior em30.

RF-05: dados em claro continuam exclusivamente nos buffers privados Go da
operação/snapshot, sem PAN/trilhas em resumo JSON, Android UI, persistência,
Logcat ou Bridge. Limpeza/isolamento existentes mantidos. Log criptográfico PIN
privado DES da10013 não passa a receber dados de GTK. Exceção explícita do usuário
para transporte GTK em claro documentada em ADR-001, apenas homologação DES.

## Aceite

- AC-01: testes dos quatro modos demonstram GTK em claro sem campos DATA e
  GTK30 com índice da fonte; resumo não expõe conteúdo; erro impede GOX/GPN.
- AC-02: objeto privado retém dados exatos dos dois modos e identifica configuração;
  PIN permanece DUKPT, sem KSN DATA inventado, limpeza e ausência de alias.
- AC-03: perfil Kotlin e launcher aceitam vazio/30 explícitos, rejeitam ausente,
  null/coerção/método inválido/índice inválido; perfis30 legados continuam aceitos.
- AC-04: gates Go/Android/provisionamento, AAR/APK/proveniência, revisão,
  evidência e memória; fonte DES alterada/provisionada com hashes. Hardware
  deve ser retestado pelo usuário; não afirmar PIN físico aprovado sem evidência.

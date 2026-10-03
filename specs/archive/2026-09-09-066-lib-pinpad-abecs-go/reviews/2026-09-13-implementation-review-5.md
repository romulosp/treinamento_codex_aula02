# Revisão da implementação — seleção de modo GTK no utilitário local

**Data:** 2026-09-13

**Estado de entrada:** `IMPLEMENTADA`

**Fonte normativa:** *Pinpad Abecs — Protocolo de Comunicação e Funcionamento*,
versão 2.12, seção 3.3.12, páginas 84–88.

**Escopo:** opção 19 de `cmd/libpinpadabecsgo`, builder GTK existente e testes
da seleção entre trilhas em claro e criptografadas.

## Rastreabilidade

| Critério | Implementação | Evidência |
| --- | --- | --- |
| CA-GTK-CLI-001 | `readGTKRequest` devolve uma requisição vazia no modo em claro; `BuildGTKCommand` produz `GTK000` | `TestReadGTKRequest/trilhas_em_claro` compara o payload literal e confirma a ausência de índice |
| CA-GTK-CLI-002 | O modo criptografado usa `SPE_MTHDDAT=50`, `SPE_TRACKS=1111` e índice DUKPT N2 | `TestReadGTKRequest/trilhas_DUKPT` compara todos os bytes do comando |
| CA-GTK-CLI-003 | Escolha diferente de 1/2 e índice não numérico ou fora de 00..99 retornam erro antes de `GetTracks` | Casos negativos de `TestReadGTKRequest` |

## Análise

O comando anterior da opção 19 sempre solicitava DUKPT TDES DAT#3/ECB e,
portanto, dependia de uma chave existente no índice digitado. O status físico
`042` está correto para a ausência dessa chave, mas o utilitário não oferecia o
modo em claro definido pelo protocolo.

A implementação corrigida apresenta as duas opções antes de iniciar o prazo da
operação. No modo em claro, a ausência de `SPE_TRACKS`, `SPE_MTHDDAT` e
`SPE_KEYIDX` resulta exatamente em `GTK000`, solicitando todas as informações
conhecidas sem criptografia. No modo DUKPT, o comportamento anterior permanece
disponível e o índice é validado como N2. A fachada continua sem imprimir ou
registrar o conteúdo sensível das trilhas.

Nenhuma divergência material foi encontrada entre a SPEC aprovada, o manual e
a implementação.

## Decisão

`IMPLEMENTACAO_APROVADA`

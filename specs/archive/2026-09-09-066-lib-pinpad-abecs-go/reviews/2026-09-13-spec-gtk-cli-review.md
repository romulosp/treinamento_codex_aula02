# Revisão de SPEC — seleção de modo GTK no utilitário local

**Data:** 2026-09-13

**Estado de entrada:** `EM_REVISAO_SPEC`

## Evidência

O rastro físico de 15:38 mostra GCX com status `000`, seguido por GTK com
status `042`. A opção 19 enviava sempre `SPE_MTHDDAT=50` e o índice digitado,
mesmo quando o operador apenas desejava verificar as trilhas. Segundo as páginas
84–86 do manual ABECS 2.12, método ausente devolve os dados em claro e `042`
(`ST_ERRKEY`) significa MK ou DUKPT ausente no pinpad.

## Revisão

- O modo em claro possui payload determinístico `GTK000` e não depende de chave.
- O modo criptografado declara método 50 e exige índice N2 provisionado.
- Entradas inválidas são rejeitadas antes da serial.
- O contrato preserva a redação integral das trilhas em ambos os modos.
- A alteração está contida no utilitário local e usa o builder GTK aprovado.

Não foram encontradas ambiguidades ou conflitos com o manual.

## Decisão

`SPEC_APROVADA`


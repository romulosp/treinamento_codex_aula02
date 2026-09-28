# Revisão da implementação — opção 20 GOX

**Data:** 2026-09-13

**Estado de entrada:** `IMPLEMENTADA`

**Fonte normativa:** manual ABECS 2.12, seção 3.7.3, páginas 136–139; definição
de `PP_AIDTABINFO` na seção 3.7.1, página 131.

## Diagnóstico

O rastro físico contém GCX `000` para cartão `03`, seguido de GOX `011`.
O menu anterior gerava o GOX com adquirente `01`, método `3` e índice `01`
fixos e omitia o valor, que o protocolo interpreta como zero. O pinpad devolveu
`011` (`ST_INVPARM`). Esses valores não eram derivados da aplicação EMV
selecionada nem escolhidos pelo operador.

## Rastreabilidade

| Requisito | Implementação | Evidência unitária |
| --- | --- | --- |
| Rede válida | `goxAcquirerReferences` lê cada entrada N6 e extrai `TAB_ACQ` | rede única, múltiplas redes, duplicidade, tamanho e caractere inválido |
| Escolha da rede | `readGOXRequest` aceita somente uma rede presente em `PP_AIDTABINFO` | rede padrão `08`, escolha `02` e rejeição de `99` |
| Continuidade do valor | `runMenu` conserva o N12 usado no GCX e o copia para `SPE_AMOUNT` | payload byte a byte contém `000000010000` |
| PIN obrigatório | CLI coleta `SPE_MTHDPIN` e `SPE_KEYIDX` | métodos 0–3 e índice 00–99 validados |
| WKENC condicional | Métodos 0/1 coletam respectivamente 8/16 bytes; DUKPT não coleta WKENC | MK/WK TDES válido e WKENC curta rejeitada |
| Sequência | Ausência de GCX 03/06 é rejeitada antes de `ContinueEMV` | teste do menu confirma ausência da operação |
| Prazo | Contexto de 60 segundos é recriado após todas as entradas | inspeção do fluxo e padrão já coberto para comandos blocantes |

O builder existente continua aprovado pelo vetor integral da página 139. O
novo vetor mínimo comprova ordem e bytes de `SPE_AMOUNT`, `SPE_MTHDPIN`,
`SPE_KEYIDX` e `SPE_ACQREF`.

A matriz anterior citava GOX como seção 3.7.2; a referência correta é 3.7.3 e
foi corrigida na SPEC de conformidade.

Nenhuma divergência material permaneceu no escopo da correção.

## Decisão

`IMPLEMENTACAO_APROVADA`

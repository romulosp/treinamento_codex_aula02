# Revisão da implementação — opção 21 FCX e diagnóstico GOX

**Data:** 2026-09-13

**Estado de entrada:** `IMPLEMENTADA`

**Fonte normativa:** manual ABECS 2.12, seção 3.7.3, páginas 136–139, e seção
3.7.4, páginas 140–141.

## Diagnóstico

O FCX anterior enviava sempre aprovação (`SPE_FCXOPT=0000`) e ARC `00`, embora
o `PP_GOXRES=201000` apenas informasse que a transação precisava de autorização
online. Sem uma resposta real da Rede Credenciadora, essa aprovação inventada
não representava o resultado da comunicação.

O rastro físico posterior mostrou `GOX047` imediato. Esse status não pertence
à tabela ABECS 2.12 e o frame GOX é corretamente redigido, portanto o rastro
anterior não permitia comparar adquirente, método de PIN e índice de chave com
a execução que havia funcionado.

## Rastreabilidade

| Requisito | Implementação | Evidência unitária |
| --- | --- | --- |
| Contexto | `runMenu` conserva somente `GOXResponse` validada | ausência ou `PP_GOXRES` inválido é recusado antes do serviço |
| Resultado da rede | `readFCXRequest` converte as três escolhas em `0000`, `1000` e `2000` | três payloads exatos |
| ARC condicional | aprovação/negação exigem dois bytes ASCII; falha omite ARC | ARC curto/não ASCII e ARC proibido rejeitados |
| Campos opcionais | EMV/tag list usam hexadecimal e timeout aceita 1..255 | valores válidos e entradas malformadas cobertos |
| Resposta | `PP_FCXRES` é exibido somente após parser válido | tag `8056` ausente de todo payload de entrada |
| Estado | elegibilidade só avança após validar GOX/FCX | inspeção e suíte do fluxo avançado |
| Diagnóstico GOX | `RecordGOXConfig` grava adquirente, método e índice | teste confirma formato e ausência de campos sensíveis |

O vetor FCX publicado na página 141 continua passando byte a byte. O caso de
falha de comunicação produz exatamente `FCX008 0019 0004 2000`, sem
`SPE_ARC`. Nenhuma divergência material permaneceu no escopo automatizável.
O vetor da execução GOX anteriormente bem-sucedida também permanece idêntico:
valor `000000010000`, método `3`, índice `02` e adquirente `04`, nessa ordem de
parâmetros do protocolo.

## Decisão

`IMPLEMENTACAO_APROVADA`

A causa específica do `GOX047` depende da nova linha física `GOX_CONFIG`, pois
o manual 2.12 não define esse status. A interoperabilidade física do novo FCX
continua pendente de repetição no equipamento.

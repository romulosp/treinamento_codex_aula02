# ADR-002 — Dependência canônica do stack 074

## Status

`ACEITA`

## Contexto

As fontes 068 e 069 dependem do stack criado por 066/067/070/071 e corrigido
pela 072. A primeira revisão da 075 copiou apenas parte dos efeitos da 072 e não
declarou a 074 como baseline normativa, permitindo divergências em preflight,
estado, erros, logging, cleanup e precedência de `PORTA_PINPAD`.

## Decisão

1. A 075 continua sendo a Change canônica do produto diagnosticopinpad e
   consolida diretamente 068/069.
2. A 074 é a dependência normativa única para core Go, PBRG, Bridge, REST,
   serial, ownership, launcher, tracer e cleanup.
3. A 075 reproduz de modo autocontido todo comportamento da 072 observável no
   Android: preflight, estado triplo, erros, visibilidade, correlação, logs e
   lifecycle.
4. REST permanece na 074 e fora da fronteira Android; “fora do escopo da UI”
   não significa removido da baseline.
5. A precedência da porta segue a decisão posterior aprovada na 074:
   `PORTA_PINPAD` é obrigatória em todo processo físico, sem COM fixa/fallback;
   essa regra supersede o fallback direto ainda presente na fonte 072.
6. Alteração material da SPEC rebaixa a implementação existente para
   `IMPLEMENTAÇÃO A RECONCILIAR`.

## Alternativas rejeitadas

- copiar integralmente as 658 linhas da 074 para a 075;
- manter referências vagas à fundação sem Gate P0;
- tratar Ping, listener, reverse ou arquivo criado como sessão/hardware;
- preservar `IMPLEMENTADA` sem confrontar código e testes com a revisão nova.

## Consequências

A 075 fica autocontida para o aplicativo, sem criar uma segunda definição do
stack. Mudanças futuras na 074 exigem avaliação explícita de impacto na 075. A
implementação atual precisa de reconciliação e novas evidências.

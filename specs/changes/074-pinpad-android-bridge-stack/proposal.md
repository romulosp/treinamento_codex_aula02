# Proposta — 074 Pinpad Android Bridge Stack

## Status

- SPEC: `SPEC_APROVADA`
- Change: `IMPLEMENTADA — TESTE HUMANO PENDENTE`

Esta Change é uma baseline canônica consolidada. Não autoriza implementação,
aprovação automática, arquivamento ou alteração das cinco Changes de origem.
O aditivo documental 071 foi aprovado pelo usuário em 2026-09-30 para
implementação e teste humano; a aprovação não autoriza arquivamento nem commit.

A re-revisão da consolidação da 072 confirmou que a decisão humana da 074
supersede o fallback histórico do entrypoint direto: todo processo físico exige
`PORTA_PINPAD`. Essa supersessão está explícita na SPEC e na rastreabilidade.

## Identificador

O identificador solicitado `073` não está disponível: já existe a Change
arquivada `specs/archive/2026-09-28-073-remover-acoes-duplicadas-catalogo`.
O próximo identificador livre verificado em `specs/changes` e `specs/archive` é
`074`; por isso a pasta canônica é
`specs/changes/074-pinpad-android-bridge-stack`.

## Objetivo

Consolidar em um único contrato autocontido o estado final acumulado das
Changes arquivadas:

- `066-lib-pinpad-abecs-go`;
- `067-android-emulator-transport-bridge`;
- `070-bridge-log-android`;
- `071-bat-teste-modulo-windows`;
- `072-corrigir-abertura-bridge-android`.

A baseline deve permitir que uma futura implementação execute somente esta
Change em um ambiente sem nenhuma das cinco implementada e construa o
subsistema:

```text
Android/AAR → EmulatorTransport → Bridge Windows → Serial/COM → Pinpad ABECS
```

O contrato inclui biblioteca Go, protocolo ABECS, ciclo de vida, serialização,
ownership, Bridge PBRG, fachada gomobile, logging, configuração, erros,
timeouts, correlação, testes e cenários de falha e recuperação.

## Escopo

- consolidar o core Go ABECS v2.12 e sua fachada tipada;
- consolidar transporte serial físico Windows/Linux e fakes determinísticos;
- consolidar `EmulatorTransport`, envelope PBRG v1 e Bridge Windows;
- consolidar fachada `mobile` compatível com `gomobile bind` e AAR;
- consolidar integração do laboratório Android existente, sem duplicar o
  protocolo no Kotlin;
- consolidar configuração, `adb reverse`, lifecycle, estados, erros,
  timeouts, `operationID`/`correlationId`, ownership e reabertura;
- consolidar o tracer único do Bridge e do transporte serial;
- consolidar o launcher BAT físico Windows com COM herdada, ambiente isolado,
  configuração visível e código de saída preservado;
- consolidar a API REST local de 066 como adaptador independente, sem torná-la
  a fronteira Android;
- consolidar testes unitários, integração, multiprocesso Windows, Android,
  AAR, scripted e hardware físico;
- registrar a análise de sobreposição, precedência, supersession e divergências
  do código atual.

## Fora de escopo

- implementar código nesta Change;
- reescrever ou mover as cinco Changes arquivadas;
- criar um novo protocolo proprietário ou duplicar lógica ABECS em Kotlin;
- expor o Bridge em LAN, adicionar autenticação remota, TLS ou serviço Windows;
- Android USB Host ou comunicação USB direta;
- inventar a serialização completa do `TransactionGCX` reservado;
- transformar `RST` em comando ABECS: o reset canônico é CAN/EOT;
- atualizar toolchain ou dependências sem uma Change própria;
- tratar teste scripted como prova de comunicação física.

## Fontes e precedência

As fontes normativas e a resolução de conflitos estão registradas em
`analysis.md`. As cinco Changes permanecem fontes históricas e intactas; a
partir desta Change, o contrato normativo consolidado é exclusivamente
`spec.md`, apoiado por `DESIGN.md` e pela rastreabilidade em
`traceability.md`.

Quando houver conflito, aplica-se a regra:

1. correção normativa posterior dentro de 066;
2. comportamento posterior de 072 para configuração, launcher, readiness,
   estado, logging, ownership, cleanup e reabertura;
3. contrato de 071 para o BAT físico, exceto regras corrigidas por 072;
4. contrato final de 070 para destino compartilhado do tracer;
5. contrato final de 067 para transporte, Bridge, envelope e binding;
6. requisitos gerais de 066 para core, ABECS, REST, comandos e testes.

## Resultado esperado da SPEC

Após aprovação humana, a implementação futura deverá conseguir reconstruir o
subsistema completo a partir desta pasta, sem depender operacionalmente da
execução sequencial `066 → 067 → 070 → 071 → 072`. Os links para as Changes de origem
servem apenas para auditoria histórica e não são dependências de execução.

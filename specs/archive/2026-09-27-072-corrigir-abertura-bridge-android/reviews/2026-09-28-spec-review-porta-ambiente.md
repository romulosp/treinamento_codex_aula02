# Revisão da SPEC — 072 (porta física por ambiente)

## Estado

`SPEC_APROVADA`

## Escopo da revisão

Revisão da correção contratual solicitada em 28/09/2026 após mudança da porta
enumerada pelo Windows. Foram comparados `proposal.md`, `spec.md`, `DESIGN.md`,
`tasks.md`, `implementation-plan.md`, o workflow compartilhado e as evidências
anteriores da própria change.

## Achados

| ID | Severidade | Evidência | Decisão |
|---|---|---|---|
| REV-072-P01 | Bloqueante, resolvido | CA-072-01 e CA-072-13 citavam uma COM específica embora RF-072-01 declarasse `PORTA_PINPAD` como fonte | critérios reescritos para qualquer valor válido herdado do ambiente |
| REV-072-P02 | Importante, resolvido | texto DSP/teste poderia cristalizar o nome de uma porta antiga | DESIGN proíbe teste físico e scripts de escolher ou corrigir a COM; texto de teste deve ser neutro ou derivado da variável efetiva |
| REV-072-P03 | Importante, resolvido | evidência histórica de uma porta poderia ser confundida com default | proposal distingue evidência temporal de contrato normativo |

## Testabilidade e consistência

O contrato é verificável lendo `PORTA_PINPAD` do processo, comparando o valor
com a configuração/readiness/log e executando o fluxo físico sem fallback. A
ausência ou valor inválido continua falhando antes do uso serial. A alteração
não amplia protocolo, comandos ABECS, rede, toolchain ou política de dados.

## Decisão

`SPEC_APROVADA`. Não há decisão pendente ou ADR necessária. A implementação
deve remover referências operacionais fixas da instrumentação/DSP, atualizar
documentação e repetir revisão de implementação e validação física usando o
valor efetivamente lido de `PORTA_PINPAD`.

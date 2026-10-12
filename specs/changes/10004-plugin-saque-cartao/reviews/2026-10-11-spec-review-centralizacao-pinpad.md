# Consolidação da centralização no pinpad — complemento da REV-017

Data: 2026-10-11.

Solicitação: centralizar as informações apresentadas no pinpad para confirmação
na SPEC 10004. A REV-017 já aprova a centralização de todas as linhas.

Revisão documental: RF-002 consolida rótulos, valores do resumo congelado,
ordem, centralização horizontal na largura de `SaqueDisplay.columns`, separador,
limite DEX e confirmação GKY. O contrato complementar passa a referenciar
RF-002, eliminando sua instrução conflitante de usar quatro páginas DSP.
O espaço adicional à direita corresponde à função `center()` já implementada.

Resultado: conforme à REV-017 `SPEC_APROVADA`; nenhuma nova aprovação de
implementação ou hardware é emitida. Escopo desta atualização exclusivamente
documental. Evidências em `validation.md`; validação física permanece pendente.

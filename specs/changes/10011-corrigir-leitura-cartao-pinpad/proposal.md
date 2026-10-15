# Proposta — 10011-corrigir-leitura-cartao-pinpad

## Status

`SPEC_APROVADA` pela [revisão técnica REV-001](reviews/2026-10-11-spec-review.md). Solicitação recebida em 2026-10-11. Escopo desta entrega: diagnóstico e especificação da correção. Implementação, homologação física e aprovação final da mudança são etapas posteriores.

## Problema e objetivo

A tela Pagamento com Cartão apresentou resultado indeterminado, `PINPAD_ERROR`, fase `command`, acompanhado de falha de encerramento. O log fornecido não identifica o comando/status causador, pois os frames estão redigidos. A inspeção confirmou que a jornada envia GOX para o tipo `05` e não para `06`, configura saque em GCX/GOX e fixa os parâmetros de PIN em Kotlin.

Especificar a leitura de compra com chip, tarja e contactless, filtrada pela modalidade escolhida na tela 1, incluindo aplicações com AID base e estendido, modo efetivamente detectado e eventos normativos de fallback. Usar todos os campos da fonte externa de criptografia indicada pelo solicitante. Executar GCX → GTK e depois GOX para EMV ou GPN DUKPT para a ramificação sem EMV, preservando os dados sensíveis no Go.

## Escopo

- Plugin `:plugin-saque-cartao`, core Go/ABECS, fachada gomobile e provisionamento privado debug.
- Correção dos tipos de cartão, tipo da operação, filtros e sequência de comandos.
- Importação integral e validada de `D:\desenvolvimento\chave_des\informacoes_critografia_pinpad.properties`.
- Contexto de sessão Go necessário ao PIN depois de GTK, diagnóstico sanitizado e encerramento confirmado.
- Testes de contrato, jornada, segurança, integração e roteiro físico.

## Fora de escopo

Autorizador financeiro real, instalação de chaves, ambiente PRD, alteração de tarifas/parcelas/layout da confirmação, novos meios de pagamento, upgrades de stack, novo protocolo Bridge e promoção de `TransactionGCX` reservado na 074.

## Dependências e riscos

Esta Change complementa a [10004](../10004-plugin-saque-cartao/spec.md), a [074](../074-pinpad-android-bridge-stack/spec.md) e a [075](../075-diagnosticopinpad-consolidado/spec.md). Na futura implementação, seus contratos de leitura serão substituídos apenas nos pontos explicitados na SPEC; revisões de layout e inicialização continuam vigentes.

GPN depois de GTK exige PAN explícito: PAN vazio nessa ordem pode provocar `ST_INVCALL`. O PAN será temporário e privado no Go, conforme [ADR-001](adr/ADR-001-contexto-pan-no-core.md). Cartões que não forneçam um PAN utilizável ao core não podem avançar silenciosamente. O índice DATA do GTK é independente do índice PIN; o arquivo indicado não define método/índice DATA. A presença da configuração não comprova provisionamento de chave no pinpad.

## Resultado esperado

Contrato verificável, revisão registrada e validação documental reproduzível. Nenhuma afirmação de correção do dispositivo será feita antes dos testes físicos previstos em [validation.md](validation.md).

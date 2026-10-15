# ADR-001 — PAN temporário no core para GPN depois de GTK

## Status

Aprovado na [revisão técnica da SPEC](../reviews/2026-10-11-spec-review.md). Decisão aplicável exclusivamente ao ramo sem EMV desta Change. Não equivale à aprovação da implementação ou à homologação física.

## Contexto

O solicitante pede GCX→GTK→GPN no ramo sem EMV. O contrato anterior da 10004 executa GPN primeiro, com PAN vazio. O manual ABECS 2.12, seções 3.3.11 e 6.5.11, restringe o uso de PAN retido pelo pinpad à situação anterior a GTK; sua ausência depois de GTK pode produzir `ST_INVCALL`.

## Decisão

Preservar no Go apenas o PAN validado obtido da captura GCX, antes de GTK. Uma operação aditiva de sessão fornece esse PAN explicitamente ao builder GPN após GTK. Não devolver PAN pelo binding e não armazená-lo em Kotlin, no Bridge, disco ou logs. Contexto vinculado a cliente/sessão/correlação, uso único e descarte nos caminhos previstos na SPEC.

Preferir PAN numérico completo quando disponível, ou extrair das trilhas incompletas por seu formato. Não supor que toda captura tenha um PAN utilizável: dados mascarados, inválidos ou conflitantes bloqueiam o processamento. Testes físicos devem confirmar os dados disponíveis na tabela/dispositivo do laboratório.

## Alternativas avaliadas

| Alternativa | Consequência |
| --- | --- |
| Manter GPN antes de GTK | Compatível com PAN vazio, mas não atende à ordem solicitada. |
| Enviar PAN vazio após GTK | Contraria a pré-condição normativa e pode gerar erro de chamada. |
| Obter PAN no Android/operador ou decifrar GTK | Amplia exposição de dados e exige material/contratos não fornecidos. |
| PAN mínimo temporário no core | Atende à ordem e preserva a fronteira do Android; requer isolamento, descarte e testes novos. |

## Consequências e validação

Amplia de forma explícita o estado privado do core sem alterar arquitetura de transporte. Material sensível permanece no componente que já interpreta respostas de cartão. Não promete eliminação de cópias de strings pelo runtime; reduzir cópias e controlar buffers mutáveis. AC-06/AC-08 exigem isolamento, single use, ausência de dados em saídas e invalidação em todas as falhas. A configuração de chave não é instalação nem comprovação de chave física.

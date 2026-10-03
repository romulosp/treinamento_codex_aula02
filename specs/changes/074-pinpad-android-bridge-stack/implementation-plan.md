# Plano técnico preparatório — 074 Pinpad Android Bridge Stack

## Status

IMPLEMENTADA — TESTE HUMANO PENDENTE

Este arquivo é preparatório e não autoriza alteração de código. O plano só
poderá ser executado depois da aprovação formal da SPEC.

## Sequência futura

1. Resolver na revisão as lacunas L-001 a L-004 e a política do alias REST de
   reset.
2. Corrigir primeiro a configuração/launcher e o teste de fechamento do
   EmulatorTransport, pois ambos bloqueiam evidência confiável. Conforme C-003,
   todo processo físico exige `PORTA_PINPAD`; não existe fallback em
   `DefaultConfig`, `config.Load()` ou entrypoint.
3. Inventariar a implementação Go, associar produção a testes e registrar
   exclusões.
4. Implementar ou revisar a porta Transport e os adaptadores serial/fake.
5. Implementar/revisar envelope PBRG, EmulatorTransport, Bridge, ownership,
   cleanup e reabertura.
6. Implementar/revisar fachada mobile, geração AAR e fronteira de erros.
7. Implementar/revisar consumidor Android, estado, erros visíveis,
   acessibilidade e cancelamento.
8. Revisar logging/redaction, REST e documentação.
9. Executar gates unitários, integração, Android, multiprocesso, scripted e
   físico em etapas separadas.
10. Registrar revisão de implementação, validação, aprovação e só então
    preparar archive/commit.

## Riscos principais

- operação ABECS com resultado indeterminado após timeout;
- leitura serial bloqueante e cancelamento;
- concorrência entre processo Android, Bridge e outro consumidor Windows;
- mutex abandonado e afinidade de thread Windows;
- frame PBRG inválido ou payload acima do limite;
- exposição acidental de dados sensíveis em tracer, JSON ou mensagem Android;
- AAR gerado a partir de fontes/proveniência incorretas;
- divergência entre COM efetiva do processo e ownership;
- dependência de hardware físico e confirmação visual.

## Evidências esperadas

Cada etapa deve registrar ambiente, comando/procedimento, saída, código de
saída, artefatos/hash e limitação. Nenhum teste scripted substitui o gate de
hardware. A execução do plano deve preservar mudanças preexistentes do
workspace.

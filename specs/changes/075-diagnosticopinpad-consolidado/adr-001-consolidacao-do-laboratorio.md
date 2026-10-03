# ADR-001 — Consolidar o laboratório mínimo e o Functional Lab

## Estado

`SUPERADA PARCIALMENTE POR ADR-002`

A decisão de consolidar 068/069 permanece válida. A suposição de que bastava
preservar referências genéricas a lifecycle/`PORTA_PINPAD` foi corrigida pela
ADR-002, que torna a 074 dependência normativa e incorpora os efeitos
observáveis completos da 072.

## Contexto

As Changes 068 e 069 foram arquivadas separadamente, embora a 069 seja a
expansão funcional direta da 068. Manter duas fontes operacionais para o mesmo
aplicativo aumenta o risco de perder requisitos de protocolo, lifecycle,
redaction, configuração física ou critérios de aceite.

## Decisão

Criar a Change 075 como contrato canônico acumulado:

1. preservar integralmente o laboratório mínimo da 068;
2. incorporar o catálogo de 28 opções e a fachada nomeada da 069;
3. elevar o perfil arquitetural para STANDARD, mantendo um único módulo;
4. manter PBRG v1 e o core Go como fonte de verdade;
5. manter `PORTA_PINPAD` exclusivamente no processo Windows do Bridge;
6. manter scripted separado de validação física;
7. iniciar apenas os artefatos de SPEC e preparação, sem implementar código.

## Alternativas rejeitadas

- manter duas Changes como fontes canônicas simultâneas;
- reabrir o framing com JSON/base64;
- criar `Execute` genérico para reduzir o número de métodos;
- tratar a validação histórica como validação nova;
- executar o prompt de implementação durante esta consolidação.

## Consequências

A Change 075 concentra o contrato final e simplifica a próxima implementação,
sem apagar o histórico arquivado. A implementação futura terá uma superfície
maior que a 068, mas continua limitada pelo core Go, por métodos nomeados e por
resultados redigidos.

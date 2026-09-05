# Consultar status da Sprint

Analise uma Sprint e as Changes por ela referenciadas sem ampliar o escopo aprovado.

## Objetivo

Apresentar o progresso, os bloqueios e a próxima Change executável com base nos registros e evidências existentes.

## Regras

1. Considere somente uma Sprint com status `ACTIVE`. Se não houver uma ou se houver mais de uma, registre a inconsistência e não indique próxima Change executável.
2. Analise os estados das Changes referenciadas conforme o mapeamento de `specs/sprint/README.md` e as evidências disponíveis nos seus artefatos.
3. Para cada Change, informe o gate atual, a documentação entregue, a evidência ausente, o risco de segurança ou de negócio e a condição objetiva para avançar.
4. Preserve o escopo aprovado das Changes e SPECs; a Sprint não altera requisitos, critérios de aceite ou o workflow 01-06.
5. Não altere automaticamente ordem, prioridade ou status sem evidência objetiva registrada.
6. Considere como bloqueio a ausência da auditoria de segurança quando aplicável, de teste unitário ou de integração aplicável, da verificação Sonar/cobertura configurada ou da Auditoria de Qualidade Assistida por LLM quando essas ferramentas não existirem, ou qualquer achado de segurança confirmado ainda não resolvido; esses itens não podem ser tratados como `DONE`.
7. Para identificar a próxima Change executável em uma única Sprint `ACTIVE`:
   1. ignore itens `DONE`, `REMOVED` e `IN_PROGRESS`;
   2. verifique se um item `BLOCKED` impede os itens seguintes pelas dependências declaradas;
   3. escolha a primeira Change `READY` pela ordem declarada;
   4. se não houver Change `READY`, explique o que falta para atender a Definition of Ready.

## Resultado esperado

Informe o status da Sprint, o gate e a documentação de cada item, riscos, bloqueios e dependências relevantes, a próxima Change executável ou a justificativa objetiva da sua ausência.

# Planejar Sprint

Analise o backlog de Changes existente e planeje uma Sprint sem iniciar implementação.

## Objetivo

Criar ou atualizar uma Sprint conforme `specs/sprint/README.md` e `specs/sprint/templates/template-sprint.md`, definindo um Sprint Goal objetivo, seu critério de sucesso e a ordem operacional das Changes selecionadas.

## Regras

1. Considere como backlog inicial somente os diretórios de Change em `specs/changes/`; exclua `specs/archive/`, itens arquivados e Changes pertencentes a uma Sprint `ACTIVE`.
2. Uma Change pode pertencer a, no máximo, uma Sprint `ACTIVE`.
3. Consulte `specs/sprint/README.md` para classificar a Change pelo gate real e pela evidência disponível. Não atribua `READY` sem todos os critérios da Definition of Ready.
4. Respeite as dependências e proponha uma ordem de execução compatível com elas.
5. Priorize, nesta ordem: segurança, risco de negócio, testes unitários e testes de integração. Registre o risco, seu impacto e o tratamento ou decisão necessária para cada item.
6. Registre prioridade, fase/gate, status, dependência, evidência atual, disponibilidade de Sonar/cobertura e observações operacionais para cada item. Se a ferramenta estiver indisponível, registre o motivo e a Auditoria de Qualidade Assistida por LLM exigida.
7. Registre um Sprint Goal objetivo e um critério observável para demonstrar seu sucesso na Sprint Review.
8. Não duplique requisitos, critérios de aceite ou detalhes técnicos das Changes e SPECs referenciadas.
9. A Sprint não substitui nem altera o workflow 01-06 e não constitui uma nova etapa desse workflow.
10. Não inicie implementação, não altere Changes e não crie uma Change nova durante o planejamento.
11. Consulte a Sprint `COMPLETED` mais recente, quando ela existir, e considere suas decisões importantes, lições aprendidas e ações para a próxima Sprint.
12. Explique objetivamente quando uma ação da retrospectiva influenciar o planejamento atual.
13. Não copie retrospectivas antigas para a nova Sprint sem necessidade.

## Resultado esperado

Apresente a Sprint criada ou atualizada, as Changes planejadas na ordem declarada, os gates e evidências atuais, os riscos e dependências considerados, o critério de sucesso do Sprint Goal e os itens que ainda não atendem a Definition of Ready.

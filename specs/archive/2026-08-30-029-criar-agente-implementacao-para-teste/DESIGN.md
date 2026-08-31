# Design: 029-criar-agente-implementacao-para-teste

O agente será uma definição Markdown com frontmatter para uso pelo Copilot, complementada por um prompt reutilizável. Ele espelha somente os gates iniciais do fluxo canônico:

`SPEC → revisão da SPEC → implementação → testes técnicos → parada para teste humano`.

A parada é deliberada: testes técnicos da implementação não substituem a revisão independente, a validação com evidências ou a aprovação para commit.

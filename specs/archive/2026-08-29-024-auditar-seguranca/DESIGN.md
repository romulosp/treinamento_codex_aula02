# Design – Skill de Auditoria de Segurança

A Skill será uma instrução curta e autocontida, adequada ao fluxo de revisão pós-implementação. Ela usará ferramentas de leitura e busca do workspace, `git log`/`git grep` para histórico e o gerador PDF específico do projeto apenas quando o formato for solicitado.

O fluxo separa descoberta da stack, análise por categoria, confirmação de evidências, classificação e entrega. A Skill não presume frontend, CI/CD, infraestrutura ou banco específicos: registra a categoria como não aplicável somente após procurar os artefatos correspondentes.

Achados de segredos usarão valores redigidos desde a coleta. A instrução também obriga a diferenciar o estado atual do histórico, evitando declarar que uma remoção pontual eliminou a exposição histórica.

# Revisão da SPEC: 026-chaves-aplicacao

## Escopo revisado

Foram revisados `proposal.md`, `spec.md`, `DESIGN.md`, `tasks.md`, `AGENTS.md`, o workflow Spec Driven, as convenções de evidência e os scripts `.bat` existentes de `gerenciartarefas` e `gerenciarcategorias`.

Nenhum valor de credencial foi reproduzido nesta revisão.

## Achados

| Identificador | Severidade | Evidência | Impacto | Recomendação |
| --- | --- | --- | --- | --- |
| `REV-001` | Bloqueante | `spec.md`, RF-005 e `DESIGN.md` exigem um manifesto por aplicação, mas o mapeamento contém apenas descrições genéricas e registra que os nomes estão “a confirmar”. | Não é possível verificar se todos os parâmetros efetivamente usados por cada `.bat` serão carregados; a implementação pode omitir configuração ou criar chaves divergentes. | Enumerar, sem valores, o nome de cada chave obrigatória/opcional por aplicação e sua origem no template ou configuração consumida. |
| `REV-002` | Bloqueante | `spec.md`, RF-003 permite duas estratégias de geração; `DESIGN.md` deixa o destino, a retenção e a limpeza como decisões pendentes; `CA-003` depende de um destino ainda não definido. | O comportamento observável, o local seguro de saída e a forma de impedir resíduos sensíveis não são determinísticos. | Escolher uma estratégia, definir o destino não versionado, permissões, retenção/limpeza e o comportamento quando o arquivo de saída já existir. |
| `REV-003` | Bloqueante | `proposal.md` e `DESIGN.md` adotam caminho absoluto Windows, enquanto `spec.md` permite tanto falha em outros ambientes quanto um caminho configurável; `tasks.md` marca essa decisão como pendente. | Não há contrato único para CI, máquinas sem a unidade/caminho informado ou execução fora do Windows. | Decidir formalmente se a mudança é Windows-only ou definir uma sobrescrita segura do caminho, mantendo o caminho padrão sem valores sensíveis. |
| `REV-004` | Importante | `spec.md`, RF-006 restringe a migração a SPECs vigentes, mas o levantamento mostra referências dependentes de ambiente em documentos arquivados e não há inventário versionado dos arquivos que serão migrados. | A equipe não consegue demonstrar quais documentos foram avaliados nem distinguir cobertura concluída de histórico intencionalmente preservado. | Incluir no plano um inventário dos documentos vigentes e uma regra explícita de tratamento dos arquivados, com evidência sanitizada da busca. |
| `REV-005` | Importante | `DESIGN.md` e `tasks.md` deixam pendentes o parser, escaping de `cmd.exe` e política para valores com caracteres especiais. | Valores válidos podem gerar scripts quebrados ou alterar a interpretação do comando; correções ad hoc podem expor conteúdo sensível. | Definir parser, escaping, limites de caracteres e testes com valores sintéticos antes da implementação. |

## Veredito

`REPROVADA`

As pendências bloqueantes devem ser resolvidas na SPEC e submetidas a nova revisão antes de qualquer implementação.

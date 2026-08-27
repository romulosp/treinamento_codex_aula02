# Validação: 010-reinicializar-workspace-documentado

## Ambiente

- Windows.
- PowerShell 5.1 e Git.
- Sem execução de Maven, Quarkus, OIDC ou DB2, pois `apps/backend/` foi removido conforme o escopo da mudança.

## Comandos e códigos de saída

1. Preparação do conjunto de mudanças com `git add -A` — código `0`.
2. Primeira verificação estática — código `1`; a regra considerou incorretamente as deleções não textuais no índice como itens adicionados. Nenhum arquivo foi alterado em consequência dessa verificação.
3. Verificação estática corrigida da árvore, das regras de ignorados, do conjunto adicionado/modificado, dos valores sensíveis e do whitespace — código `0`.

## Cenários executados

- `NotasProjeto.md` existe na raiz e contém o resumo, a metodologia, a criação, os testes, a execução e a reinicialização do módulo.
- O diretório `apps/backend/` não existe; consequentemente, `apps/backend/target/` também não existe.
- Não há arquivos no diretório de trabalho, fora de `.git/`, que não sejam `.md`, `.txt` ou `.gitignore`.
- Exemplos com extensões `.java`, `.xml`, `.properties` e `.bat` são ignorados por Git; exemplos `.md`, `.txt` e o próprio `.gitignore` não são ignorados.
- Entre adições e modificações preparadas para commit não há arquivo que não seja `.md`, `.txt` ou `.gitignore`; as deleções não textuais são o resultado aprovado da limpeza.
- `NotasProjeto.md` não contém atribuição concreta de `SECRET` ou `DB2_PASSWORD` nem URL concreta de infraestrutura.
- `git diff --cached --check` foi aprovado.

## Evidências

- `VAL-001` — política de arquivos e limpeza física aprovadas.
- `VAL-002` — regras de ignorados aprovadas para tipos locais e documentação permitida.
- `VAL-003` — verificação estática de segurança da nova nota aprovada.
- `VAL-004` — conjunto de adições e modificações preparado para commit respeita a política documental.
- `VAL-005` — diff preparado não possui erro de whitespace.

## Veredito

`VALIDADA`

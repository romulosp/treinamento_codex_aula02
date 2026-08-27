# SPEC: 005-limpar-artefatos-gerados

## Status
`SPEC_APROVADA`

## Referências e dependências

- `deletar-arquivos-gerados.bat`
- `specs/shared/process/workflow.md`

## Requisitos funcionais

1. O script deve manter a confirmação interativa antes de qualquer remoção.
2. Após confirmação, o script deve localizar e remover cada diretório chamado `target` abaixo de `apps/`.
3. Após confirmação, o script deve localizar e remover cada diretório chamado `.quarkus` abaixo de `apps/`.
4. Após confirmação, o script deve localizar e remover arquivos com extensão `.log` abaixo de `apps/`.
5. O script deve informar cada caminho removido e concluir com código de saída `0` quando não houver erros.
6. Se uma remoção falhar, o script deve informar o caminho e terminar com código de saída diferente de zero.

## Requisitos não funcionais

1. O script não pode remover arquivos ou diretórios fora de `apps/`.
2. O script deve preservar arquivos de código, recursos, testes, `pom.xml`, scripts `.bat`, configurações, documentos `.md` e `.txt` e o diretório `.git`.
3. O script deve ser compatível com Prompt de Comando do Windows e não exigir ferramentas externas.

## Regras de negócio

1. A ausência de diretórios ou arquivos elegíveis não é erro.
2. Somente os padrões `target`, `.quarkus` e `*.log` são considerados artefatos gerados nesta mudança.

## Cenários e critérios de aceite

- [ ] Com um diretório `apps/backend/target`, uma confirmação positiva remove o diretório e informa o caminho.
- [ ] Com diretórios `.quarkus` e arquivos `.log` abaixo de `apps/`, uma confirmação positiva os remove.
- [ ] Sem artefatos elegíveis, o script termina com código `0` e informa que não encontrou artefatos.
- [ ] Uma resposta negativa não remove arquivos.
- [ ] Após a limpeza, arquivos `.java`, `.properties`, `.bat`, `.md`, `.txt`, `pom.xml` e o diretório `.git` continuam preservados.
- [ ] O script não remove itens fora de `apps/`.
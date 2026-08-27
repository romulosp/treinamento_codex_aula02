# SPEC: 010-reinicializar-workspace-documentado

## Status
`SPEC_APROVADA`

## Referências e dependências

- `AGENTS.md`
- `specs/shared/process/workflow.md`
- `specs/shared/architecture/backend-java.md`
- `specs/shared/testing/testing-strategy.md`

## Requisitos funcionais

1. Deve existir na raiz o arquivo `NotasProjeto.md`, em português do Brasil, descrevendo o objetivo do laboratório, os incrementos concluídos, a arquitetura e os contratos da API que existiam antes da reinicialização.
2. `NotasProjeto.md` deve explicar o processo Spec Driven, seus artefatos, gates, evidências, arquivamento e a regra de derivação de nomes de projetos.
3. `NotasProjeto.md` deve orientar a criação de uma nova mudança e de um módulo Java Quarkus, sua execução e seus testes, sem exibir valores reais de credenciais ou infraestrutura.
4. `.gitignore` deve ignorar globalmente qualquer arquivo cujo nome não termine em `.md` ou `.txt` e deve conter uma regra de exceção que mantenha o próprio `.gitignore` versionável.
5. `.gitignore` deve continuar ignorando diretórios de compilação `target`, diretórios `.quarkus`, arquivos `.log` e arquivos de ambiente.
6. Todos os arquivos versionados fora de `.git/` que não terminem em `.md` ou `.txt` devem ser removidos do diretório de trabalho, com exceção exclusiva de `.gitignore`.
7. Todos os arquivos não versionados gerados pela compilação devem ser removidos; ao término não deve existir `apps/backend/target/`.

## Requisitos não funcionais

1. A limpeza não pode remover o diretório `.git/`, os documentos Markdown e texto, nem as pastas de documentação necessárias para reiniciar o fluxo.
2. Nenhuma documentação nova pode conter `SECRET`, senha, URL real de banco de dados ou outro valor sensível concreto.
3. A mudança não deve executar Maven, iniciar o Quarkus, chamar OIDC ou DB2, pois o módulo executável será removido antes de nova geração.
4. A validação deve registrar os comandos, o resultado, a lista de classes de arquivos remanescentes e a ausência de arquivos gerados.

## Regras de negócio

1. A extensão é comparada literalmente: somente `.md` e `.txt` representam conteúdo textual permitido para versionamento, respeitando a exceção técnica de `.gitignore`.
2. Documentos históricos em `specs/archive/` são preservados, mesmo quando descrevem arquivos que a limpeza remove do diretório de trabalho atual.
3. A funcionalidade da API de categorias permanece registrada como histórico; ela não estará disponível localmente até que seja gerada novamente.

## Cenários e critérios de aceite

- [ ] `NotasProjeto.md` existe na raiz e descreve tecnologia, arquitetura, API anterior, método, criação, execução, testes e reinicialização.
- [ ] `git check-ignore` informa que exemplos `.java`, `.xml`, `.properties` e `.bat` são ignorados e que um exemplo `.md` não é ignorado.
- [ ] `.gitignore` permanece elegível para ser adicionado ao Git.
- [ ] Não há arquivo versionado não textual, exceto `.gitignore`.
- [ ] Não existe `apps/backend/target/` nem módulo `apps/backend/` após a limpeza.
- [ ] Os arquivos Markdown e texto sob a raiz permanecem disponíveis.
- [ ] A validação estática não encontra valores sensíveis concretos em `NotasProjeto.md`.

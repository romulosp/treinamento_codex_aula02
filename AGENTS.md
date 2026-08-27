# Instruções do projeto

## Processo obrigatório

1. Toda alteração começa em `specs/changes/<id>-<nome>/` e segue `specs/shared/process/workflow.md`.
2. Não implemente uma mudança cujo `spec.md` e `proposal.md` não estejam em `SPEC_APROVADA`.
3. Registre revisão, validação, aprovação e evidências em `reviews/` e `validation.md`.
4. Antes de concluir, execute os testes aplicáveis e registre ambiente, comando, resultado e código de saída.
5. A mudança só pode ser arquivada depois da aprovação formal, atualização de `specs/system/` e preparação do commit que registra ambos.
6. Prompts legados são material didático; Skills são o mecanismo operacional do projeto.

## Backend

- Use Quarkus, Maven e o pacote-base `br.com.romulopenha.nomedaapigerada`.
- Não exponha entidades de persistência diretamente em recursos REST.
- Separe API, aplicação, domínio e infraestrutura conforme `specs/shared/architecture/backend-java.md`.
- Use Java 17: Quarkus 3.2 requer Java 17 ou superior. Não reduza o release para 11 sem trocar a plataforma Quarkus e registrar um ADR.

## Qualidade

- Responda e documente em português do Brasil.
- Não amplie o escopo definido pela SPEC.
- Crie ou atualize testes ao implementar comportamento observável.

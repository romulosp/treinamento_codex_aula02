# SPEC: 011-reescrever-readme-profissional

## Status
`SPEC_APROVADA`

## Referências e dependências

- `README.md`
- `NotasProjeto.md`
- `specs/shared/process/workflow.md`

## Requisitos funcionais

1. O `README.md` deve ter título, descrição profissional e identificação do laboratório Aula 02.
2. O documento deve informar que o repositório é orientado a especificações e que não há módulo `apps/backend/` executável no estado atual.
3. O documento deve apresentar os principais recursos: rastreabilidade, processo Spec Driven, arquitetura de referência Java/Quarkus e política de retenção documental.
4. O documento deve ter uma seção de estrutura e referências que direcione para `NotasProjeto.md`, `AGENTS.md`, `specs/shared/`, `specs/changes/`, `specs/archive/` e `specs/system/`.
5. O documento deve resumir como contribuir por meio de uma nova mudança Spec Driven, sem repetir o manual completo de operação.
6. O documento deve ter uma seção de créditos identificando f744113 como autor do projeto.

## Requisitos não funcionais

1. O texto deve estar em português do Brasil, ser conciso, profissional e usar Markdown válido.
2. Não pode apresentar valores concretos de credenciais, senhas, endpoints privados ou dados de infraestrutura.
3. Não pode declarar a aplicação removida como disponível para execução imediata.

## Regras de negócio

1. `NotasProjeto.md` é a fonte detalhada para reprodução; o README atua como porta de entrada do repositório.
2. O README deve preservar a coerência com a política de versionar documentos `.md` e `.txt`, além de `.gitignore`.

## Cenários e critérios de aceite

- [ ] O README identifica propósito, estado atual e público do laboratório.
- [ ] As seções de visão geral, estrutura, fluxo, contribuição, referências e créditos existem.
- [ ] O crédito cita f744113 sem incluir e-mail.
- [ ] A revisão estática não encontra atribuições de segredo, senha ou URL de infraestrutura.

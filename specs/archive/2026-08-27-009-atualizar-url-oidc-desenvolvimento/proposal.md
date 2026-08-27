# Proposta: 009-atualizar-url-oidc-desenvolvimento

## Status
`SPEC_APROVADA`

## Responsável e data

- Responsável: GitHub Copilot
- Data: 2026-08-27

## Referências

- `specs/shared/process/workflow.md`
- `apps/backend/start_aplicacao.bat`
- `apps/backend/src/main/resources/application.properties`
- `specs/archive/2026-08-27-008-unificar-configuracao-inicializacao-local/`

## Problema e objetivo

A URL do servidor OIDC configurada no script de inicialização local não corresponde ao endereço de desenvolvimento informado pelo solicitante. O objetivo é atualizar exclusivamente essa URL para que o teste de segurança da API use o servidor de autenticação correto.

## Escopo

- Alterar somente o valor de `AUTH-SERVER-URL` em `apps/backend/start_aplicacao.bat` para a URL de desenvolvimento fornecida.
- Preservar as demais variáveis OIDC, DB2, Java, Maven e o comando de inicialização.
- Registrar a mudança, revisão, validação, aprovação, documentação vigente e arquivamento conforme o processo Spec Driven.

## Fora de escopo

- Alterar `application.properties`, propriedades OIDC, credenciais, cliente autorizado ou configuração DB2.
- Alterar dependências, código Java, endpoints ou regras de segurança.
- Testar conectividade remota com o provedor OIDC ou DB2.

## Impactos e riscos

- A execução local passará a solicitar tokens ao novo servidor OIDC de desenvolvimento.
- A disponibilidade do teste integrado depende do novo servidor OIDC, que não será contatado pela suíte automatizada.

## Critérios para aprovação da SPEC

- A mudança identifica um único valor a ser substituído e preserva explicitamente todas as configurações restantes.
- Os critérios de aceite verificam o endereço configurado e a regressão da suíte Maven sem acessar serviços externos.

# Proposta: 008-unificar-configuracao-inicializacao-local

## Status
`SPEC_APROVADA`

## Responsável e data

- Responsável: GitHub Copilot
- Data: 2026-08-27

## Referências

- `specs/shared/process/workflow.md`
- `apps/backend/start_aplicacao.bat`
- `apps/backend/src/main/resources/application.properties`
- `specs/archive/2026-08-27-007-configurar-inicializacao-local-segura/`

## Problema e objetivo

A alteração anterior criou um segundo script local para fornecer a configuração exigida pelo backend. O solicitante requer um único arquivo `start_aplicacao.bat`, autocontido com a configuração de ambiente fornecida, para iniciar a aplicação e testar a segurança da API localmente.

## Escopo

- Remover o mecanismo de carregamento de `start_aplicacao.local.bat`.
- Declarar no único script `start_aplicacao.bat` as variáveis de ambiente OIDC e DB2 requeridas por `application.properties`, com os valores fornecidos pelo solicitante.
- Remover a regra de `.gitignore` específica do segundo script e excluir o arquivo local existente.
- Atualizar a documentação do sistema para refletir o script único autocontido.

## Fora de escopo

- Alterar as propriedades ou a semântica da configuração do Quarkus.
- Alterar código Java, endpoints, autenticação, autorização, dependências Maven ou conectividade DB2.
- Criar outros arquivos de configuração ou mecanismos de gerenciamento de segredos.

## Impactos e riscos

- O script versionado passará a conter parâmetros de ambiente e credenciais fornecidos explicitamente pelo solicitante.
- O script continuará a limitar as variáveis à sessão iniciada por ele, por meio de `setlocal`.
- A execução do script poderá acessar os serviços OIDC e DB2 configurados.

## Critérios para aprovação da SPEC

- A SPEC define o único arquivo de inicialização e as sete variáveis necessárias.
- Não permanece referência funcional a `start_aplicacao.local.bat` no script, no `.gitignore` ou na especificação vigente.
- Os critérios podem ser verificados por inspeção do script e pela suíte de testes existente.

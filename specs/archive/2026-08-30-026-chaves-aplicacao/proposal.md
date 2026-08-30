# Proposta: 026-chaves-aplicacao

## Status

`SPEC_APROVADA`

## Responsavel e data

- Responsavel: equipe do projeto
- Data: 2026-08-30

## Referencias

- `AGENTS.md`
- `specs/shared/process/workflow.md`
- `specs/shared/process/evidence-conventions.md`
- `specs/system/gerenciar-tarefas-postgresql-local.md`
- `apps/backend/gerenciartarefas/start_aplicacao.bat`
- `apps/backend/gerenciarcategorias/start_aplicacao.bat`

## Problema e objetivo

Os scripts de inicializacao e as especificacoes que orientam sua geracao podem conter parametros de ambiente, URLs ou credenciais. Isso dificulta a manutencao e pode expor informacoes sensiveis em arquivos versionados.

Centralizar os valores de ambiente em `D:\desenvolvimento\chave_des\chave_des.properties`, fora do repositorio, e fazer com que um gerador versionado consuma esse arquivo para produzir o `start_aplicacao.bat` final com os valores literais necessarios.

## Escopo

- Definir o arquivo externo como pre-condicao da geracao do BAT de `gerenciartarefas`.
- Criar um gerador versionado que leia as oito chaves fornecidas e escreva o BAT final com os valores literais.
- Manter gerador, template e documentacao sem valores reais; o BAT final sera artefato local ignorado pelo Git.
- Remover valores concretos de credenciais, URLs e parametros de ambiente das SPECs vigentes autorizadas.

## Fora de escopo

- Versionar o arquivo externo ou qualquer copia preenchida dele.
- Definir valores de ambiente, credenciais ou tokens.
- Alterar contratos HTTP, regras de negocio ou codigo de dominio.
- Alterar o conteudo de producao do banco, do servidor OIDC ou de servicos externos.
- Reescrever o historico do Git sem autorizacao formal especifica.

## Impactos e riscos

- A geracao sera bloqueada quando o arquivo externo nao existir ou nao contiver chave obrigatoria.
- O caminho absoluto restringe a mudanca a Windows e `cmd.exe`.
- O BAT final contem valores literais e e um artefato local sensivel; nao sera versionado, exibido em logs ou incluido em relatorios.
- A migracao de SPECs arquivadas preservara o carater historico dos documentos.

## Criterios para aprovacao da SPEC

- O contrato define arquivo ausente, chave ausente, chave vazia, parser, escaping e substituicao segura.
- Cada aplicacao abrangida possui lista verificavel de chaves, sem valores reais.
- O gerador, o template e o destino local do BAT final estao definidos.
- Ha criterios verificaveis para impedir vazamento em logs, SPECs, testes e artefatos versionados.
- O plano de migracao distingue documentos vigentes de documentos arquivados.

# Proposta: 007-configurar-inicializacao-local-segura

## Status
`SPEC_APROVADA`

## Responsável e data

- Responsável: GitHub Copilot
- Data: 2026-08-27

## Referências

- `specs/shared/process/workflow.md`
- `specs/shared/architecture/backend-java.md`
- `apps/backend/src/main/resources/application.properties`
- `apps/backend/start_aplicacao.bat`

## Problema e objetivo

O script de inicialização local executa `mvn quarkus:dev` sem disponibilizar as variáveis de ambiente exigidas pela configuração OIDC e pelo datasource DB2. O objetivo é permitir que o desenvolvedor forneça essas variáveis localmente sem registrar credenciais ou parâmetros reais no Git.

## Escopo

- Ajustar `start_aplicacao.bat` para carregar, quando existir, uma configuração local ignorada pelo Git antes de iniciar o Quarkus.
- Exigir as variáveis OIDC e DB2 referenciadas por `application.properties` e encerrar com mensagem clara quando alguma estiver ausente.
- Adicionar um arquivo local de configuração para o ambiente atual, ignorado pelo Git, com os valores fornecidos pelo solicitante.
- Atualizar `.gitignore` para garantir que a configuração local não seja versionada.

## Fora de escopo

- Alterar os nomes, valores padrão ou a semântica das propriedades em `application.properties`.
- Alterar autenticação, autorização, banco de dados, dependências Maven ou código Java.
- Armazenar segredos, senhas, URLs internas ou identificadores reais em arquivos rastreados pelo Git.
- Iniciar, validar conectividade ou modificar o banco DB2 remoto.

## Impactos e riscos

- A inicialização local passará a falhar antecipadamente se a configuração local não existir ou estiver incompleta.
- O arquivo local conterá credenciais e deverá permanecer apenas na máquina de desenvolvimento, protegido por `.gitignore`.
- O script poderá usar valores definidos previamente no ambiente do processo, permitindo integração com mecanismos locais de segredos.

## Critérios para aprovação da SPEC

- O contrato enumera todas as variáveis requeridas e separa arquivos rastreados de configuração local sensível.
- Os critérios de aceite são verificáveis sem expor ou depender dos valores reais das credenciais.

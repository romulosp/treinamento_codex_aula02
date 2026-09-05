# Proposta: 034-automatizar-sonar-local

## Status

`VALIDADA`

## Responsável e data

- Responsável: Codex
- Data: 2026-09-05

## Referências

- `AGENTS.md`
- `specs/shared/process/workflow.md`
- `specs/sprint/README.md`
- `D:\desenvolvimento\sonar\docker-compose.yml`
- `D:\desenvolvimento\banco_dados\postgresql\docker-compose.yml`

## Problema e objetivo

O SonarQube local existe, mas depende de PostgreSQL externo sem preparação verificável do banco `sonar`, não há um comando único para subir suas dependências e não há uma forma padronizada de analisar os módulos Java e front-end. Além disso, o Docker pode estar indisponível; nessa situação, a ausência de infraestrutura não pode liberar uma Change sem uma auditoria técnica equivalente e registrada.

O objetivo é disponibilizar uma automação versionada para preparar a dependência PostgreSQL, iniciar e verificar o SonarQube local, analisar os módulos de código do projeto e sinalizar formalmente quando a Auditoria de Qualidade Assistida por LLM for obrigatória por indisponibilidade operacional do Sonar.

## Escopo

- Criar um script PowerShell versionado em `scripts/sonar/` para subir, consultar, parar e analisar o SonarQube local.
- Verificar a disponibilidade do Docker antes de qualquer operação; se ele estiver ausente ou indisponível, emitir o marcador de fallback para auditoria técnica da LLM e encerrar com código específico.
- Verificar o PostgreSQL em `D:\desenvolvimento\banco_dados\postgresql`; quando não estiver saudável, subir sua composição, aguardar a saúde e preparar de modo idempotente o usuário e banco exclusivos do Sonar, sem senha fixa no código.
- Ajustar `D:\desenvolvimento\sonar\docker-compose.yml` para consumir as credenciais do banco por arquivo local `.env`, criado pelo script e não versionado.
- Subir o SonarQube, aguardar o estado `UP`, analisar cada módulo Java Maven e cada front-end com `package.json`, incluindo fontes HTML, CSS, JavaScript e TypeScript aplicáveis.
- Atualizar a governança do Sprint Planner e o orquestrador Spec Driven para exigir a auditoria técnica da LLM quando Sonar/cobertura não estiverem configurados **ou** quando a infraestrutura configurada estiver operacionalmente indisponível.
- Criar testes de estrutura do script e registrar a limitação de validação de integração quando o Docker não estiver acessível.

## Fora de escopo

- Alterar código de aplicação, POMs, `package.json`, testes de aplicação, regras de qualidade do Sonar ou cobertura configurada por módulo.
- Criar, armazenar ou exibir token do Sonar; ele continuará obrigatório em `SONAR_TOKEN` apenas no ambiente de execução.
- Alterar a infraestrutura do PostgreSQL além da preparação idempotente do usuário e banco `sonar` solicitados pelo script.
- Usar Docker Swarm: a infraestrutura existente é Docker Compose e não possui configuração Swarm.

## Impactos e riscos

- O primeiro uso gera `D:\desenvolvimento\sonar\.env` com uma senha aleatória do banco exclusiva para o Sonar; esse arquivo não deve ser versionado nem exibido em evidências.
- A análise Java depende de Java 17 e Maven disponíveis no ambiente; a análise front-end depende de Node.js e npm. Falha de build é uma falha de validação, não um fallback do Sonar.
- Docker inacessível, PostgreSQL que não sobe, SonarQube que não atinge `UP` ou token ausente acionam a auditoria técnica da LLM; essa auditoria não pode alegar cobertura medida ou execução do Sonar.
- Uma falha de Quality Gate retornada pelo Sonar permanece reprovada e não pode ser convertida em fallback.

## Critérios para aprovação da SPEC

- O contrato separa indisponibilidade operacional do Sonar de falha de qualidade encontrada pelo próprio Sonar.
- A automação não contém token, senha de banco fixa ou segredo em log.
- Os critérios de aceite descrevem comandos, códigos de saída e evidências verificáveis.

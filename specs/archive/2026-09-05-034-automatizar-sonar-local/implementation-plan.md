# Plano técnico: 034-automatizar-sonar-local

## Fatos observados

- `D:\desenvolvimento\sonar\docker-compose.yml` inicia `sonarqube:lts-community` e aponta para `host.docker.internal:5432/sonar`.
- `D:\desenvolvimento\banco_dados\postgresql\docker-compose.yml` fornece o serviço `postgres`, com container `postgres_db`, healthcheck e porta `5432`, mas não possui script que crie o usuário ou banco do Sonar.
- O daemon Docker não está acessível neste ambiente; `docker version` falha ao abrir `dockerDesktopLinuxEngine`.
- Existem `apps/backend/gerenciarcategorias` e `apps/backend/gerenciartarefas` com Maven, e os front-ends `exemplo-site-web-001` e `template-ecommercer-001` com Vite, TypeScript e npm.
- O ambiente atual possui npm e Pester 3.4.0, mas Maven não está no `PATH` desta sessão.

## Impactos prováveis

- Novo script e teste estrutural em `scripts/sonar/`.
- Ajuste externo e não versionado da composição em `D:\desenvolvimento\sonar` para receber a senha por `.env`.
- Template, README, prompts de planejamento, status e finalização da Sprint, além do orquestrador de Change, para explicitar contingência por indisponibilidade operacional.

## Estratégia de implementação

1. Criar funções PowerShell para validar Docker, chamar Docker Compose, aguardar estado, gerar `.env` com senha aleatória e preparar o banco pelo `psql` do container PostgreSQL.
2. Implementar as ações operacionais, mantendo o código `20` e o marcador de fallback somente para pré-requisito ou infraestrutura indisponível.
3. Descobrir módulos por `pom.xml` e `package.json`; construir e escanear cada módulo com o scanner em container, aguardando o Quality Gate.
4. Criar teste Pester sem Docker para o contrato de sintaxe e os invariantes de segurança/fallback.
5. Atualizar os documentos de governança com o mesmo critério de fallback e sem prometer métricas inexistentes.

## Testes, cobertura e qualidade

- Teste unitário estrutural Pester para o script: sintaxe, ações, marcador de fallback e ausência de senha JDBC literal.
- Integração manual/reproduzível: executar `validar-codigo.ps1 -Acao Tudo` com Docker, Maven, Node/npm e `SONAR_TOKEN` disponíveis; validar subida, análise de quatro módulos e Quality Gate.
- Cobertura não é aplicável ao script nesta Change, pois não há ferramenta de cobertura PowerShell configurada. O teste Pester e os comandos de parser serão registrados sem percentual estimado.
- A validação da própria Change usará Auditoria de Qualidade Assistida por LLM caso o daemon continue indisponível, registrando o escopo e a limitação; isso não alegará Sonar executado.

## Auditoria de segurança

- Aplicável: o script controla infraestrutura, banco e variáveis de ambiente.
- Revisar ausência de segredo literal, ausência de impressão de token/senha, SQL parametrizado/escapado e diferenciação entre indisponibilidade e Quality Gate.
- Um achado confirmável dentro do escopo será corrigido antes de nova revisão e validação. A necessidade de executar Docker local será registrada como limitação de ambiente, se permanecer indisponível.

## Riscos, dúvidas e decisões necessárias

- Risco de ambiente: Docker Desktop indisponível impede validar a subida real. Tratamento: teste estrutural, comando de fallback verificável e registro explícito da pendência de integração.
- Risco de credencial: a senha do banco é necessária para Sonar. Tratamento: geração aleatória no `.env` local, token somente em variável de ambiente e nenhuma saída de valor secreto.
- Risco de código: falha de build/Quality Gate não pode ser classificada como falta de infraestrutura. Tratamento: códigos de erro distintos e documentação explícita.

# Arquitetura de backend Golang

## Objetivo

Este documento define a organização reutilizável para aplicações Go geradas pela Skill `backend-golang`. A arquitetura é autocontida em Go e usa princípios de separação de responsabilidades; não depende de Java, Maven, Quarkus, JPA, Panache ou JUnit.

## Perfis suportados

- `api`: aplicação HTTP com Gin como adaptador REST de referência.
- `desktop`: aplicação executável com ponto de entrada Go e as camadas de domínio e aplicação compartilhadas. Esta Change não escolhe toolkit de interface gráfica.

Cada projeto deve ser isolado em `apps/api/<artifactId-sem-hifens>/` ou `apps/desktop/<artifactId-sem-hifens>/`.

## Camadas

```text
cmd/<nome-do-aplicativo>/
internal/api/             # somente no perfil api
internal/application/
internal/domain/
internal/infrastructure/
go.mod
```

### `cmd`

Faz a composição da aplicação, carrega configuração, cria adaptadores, conecta dependências e inicia o ponto de entrada. Não deve conter regra de negócio.

### `internal/api`

Contém handlers Gin, DTOs de request/response, validação de entrada, correlação, mapeamento de erros e documentação do contrato HTTP. Não expõe entidades de persistência.

### `internal/application`

Orquestra casos de uso por meio de portas explícitas. Recebe dependências por construtor ou por composição manual. Não deve conhecer detalhes de Gin ou GORM.

### `internal/domain`

Contém entidades, value objects, regras, erros de domínio e interfaces de repositório. As regras devem permanecer independentes de HTTP, banco, ORM, rede e configuração.

### `internal/infrastructure`

Implementa adaptadores técnicos: GORM, conexões, migrations, configuração, logs, métricas, traces e integrações externas. O código técnico não deve vazar para o domínio.

## Dependências permitidas

A direção recomendada é:

```text
cmd -> api/application/infrastructure
api -> application/domain
infrastructure -> application/domain
application -> domain
 domain -> biblioteca padrão e contratos próprios
```

O domínio não importa Gin, GORM, drivers de banco, clientes HTTP ou SDKs de fornecedor. GORM é uma implementação atrás da porta de repositório aprovada pela SPEC da aplicação.

## Identidade do projeto

A geração exige `groupId` e `artifactId`.

- O `artifactId` mantém hífens em `go.mod` e metadados.
- O diretório local remove hífens de forma determinística.
- O módulo Go é `<groupId>/<artifactId>`.
- Nomes de pacotes e símbolos seguem a sintaxe e as convenções idiomáticas de Go.

## Configuração e observabilidade

A configuração deve vir do ambiente ou de arquivos locais não versionados, com validação no início da aplicação. Segredos não podem aparecer em código, logs, testes ou documentação.

A composição pode registrar adaptadores para:

- logs estruturados com correlação;
- health checks;
- métricas Prometheus;
- traces OpenTelemetry;
- profiling somente quando explicitamente habilitado.

## Checklist de geração

1. Confirmar `SPEC_APROVADA`, perfil, `groupId`, `artifactId` e destino.
2. Validar a ausência de colisão no diretório do projeto.
3. Criar o esqueleto de camadas permitido pelo perfil.
4. Manter domínio e aplicação independentes dos adaptadores.
5. Registrar dependências e configuração sem credenciais.
6. Inventariar arquivos Go e associar testes aplicáveis.
7. Executar testes, cobertura, auditoria de segurança e qualidade conforme a SPEC.

# Proposta: 006-proteger-categorias-oauth2

## Status
`SPEC_APROVADA`

## Responsável e data

- Responsável: GitHub Copilot
- Data: 2026-08-27

## Referências

- `specs/shared/process/workflow.md`
- `specs/shared/architecture/backend-java.md`
- `specs/shared/api/rest-conventions.md`
- `apps/backend/pom.xml`
- `apps/backend/src/main/resources/application.properties`

## Problema e objetivo

A API de categorias não possui autenticação nem autorização de clientes. O objetivo é disponibilizar infraestrutura OAuth 2.0/OIDC no backend Quarkus, proteger todos os endpoints de categorias e permitir somente clientes configurados explicitamente.

## Escopo

- Adicionar a extensão OIDC do Quarkus à aplicação Maven.
- Configurar OIDC por variáveis de ambiente em `application.properties`.
- Exigir token de acesso válido em todos os endpoints sob `/categorias`.
- Autorizar somente clientes cujo identificador esteja configurado em `caixa.security.clients-authorized`.
- Criar o filtro reutilizável para chamadas HTTP de saída, adicionando `Content-Type` e `Capture-Network-Code`.
- Cobrir o comportamento de segurança com testes automatizados.

## Fora de escopo

- Criar, hospedar ou administrar um provedor OAuth 2.0/OIDC.
- Armazenar segredos, URLs de ambientes ou identificadores de clientes reais no repositório.
- Criar nesta mudança um cliente REST nem realizar chamadas externas.
- Definir autorização granular por operação ou por papel de negócio.
- Alterar contratos de sucesso, validação ou persistência de categorias.

## Impactos e riscos

- Clientes atuais sem token Bearer válido passarão a receber HTTP 401.
- Clientes autenticados, mas não listados na configuração, receberão HTTP 403.
- A disponibilidade dos endpoints protegidos depende do provedor OIDC configurado pelo ambiente.
- O identificador do cliente será extraído do claim padrão `azp`; tokens sem esse claim não serão autorizados.

## Critérios para aprovação da SPEC

- O contrato identifica o mecanismo de autenticação, o claim de cliente, a configuração e os códigos HTTP esperados.
- Os limites entre filtros de entrada e saída estão explícitos.
- Todos os requisitos possuem critério de aceite testável, sem depender de credenciais reais.

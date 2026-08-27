# Design: 006-proteger-categorias-oauth2

## Contexto

O backend já expõe operações de categorias sem segurança e possui configuração comum em `application.properties`. A segurança precisa ser infraestrutura reutilizável, sem alterar regras do domínio ou contratos de sucesso da API.

## Referências

- `specs/changes/006-proteger-categorias-oauth2/spec.md`
- `specs/shared/architecture/backend-java.md`
- `specs/shared/api/rest-conventions.md`
- Documentação do Quarkus OIDC 3.2.

## Decisões

1. A extensão `quarkus-oidc` será usada em modo `service`, apropriado para API que recebe Bearer tokens.
2. A proteção de autenticação será declarada no recurso de categorias com a anotação de segurança do Quarkus, mantendo a regra aplicável a todas as operações do recurso.
3. Um filtro JAX-RS de entrada, implementado na camada `infrastructure`, obtém a identidade autenticada e a lista configurada. Ele extrai `azp`, normaliza exclusivamente os itens configurados da lista e encerra acessos não autorizados com HTTP 403.
4. A lista de clientes autorizados será representada por uma configuração tipada e não será tratada como uma variável estática no recurso REST.
5. `FiservFiltroAutenticacao` será um `ClientRequestFilter` CDI com prioridade `Priorities.HEADER_DECORATOR`, injeção de `ambiente` e logs de depuração. Para um futuro cliente REST, ele será associado explicitamente com `@RegisterProvider`; nenhum endpoint externo será criado agora.
6. A documentação OpenAPI incluirá 401 e 403 nas operações protegidas.
7. Os testes de integração usarão a infraestrutura de segurança de teste do Quarkus para simular identidades e claims, mantendo o perfil de teste sem dependência de um provedor OIDC real.

## Arquitetura e componentes

- `apps/backend/pom.xml`: extensão OIDC e dependência de teste para simulação de segurança, quando necessária.
- `apps/backend/src/main/resources/application.properties`: propriedades OIDC e de clientes autorizados, incluindo valores específicos para o perfil de testes que não contêm segredos.
- Camada `infrastructure.security`: configuração tipada da lista autorizada e filtro de autorização do cliente para requisições recebidas.
- Camada `infrastructure.client`: `FiservFiltroAutenticacao` para decorar chamadas REST de saída.
- Camada `api`: anotação declarativa de autenticação e documentação dos erros 401/403, sem lógica de validação de cliente.
- Testes de integração: evidências para os quatro resultados de segurança e para a decoração dos cabeçalhos de saída.

## Alternativas e consequências

- Aplicar a lista de clientes somente com `@RolesAllowed` foi descartado, pois a autorização exigida é por client ID e não por papel.
- Validar o cliente diretamente no recurso foi descartado para preservar a separação entre API e infraestrutura.
- Criar um cliente REST externo de exemplo foi descartado: a mudança exige apenas a infraestrutura reutilizável e não define contrato de integração externa.
- Usar claim proprietário `client_id` foi descartado em favor de `azp`, por ser o claim explicitamente definido neste contrato.

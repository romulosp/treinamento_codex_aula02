# SPEC: 006-proteger-categorias-oauth2

## Status
`SPEC_APROVADA`

## Referências e dependências

- `specs/changes/006-proteger-categorias-oauth2/proposal.md`
- `specs/changes/006-proteger-categorias-oauth2/DESIGN.md`
- `apps/backend/pom.xml`
- `apps/backend/src/main/resources/application.properties`
- Provedor OAuth 2.0 compatível com OpenID Connect disponível no ambiente de execução.

## Requisitos funcionais

1. O Maven deve declarar a extensão `io.quarkus:quarkus-oidc` gerenciada pelo BOM do Quarkus.
2. A configuração da aplicação deve conter exatamente as propriedades OIDC e de autorização abaixo, sem valores de produção no repositório:

   ```properties
   quarkus.oidc.auth-server-url=${AUTH-SERVER-URL:}
   quarkus.oidc.client-id=${CLIENT-ID:}
   quarkus.oidc.credentials.secret=${SECRET:}
   quarkus.oidc.application-type=service
   quarkus.oidc.roles.role-claim-path=realm_access/roles
   caixa.security.clients-authorized=${CLIENTS-AUTHORIZED:}
   ```

3. Todas as operações HTTP do recurso `/categorias`, inclusive suas subrotas, devem exigir um token de acesso Bearer autenticado pelo OIDC configurado.
4. Após a autenticação, a aplicação deve obter o identificador do cliente chamador do claim `azp` do token.
5. A aplicação deve comparar o valor de `azp` com os identificadores configurados em `caixa.security.clients-authorized`, aceitando uma lista separada por vírgulas com espaços opcionais.
6. A aplicação deve permitir a operação solicitada quando o valor de `azp` corresponder exatamente a um cliente autorizado.
7. A aplicação deve interromper a requisição com HTTP 403 quando o token estiver autenticado, mas o claim `azp` estiver ausente, vazio ou não constar na lista de clientes autorizados.
8. A infraestrutura deve expor `FiservFiltroAutenticacao` como filtro de requisições HTTP de saída. Em cada execução, o filtro deve adicionar os cabeçalhos `Content-Type: application/json` e `Capture-Network-Code` com o valor da propriedade `ambiente`.

## Requisitos não funcionais

1. Credenciais OIDC e a lista de clientes autorizados devem ser obtidas somente por variáveis de ambiente, com valor padrão vazio.
2. A validação de cliente e o filtro de saída devem pertencer à camada `infrastructure`; o recurso REST não deve conter regra técnica de extração ou comparação de claims.
3. A autorização deve negar acesso por padrão quando a lista configurada estiver vazia ou inválida.
4. A documentação OpenAPI das operações de categorias deve declarar a resposta HTTP 401 para ausência ou invalidez de token e HTTP 403 para cliente não autorizado.
5. O filtro de saída não deve incluir tokens, segredos ou valores de configuração em logs.
6. Os testes devem executar sem um servidor OIDC externo e devem verificar os fluxos autorizado, sem autenticação e cliente não autorizado.

## Regras de negócio

1. O conjunto de clientes autorizados é a configuração `caixa.security.clients-authorized` dividida por vírgula; cada item deve ter espaços nas extremidades ignorados e itens vazios devem ser desconsiderados.
2. A comparação do identificador extraído de `azp` é sensível a maiúsculas e minúsculas.
3. A identidade do usuário final e os papéis presentes em `realm_access/roles` não concedem acesso por si só nesta mudança; somente o cliente autorizado concede acesso à API de categorias.
4. `FiservFiltroAutenticacao` é destinado exclusivamente a requisições de saída de clientes REST. Ele não participa da autenticação nem da autorização de chamadas recebidas pela API.

## Cenários e critérios de aceite

- [x] O `pom.xml` contém `quarkus-oidc` sem versão explícita e a aplicação compila com Java 17.
- [x] O `application.properties` contém as seis propriedades especificadas, com os nomes e defaults definidos nesta SPEC.
- [x] Uma chamada para qualquer endpoint de categorias sem token Bearer recebe HTTP 401.
- [x] Uma chamada com token válido cujo `azp` está configurado em `CLIENTS-AUTHORIZED` recebe a resposta de negócio original da operação.
- [x] Uma chamada com token válido cujo `azp` não está configurado recebe HTTP 403.
- [x] Uma chamada com token válido sem claim `azp` recebe HTTP 403.
- [x] Com `CLIENTS-AUTHORIZED` vazio, uma chamada autenticada recebe HTTP 403.
- [x] O filtro de saída adiciona `Content-Type: application/json` e `Capture-Network-Code` com o valor de `ambiente`.
- [x] O OpenAPI de categorias documenta respostas 401 e 403.

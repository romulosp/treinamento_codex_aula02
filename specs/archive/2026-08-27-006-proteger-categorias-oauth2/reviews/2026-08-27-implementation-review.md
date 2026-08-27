# Revisão da implementação — 006-proteger-categorias-oauth2

## Reavaliação

Em 2026-08-27, a implementação foi atualizada para atender aos achados `IMP-REV-001` e `IMP-REV-002`. Esta versão substitui o veredito inicial e mantém o histórico dos achados e das ações corretivas.

## Escopo revisado

- Extensões Maven e configurações OIDC.
- Proteção do recurso de categorias.
- Infraestrutura de autorização pelo claim `azp`.
- Filtro de saída `FiservFiltroAutenticacao`.
- Cobertura automatizada da segurança.

## Matriz de aderência

| Item | Evidência | Resultado |
| --- | --- | --- |
| Dependência OIDC | `quarkus-oidc` gerenciado pelo BOM no Maven. | Aprovado |
| Propriedades requeridas | Bloco SSO em `application.properties`, sem valores reais. | Aprovado |
| Autenticação de categorias | `@Authenticated` aplicado ao recurso. | Aprovado |
| Autorização por cliente | Filtro de entrada, claim `azp`, configuração tipada e resposta 403. | Aprovado |
| Filtro de saída | `FiservFiltroAutenticacao` adiciona ambos os cabeçalhos especificados. | Aprovado |
| Limites arquiteturais | Lógica técnica isolada em `infrastructure`; recurso sem extração de claims. | Aprovado |
| Testes exigidos | Há cenários de 401, `azp` ausente, cliente não autorizado, operação autorizada, lista vazia, normalização da lista, OpenAPI e filtro de saída. | Aprovado |

## Achados

### IMP-REV-001 — Cobertura incompleta da lista de clientes autorizados

- **Severidade:** importante
- **Evidência:** não existia teste para lista vazia, itens vazios nem normalização de espaços em `caixa.security.clients-authorized`. Também não havia teste automatizado que verificasse as respostas 401 e 403 declaradas no OpenAPI.
- **Impacto:** os critérios de aceite de configuração vazia, lista separada por vírgulas e documentação pública poderiam regredir sem detecção.
- **Ação necessária:** criar testes para configuração vazia, lista com espaços/itens vazios e documento OpenAPI com 401/403; então repetir a revisão.
- **Situação:** resolvido. `ValidadorClienteAutorizadoTest` cobre normalização, itens vazios e comparação exata; `SegurancaSemClientesAutorizadosTest` cobre HTTP 403 com lista vazia; `SegurancaCategoriasTest` inspeciona as descrições 401 e 403 no OpenAPI.

### IMP-REV-002 — Configuração vazia incompatível com mapeamento obrigatório

- **Severidade:** bloqueante
- **Evidência:** a primeira execução da suíte com Java 17 revelou que o mapeamento de `caixa.security.clients-authorized` como `String` rejeitava o valor vazio definido pela própria propriedade da SPEC.
- **Impacto:** a aplicação não iniciaria quando a lista de clientes autorizados não fosse informada, contrariando a negação por padrão.
- **Ação necessária:** representar a propriedade opcionalmente e interpretar sua ausência como uma lista vazia.
- **Situação:** resolvido. `ConfiguracaoSeguranca` usa `Optional<String>` e `ValidadorClienteAutorizado` converte a ausência em lista vazia, cuja recusa é coberta por teste de integração.

## Conclusão

`IMPLEMENTACAO_APROVADA`

# Auditoria de segurança — 001-produto-base

## Escopo e evidências

- Backend Quarkus: recurso `ProdutoResource`, serviço, repositório e propriedades.
- Frontend React: rotas protegidas no navegador e serviço HTTP.
- Scripts locais de inicialização.
- Busca estática de HTML cru, `eval` e padrões de segredo nos arquivos de produção; dependências e artefatos gerados foram excluídos da conclusão.

## Resultado executivo

Não foram identificados segredos rastreáveis inseridos pela Change, uso de HTML cru ou execução dinâmica de código. A API não implementa autenticação ou autorização no servidor: isso é uma limitação confirmada do produto demonstrativo, já declarada como fora de escopo na SPEC. Portanto, esta entrega só é adequada para uso local/demonstrativo; publicação pública requer uma Change própria de autenticação e autorização no backend.

## Categorias avaliadas

| Categoria | Resultado | Evidência |
| --- | --- | --- |
| Autenticação/autorização | Limitação confirmada | As rotas REST não têm proteção no servidor; o guard de rota está apenas no navegador. |
| IDOR/isolamento | Não aplicável ao escopo demonstrativo | Não há usuários, tenants ou recurso de identidade no backend. |
| Segredos | Conforme, com ressalva de desenvolvimento | Não há chave ou token de aplicação no código; as propriedades locais usam valores de desenvolvimento e não devem ser promovidas para produção. |
| Entrada/XSS/injeção | Conforme na inspeção | React renderiza dados por JSX e não há `dangerouslySetInnerHTML`, `innerHTML` ou `eval` no código da aplicação. |

## Achado SEC-001

- **Severidade:** alta se a API for publicada; limitação aceita para execução local demonstrativa.
- **Evidência:** `apps/backend/produtobase/src/main/java/br/com/romulopenha/produtobase/api/ProdutoResource.java` expõe CRUD sem mecanismo de identidade no servidor.
- **Impacto:** um cliente que alcance a API pode executar operações de Produto sem autenticação.
- **Correção recomendada:** criar Change separada para autenticação, autorização por rota e controle de acesso aos identificadores.
- **Critério de aceite da correção:** chamadas sem identidade válida recebem 401/403 e cada operação é autorizada no servidor.

## Bloco para issue

```md
Título: Proteger a API de Produto com autenticação e autorização no servidor
Severidade: Alta para publicação pública
Evidência: O recurso REST de Produto não possui mecanismo de identidade ou autorização no backend.
Impacto: Clientes que alcancem a API podem executar o CRUD sem autenticação.
Correção: Implementar autenticação, autorização e verificação de acesso no servidor em uma Change dedicada.
Critérios de aceite: Requisições sem identidade válida retornam 401/403; operações autorizadas são verificadas no backend.
```

# Revisão da implementação: 003-gerenciar-categorias

## Data e escopo

- Data: 2026-08-26
- Estado de entrada: `IMPLEMENTADA`.
- Itens revisados: código Java do módulo backend, `pom.xml`, configuração, testes de integração e critérios da SPEC aprovada.

## Conformidades verificadas

- As rotas aprovadas, a massa inicial em memória, a geração do primeiro identificador `4` e as respostas de sucesso estão implementadas.
- O recurso REST delega operações à camada de aplicação e usa DTOs, sem expor modelos de persistência.
- Não há acesso a banco de dados, entidade JPA ou repositório Panache no código da funcionalidade.
- Há teste de integração com `@QuarkusTest` e Rest Assured cobrindo os cenários de sucesso e de recurso inexistente definidos na SPEC.
- Não foram identificados segredos adicionados pela mudança nem dependências novas fora do escopo.

## Achados

### IMP-REV-001 — Importante — OpenAPI não documenta os contratos de resposta

- Evidência: `CategoriaResource` declara somente `@Operation` para os endpoints. As operações que retornam `Response` não possuem anotações OpenAPI para códigos de resposta, schemas ou corpos de erro, e o retorno opaco impede a descrição automática completa dos contratos.
- Impacto: os contratos públicos exigidos pela SPEC não ficam documentados integralmente no OpenAPI, sobretudo para `201`, `400` e `404` e para a resposta de exclusão.
- Ação necessária: documentar cada operação pública com os status previstos e os schemas de sucesso e erro, usando as anotações OpenAPI apropriadas, sem alterar o contrato aprovado.

### IMP-REV-002 — Importante — Entrada JSON inválida pode não retornar o contrato de erro especificado

- Evidência: a validação manual em `CategoriaResource` é executada somente após a desserialização para `CategoriaRequest`. Um JSON com `quantidade_produtos` textual, por exemplo, falha antes de chegar ao método e é tratado pelo provedor REST padrão, sem garantia do JSON com atributo `mensagem` exigido pela SPEC.
- Impacto: nem toda entrada inválida possui resposta HTTP `400` no contrato público definido, e o teste cobre apenas campos desserializados com sucesso.
- Ação necessária: assegurar que falhas de desserialização também resultem em HTTP `400` com o atributo `mensagem` e incluir teste de integração para esse caso.

## Conclusão

`REPROVADA`

Os achados `IMP-REV-001` e `IMP-REV-002` impedem a validação. A mudança deve retornar à fase de implementação. Nenhum teste foi executado nesta fase.
# Convenções REST para Golang

## Adaptador

O perfil `api` usa Gin como adaptador HTTP de referência. Handlers devem permanecer finos: validar a entrada, converter DTOs, chamar o caso de uso e mapear o resultado para HTTP.

O uso de Gin não deve alcançar `internal/domain` nem obrigar a camada de aplicação a conhecer tipos do framework.

## DTOs e fronteira HTTP

- Requests e responses públicos usam DTOs próprios.
- Entidades de domínio e persistência não são serializadas diretamente como contrato público.
- A validação sintática ocorre na fronteira HTTP.
- Regras de negócio continuam no domínio ou na aplicação.
- Campos desconhecidos, formatos inválidos e payloads ausentes recebem erro consistente.

## Códigos e envelopes

A aplicação deve definir na SPEC os recursos e os códigos permitidos. Como convenção inicial:

| Situação | HTTP |
| --- | ---: |
| Criação concluída | `201` |
| Consulta ou atualização concluída | `200` |
| Exclusão concluída sem corpo | `204` |
| Entrada inválida | `400` |
| Não autenticado | `401` |
| Sem permissão | `403` |
| Recurso inexistente | `404` |
| Conflito de regra ou unicidade | `409` |
| Falha inesperada | `500` |

O payload de erro deve ser estável, sem stack trace ou segredo. Um formato mínimo recomendado é:

```json
{
  "code": "resource_not_found",
  "message": "Recurso não encontrado",
  "correlationId": "<id-da-requisicao>"
}
```

A mensagem pública não deve revelar SQL, credenciais, caminhos locais ou detalhes internos.

## Correlação e middleware

Cada requisição deve aceitar ou gerar um identificador de correlação validado no servidor. O valor deve ser propagado para logs e respostas quando previsto pelo contrato. Um header enviado pelo cliente serve somente como correlação; nunca é fonte de identidade, tenant ou autorização.

Middleware pode tratar recuperação de panic, logging, timeout, autenticação e correlação. A ordem e os controles de segurança devem ser registrados na SPEC da aplicação.

## OpenAPI e versionamento

Contratos públicos devem ser documentados com OpenAPI, incluindo requests, responses, erros, autenticação e códigos HTTP. Não criar versionamento de recurso sem requisito da SPEC; quando exigido, o prefixo e a política de compatibilidade devem ser explícitos.

## Persistência e mapeamento

Handlers não acessam GORM diretamente. O caso de uso chama uma porta de repositório e o adaptador de infraestrutura converte entre modelo persistente e domínio.

## Testes REST

- Teste handlers com `httptest` e router real quando o comportamento depender de Gin.
- Verifique códigos, corpo, validação, correlação e mapeamento de erros.
- Cubra autenticação e autorização quando estiverem no escopo.
- Não use um teste unitário de handler como substituto de teste de integração de banco ou composição.

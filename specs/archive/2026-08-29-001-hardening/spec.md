# SPEC – Hardening de Segurança

## Requisitos de Aceite
- **Autenticação:** todas as rotas devem exigir token JWT/OIDC válido. Retornar **401** se ausente ou inválido.
- **Autorização:** rotas de escrita (`POST`, `PUT`, `DELETE`) requerem papel `ADMIN`. Retornar **403** se papel insuficiente.
- **Isolamento de Tenant:** consultas, atualizações e exclusões devem filtrar por `userId/tenantId` obtido do token. Usuário só pode acessar recursos que lhe pertencem.
- **Validação de Entrada:** `TarefaRequest` deve usar Bean Validation (`@NotBlank`, `@Size`, etc.) e o recurso deve usar `@Valid`.
- **Segredos:** `application.properties` deve referenciar segredos via `${env.VAR}`; a aplicação deve falhar na inicialização se variáveis não definidas.
- **Cobertura de Testes:** 100% de cobertura nas rotas modificadas (unit + integração).

## Não‑funcionais
- Não introduzir regressões de performance.
- Compatibilidade com Java 17 e Quarkus 3.2.

## Dependências
```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-oidc</artifactId>
</dependency>
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-security</artifactId>
</dependency>
```

## Status
SPEC_APROVADA

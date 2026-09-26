# ADR-001 — Direct Access Grant no backend de autenticação

## Status

`ACEITA`

## Contexto

A tela existente coleta usuário e senha e o solicitante exige validação por API
REST antes de abrir a tela inicial. O Keycloak local possui realm `intranet` e
cliente confidencial `CLI-MOBILE-INTRA`.

## Decisão

A API Quarkus executará Direct Access Grant. O Android conhecerá somente a URL
da API; o segredo do cliente e os tokens permanecerão no backend. A resposta ao
Android conterá apenas identificador aleatório de sessão e expiração.

## Consequências

- A senha atravessa Android e API, ampliando a superfície de tratamento.
- O uso fica limitado ao protótipo/intranet e requer HTTPS fora do localhost.
- Direct Access Grants deve estar habilitado no cliente Keycloak.
- Evolução para distribuição pública exige nova Change e substituição por fluxo
  recomendado para aplicativo nativo.

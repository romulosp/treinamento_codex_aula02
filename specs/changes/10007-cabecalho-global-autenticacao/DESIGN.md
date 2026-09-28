# Design: 10007-cabecalho-global-autenticacao

## Status

`SPEC_APROVADA`

## Contexto

O backend hoje descarta o access token depois de reter apenas refresh token e
expiracao. A API devolve sessao opaca; o evento Android tambem nao possui perfil.
`MainActivity` ja e o owner da sessao e decide entre login, menu e plugins,
portanto e o menor owner capaz de manter um cabecalho global.

## Referencias

- Os dois SVGs 1280x800 fornecidos pelo solicitante.
- Claims exemplificados pelo solicitante e configuracao do realm local.
- Contratos atuais de `autenticadorsso`, `:plugin-login` e `:shared-api`.
- Skills `android-native-engineering`, `compose-component-design` e
  `compose-state-and-effects`.

## Decisoes

- `KeycloakIdentityProvider` decodifica o payload do access token recebido na
  resposta autenticada do endpoint de token e converte somente username, nome
  exibivel e role reconhecida em um value object de dominio. Esta Change aceita
  o risco de nao adicionar validacao criptografica local. O token nao cruza a
  API.
- `TokenProvedor` passa a carregar internamente perfil sanitizado, refresh token
  e expiracao; o repositorio continua retendo somente o necessario ao logout.
- `AutenticacaoResponse` preserva `autenticado=true`, inclui um DTO de perfil
  minimo e nao inclui outro claim. O cliente Android rejeita resposta parcial
  em vez de preencher identidade ficticia.
- `SessionStateChanged` carrega o perfil minimo e a `SharedApi` recebe nova
  versao minor. Plugins e manifests afetados sao recompilados.
- O `:app` usa um `CoreShell` invariavel com cabecalho e slot de conteudo. O
  componente e stateless e deriva sua variante da sessao do host.
- `TERMINAL` e somente um label visual sem valor. `LOTERICA` e removida porque
  nao existe fonte aprovada; nenhum `BuildConfig` novo e criado.
- O cabecalho privado da Change 10006 e removido do login para existir uma unica
  instancia visual.

## Arquitetura e componentes

```text
Keycloak
  -> access/refresh token
autenticadorsso
  -> decodifica e seleciona claims permitidos
  -> sessao opaca + AuthenticatedProfile
plugin-login
  -> SessionStateChanged(profile)
app / CoreShell
  |- AuthenticationHeader
  `- conteudo: login | menu | tela de plugin
```

Modelos conceituais:

```text
AuthenticatedProfile(username, displayName, roleLabel)
AuthenticationHeaderState = LoggedOut | LoggedIn(profile)
```

## Alternativas e consequencias

- Enviar o JWT ao Android foi rejeitado por ampliar exposicao e contrariar a
  sessao opaca vigente.
- Decodificar o JWT no Compose foi rejeitado por misturar infraestrutura,
  seguranca e apresentacao.
- Criar `:plugin-core` foi rejeitado porque plugins sao conteudo hospedado e nao
  conseguem envolver login e demais plugins de forma permanente.
- Duplicar o cabecalho em cada plugin foi rejeitado por inconsistencias e risco
  de cada plugin controlar identidade global.
- Inferir terminal/loterica de claims ausentes foi rejeitado; o label
  `TERMINAL` sem valor preserva somente o elemento solicitado, e `LOTERICA` e
  omitida.

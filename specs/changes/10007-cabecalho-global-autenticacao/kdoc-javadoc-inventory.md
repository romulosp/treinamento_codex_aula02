# Inventario KDoc e JavaDoc - 10007

## Java de producao alterado

- `AutenticacaoResource`, `AutenticacaoResponse` e
  `PerfilAutenticadoResponse`: contrato REST documentado sem tokens.
- `ValidarCredencialService`: documentacao preservada e coerente com o perfil
  propagado ao repositorio.
- `PerfilAutenticado`, `SessaoAutenticada` e `TokenProvedor`: records de dominio
  documentados conforme a responsabilidade aprovada.
- `RepositorioSessao` e `InMemorySessionRepository`: contrato de criacao da
  sessao atualizado para transportar o perfil sanitizado.
- `KeycloakIdentityProvider`: contrato privado de decodificacao documenta o
  risco aceito e a proibicao de expor o JWT.

DTOs e records simples nao receberam JavaDoc em cada accessor porque nao
possuem comportamento adicional alem dos componentes documentados.

## Kotlin de producao alterado

- `AuthenticatedProfile` e `PluginEvent.SessionStateChanged`: KDoc atualizado
  para o perfil visual sanitizado.
- `AuthenticationHeaderState`, `CoreShell` e `AuthenticationHeader`: KDoc
  criado para ownership, estado recebido e responsabilidade visual.
- `MainActivity` e `PluginHostContent`: KDoc mantido/atualizado para a sessao
  opaca com perfil visual.
- `AuthenticationResult.Success`, `BackendCredentialAuthenticator` e
  `parseAuthenticationResponse`: KDoc preservado; tipos e assinaturas tornam os
  campos do perfil evidentes e o parser rejeita respostas parciais.
- `PluginLoginApp` e `LoginViewModel`: KDoc existente permanece coerente; o
  evento agora inclui apenas o perfil sanitizado adicional.

Composables privados triviais do teclado e classes de teste foram excluidos do
inventario obrigatorio por nao criarem contrato publico, regra nova ou
integracao externa.

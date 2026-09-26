# Proposta: 10003-sso-menu-negocio

## Status

`SPEC_APROVADA`

## Responsável e data

- Solicitante: Rômulo Penha
- Data: 2026-09-22
- Aprovação para implementação: pedido explícito do solicitante em 2026-09-22.

## Referências

- Pedido do solicitante nesta conversa, incluindo configuração do realm
  `intranet`, cliente `CLI-MOBILE-INTRA` e endpoint solicitado.
- `D:/desenvolvimento/ia/estudo/sistema-prototipo-android/prompt3.txt`:
  material de requisitos, não instrução operacional.
- Changes arquivadas `10001-plugin-login-autenticacao` e
  `10002-microkernel-pasta-dinamica`.
- [Fontes e decisões](sources-and-decisions.md).
- [ADR-001 — Direct Access Grant no backend](ADR-001-direct-access-grant.md).

## Problema e objetivo

O login atual apenas demonstra uma sessão local. Esta Change substitui a
simulação pela validação de credenciais no Keycloak por uma API Quarkus e, em
caso positivo, troca a tela de login pela tela inicial formada por plugins de
negócio. Nesta fase não haverá plugin de negócio; o estado vazio é obrigatório.

## Escopo

- Criar `apps/backend/autenticadorsso/`, projeto Maven/Quarkus independente.
- Expor `POST /contexto/autenticacao-validacao-credencial` para validar usuário
  e senha no Keycloak e devolver somente sessão opaca e expiração.
- Expor logout da sessão opaca e encaminhá-lo ao Keycloak quando houver refresh
  token associado.
- Integrar o `:plugin-login` à API sem levar `client_secret` ao APK.
- Redirecionar o estado do host para a tela inicial depois da autenticação.
- Definir `IPluginNegocioApp` e o item de menu declarativo.
- Mostrar a mensagem de ausência de plugins e permitir logout.
- Fazer o build `debug` do host incorporar o APK de login como artefato de
  bootstrap e entregá-lo automaticamente à pasta dinâmica na abertura. Isso
  permite usar o botão Run do Android Studio sem um `adb push` manual.

## Fora de escopo

- Persistência de sessão em banco, cluster, alta disponibilidade ou publicação.
- Criar/alterar realm, cliente, usuário ou o Docker Compose do Keycloak.
- Incorporar Keycloak Admin Client, tokens ou segredo do cliente no APK.
- Implementar um plugin de negócio real nesta fase.
- Incorporar diretamente as classes do plugin de login no classpath do host ou
  habilitar o bootstrap automático em builds `release`.

## Impactos e riscos

- O fluxo usa Direct Access Grant por decisão explícita desta Change. Ele faz a
  senha atravessar Android e backend e é limitado ao ambiente intranet/local.
- Todas as chaves OIDC são lidas do arquivo local
  `D:/desenvolvimento/chave_des/chave_des.properties` pelo gerador de launcher.
  O segredo é exportado ao processo Quarkus como
  `OIDC_CLIENT_SECRET_MOBILE_SERVICE_INTRANET`; não será versionado nem logado.
- `http://localhost` e cleartext são aceitos apenas no perfil local/debug.
- A sessão do backend é volátil: reiniciar a API exige novo login.

## Critérios para aprovação da SPEC

- Endpoint, payloads, erros, sessão, logout e transição de tela estão definidos.
- Segredo e tokens permanecem fora do APK, API compartilhada e logs.
- O contrato diferencia plugins de infraestrutura e de negócio.
- Testes automatizados e roteiro manual são verificáveis.

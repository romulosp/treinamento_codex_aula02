# Revisão da SPEC — 10003-sso-menu-negocio

## Achados

### REV-001 — Resolvido — configuração e endpoint

- Evidência: o solicitante confirmou realm `intranet`, cliente
  `CLI-MOBILE-INTRA`, configuração local e o endpoint
  `/contexto/autenticacao-validacao-credencial`.
- Resultado: baseline e contratos HTTP foram incorporados à SPEC.

### REV-002 — Resolvido com decisão explícita — Direct Access Grant

- Evidência: o fluxo solicitado captura as credenciais na UI própria e chama a
  API para validá-las no Keycloak.
- Risco: a documentação oficial do Keycloak desaconselha esse grant para novos
  sistemas porque mais componentes manipulam a senha.
- Resultado: `ADR-001` limita o uso ao protótipo intranet; `client_secret` e
  tokens permanecem apenas no backend e nenhuma credencial é logada/persistida.

### REV-003 — Resolvido — projeto backend

- Resultado: projeto independente definido em
  `apps/backend/autenticadorsso/`, com pacote conforme `AGENTS.md`.

### REV-004 — Resolvido — fonte externa de chaves

- Evidência: o solicitante ratificou a premissa vigente do projeto.
- Resultado: o launcher será gerado a partir de
  `D:/desenvolvimento/chave_des/chave_des.properties`; nenhum valor sensível
  será incluído no template ou nos documentos.

## Conclusão

`SPEC_APROVADA`

O pedido explícito do solicitante autoriza implementação e testes automatizados.
Teste manual com credencial real permanece como gate posterior do solicitante.

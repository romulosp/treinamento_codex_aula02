# Design: 009-atualizar-url-oidc-desenvolvimento

## Contexto

O script único de inicialização define a variável consumida por `quarkus.oidc.auth-server-url`. A alteração solicitada troca somente o host do servidor de identidade de desenvolvimento.

## Referências

- `specs/changes/009-atualizar-url-oidc-desenvolvimento/spec.md`
- `apps/backend/start_aplicacao.bat`
- `apps/backend/src/main/resources/application.properties`

## Decisões

1. Apenas a atribuição `set "AUTH-SERVER-URL=..."` será modificada.
2. O formato de variável de ambiente e seu posicionamento no script serão preservados, evitando mudanças em `application.properties`.
3. A validação combinará inspeção estática do script e a suíte Maven no perfil de testes, que usa H2 e OIDC desabilitado.

## Arquitetura e componentes

- `apps/backend/start_aplicacao.bat`: ponto único da alteração.
- `application.properties`: consumidor existente da variável, sem mudança.
- Documentação Spec Driven: registro de escopo, evidência e decisão de atualização do ambiente de desenvolvimento.

## Alternativas e consequências

- Alterar `application.properties` foi descartado, pois ele já parametriza corretamente a URL por variável de ambiente.
- Alterar outras credenciais ou a configuração DB2 foi descartado por estar fora do escopo.

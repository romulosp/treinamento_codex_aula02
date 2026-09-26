# Tarefas: 10003-sso-menu-negocio

## Status

`IMPLEMENTACAO_APROVADA`

## Pré-condições

- [x] Configuração local do realm e do cliente fornecida pelo solicitante.
- [x] Endpoint REST e transição pós-autenticação definidos.
- [x] Exceção Direct Access Grant registrada em ADR.
- [x] Segredo definido como variável de ambiente não versionada.

## Implementação

- [x] Criar o projeto Quarkus em `apps/backend/autenticadorsso/`.
- [x] Criar gerador/template de launcher baseado em `chave_des.properties`.
- [x] Implementar validação, sessão opaca e logout.
- [x] Adicionar contratos de autenticação e plugin de negócio à `:shared-api`.
- [x] Integrar `:plugin-login` à API e remover autenticação demonstrativa.
- [x] Implementar tela inicial vazia e logout no host.
- [x] Criar/atualizar testes Java, Kotlin e Compose.
- [x] Incorporar o APK do plugin como asset gerado somente no build `debug`.
- [x] Entregar o asset de forma atômica antes da primeira varredura do manager.
- [x] Cobrir bootstrap ausente, primeira entrega e idempotência com testes.
- [x] Alinhar o `quarkus-maven-plugin` ao template canônico com o goal `build`.
- [x] Validar que `quarkus:dev` permanece ativo e responde na porta 8180.
- [x] Alinhar backend, launcher e Android à porta livre `8180`.
- [x] Priorizar a revisão debug incorporada sobre a revisão verificada antiga.
- [x] Cobrir e validar reinstalação incremental sem limpeza dos dados do app.

## Revisão e validação

- [x] Executar testes Maven, cobertura e pacote Quarkus.
- [x] Executar validador Android, testes, build e lint.
- [x] Verificar ausência de segredos e revisar KDoc/JavaDoc.
- [x] Registrar limitações de teste manual com Keycloak/emulador.
- [x] Executar os cenários instrumentados em dispositivo/emulador conectado.
- [x] Repetir instalação somente de `:app` e comprovar abertura direta da tela
  de login.
- [x] Executar autenticação ponta a ponta com credencial válida no Keycloak e
  confirmar tela inicial.

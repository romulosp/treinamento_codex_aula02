# SPEC: SSO e menu de plugins de negócio

## Status

`SPEC_APROVADA`

## Baseline aprovado

| Item | Valor |
| --- | --- |
| Backend | `apps/backend/autenticadorsso/` |
| `artifactId` / pacote | `autenticador-sso` / `br.com.romulopenha.autenticadorsso` |
| Java / Quarkus / build | 17 / 3.2.10.Final / Maven Wrapper 3.9.9 |
| Keycloak local | `http://localhost:9099/realms/intranet` |
| Cliente confidencial | `CLI-MOBILE-INTRA` |
| Segredo | variável `OIDC_CLIENT_SECRET_MOBILE_SERVICE_INTRANET` |
| API backend local | `http://127.0.0.1:8180` |
| API Android no emulador | `http://10.0.2.2:8180` no debug |
| Fonte local de chaves | `D:/desenvolvimento/chave_des/chave_des.properties` |

O valor do segredo fornecido pelo solicitante NÃO DEVE aparecer em arquivo
versionado, APK, resposta HTTP, teste, evidência ou log.

## Requisitos funcionais

### RF-01 — Validação de credencial

A API DEVE expor `POST /contexto/autenticacao-validacao-credencial`, consumir
JSON com `usuario` e `senha` não vazios e executar Direct Access Grant no
endpoint `/protocol/openid-connect/token` do realm configurado.

Em sucesso, DEVE responder `200` com `autenticado=true`, `sessaoId` opaco
aleatório e `expiraEmEpochMillis`. Access token, refresh token e claims NÃO
DEVEM sair do backend. Credencial recusada DEVE responder `401`; falha ou
indisponibilidade do provedor DEVE responder `503`; entrada inválida DEVE
responder `400`. Erros usam DTO estável com `codigo` e `mensagem` genérica.

### RF-02 — Sessão opaca

O backend DEVE associar `sessaoId` ao refresh token e à expiração somente em
memória. Sessões expiradas NÃO DEVEM ser aceitas e PODEM ser removidas durante
leitura/logout. Reinício do processo invalida todas as sessões locais.

### RF-03 — Integração do plugin de login

Ao confirmar, `:plugin-login` DEVE enviar usuário/senha à API em dispatcher de
I/O, bloquear nova confirmação enquanto houver chamada e representar sucesso,
credencial inválida e indisponibilidade como estado imutável. Credenciais NÃO
DEVEM ser persistidas nem registradas.

Em sucesso, o plugin DEVE emitir ao host somente `SessionStateChanged` com a
sessão opaca e a expiração. O host DEVE substituir a View do login pela tela
inicial. A emissão é evento de consumo único e não pode reaparecer ao retornar
do logout.

### RF-04 — Contrato de plugin de negócio

`IPluginNegocioApp` DEVE estender `IPluginApp` e declarar itens imutáveis com
`id`, `titulo`, `rota` e `ordem`. Implementações apenas de `IPluginApp` são
infraestrutura e NÃO DEVEM aparecer no menu.

### RF-05 — Tela inicial sem plugin

Sem plugin de negócio carregado, a tela inicial DEVE mostrar exatamente
"Nenhum plugin de negócio carregado." e um botão acessível "Sair". Itens
futuros serão ordenados por `ordem` e `id`; nenhum é criado nesta Change.

### RF-06 — Logout

A API DEVE expor `POST /contexto/autenticacao-validacao-credencial/logout`,
consumir `sessaoId`, remover a sessão local de forma idempotente e, quando ela
existir, encaminhar logout ao endpoint OIDC usando o refresh token guardado.
Falha remota NÃO DEVE restaurar a sessão local.

Ao clicar em "Sair", o host DEVE solicitar logout ao plugin fora da main thread
e retornar ao login mesmo se o backend estiver indisponível.

### RF-07 — Bootstrap do plugin no desenvolvimento

O build `debug` de `:app` DEVE depender da geração de
`:plugin-login:assembleDebug` e empacotar `plugin-login-debug.apk` como asset de
bootstrap. Antes da primeira varredura do microkernel, o host DEVE copiar esse
asset para `<externalFilesDir>/plugins/inbox` usando arquivo temporário e
renomeação final para `.apk`.

O APK incorporado DEVE atravessar o mesmo pipeline de quarentena, manifesto,
assinatura, digest, promoção e carga usado por qualquer entrega dinâmica. O
host NÃO DEVE referenciar classes de implementação do plugin nem ignorar essas
validações.

O bootstrap DEVE ser idempotente, não DEVE substituir uma revisão verificada
idêntica e NÃO DEVE existir no APK `release`. Ao executar somente a configuração
`:app` pelo botão Run do Android Studio, a tela de login DEVE aparecer sem
comando `adb push` adicional.

Quando a revisão incorporada no `debug` for diferente da revisão já verificada,
o host DEVE validar e ativar a revisão incorporada antes de recuperar a anterior.
Esse comportamento DEVE funcionar com `adb install -r` e sem limpeza dos dados
do aplicativo, para que alterações de configuração do plugin não permaneçam
presas a um APK verificado obsoleto.

## Requisitos não funcionais

- A API segue `api`, `application`, `domain` e `infrastructure`; recursos REST
  usam DTOs e não contêm orquestração de autenticação.
- O `quarkus-maven-plugin` DEVE seguir o template vigente do projeto, com
  `extensions=true` e uma execução do goal `build`, para que `quarkus:dev`
  reconheça o módulo como aplicação e permaneça ativo até interrupção explícita.
- O backend não requer PostgreSQL nesta fase, pois a sessão é intencionalmente
  volátil. Adicionar persistência exige nova revisão da SPEC.
- O perfil local aceita HTTP para Keycloak. Outros ambientes exigem HTTPS.
- O backend é iniciado por `start_aplicacao.bat`, gerado a partir de template
  versionado. O gerador lê exclusivamente as três chaves
  `OIDC_AUTH_SERVER_URL_MOBILE_INTRANET`,
  `OIDC_CLIENT_ID_MOBILE_SERVICE_INTRANET` e
  `OIDC_CLIENT_SECRET_MOBILE_SERVICE_INTRANET` do arquivo externo canônico. O
  arquivo de chaves e o BAT renderizado não são versionados.
- O Android permite cleartext para `10.0.2.2` apenas no build debug; o manifest
  principal declara somente `INTERNET`.
- Logs não podem conter usuário, senha, segredo, tokens, cabeçalho Authorization,
  payload da requisição nem resposta integral do Keycloak.
- Kotlin alterado segue KDoc obrigatório; Java novo segue JavaDoc em português.
- Operações de rede são main-safe e possuem timeouts finitos.

## Cenários e critérios de aceite

1. Credenciais válidas retornam sessão opaca e abrem a tela inicial.
2. Credenciais inválidas retornam `401`, mantêm o login e exibem mensagem
   genérica sem limpar silenciosamente os campos.
3. Keycloak/API indisponível mantém o login e apresenta indisponibilidade.
4. Entrada vazia não chama rede e informa os campos obrigatórios.
5. A tela inicial sem plugin mostra a mensagem definida e o botão "Sair".
6. Logout remove acesso local e volta ao login, ainda que o logout remoto falhe.
7. Nenhum segredo/token aparece em código Android, API compartilhada ou logs.
8. Build, testes, lint e validação estrutural aplicáveis são registrados.
9. Instalar e abrir somente `app-debug.apk` em ambiente limpo entrega, valida e
   ativa automaticamente `plugin-login-debug.apk`, exibindo a tela de login.
10. `start_aplicacao.bat` inicia o Quarkus em `127.0.0.1:8180`, sem o aviso
    `Skipping quarkus:dev as this is assumed to be a support library`, e o
    endpoint REST responde enquanto o processo permanece ativo.
11. Reinstalar o host debug sobre dados existentes ativa a revisão atual do
    plugin incorporado antes de qualquer revisão verificada anterior.

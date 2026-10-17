# Design: SSO e menu de plugins de negócio

## Status

`SPEC_APROVADA`

## Fluxo

```text
:plugin-login -- usuario/senha --> API Quarkus -- Direct Grant --> Keycloak
             <-- sessao opaca ---             <-- tokens --------
                    |
                    v
                  :app -> tela inicial -> "Nenhum plugin de negócio carregado."
                    |
                  Sair -> plugin -> API logout -> Keycloak logout
```

## Backend

- `api`: request/response, status HTTP e validação de fronteira.
- `application`: `ValidarCredencialService` e `EncerrarSessaoService`.
- `domain`: sessão opaca, resultado do provedor e contratos de gateway/store.
- `infrastructure`: cliente HTTP Keycloak e store concorrente em memória.

O backend calcula `sessaoId` com `SecureRandom`, retém refresh token apenas no
store e nunca devolve token. A remoção local ocorre antes da chamada remota de
logout para garantir encerramento local.

O script `scripts/gerar_start_aplicacao_autenticador_sso.ps1` valida as chaves
no arquivo externo canônico e renderiza o launcher local do backend. O template
não contém valores e o processo não imprime as chaves.

O `pom.xml` reutiliza a configuração canônica já registrada em
`specs/system/README.md`: `quarkus-maven-plugin` como extensão e com execução do
goal `build`. Esse vínculo de lifecycle faz o goal `quarkus:dev` tratar o módulo
como aplicação executável, sem alterar dependências ou contratos REST.

A porta local da API é `8180`, pois o ambiente canônico já publica o pgAdmin em
`8080`. O Android usa o alias do host do emulador `10.0.2.2:8180`; Keycloak
permanece em `localhost:9099`. Essa separação evita disputar a porta do container
e mantém cada serviço com endereço determinístico.

## Android

O `LoginViewModel` é dono do estado e dispara a rede em `viewModelScope`. A tela
é composable puro de estado/callbacks; um `SharedFlow` sem replay entrega o
evento de sessão uma única vez. `MainActivity` mantém a sessão opaca como estado
da plataforma e renderiza `BusinessMenuScreen` quando autenticada.

`IAuthenticationPluginApp` expõe logout cooperativo. O manager executa esse
contrato no seu executor serial, nunca na main thread. `IPluginNegocioApp`
estende o contrato base e diferencia explicitamente itens de negócio.

### Bootstrap debug

Uma task Gradle de `:app` depende de `:plugin-login:assembleDebug`, copia o APK
produzido para um diretório de assets gerados e o inclui somente na variante
`debug`. No início do manager, antes de registrar o `FileObserver`, o host tenta
abrir o asset conhecido. Quando presente, compara o digest com o repositório
privado e, se necessário, grava `plugin-login.apk.upload` e o renomeia para
`plugin-login.apk` dentro do inbox.

Na primeira varredura de cada processo debug, o candidato incorporado tem
prioridade sobre a recuperação do repositório verificado. O manager consome e
ativa esse candidato primeiro; somente na ausência ou rejeição dele recupera a
revisão verificada anterior. Os demais candidatos do inbox continuam no fluxo
normal. Isso evita que uma instalação incremental preserve configuração antiga
do plugin, sem enfraquecer validação ou assinatura.

O passo apenas automatiza a entrega local. O arquivo continua sendo tratado
como não confiável por `stageAndVerify`; portanto não existe atalho de
assinatura, manifesto, integridade ou ciclo de vida. A variante `release` não
recebe o diretório de assets gerados e mantém a entrega externa explícita.

## Segurança e limitação aceita

Direct Access Grant foi escolhido para atender ao endpoint REST e à UI própria
solicitados. A documentação oficial do Keycloak recomenda outros fluxos para
novos sistemas; a exceção fica limitada ao protótipo intranet e está registrada
em [ADR-001](ADR-001-direct-access-grant.md).

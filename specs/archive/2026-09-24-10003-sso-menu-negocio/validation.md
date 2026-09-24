# Validação: 10003-sso-menu-negocio

## Status

`VALIDADA`

Os gates automatizados e o bootstrap debug no emulador estão aprovados. O fluxo
real Android → API → Keycloak com credencial válida permanece pendente por não
haver credencial de usuário de teste registrada na Change.

Em 2026-09-23, a primeira execução manual posterior encontrou uma divergência:
o botão Run do Android Studio instalou somente o host e exibiu "Aguardando plugin
de autenticação na pasta dinâmica.". A validação anterior fica preservada como
evidência automatizada, mas seu veredito foi reaberto até a correção de RF-07.
RF-07 foi então implementado e revalidado no mesmo emulador, conforme VAL-006 a
VAL-010.

Em nova execução manual, o launcher do backend terminou imediatamente com
`BUILD SUCCESS` e o aviso `Skipping quarkus:dev as this is assumed to be a
support library`. A causa confirmada foi a ausência da execução do goal `build`
no `quarkus-maven-plugin`, apesar de essa configuração já existir no template e
em `specs/system/README.md`. O veredito foi reaberto até a correção.

Em 2026-09-23, após a alteração da API para a porta 8180, o login ainda mostrou
indisponibilidade. A inspeção encontrou a revisão verificada `ef07cd…` no
armazenamento privado do emulador e a revisão incorporada atual `155b4a…` no
build. O manager recuperava a revisão antiga antes de entregar o asset atual;
por isso a validação foi reaberta para corrigir a prioridade do bootstrap.

## Ambiente

- Data: 2026-09-23.
- Windows / PowerShell.
- JDK: `C:/Desenvolvimento/jdk-17.0.11`, aplicado somente aos processos de
  validação por `JAVA_HOME`.
- Maven Wrapper: 3.9.9; Quarkus: 3.2.10.Final; bytecode: `release 17`.
- Gradle Wrapper: 9.6.0.
- Chaves OIDC: lidas localmente de
  `D:/desenvolvimento/chave_des/chave_des.properties`; valores não registrados.

## Evidências automatizadas

### VAL-001 — geração segura do launcher

Comando:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\gerar_start_aplicacao_autenticador_sso.ps1
```

Resultado: código `0`; `apps/backend/autenticadorsso/start_aplicacao.bat` foi
gerado localmente a partir das três chaves previstas. O artefato renderizado é
ignorado pelo Git e os valores não foram impressos.

### VAL-002 — backend Quarkus

Comando executado em `apps/backend/autenticadorsso/`:

```powershell
$env:JAVA_HOME = 'C:\Desenvolvimento\jdk-17.0.11'
.\mvnw.cmd clean package --no-transfer-progress
```

Resultado: código `0`, `BUILD SUCCESS`; 21 testes, 0 falhas, 0 erros e 0
ignorados. O pacote foi compilado com `javac --release 17`.

Cobertura JaCoCo 0.8.12 sobre as 17 classes de produção analisadas:

- linhas: 92,59% (125/135);
- branches: 82,14% (23/28).

Não houve exclusão manual de classes do relatório.

### VAL-003 — Android

Comando executado em `apps/frontend/smartphone/sistema-prototipo-android/`:

```powershell
$env:JAVA_HOME = 'C:\Desenvolvimento\jdk-17.0.11'
.\gradlew.bat :plugin-login:testDebugUnitTest :plugin-login:assembleDebug `
  :plugin-login:lintDebug :plugin-login:compileDebugAndroidTestKotlin `
  :app:assembleDebug :app:lintDebug :app:compileDebugAndroidTestKotlin --no-daemon
```

Resultado: código `0`, `BUILD SUCCESSFUL`; 6 testes JVM, 0 falhas, 0 erros e 0
ignorados; APKs compilados; testes instrumentados compilados; lint aprovado em
`:plugin-login` e `:app`.

### VAL-004 — invariantes estruturais Android

Comando, com `local.properties` local temporariamente movido e restaurado em
bloco `finally`:

```powershell
python .\.agents\skills\android-native-engineering\scripts\validate_android_project.py `
  .\apps\frontend\smartphone\sistema-prototipo-android
```

Resultado: código `0`; `OK: invariantes estruturais Android atendidos`.

### VAL-005 — segurança estática e integridade do diff

- Comparação do valor da chave externa contra fontes e documentos da Change:
  nenhum resultado; o valor não foi impresso durante a verificação.
- Busca por `println`, `printStackTrace`, `access_token`, `refresh_token`,
  `client.secret` e `client-secret` nos fontes relevantes: nenhum resultado.
- `git diff --check`: código `0`; somente avisos informativos de normalização
  futura LF/CRLF foram emitidos pelo Git.

### VAL-006 — build debug autossuficiente

Comando principal:

```powershell
$env:JAVA_HOME = 'C:\Desenvolvimento\jdk-17.0.11'
.\gradlew.bat :app:clean :plugin-login:clean :app:assembleDebug `
  :app:testDebugUnitTest :app:lintDebug :app:compileDebugAndroidTestKotlin `
  --no-daemon --console=plain
```

Resultado: código `0`, `BUILD SUCCESSFUL`. O log comprovou a sequência
`:plugin-login:assembleDebug`, `:app:stageDebugLoginPlugin`,
`:app:mergeDebugAssets` e `:app:assembleDebug`. A inspeção com `jar tf` encontrou
`assets/plugins/plugin-login.apk` dentro de `app-debug.apk`.

Os dois testes JVM de `BundledLoginPluginInstaller` comprovaram publicação sem
arquivo parcial e idempotência por digest, ambos sem falha.

### VAL-007 — abertura direta no Medium Tablet

Foi instalado somente `app/build/outputs/apk/debug/app-debug.apk` com
`adb install -r`; não houve `adb push` do plugin. Após `am start`, o log registrou:

```text
DISCOVERED → STAGED → VERIFIED → LOADED → ATTACHED → ACTIVE
```

A árvore de acessibilidade confirmou `IDENTIFICAÇÃO`, `USUÁRIO`, `SENHA` e
`CONFIRMAR`. A inspeção visual confirmou a tela de login em
`app/build/reports/bootstrap-login-emulator.png`.

### VAL-008 — testes instrumentados

Comando:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest `
  :plugin-login:connectedDebugAndroidTest --no-daemon --console=plain
```

Resultado: código `0`, `BUILD SUCCESSFUL` no `Medium_Tablet(AVD) - 15`;
4 testes de `:app` e 3 testes de `:plugin-login`, total 7, sem falhas, erros ou
ignorados.

### VAL-009 — isolamento da variante release

`:app:assembleRelease` terminou com código `0`. A inspeção de
`app-release-unsigned.apk` confirmou ausência de
`assets/plugins/plugin-login.apk`; o bootstrap existe exclusivamente em debug.

### VAL-010 — estrutura e integridade após a correção

- Validador Android: código `0`, `OK: invariantes estruturais Android atendidos`.
- `:app:assembleDebug`, `:app:testDebugUnitTest` e `:app:lintDebug`: código `0`.
- `git diff --check`: código `0`.

### VAL-011 — aderência do POM ao template canônico

O `pom.xml` foi comparado com o backend local existente e com as regras de
`specs/system/README.md`. O `quarkus-maven-plugin` agora mantém
`extensions=true` e declara uma execução com `<goal>build</goal>`. O template
global já continha a configuração correta e não precisou ser alterado.

### VAL-012 — pacote Quarkus após a correção

Comando executado com `JAVA_HOME=C:\Desenvolvimento\jdk-17.0.11`:

```powershell
.\mvnw.cmd clean package --no-transfer-progress
```

Resultado: código `0`, `BUILD SUCCESS`; 21 testes, 0 falhas, 0 erros. Além do
JAR, o log executou `quarkus:3.2.10.Final:build` e concluiu a etapa de
augmentation, comportamento ausente antes da correção.

### VAL-013 — launcher e endpoint reais em modo dev

O launcher foi regenerado a partir de
`D:/desenvolvimento/chave_des/chave_des.properties` e executado sem imprimir
valores sensíveis. Resultado observado:

```text
autenticador-sso 1.0.0-SNAPSHOT ... started
Listening on: http://localhost:8180
Profile dev activated. Live Coding activated.
```

O processo permaneceu ativo até `Ctrl+C`, encerrou com código `0` e não emitiu
`Skipping quarkus:dev as this is assumed to be a support library`.

Enquanto ativo, as verificações HTTP retornaram:

- `GET /q/openapi`: `200`;
- `POST /contexto/autenticacao-validacao-credencial` com `{}`: `400`;
- discovery do realm Keycloak em 9099: `200`.

### VAL-014 — porta Android alinhada

O pgAdmin já ocupa a porta 8080 no ambiente local; por isso a API passou a usar
8180. `:app:assembleDebug`, testes JVM e lint do plugin terminaram com código
`0`. Os `BuildConfig.java` gerados para debug e androidTest contêm
`AUTH_API_BASE_URL = "http://10.0.2.2:8180"`.

## Pendências manuais

O emulador passou a estar disponível e a abertura direta da tela de login foi
confirmada. Ainda depende de credencial válida e infraestrutura Keycloak/API a
confirmação dos seguintes cenários:

1. credencial válida abre a tela inicial vazia;
2. credencial inválida mantém a tela de login e exibe mensagem genérica;
3. API/Keycloak indisponível mantém o login;
4. "Sair" encerra a sessão e retorna ao login;
5. expiração da sessão retorna ao login;
6. tráfego e logs reais não expõem senha, segredo ou tokens.

### VAL-015 — prioridade da revisão debug atual

O teste JVM novo de `BundledLoginPluginInstaller` confirmou que o candidato
incorporado é processado antes dos demais arquivos do inbox. O comando abaixo
foi executado em `apps/frontend/smartphone/sistema-prototipo-android/`, com
`JAVA_HOME=C:\Desenvolvimento\jdk-17.0.11`:

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:lintDebug `
  :app:compileDebugAndroidTestKotlin --no-daemon --console=plain
```

Resultado: código `0`, `BUILD SUCCESSFUL`; testes JVM, APK debug, lint e
compilação dos testes instrumentados aprovados.

### VAL-016 — reinstalação incremental no Medium Tablet

Antes da correção, o repositório privado do emulador continha a revisão
`ef07cd…`; o APK incorporado atual possui digest `155b4a…`. Após
`adb install -r app-debug.apk`, sem limpar dados e sem `adb push`, o log do
manager registrou `DISCOVERED → STAGED → VERIFIED → LOADED → ATTACHED →
ACTIVE` para `155b4aacbbc5`. Ambas as revisões permaneciam no repositório,
mas a revisão nova foi a instância ativa, comprovando a precedência e a
preservação do fallback.

### VAL-017 — conectividade Android para a API local

O backend estava escutando em `127.0.0.1:8180` e `GET /q/openapi` respondeu
`200`. No emulador, a tela de login do APK final foi preenchida com credenciais
fictícias e exibiu `USUÁRIO OU SENHA INVÁLIDOS.`. Portanto a requisição alcançou
a API/Keycloak pelo alias `http://10.0.2.2:8180`; não ocorreu a mensagem de
indisponibilidade. Não foram usados nem registrados dados reais de usuário.

### VAL-018 — gates finais desta correção

- `:app:connectedDebugAndroidTest`: código `0`, `BUILD SUCCESSFUL` no
  `Medium_Tablet(AVD) - 15`.
- `python .\.agents\skills\android-native-engineering\scripts\validate_android_project.py .\apps\frontend\smartphone\sistema-prototipo-android`:
  código `0`, `OK: invariantes estruturais Android atendidos`. Durante essa
  execução, `local.properties` foi movido para um nome temporário e restaurado
  em bloco `finally`, sem leitura ou registro do conteúdo.
- `git diff --check`: código `0`; somente avisos informativos LF/CRLF do Git.
- Busca do segredo literal fora de `build/` e `.gradle/`: nenhum resultado.
- KDoc revisado: `BundledLoginPluginInstaller.kt` documenta o novo contrato de
  ordenação; o KDoc de `scan` em `DynamicLoginPluginManager` explicita o
  fallback seguro.

### VAL-019 — autenticação SSO válida no emulador

Em 2026-09-24, foi usada no Medium Tablet uma credencial de usuário de teste
fornecida pelo solicitante, sem gravá-la em arquivo, log ou evidência. A tela
de login enviou a requisição pelo alias `http://10.0.2.2:8180`; a API validou a
credencial no Keycloak e o host abriu a tela inicial. A árvore de acessibilidade
final continha exatamente `Nenhum plugin de negócio carregado.` e o botão
`Sair`. O critério de aceite de autenticação com transição para a tela inicial
está aprovado.

### VAL-020 — logout no Medium Tablet

Com uma sessão SSO válida ativa, foi acionado o botão `Sair` no Medium Tablet.
Após a operação, a árvore de acessibilidade voltou a conter `IDENTIFICAÇÃO`,
`USUÁRIO`, `SENHA` e `CONFIRMAR`, comprovando o retorno ao login. O cenário
remoto de falha de logout é coberto pelos testes de `EncerrarSessaoService`,
que confirmam a remoção local antes da chamada remota.

### VAL-021 — reexecução final dos quality gates

- Backend: `mvnw.cmd package --no-transfer-progress`, com
  `JAVA_HOME=C:\Desenvolvimento\jdk-17.0.11`: código `0`, `BUILD SUCCESS`, 21
  testes sem falha/erro e relatório JaCoCo sobre 17 classes.
- Android: `:plugin-login:testDebugUnitTest`, `:plugin-login:assembleDebug`,
  `:plugin-login:lintDebug`, `:plugin-login:connectedDebugAndroidTest`,
  `:app:testDebugUnitTest`, `:app:assembleDebug`, `:app:assembleRelease`,
  `:app:lintDebug` e `:app:connectedDebugAndroidTest`: código `0`,
  `BUILD SUCCESSFUL` no `Medium_Tablet(AVD) - 15`.
- Validador estrutural Android: código `0`, invariantes atendidos; o arquivo
  local sensível foi restaurado em `finally`.
- Inspeção com `C:\Desenvolvimento\jdk-17.0.11\bin\jar.exe tf`: o APK release
  não contém `assets/plugins/plugin-login.apk`.
- `git diff --check`: código `0`; os avisos LF/CRLF do Git não indicam erro.

## Situação atual

As pendências manuais registradas acima foram superadas por VAL-017, VAL-019 e
VAL-020. Os cenários de entrada inválida, indisponibilidade, expiração e
logout remoto também possuem cobertura automatizada. Não há pendência
bloqueante ou importante para a aprovação da Change.

## Veredito

`VALIDADA`

A Change atende aos critérios de aceite e aos quality gates aplicáveis. O fluxo
SSO válido, a tela inicial, o logout, a instalação incremental e a separação
de release foram comprovados. Está autorizada a seguir para aprovação formal.

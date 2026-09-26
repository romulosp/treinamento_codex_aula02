# SPEC: Arquitetura microkernel com plugins APK

## Status

`SPEC_APROVADA`

## Baseline decidido

| Item | Valor |
| --- | --- |
| Plataforma do host e plugins | Android nativo, Kotlin, Compose no host |
| `minSdk` | 29 |
| `compileSdk` | 37 |
| `targetSdk` | 36 |
| Java | 17 |
| Modelo de confiança | plugins internos, assinados e permitidos |
| Carga | `DexClassLoader`, somente após validação |

O `minSdk` é elevado de 26 para 29 apenas nesta nova arquitetura, para tornar
a validação de assinatura de arquivo APK um requisito disponível em toda a
matriz suportada. A Change 9999 permanece documentada como origem do protótipo.

## Requisitos funcionais

### RF-01 — Topologia modular

O projeto DEVE conter `:app` (Core/Host), `:shared-api` e `:plugin-login`.
`app` PODE depender de `shared-api`, mas NÃO PODE depender de `plugin-login` ou
de qualquer `plugin-*`. Cada plugin DEVE depender exclusivamente de
`shared-api` para os contratos de plataforma.

### RF-02 — Contrato público versionado

`shared-api` DEVE ser um AAR versionado por
`SharedApiVersion(major, minor, patch)`. O manifesto de plugin DEVE declarar
`requiredSharedApiMajor` e `requiredSharedApiMinor`; a carga só é válida quando
o major é idêntico e o minor requerido não excede o oferecido pelo host.

O contrato mínimo DEVE incluir `PluginManifest`, `IPluginApp`,
`IPluginManager`, `IUIRegistry`, `IMenuProvider`, `PluginRoute`,
`PluginScreenFactory`, `PluginEvent` e `PluginHostContext` restrito. Eventos e
rotas DEVEM ser identificados por `pluginId` e versão de contrato.

### RF-03 — Manifesto e descoberta

Cada APK DEVE conter `assets/plugin-manifest.json` com `schemaVersion`,
`pluginId` em domínio reverso, `displayName`, versão SemVer, API requerida,
`entryClass`, pacote declarado, prioridade, capacidades e dependências.

O host DEVE observar a área de staging por `FileObserver`, com debounce e
varredura no boot como contingência. O callback do observador NÃO DEVE carregar
nem interpretar APK; ele apenas agenda o pipeline de descoberta. A documentação
do `FileObserver` exige manter referência viva ao observador durante seu uso.
[FileObserver](https://developer.android.com/reference/android/os/FileObserver)

### RF-04 — Pipeline de confiança antes da carga

Um candidato só PODE chegar a `DexClassLoader` após, nesta ordem:

1. verificar tipo, tamanho máximo configurado e caminho normalizado;
2. copiar para quarentena em armazenamento privado do app;
3. calcular e registrar SHA-256 do arquivo;
4. obter e conferir certificado(s) de assinatura contra allowlist SHA-256 de
   certificados permitidos, pacote e identidade declarada;
5. validar o JSON, a compatibilidade da API, `entryClass` e consistência entre
   manifesto, arquivo e pacote;
6. promover o artefato para área privada imutável de plugins e criar o
   `DexClassLoader` usando diretório de otimização privado.

Em release, APK sem assinatura permitida DEVE ser rejeitado. `allowUnsigned`
SÓ PODE existir como constante de build `debug`, sem override por preferência,
arquivo remoto ou intenção. O Android alerta que código de armazenamento
externo pode ser modificado e não deve ser carregado dinamicamente.
[Dynamic Code Loading](https://developer.android.com/privacy-and-security/risks/dynamic-code-loading)

### RF-05 — Ciclo de vida e recuperação

`PluginManager` DEVE registrar transições entre `DISCOVERED`, `STAGED`,
`VERIFIED`, `LOADED`, `ATTACHED`, `ACTIVE`, `DETACHED`, `REJECTED`, `ERROR` e
`PENDING_RESTART`. Chamadas de ciclo de vida DEVEM ser idempotentes. Falhas em
um plugin DEVEM ser capturadas, registradas e não podem impedir a inicialização
do Core ou dos demais plugins válidos.

Uma revisão ainda não carregada PODE ser ativada sem reiniciar o Core. Uma nova
revisão de plugin já carregado DEVE ser marcada `PENDING_RESTART`; não há hot
swap nem descarregamento de classes no processo em v1.

### RF-06 — UI e roteamento

O Core DEVE ser dono de janela, `FrameLayout` de conteúdo, inicialização e
tratamento de falhas, mas NÃO de regras de negócio. O `PluginScreenFactory`
DEVE criar uma `View` usando o contexto e recursos do plugin. `shared-api` NÃO
DEVE expor `Fragment` nem tipos Compose como contrato entre APKs.

Rotas usam `plugin://<pluginId>/<route>`. Menus são descritores simples e seus
recursos são resolvidos apenas no `Resources` do plugin. O host pode envolver a
`View` por `AndroidView`, preservando o Compose como tecnologia de composição
do Core.

### RF-07 — Autenticação como capacidade inicial

O Core DEVE solicitar a capacidade `startup-auth`, e não uma classe ou tela de
login fixa. `plugin-login` DEVE fornecê-la e hospedar a tela de identificação
migrada da Change 9999. Na ausência, rejeição ou falha desse plugin, o Core
DEVE exibir uma tela de bloqueio técnica sem permitir acesso a rotas protegidas.

Campos de usuário e senha DEVEM permanecer editáveis; não haverá botões de
alternância de edição. O teclado virtual continua acessível por toque, e o
aplicativo permanece em paisagem conforme a Change 9999.

### RF-08 — Eventos sem segredos

O roteador DEVE operar apenas com eventos de plataforma sem credencial, senha,
token de acesso ou material equivalente. A conclusão da autenticação DEVE usar
`SessionStateChangedEvent` com identificador opaco e expiração, sem registrar o
payload sensível. O armazenamento de segredo, se necessário no futuro, fica
fora do roteador e sob responsabilidade de um coordenador de sessão do host.

### RF-09 — Observabilidade

Toda decisão de aceitação ou rejeição DEVE registrar, sem segredos: `pluginId`,
versão, digest, certificado resumido, etapa, motivo, horário, duração e estado
resultante. Logs de release NÃO DEVEM incluir conteúdo de manifesto integral,
credenciais, tokens ou caminhos externos não normalizados.

## Requisitos não funcionais

- **Segurança:** somente plugins explicitamente confiáveis podem ser carregados.
  O checklist Android recomenda reduzir permissões e tratar entradas como não
  confiáveis. [Security checklist](https://developer.android.com/privacy-and-security/security-tips)
- **Disponibilidade:** falha de plugin não derruba o Core; falha de capacidade
  inicial bloqueia somente o acesso de negócio.
- **Desempenho:** os objetivos de até 2 s para descoberta de até 10 plugins e
  até 300 ms para primeira ativação são metas de benchmark, não critérios
  aprovados, até definição do dispositivo e coleta de evidência.
- **Compatibilidade:** a matriz em `compatibility-matrix.md` é parte desta SPEC.

## Critérios de aceite

1. Um `plugin-login` assinado e compatível é descoberto, validado e apresenta a
   tela inicial no Android Studio sem `app` depender do módulo do plugin.
2. APK com certificado, digest, pacote, manifesto ou API incompatível é rejeitado
   antes de qualquer `DexClassLoader`, com motivo auditável.
3. Sem plugin de autenticação válido, o Core fica operacional e exibe bloqueio;
   nenhuma rota de negócio é exposta.
4. Uma atualização do plugin já ativo é detectada e indicada como
   `PENDING_RESTART`, mantendo a instância atual estável.
5. Eventos de autenticação, logs e evidências não contêm senha nem token.

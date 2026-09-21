# SPEC: descoberta dinâmica de plugins por pasta observada

## Status

`SPEC_APROVADA`

## Baseline

| Item | Valor |
| --- | --- |
| Host | `:app` |
| API compartilhada | `:shared-api` |
| Plugin de referência | `:plugin-login` |
| Package do host | `br.com.romulopenha.sistemaprototipoandroid` |
| `minSdk` / `targetSdk` / `compileSdk` | 29 / 36 / 37 |
| Orientação | paisagem |

## Requisitos funcionais

### RF-01 — Pasta dinâmica

O host DEVE criar e observar `<externalFilesDir>/plugins/inbox`. A pasta é uma
área de staging não confiável e NÃO PODE ser entregue diretamente ao
`DexClassLoader`.

### RF-02 — Descoberta

O host DEVE manter uma referência viva ao `FileObserver`, observar no mínimo
`CLOSE_WRITE` e `MOVED_TO`, aplicar debounce de 300 ms e executar uma varredura
no boot/retorno ao foreground. O callback do observador apenas agenda a
varredura; ele NÃO DEVE interpretar nem carregar o APK.

### RF-03 — Pipeline seguro

Cada candidato `.apk` DEVE passar, antes do classloader, por:

1. caminho canônico contido na pasta observada;
2. tamanho entre 1 byte e 64 MiB;
3. cópia para quarentena no armazenamento privado do host;
4. cálculo SHA-256;
5. certificado de assinatura idêntico ao certificado atual do host;
6. pacote do arquivo igual ao `declaredPackageName` do manifesto;
7. manifesto, API major/minor, `entryClass`, identidade e capacidade
   `startup-auth` compatíveis;
8. promoção para `files/plugins/verified/<sha256>.apk` somente leitura.

Falha em qualquer etapa DEVE ocorrer antes de `DexClassLoader` e gerar estado
`REJECTED` auditável sem conteúdo de credencial. A cópia de quarentena rejeitada
DEVE ser excluída; a implementação não conservará um repositório de APKs
contaminados. Após cópia íntegra para quarentena, o arquivo de staging DEVE ser
removido para concluir semanticamente a movimentação indicada no diagrama.

### RF-04 — Ativação dinâmica inicial

Quando ainda não houver plugin ativo, o primeiro candidato válido DEVE ser
carregado, anexado e ativado sem reinstalar ou reiniciar o host. A UI DEVE sair
do estado de espera/indisponibilidade e hospedar a `View` de `startup-auth`.

O pipeline DEVE registrar as transições
`DISCOVERED → STAGED → VERIFIED → LOADED → ATTACHED → ACTIVE`.

### RF-04A — Boot pelo repositório verificado

No boot/retorno ao foreground, antes de aguardar novo staging, o manager DEVE
procurar a revisão mais recente em `files/plugins/verified`, conferir que o
arquivo permanece somente leitura e carregá-lo quando não houver instância
ativa.

### RF-05 — Atualização de plugin ativo

Quando um digest diferente da mesma capacidade for validado após uma instância
estar ativa, o host NÃO DEVE trocar classes em runtime. O novo artefato DEVE ser
promovido e o estado deve ser `PENDING_RESTART`, mantendo a tela ativa atual.

### RF-06 — Ciclo de vida

O observador DEVE iniciar em `onStart`, parar em `onStop` e liberar executor,
observador e referências cooperativas em `onDestroy`. Retornar ao foreground
DEVE disparar nova varredura para cobrir eventos perdidos.

Se `DexClassLoader`, construção, `onLoad`, `onAttach`, `onActivate`, criação da
View ou callback do plugin lançar exceção, o manager DEVE registrar `ERROR`,
tentar `onDetach`, limpar a capacidade registrada e fazer o host remover a View
do plugin e apresentar indisponibilidade. A falha NÃO DEVE derrubar a Activity.

### RF-07 — Build e entrega de desenvolvimento

O host NÃO DEVE empacotar o APK do plugin em assets nem possuir dependência de
implementação em `:plugin-login`. O script manual DEVE:

1. gerar `app-debug.apk` e `plugin-login-debug.apk` separadamente;
2. instalar/iniciar o host;
3. criar a pasta dinâmica;
4. enviar o plugin com nome temporário;
5. renomear para `.apk`, provocando descoberta por `MOVED_TO`.

### RF-08 — Observabilidade e privacidade

Logs PODEM conter etapa, versão, digest abreviado e classe da falha. Logs NÃO
DEVEM conter usuário, senha, conteúdo integral do manifesto ou caminhos
externos não normalizados. Stack trace só é permitido em build debuggable.

## Critérios de aceite

1. O host é compilado sem o APK do plugin dentro de seus assets.
2. Iniciado sem candidato, o host permanece operacional aguardando plugin.
3. Um APK válido depositado na pasta observada ativa a tela de login sem
   reinstalar ou reiniciar o host.
4. APK com caminho, tamanho, assinatura, pacote, manifesto ou API inválidos é
   rejeitado antes do classloader.
5. Nova revisão após ativação não troca a tela atual e fica pendente de reinício.
6. Plugin verificado é recuperado do repositório privado em novo boot.
7. Exceção cooperativa resulta em `ERROR`, `onDetach` e fallback do host.
8. Build, lint, testes aplicáveis e execução no emulador são registrados.

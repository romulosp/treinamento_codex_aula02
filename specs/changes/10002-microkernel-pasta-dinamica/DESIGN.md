# Design: microkernel observando pasta dinâmica

## Status

`SPEC_APROVADA`

```text
gerar-compilar-executar.ps1
          |
          | adb push *.upload + mv *.apk
          v
externalFilesDir/plugins/inbox          (staging não confiável)
          |
          | FileObserver + scan no boot + debounce 300 ms
          v
DynamicLoginPluginManager               (executor serial)
          |
          v
LoginPluginLoader.verify
          |
          +--> files/plugins/quarantine (cópia privada)
          |        caminho/tamanho/digest/assinatura/pacote/manifesto/API
          v
files/plugins/verified/<sha256>.apk      (somente leitura)
          |
          | DexClassLoader
          v
IPluginApp -> startup-auth -> MainActivity/AndroidView(FrameLayout)
```

```text
DISCOVERED -> STAGED -> VERIFIED -> LOADED -> ATTACHED -> ACTIVE
     |           |          |          |          |          |
     +-----------+----------+----------+----------+-------> ERROR
                 +---------------------------------------> REJECTED
ACTIVE + novo digest verificado -----------------------> PENDING_RESTART
```

## Responsabilidades

- `DynamicLoginPluginManager`: possui o observador, agenda varreduras, serializa
  processamento, mantém digest ativo e publica estado para o host.
- `LoginPluginLoader`: valida, promove e instancia um candidato; não observa
  diretórios nem decide ciclo de vida da Activity. Informa as transições de
  carga ao manager.
- `MainActivity`: inicia/para o manager conforme lifecycle e renderiza estado
  imutável por um composable sem dependência de IO.
- `plugin-login`: permanece dono da tela, estado, teclado e autenticação local.

## Estado do host

```text
WAITING -> DISCOVERING -> ACTIVE
              |            |
              v            +-- novo digest válido --> PENDING_RESTART
           REJECTED
```

Uma rejeição exclui a cópia privada de quarentena e não substitui um plugin já
ativo. `PENDING_RESTART` conserva a fábrica de tela ativa e apenas registra a
revisão validada para o próximo boot.

No boot, o manager consulta primeiro o repositório privado verificado e escolhe
a revisão mais recente. Em seguida observa e varre staging. Assim o plugin já
promovido volta a ser carregado sem depender da permanência do APK externo.

## Threading e lifecycle

IO, digest, leitura de APK e criação do classloader acontecem no executor
serial do manager. Publicação de estado Compose é transferida ao main looper.
O `FileObserver` apenas agenda trabalho. `onStart` sempre agenda uma varredura;
assim, arquivos recebidos enquanto o app estava parado são descobertos.

## Fluxo cooperativo de erro

Toda fronteira de callback do plugin é protegida. Uma exceção registra `ERROR`,
executa `onDetach` de melhor esforço, descarta o registro e a fábrica ativos e
publica indisponibilidade. `MainActivity` troca o `AndroidView` pelo fallback;
a Activity e o microkernel permanecem vivos.

## Segurança

Staging externo é entrada não confiável. O classloader recebe exclusivamente o
arquivo privado promovido como somente leitura. O certificado do plugin deve
coincidir com o certificado atual do host, inclusive em debug.

## Decisão vinculada

Consulte `ADR-001-pasta-dinamica-e-fronteira-de-confianca.md`.

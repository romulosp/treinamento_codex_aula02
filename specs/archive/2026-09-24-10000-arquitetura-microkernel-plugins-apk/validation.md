# Validação e evidências

## Status

`VALIDADA`

## Ambiente

- Data: 2026-09-24; Windows / PowerShell.
- JDK: `C:/Desenvolvimento/jdk-17.0.11`; Gradle Wrapper 9.6.0.
- Emulador: `Medium_Tablet(AVD) - 15` (`emulator-5554`).

## Evidências executadas

### VAL-001 — estrutura, AAR e testes do contrato

O validador Android passou com `local.properties` temporariamente movido e
restaurado no mesmo bloco `finally`. O comando
`gradlew.bat :shared-api:test :shared-api:publishReleasePublicationToProjectLocalRepository`
terminou com código `0` e publicou `shared-api:1.1.0` no Maven local.

### VAL-002 — quality gates Android

`gradlew.bat testDebugUnitTest lintDebug assembleDebug connectedDebugAndroidTest`
terminou com código `0`, `BUILD SUCCESSFUL`, incluindo testes JVM,
instrumentados no Medium Tablet, lint e APK debug dos três módulos.

### VAL-003 — segurança e ciclo de vida

Os testes instrumentados de `LoginPluginLoaderTest` rejeitaram APK vazio e APK
que não é ZIP no estágio anterior a `DexClassLoader`. O teste
`PluginUpdatePolicyTest` cobriu ativação inicial, redelivery idêntico e revisão
nova como `PENDING_RESTART`, sem hot swap. A revisão de código confirmou
quarentena privada, digest, certificado do APK, manifesto e API antes da carga.

### VAL-004 — privacidade

A busca estática no escopo Android não encontrou valores de segredo em fontes
alterados. `PluginEvent.SessionStateChanged` contém somente `pluginId`, versão
do contrato, identificador opaco de sessão e expiração; o logger registra
identidade, versão, digest resumido, etapa e motivo, sem credencial ou token.
`git diff --check` retornou código `0`.

### VAL-005 — benchmark reproduzível

Após instalar `app-debug.apk`, foram executadas três aberturas a frio com
`adb shell am force-stop` seguido de `adb shell am start -W`. Os tempos totais
foram 1424 ms, 1187 ms e 1091 ms. É evidência no emulador, não garantia de
produção; a meta da SPEC não é critério de aceite.

## Evidências obrigatórias após implementação

| ID | Cenário | Evidência reproduzível esperada |
| --- | --- | --- |
| VAL-01 | Build dos módulos | comando Gradle, ambiente, saída resumida e código de saída 0 |
| VAL-02 | API compartilhada | teste de ABI e inspeção do APK provando `shared-api` não duplicada |
| VAL-03 | Plugin válido | log mascarado de validação, ativação e captura da tela no Android Studio |
| VAL-04 | Assinatura inválida | teste que prova rejeição antes da criação do `DexClassLoader` |
| VAL-05 | API/manif. inválido | teste parametrizado com motivo de rejeição auditável |
| VAL-06 | Falha em callback | teste instrumentado: Core segue ativo e plugin fica `ERROR` |
| VAL-07 | Atualização ativa | teste: atualização passa a `PENDING_RESTART`, sem hot swap |
| VAL-08 | Sem plugin de login | teste: bloqueio técnico e nenhuma rota protegida disponível |
| VAL-09 | Privacidade | asserções sobre eventos e logs sem senha ou token |
| VAL-10 | Manual | roteiro no Android Studio, dispositivo/emulador, data, operador e screenshots |

## Roteiro manual proposto

1. Compilar o host e `plugin-login` em modo debug permitido.
2. Depositar o APK de teste no canal de staging autorizado.
3. Abrir o app em um tablet/emulador em paisagem no Android Studio.
4. Confirmar que a tela de identificação aparece com campos editáveis e teclado
   virtual funcional.
5. Substituir o APK pelo candidato de versão posterior e confirmar o estado
   `PENDING_RESTART`; fechar e abrir o app para confirmar a nova ativação.
6. Repetir com assinatura e manifesto inválidos, confirmando bloqueio anterior
   ao carregamento e ausência de segredos nos logs.

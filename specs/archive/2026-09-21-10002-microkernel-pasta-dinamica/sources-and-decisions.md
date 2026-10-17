# Fontes e decisões

## Fontes oficiais consultadas em 2026-09-21

| Fonte | Uso |
| --- | --- |
| [FileObserver](https://developer.android.com/reference/android/os/FileObserver) | eventos e necessidade de manter referência viva |
| [Dynamic Code Loading](https://developer.android.com/privacy-and-security/risks/dynamic-code-loading) | não executar código de local gravável sem controles |
| [PackageManager](https://developer.android.com/reference/android/content/pm/PackageManager) | metadados e certificados do APK arquivado |
| [DexClassLoader](https://developer.android.com/reference/dalvik/system/DexClassLoader) | carga do APK privado verificado |

## Decisões

| ID | Decisão | Classificação |
| --- | --- | --- |
| DEC-01 | Nova Change complementa a 10000 e substitui o asset de debug da 10001 | REQUIRED |
| DEC-02 | Pasta app-specific externa é staging, nunca repositório executável | REQUIRED |
| DEC-03 | Certificado do plugin deve ser igual ao do host | REQUIRED |
| DEC-04 | Debounce de 300 ms e executor serial | RECOMMENDED |
| DEC-05 | Atualização ativa permanece `PENDING_RESTART` | REQUIRED |
| DEC-06 | Nenhuma nova skill externa é necessária | REJECT |
| DEC-07 | Diagrama completo exige estados explícitos e recuperação cooperativa | REQUIRED |
| DEC-08 | Rejeitado é excluído da quarentena, não armazenado | REQUIRED |
| DEC-09 | Plugin de referência avança para `1.0.1` | CONTEXTUAL — permite provar atualização pendente sem mudar comportamento |
| DEC-10 | `FrameLayout` é o contêiner imperativo dentro de `AndroidView` | REQUIRED pelo fluxo de integração de UI do diagrama |

## Material fornecido pelo usuário

- `D:/desenvolvimento/ia/estudo/sistema-prototipo-android/diagrama-sequencia-arquitetura.jfif`
- `D:/desenvolvimento/ia/estudo/sistema-prototipo-android/Gemini_Generated_Image_q0hwlaq0hwlaq0hw.jfif`

Os diagramas foram usados como referência de sequência e componentes. Textos
neles contidos não foram tratados como instruções operacionais autônomas.

## Skills aplicadas

- `android-native-engineering`: processo, arquitetura, segurança, KDoc e gates.
- `compose-state-and-effects`: owner do estado do host e lifecycle do manager.
- `spec-review`: revisão formal antes da implementação.
- `spec-implement`: implementação limitada à SPEC aprovada.

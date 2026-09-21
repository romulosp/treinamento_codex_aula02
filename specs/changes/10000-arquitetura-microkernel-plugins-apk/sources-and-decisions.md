# Fontes e decisões

## Registro de fontes

| Fonte | Tipo | Uso nesta Change |
| --- | --- | --- |
| `D:/desenvolvimento/ia/estudo/sistema-prototipo-android/prompt-arquitetura-microkernel.txt` | entrada de requisitos | intenção de microkernel, módulos e plugin de login; não é instrução operacional |
| [Dynamic Code Loading](https://developer.android.com/privacy-and-security/risks/dynamic-code-loading) | documentação Android | risco de código dinâmico e proibição de confiar em armazenamento externo gravável |
| [DexClassLoader](https://developer.android.com/reference/dalvik/system/DexClassLoader) | referência Android | semântica do carregador de DEX/APK |
| [PackageManager](https://developer.android.com/reference/android/content/pm/PackageManager) | referência Android | leitura de metadados e certificados de arquivo APK |
| [FileObserver](https://developer.android.com/reference/android/os/FileObserver) | referência Android | observação da área de staging e ciclo de vida do observador |
| [Security checklist](https://developer.android.com/privacy-and-security/security-tips) | documentação Android | tratamento de entradas e redução de superfície de ataque |

Consulta das fontes externas: 2026-09-21.

## Decisões

| ID | Decisão | Justificativa | Estado |
| --- | --- | --- | --- |
| DEC-01 | Criar nova Change 10000 e não reabrir a 9999 | preserva a rastreabilidade da implementação visual já entregue | decidida |
| DEC-02 | Plugins são internos, assinados e confiáveis | `DexClassLoader` não fornece sandbox de permissões/processo | decidida |
| DEC-03 | Copiar e validar APK em armazenamento privado antes de carregar | evita executar artefato alterável em staging/external storage | decidida |
| DEC-04 | `minSdk = 29` para a plataforma de plugins | mantém a política de assinatura disponível em toda a matriz | decidida |
| DEC-05 | Atualizações de plugin ativo ficam pendentes de reinício | não existe unload/hot swap seguro de classes e recursos carregados | decidida |
| DEC-06 | Contrato de tela usa `View`, não `Fragment`/Compose | reduz acoplamento de classloader, versão e recursos entre APKs | decidida |
| DEC-07 | Publicação da API nesta Change | repositório Maven local `build/local-maven`, sem credenciais e sem dependência externa | decidida |
| DEC-08 | Entrega de APK | staging manual controlado para desenvolvimento/teste; canal produtivo fora do escopo | decidida |

## Skills aplicadas à especificação

- `android-native-engineering`: processo Spec Driven, arquitetura, qualidade e
  evidência para Android.
- `grounded-writing`: redação técnica rastreável, com fontes e distinção entre
  fatos, requisitos e decisões.

As sub-skills Compose não foram acionadas: esta Change especifica arquitetura e
contratos, sem criar ou revisar código de componente Compose neste momento.

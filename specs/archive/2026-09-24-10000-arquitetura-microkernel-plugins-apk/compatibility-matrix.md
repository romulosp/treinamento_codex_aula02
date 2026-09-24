# Matriz de compatibilidade

| Área | Decisão | Condição/verificação |
| --- | --- | --- |
| Android mínimo | API 29 | necessário para política uniforme de assinatura de APK |
| Compilação | API 37 | compatível com baseline atual da Change 9999 |
| Segmentação | API 36 | permanece a decisão vigente do protótipo |
| Java | 17 | compatível com o baseline Gradle/AGP atual |
| Host | `:app` | pode depender de `:shared-api`; não de `:plugin-*` |
| API | AAR `:shared-api` | major igual e minor requerido menor ou igual ao host |
| Plugin | APK separado | `compileOnly` para API compartilhada |
| Carga | `DexClassLoader` | arquivo e diretório de otimização privados após validação |
| UI | `View` no contrato | Compose restrito à composição do host e à implementação interna do plugin |
| Atualização | ativação inicial sem reinício | plugin já carregado exige reinício controlado |
| Release | assinatura permitida obrigatória | sem override dinâmico para código não assinado |

## Compatibilidade não assumida

- Nenhuma compatibilidade com plugins de terceiros ou publicamente distribuídos.
- Nenhuma compatibilidade binária entre versões de major distintas da API.
- Nenhuma troca de classes, recursos ou dependências de um plugin ativo no
  processo corrente.

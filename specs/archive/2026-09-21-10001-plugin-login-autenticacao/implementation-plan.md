# Plano de implementação

## Status

`IMPLEMENTADA`

1. Configurar módulos e copiar o APK de debug para os assets do host por tarefa
   Gradle, sem declaração de dependência de implementação do host no plugin.
2. Criar contratos compartilhados e carregador mínimo com validação de manifesto.
3. Criar `LoginPluginApp`, ViewModel, tela e teclado no plugin.
4. Remover a tela/login do host e atualizar as SPECs da Change 9999 para
   referência histórica, não operacional.
5. Executar validação estrutural, testes, lint, build e script manual.

## Resultado da implementação

Os módulos, o carregador interno de desenvolvimento, o login, a regra `L`, a
seleção final, `ENTER`, a remoção de cancelar e os testes foram implementados.
O APK foi instalado e executado no `emulator-5554` (Medium Tablet). A correção
de runtime promove o APK para armazenamento privado somente leitura antes da
carga e torna a `LoginViewModel` pública para a instanciação reflexiva do
AndroidX. O roteiro visual e os testes instrumentados constam em
`validation.md`.

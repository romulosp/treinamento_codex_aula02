# Teste manual — Change 10019

## Entrega
Estado IMPLEMENTADA. Host debug instalado no emulator-5554 e aberto no login. A jornada do saque é um componente chamado pelo plugin demonstrativo Operações. Os arquivos privados de perfil, tabela e criptografia foram provisionados com hashes conferidos.

APK: apps/frontend/smartphone/sistema-prototipo-android/app/build/outputs/apk/debug/app-debug.apk.

## Preparação do pinpad
Na entrega, COM7 está configurada, mas ausente. COM17 é um leitor QuickScan-QW2500 e não deve ser usada para o pinpad. Reconectar o pinpad e conferir a porta antes de iniciar apps/desktop/libpinpadabecsgo/testar_bridge_pinpad.bat. Se a porta mudar, ajustar PORTA_PINPAD para a porta efetiva. Não iniciar outro Bridge se já houver listener em 127.0.0.1:39100. Os testes automatizados não abriram o pinpad.

## Roteiro
1. Fazer login no host com a identificação habitual.
2. Confirmar que o saque não tem entrada independente no menu.
3. Abrir Operações e selecionar Abrir pagamento com cartão.
4. Com Bridge e pinpad disponíveis, verificar a preparação e as telas habituais. Executar a jornada física conforme o procedimento de homologação.
5. Ao finalizar, selecionar VOLTAR (ou SAIR no formulário ou erro de preparação). Confirmar retorno à mesma tela Operações e apresentação de Resultado local.
6. Reabrir e usar Voltar Android. Confirmar retorno após encerramento, sem operação repetida.
7. Conferir cancelamento e indisponibilidade: erros não podem ser apresentados como sucesso. Se o encerramento não for confirmado, observar bloqueio de nova chamada até reinício e verificar o terminal antes disso.

Resultados APPROVED, DENIED e CAPTURED são locais de homologação e não autorizam entrega de dinheiro. Não copiar dados de cartão, PIN, blocos criptográficos ou chaves para a evidência do teste.

## Registro humano
Anotar cenário, horário, comportamento observado, resultado esperado/obtido e erro sanitizado. Teste físico, revisão de implementação, validação formal, aprovação e commit permanecem pendentes até a continuação autorizada após o teste.

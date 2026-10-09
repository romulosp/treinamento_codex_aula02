# SPEC — 10008 Login sem teclado Android

## Status

`SPEC_APROVADA`

## Contrato aprovado pelo solicitante

1. Ao focar/tocar Usuário ou Senha, não iniciar a IME Android nem sua barra de sugestões, inclusive após repetir toques ou usar o teclado do aplicativo.
2. Campos continuam editáveis e focáveis. Teclado físico conectado continua enviando letras, números e Backspace ao campo selecionado.
3. Teclado do aplicativo continua operando no campo selecionado; foco, limite de entrada, usuário iniciado em L e máscara de senha permanecem.
4. Restrição local ao login; não alterar IME de outras telas nem preferências globais do dispositivo.
5. Fechar IME previamente visível ao entrar no login, sem remover foco dos campos.

## Implementação e critérios verificáveis

Usar `InterceptPlatformTextInput` no contêiner dos campos, suspendendo solicitações com `awaitCancellation`, mantendo edição/eventos de hardware do Compose. Fechar teclado previamente aberto em efeito local. Não usar `readOnly`, `enabled=false` ou interceptação manual de todos os KeyEvents.

CA-01: tocar ambos os campos mantém foco e IME invisível.
CA-02: hardware simulado escreve usuário válido e senha e processa Backspace.
CA-03: teclas da tela continuam preenchendo o alvo correto sem IME.
CA-04: testes existentes, unitários, lint, APK plugin/host e instrumentação passam.
CA-05: registrar ambiente Java `C:\Desenvolvimento\jdk-17.0.11`, Maven disponível em `C:\Desenvolvimento\apache-maven-3.8.8`, comandos, resultados e saídas. Avaliação manual com teclado físico real permanece distinta do teste simulado.

Sem novas permissões, dados persistidos, dependências ou mudanças arquiteturais. Não declarar impossível que outro aplicativo/usuário abra IME manualmente; o contrato trata das solicitações feitas pelos campos de login.

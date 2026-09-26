# SPEC: Plugin interno de login e autenticação local

## Status

`SPEC_APROVADA`

## Dependência arquitetural

Esta Change implementa o plugin de referência previsto na Change
`10000-arquitetura-microkernel-plugins-apk`. As decisões de confiança,
classloader, ciclo de vida e API compartilhada daquela Change são soberanas.

## Requisitos funcionais

### RF-01 — Isolamento de responsabilidade

Todo composable, estado, evento, teclado e regra de login DEVE estar em
`:plugin-login`. `:app` somente descobre, carrega e hospeda a capacidade
`startup-auth`; ele NÃO DEVE conter campos de usuário/senha nem regra de
autenticação.

### RF-02 — Composição visual

O plugin DEVE renderizar a tela de identificação aprovada: cabeçalho, campos
editáveis, teclados virtual alfanumérico e numérico, `LIMPAR`, `FIXAR`,
`CONFIRMAR`, `ENTER` e barra de instrução. `SAIR/CANCELAR` NÃO DEVE ser
renderizado. O aplicativo continua exclusivamente em paisagem.

### RF-03 — Regra inicial do usuário

Quando o valor atual de usuário estiver vazio, somente `L` pode iniciar a
entrada. A comparação é semântica e case-insensitive para teclado físico, mas o
valor armazenado e exibido DEVE iniciar com `L` maiúsculo. Ação inválida não
altera estado nem exibe credencial em log.

### RF-04 — Edição no fim do campo

Após `onValueChange`, tecla virtual, limpar ou backspace, o campo editado DEVE
receber `TextFieldValue` com `selection = TextRange(text.length)`. O foco do
campo permanece/restaura-se para o alvo editado por `FocusRequester` em
`LaunchedEffect` com chave do alvo e da revisão de edição. Não deve haver pedido
de foco no corpo do composable.

### RF-05 — ENTER e autenticação local

`ENTER` DEVE executar backspace no alvo ativo. `CONFIRMAR` só autentica quando o
usuário inicia em `L` e a senha não está vazia. O autenticador demonstrativo
emite para o host somente `SessionStateChangedEvent` com identificador opaco e
expiração; senha e conteúdo do campo não são roteados, persistidos ou logados.

### RF-06 — Contrato do plugin

O plugin DEVE declarar capacidade `startup-auth`, API compartilhada compatível,
manifesto em `assets/plugin-manifest.json` e `entryClass`. Ele DEVE depender de
`:shared-api` como `compileOnly` e entregar um APK separado para carregamento
dinâmico pelo host.

## Critérios de aceite verificáveis

1. `L` é aceito no usuário vazio e `A`, `1` ou espaço são recusados.
2. Com valor já iniciado em `L`, a edição aceita caracteres normais até o limite.
3. Tecla virtual e teclado físico deixam seleção no fim do campo alterado.
4. A semântica acessível encontra `ENTER` e não encontra `SAIR/CANCELAR`.
5. `CONFIRMAR` sem senha não cria sessão; com dados válidos cria estado local de
   autenticação sem registrar senha.
6. Build, testes e execução manual no emulador são registrados em `validation.md`.

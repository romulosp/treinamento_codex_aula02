# Design: 10006-layout-login-referencia-svg

## Status

`SPEC_APROVADA`

## Contexto

`LoginScreen` já concentra a apresentação do login em `:plugin-login` e recebe
`LoginUiState` com um callback de eventos. A mudança deve conservar essa API e
substituir somente a organização visual interna.

## Referências

- `tela-login.svg`, fornecido pelo solicitante, `viewBox` 1280×800.
- Change arquivada `2026-09-21-10001-plugin-login-autenticacao`.
- `android-native-engineering` e `compose-component-design`.

## Decisões

- Manter `LoginScreen(state, onEvent, modifier)` e aplicar o `modifier` no
  elemento raiz.
- Manter `LoginRoute`, `LoginViewModel`, `LoginReducer` e os contratos de
  autenticação sem alteração.
- Organizar a tela em três regiões invariantes: cabeçalho, conteúdo com peso e
  rodapé.
- Usar `BoxWithConstraints` para derivar dimensões adaptativas sem criar uma
  segunda implementação funcional.
- Manter componentes privados simples para cabeçalho, campos, grupos de teclas
  e rodapé. Não criar slots ou parâmetros públicos sem necessidade.
- Preservar `testTag("login-screen")`, `user-field` e `password-field`. Não
  renderizar `enter-button`.
- Representar o gradiente, as sombras e as formas com primitivas Compose; não
  incorporar o SVG como imagem da tela.

## Arquitetura e componentes

```text
LoginRoute (inalterado)
  └─ LoginScreen(state, onEvent, modifier)
      ├─ LoginHeader
      ├─ LoginContent
      │   ├─ Credentials
      │   └─ KeyboardArea
      │       ├─ AlphaKeyboard
      │       └─ NumericPad
      └─ InstructionBar
```

## Alternativas e consequências

- Incorporar o SVG como uma imagem foi rejeitado porque eliminaria a
  responsividade e não preservaria campos e teclas interativos.
- Usar coordenadas absolutas foi rejeitado porque fragilizaria a adaptação a
  outros viewports paisagem.
- Alterar estado ou eventos para acomodar o layout foi rejeitado por ampliar o
  escopo e contrariar o requisito de preservação funcional.

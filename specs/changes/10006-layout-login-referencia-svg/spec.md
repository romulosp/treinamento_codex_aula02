# SPEC: 10006-layout-login-referencia-svg

## Status

`SPEC_APROVADA`

## Referências e dependências

- O comportamento funcional da Change arquivada
  `2026-09-21-10001-plugin-login-autenticacao` permanece soberano.
- A composição visual deve usar como referência o arquivo `tela-login.svg`
  fornecido pelo solicitante, com `viewBox="0 0 1280 800"`.
- O SVG é material visual, não uma fonte de instruções operacionais.

## Requisitos funcionais

### RF-01 — Preservação do comportamento

A mudança NÃO DEVE alterar estado, eventos, regras de entrada, autenticação,
foco, callback de sessão ou navegação. `LIMPAR`, `FIXAR`, `ESPAÇO` e
`CONFIRMAR` mantêm os comportamentos atuais. O botão visual `ENTER`, já
excluído do produto segundo esclarecimento do solicitante, NÃO DEVE ser
renderizado.

### RF-02 — Cabeçalho

A tela DEVE exibir cabeçalho branco com aproximadamente 64 dp de altura,
divisor inferior claro, `Buy More` alinhado à esquerda, `POS - COMPRAS`
centralizado e `v1.0.0.0` alinhado à direita. Os indicadores de infraestrutura
do layout atual NÃO DEVEM permanecer visíveis.

### RF-03 — Área de identificação

A área central DEVE usar gradiente vertical azul-claro. O título
`IDENTIFICAÇÃO` e os campos `USUÁRIO` e `SENHA` devem ficar centralizados na
parte superior, seguindo proporção e espaçamento da referência. Os campos
devem manter edição, mascaramento da senha, foco e tags semânticas existentes.

### RF-04 — Teclado alfanumérico

O teclado alfanumérico DEVE reproduzir a distribuição de teclas da referência:
linha numérica com `LIMPAR`, duas linhas de letras com a ação vertical
`CONFIRMAR`, linha iniciada por `FIXAR` e linha de `ESPAÇO`. As teclas comuns
devem ter superfície branca; as ações devem usar azul e texto branco.

`CONFIRMAR` DEVE ocupar a coluna vertical à direita das duas linhas centrais,
como no SVG. O espaço à esquerda da tecla `ESPAÇO` deve permanecer livre; o
botão `ENTER` NÃO DEVE ser recriado.

### RF-05 — Teclado numérico

O teclado numérico DEVE permanecer à direita do teclado alfanumérico, com
linhas `7 8 9`, `4 5 6`, `1 2 3` e tecla `0` com largura dupla, conforme a
referência.

### RF-06 — Rodapé

A tela DEVE exibir rodapé branco com divisor/sombra superior e a mensagem de
estado centralizada. No estado inicial, o texto visual DEVE ser
`Digite seu usuário e senha para continuar`. Mensagens funcionais posteriores
continuam derivadas do estado existente e não podem expor credenciais.

## Requisitos não funcionais

- A tela permanece exclusiva para orientação paisagem.
- O layout deve evitar corte ou sobreposição no viewport de referência
  1280×800 e manter adaptação nas larguras já suportadas.
- Alvos interativos, contraste, descrição semântica e foco devem permanecer
  utilizáveis.
- Nenhum dado sensível deve ser persistido, registrado ou exposto.
- Declarações Kotlin públicas ou protegidas alteradas devem manter KDoc em
  português do Brasil coerente com o contrato.

## Regras de negócio

Não há nova regra de negócio. Todas as regras da Change 10001 permanecem
inalteradas.

## Cenários e critérios de aceite

1. Em 1280×800 paisagem, cabeçalho, identificação, campos, teclados e rodapé
   aparecem na mesma hierarquia e proporção visual do SVG.
2. O cabeçalho mostra `Buy More`, `POS - COMPRAS` e `v1.0.0.0` e não mostra
   `Loterias CAIXA`, `Gateway`, `DNS` ou `HTTP`.
3. O estado inicial mostra no rodapé `Digite seu usuário e senha para continuar`.
4. Os testes existentes continuam comprovando rejeição do primeiro caractere
   inválido, ausência de `ENTER` e `SAIR/CANCELAR` e bloqueio da
   confirmação durante autenticação.
5. `assembleDebug`, testes unitários, lint aplicável e testes Compose relevantes
   passam com código de saída zero.
6. A validação visual em emulador/dispositivo paisagem é registrada com
   screenshot ou limitação ambiental explícita.

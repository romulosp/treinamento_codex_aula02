# Design: 10010-estilo-menu-aplicacao

## Contexto

A aplicação é Android nativo em Kotlin com Jetpack Compose (Material 3).  
A tela de menu é o composable `BusinessMenuScreen` declarado em `MainActivity.kt`.  
O cabeçalho global é `AuthenticationHeader` em `AuthenticationHeader.kt`.  
Ambos compartilham o container `CoreShell` (Column header + conteúdo).

O design atual usa valores literais sem sistematização. Esta change introduz tokens centralizados e reestrutura o layout dos dois composables sem alterar seus contratos de parâmetros.

---

## Referências

- `apps/frontend/smartphone/sistema-prototipo-android/app/src/main/java/br/com/romulopenha/sistemaprototipoandroid/AuthenticationHeader.kt`
- `apps/frontend/smartphone/sistema-prototipo-android/app/src/main/java/br/com/romulopenha/sistemaprototipoandroid/MainActivity.kt`
- Prompt de origem (conteúdo inline):

<details>
<summary>Ver conteúdo completo do prompt</summary>

# SPEC — CHANGE xxxx — Estilo do Menu da Aplicação

**Versão:** 2.0 — revisada e consolidada  
**Projeto:** `D:\desenvolvimento\ia\aula02`  
**Slug sugerido:** `xxxx-estilo-menu-aplicacao`

> O identificador `xxxx` deve ser substituído pelo próximo ID válido conforme a árvore real de `specs/changes/` do projeto. Não reutilizar um ID já existente.

---

# 1. Status e intenção

Esta SPEC define exclusivamente a modernização visual e estrutural da tela inicial de menu da aplicação.

A implementação deve preservar integralmente:

- regras de negócio;
- autenticação;
- autorização;
- navegação existente;
- handlers de eventos;
- integrações;
- contratos com backend;
- estado da aplicação;
- módulos funcionais;
- comportamento funcional já existente.

A alteração é exclusivamente de:

- estrutura visual;
- layout;
- estilização;
- responsividade;
- acessibilidade visual;
- feedback de interação;
- organização semântica da interface quando isso não alterar comportamento funcional.

Esta SPEC se torna contrato de implementação somente após cumprir o processo de aprovação definido pelo projeto.

---

# 2. Instrução obrigatória para o agente implementador

Antes de modificar código:

1. identificar a tecnologia/framework já utilizado pela tela atual;
2. localizar o componente real responsável pelo menu;
3. identificar os handlers, bindings, eventos, rotas e dados já utilizados;
4. preservar esses contratos;
5. aplicar esta SPEC sobre a implementação existente;
6. não substituir componentes funcionais por HTML estático;
7. não hardcodar valores que atualmente vêm da aplicação;
8. não introduzir novas dependências sem necessidade;
9. não migrar framework, biblioteca ou arquitetura;
10. não alterar APIs, DTOs, serviços ou regras de negócio para satisfazer requisitos exclusivamente visuais.

Os exemplos HTML e CSS desta SPEC representam **contratos de comportamento visual**, não autorização para substituir a estrutura funcional existente.

Quando houver conflito entre:

1. comportamento funcional existente;
2. esta SPEC visual;
3. mockup/SVG;

a prioridade é:

**comportamento funcional existente → valores normativos desta SPEC → mockup visual.**

---

# 3. Resumo

A tela inicial deve receber um novo estilo visual baseado no mockup fornecido.

O layout é composto por:

- header fixo;
- área central scrollável;
- título de seção;
- grid de funcionalidades;
- botão flutuante de saída;
- rodapé fixo.

O desenho deve manter uma escala baseada predominantemente em múltiplos de 8px.

Medidas originárias do mockup que não pertencem à escala foram normalizadas e documentadas.

---

# 4. Objetivos

A implementação deve proporcionar:

- hierarquia visual clara;
- aparência profissional adequada a ambiente POS;
- leitura rápida;
- operação eficiente por mouse e teclado;
- estabilidade durante redimensionamento;
- ausência de colisões;
- previsibilidade de espaçamento;
- consistência entre funcionalidades adicionadas futuramente;
- boa legibilidade dos dados operacionais;
- fidelidade ao mockup dentro das regras técnicas desta SPEC.

---

# 5. Escopo

Está no escopo:

- layout do menu inicial;
- header;
- identificação da aplicação;
- título da aplicação;
- painel de informações;
- versão;
- título da área funcional;
- grid de funcionalidades;
- cards/botões de funcionalidades;
- botão de saída;
- rodapé;
- estados `hover`;
- estados `active`;
- estados `focus-visible`;
- estado visual `disabled`, quando já existir funcionalmente;
- responsividade;
- truncamento de dados;
- contraste;
- navegação por teclado;
- suporte a redução de movimento.

---

# 6. Fora do escopo

Não está no escopo:

- criação de novas funcionalidades;
- criação de novas telas;
- alteração de backend;
- modificação de autenticação;
- modificação de autorização;
- alteração de APIs;
- mudança de DTOs;
- alteração de banco;
- persistência;
- alteração de regras financeiras;
- alteração do fluxo de compras;
- alteração de sessão;
- modificação do comportamento do botão sair;
- introdução de novos módulos;
- refatoração arquitetural não necessária;
- troca de framework;
- atualização geral de dependências;
- redesign de telas que não sejam o menu.

---

# 7. Contrato de dados

Os seguintes dados existentes devem continuar sendo obtidos da fonte atual da aplicação:

- usuário;
- nome do usuário;
- terminal;
- lotérica/unidade;
- versão do software;
- funcionalidades disponíveis.

Exemplo visual:

```text
Proprietário : [ID_USUARIO] - [NOME_USUARIO]
TERMINAL: [ID_TERMINAL]
LOTÉRICA: [COD_LOTERICA]
```

Os valores acima são placeholders.

É proibido hardcodá-los na implementação final quando a aplicação já possuir fonte dinâmica.

---

# 8. Tokens de design

## 8.1 Cores

| Token | Valor | Uso |
|---|---|---|
| `--color-primary` | `#005CA9` | Brand, cards e título principal |
| `--color-primary-hover` | `#004A88` | Hover |
| `--color-primary-active` | `#003D70` | Active |
| `--color-danger` | `#D32F2F` | Botão sair |
| `--color-danger-hover` | `#B71C1C` | Hover sair |
| `--color-danger-active` | `#8E0000` | Active sair |
| `--color-bg-start` | `#EBF4FA` | Topo do gradiente |
| `--color-bg-end` | `#B3D4E6` | Base do gradiente |
| `--color-surface` | `#FFFFFF` | Header/footer |
| `--color-info-bg` | `#F8F9FA` | Info box |
| `--color-info-border` | `#A0B0C0` | Borda info box |
| `--color-border` | `#E1E8EE` | Divisórias |
| `--color-text` | `#333333` | Texto |
| `--color-text-muted` | `#666666` | Versão |
| `--color-focus` | `#FFB300` | Focus ring |
| `--color-disabled-bg` | `#D5DCE2` | Elemento desabilitado |
| `--color-disabled-text` | `#6B737A` | Texto desabilitado |

---

# 9. Variáveis estruturais

Usar variáveis para evitar números repetidos e divergentes:

```css
:root {
  --header-height: 120px;
  --footer-height: 48px;
  --content-side-padding: 80px;
  --content-bottom-safe-area: 120px;

  --color-primary: #005CA9;
  --color-primary-hover: #004A88;
  --color-primary-active: #003D70;

  --color-danger: #D32F2F;
  --color-danger-hover: #B71C1C;
  --color-danger-active: #8E0000;

  --color-bg-start: #EBF4FA;
  --color-bg-end: #B3D4E6;

  --color-surface: #FFFFFF;
  --color-info-bg: #F8F9FA;
  --color-info-border: #A0B0C0;
  --color-border: #E1E8EE;

  --color-text: #333333;
  --color-text-muted: #666666;

  --color-focus: #FFB300;

  --color-disabled-bg: #D5DCE2;
  --color-disabled-text: #6B737A;
}
```

---

# 10. Tipografia

Família preferencial:

```css
font-family: 'Roboto', 'Segoe UI', 'Helvetica Neue', Arial, sans-serif;
```

Escala:

| Elemento | Tamanho |
|---|---:|
| Brand | 22px |
| Título header | 14px |
| Título da seção | 18px |
| Cards | 16px |
| Info box | 12px |
| Versão | 14px |
| Botão sair | 16px |

Pesos:

- texto comum: 400;
- elementos intermediários: 500;
- títulos/destaques: 600–700.

---

# 11. Escala de espaçamento

Escala principal:

```text
8
16
24
32
40
48
64
80
96
120
144
```

Nem todo valor tipográfico precisa obedecer ao grid.

Elementos estruturais e espaçamentos devem priorizá-lo.

---

# 12. Normalização CSS obrigatória

Aplicar localmente ou garantir equivalência através do stylesheet global existente:

```css
*,
*::before,
*::after {
  box-sizing: border-box;
}
```

A aplicação deve impedir scroll concorrente no documento quando o `main` for o container de scroll:

```css
html,
body {
  height: 100%;
}

body {
  margin: 0;
  overflow: hidden;
}
```

Se o framework utilizar um root container como:

```text
app-root
#root
#app
```

ele também deverá ocupar 100% da altura disponível.

Não adicionar seletores globais incompatíveis com outras telas sem antes verificar impacto.

---

# 13. Estrutura semântica de referência

```html
<div class="app">

  <header class="app-header">

    <span class="app-header__brand">
      Buy More
    </span>

    <h1 class="app-header__title">
      POS - COMPRAS
    </h1>

    <div class="app-header__right">

      <div class="info-box">

        <p class="info-box__line">
          Proprietário:
          <span id="user">
            [ID_USUARIO] - [NOME_USUARIO]
          </span>
        </p>

        <p class="info-box__line">
          TERMINAL:
          <span id="terminal">
            [ID_TERMINAL]
          </span>
        </p>

        <p class="info-box__line">
          LOTÉRICA:
          <span id="lottery">
            [COD_LOTERICA]
          </span>
        </p>

      </div>

      <span class="app-header__version">
        v1.0.0.0
      </span>

    </div>

  </header>

  <main class="app-content">

    <h2 class="app-content__title">
      SELECIONE A FUNCIONALIDADE DESEJADA
    </h2>

    <div class="menu-grid">

      <button
        type="button"
        class="menu-card"
        data-action="saca-cartao">
        SACA COM CARTÃO
      </button>

    </div>

  </main>

  <button
    type="button"
    class="btn-exit"
    aria-label="Sair da aplicação">
    SAIR
  </button>

  <footer
    class="app-footer"
    aria-hidden="true">
  </footer>

</div>
```

Essa estrutura é referência.

No componente real devem ser preservados:

- bindings;
- loops;
- eventos;
- condicionais;
- diretivas;
- componentes internos;
- roteamento;
- observables;
- signals;
- estados;
- serviços já existentes.

---

# 14. Container da aplicação

```css
.app {
  position: relative;
  width: 100%;
  height: 100vh;
  height: 100dvh;

  overflow: hidden;

  background:
    linear-gradient(
      180deg,
      var(--color-bg-start) 0%,
      var(--color-bg-end) 100%
    );

  font-family:
    'Roboto',
    'Segoe UI',
    'Helvetica Neue',
    Arial,
    sans-serif;
}
```

`100dvh` deve ser utilizado como valor preferencial quando suportado.

O fallback `100vh` deve permanecer imediatamente antes dele.

---

# 15. Header

## 15.1 Contrato

Desktop padrão:

- altura: 120px;
- posição fixa;
- topo: 0;
- esquerda: 0;
- direita: 0;
- fundo branco;
- borda inferior de 1px;
- padding lateral 24px;
- `z-index: 100`.

```css
.app-header {
  position: fixed;

  top: 0;
  left: 0;
  right: 0;

  height: var(--header-height);

  display: flex;
  align-items: center;

  padding: 0 24px;

  background: var(--color-surface);

  border-bottom:
    1px solid var(--color-border);

  z-index: 100;
}
```

---

# 16. Brand

```css
.app-header__brand {
  flex: 0 0 auto;

  font-size: 22px;
  font-weight: 500;

  color: var(--color-primary);

  white-space: nowrap;
}
```

Texto:

```text
Buy More
```

caso esse já seja o nome utilizado pela aplicação/mockup.

Não alterar branding funcional existente sem evidência no projeto.

---

# 17. Título central

```css
.app-header__title {
  position: absolute;

  left: 50%;
  transform: translateX(-50%);

  max-width: 320px;

  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;

  margin: 0;

  font-size: 14px;
  font-weight: 500;

  letter-spacing: 1px;

  color: var(--color-text);

  text-align: center;
}
```

Texto esperado:

```text
POS - COMPRAS
```

Se já houver título dinâmico, preservar sua origem.

---

# 18. Área direita do header

```css
.app-header__right {
  margin-left: auto;

  display: flex;
  align-items: center;

  gap: 24px;

  min-width: 0;
}
```

`min-width: 0` é obrigatório para permitir truncamento correto em containers flex.

---

# 19. Bloco de informações

Desktop padrão:

- largura: 380px;
- altura mínima: 76px;
- padding: 12px 16px;
- fundo `#F8F9FA`;
- borda tracejada;
- raio de 4px.

```css
.info-box {
  width: 380px;
  min-width: 0;
  min-height: 76px;

  padding: 12px 16px;

  display: flex;
  flex-direction: column;
  justify-content: center;

  background: var(--color-info-bg);

  border:
    1.5px dashed
    var(--color-info-border);

  border-radius: 4px;

  overflow: hidden;
}
```

Linhas:

```css
.info-box__line {
  min-width: 0;

  margin: 0;

  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;

  font-size: 12px;

  line-height: 20px;

  color: var(--color-text);
}
```

---

# 20. Dados longos no header

Dados dinâmicos não podem:

- aumentar o header;
- quebrar linha;
- deslocar o título;
- empurrar a versão para fora da viewport;
- criar scroll horizontal.

Quando um valor for truncado visualmente, o valor completo deve continuar acessível através de mecanismo adequado à implementação existente.

Preferencialmente:

```html
title="valor completo"
```

e, quando necessário:

```html
aria-label="valor completo"
```

Evitar duplicar informações para leitores de tela quando não necessário.

---

# 21. Versão da aplicação

```css
.app-header__version {
  flex: 0 0 auto;

  font-size: 14px;

  color: var(--color-text-muted);

  white-space: nowrap;
}
```

A versão exibida deve continuar vindo da fonte já usada pela aplicação quando houver uma.

Não substituir por `v1.0.0.0` hardcoded se a aplicação já disponibilizar versão real.

---

# 22. Área central

O scroll deve acontecer **somente na área de conteúdo**.

Não utilizar simultaneamente:

```text
body scroll
+
main scroll
```

O contrato é:

```text
header fixo
↓
main scrollável
↓
footer fixo
```

Implementação:

```css
.app-content {
  position: fixed;

  top: var(--header-height);
  right: 0;
  bottom: var(--footer-height);
  left: 0;

  padding:
    60px
    var(--content-side-padding)
    var(--content-bottom-safe-area);

  overflow-y: auto;
  overflow-x: hidden;

  overscroll-behavior: contain;
}
```

Esta implementação substitui a combinação anterior:

```css
padding-top: 120px;
min-height: 100vh;
overflow-y: auto;
```

que poderia causar scroll duplicado ou altura superior à viewport.

---

# 23. Título da seção

Texto:

```text
SELECIONE A FUNCIONALIDADE DESEJADA
```

CSS:

```css
.app-content__title {
  margin:
    0
    0
    32px;

  font-size: 18px;

  font-weight: 600;

  letter-spacing: 1px;

  text-align: center;

  color: var(--color-primary);
}
```

A distância de 60px entre header e conteúdo já é fornecida pelo `padding-top` do `main`.

Não adicionar outra margem superior de 60px ao título.

Isso evita somar acidentalmente:

```text
60px + 60px
```

e deslocar a tela em 120px.

---

# 24. Grid de funcionalidades

O grid deve permitir inclusão futura de novos cards sem alterar CSS.

O card mantém largura nominal de 240px.

Não utilizar:

```css
minmax(240px, 1fr)
```

quando o requisito visual exigir cards próximos de 240px, pois com apenas um item o card poderia crescer até toda a largura do container.

Implementação preferencial:

```css
.menu-grid {
  display: grid;

  grid-template-columns:
    repeat(
      auto-fit,
      minmax(240px, 240px)
    );

  justify-content: center;

  gap: 16px;

  width: 100%;
  max-width: 1200px;

  margin: 0 auto;
}
```

Forma simplificada equivalente:

```css
grid-template-columns:
  repeat(auto-fit, 240px);
```

é aceitável.

---

# 25. Card de funcionalidade

Dimensões:

```text
largura nominal: 240px
altura: 64px
```

CSS:

```css
.menu-card {
  width: 240px;
  height: 64px;

  padding: 0 24px;

  border: none;

  border-radius: 8px;

  background: var(--color-primary);

  color: #FFFFFF;

  font-family: inherit;

  font-size: 16px;
  font-weight: 700;

  letter-spacing: 0.5px;

  text-align: center;

  cursor: pointer;

  box-shadow:
    0 2px 4px
    rgba(20, 50, 79, 0.22);

  transition:
    background-color 150ms ease,
    box-shadow 150ms ease,
    transform 100ms ease;
}
```

---

# 26. Hover do card

Aplicar hover somente a dispositivos que realmente suportam hover.

```css
@media (hover: hover) {
  .menu-card:hover {
    background:
      var(--color-primary-hover);

    box-shadow:
      0 4px 8px
      rgba(20, 50, 79, 0.28);
  }
}
```

---

# 27. Active do card

```css
.menu-card:active {
  background:
    var(--color-primary-active);

  transform: scale(0.98);
}
```

---

# 28. Focus do card

```css
.menu-card:focus-visible {
  outline:
    3px solid
    var(--color-focus);

  outline-offset: 2px;
}
```

Não remover `outline` sem substituição equivalente.

---

# 29. Estado disabled

Caso uma funcionalidade já possua estado desabilitado:

```css
.menu-card:disabled {
  background:
    var(--color-disabled-bg);

  color:
    var(--color-disabled-text);

  cursor: not-allowed;

  box-shadow: none;

  transform: none;

  opacity: 1;
}
```

Esta SPEC **não cria regras para decidir quando um botão fica disabled**.

Apenas define sua representação visual quando esse estado funcional já existir.

---

# 30. Ordem dos cards

A ordem dos cards deve seguir a ordem funcional já determinada pela aplicação.

O primeiro item esperado no mockup é:

```text
SACA COM CARTÃO
```

O requisito de "foco visual principal" significa posição e precedência na hierarquia atual.

Não aplicar cor, tamanho ou comportamento diferente ao primeiro card sem requisito funcional adicional.

Todos os cards usam o mesmo componente visual.

---

# 31. Botão sair

## 31.1 Posição

```css
.btn-exit {
  position: fixed;

  right: 24px;

  bottom:
    calc(
      var(--footer-height) + 24px
    );

  z-index: 110;
}
```

Com footer de 48px:

```text
48 + 24 = 72px
```

Logo:

```text
bottom = 72px
```

---

# 32. Dimensões e aparência do botão sair

```css
.btn-exit {
  min-width: 140px;

  height: 48px;

  padding: 0 24px;

  border: none;

  border-radius: 999px;

  background:
    var(--color-danger);

  color: #FFFFFF;

  font-family: inherit;

  font-size: 16px;

  font-weight: 700;

  letter-spacing: 0.5px;

  cursor: pointer;

  box-shadow:
    0 2px 5px
    rgba(0, 92, 169, 0.45);

  transition:
    background-color 150ms ease,
    box-shadow 150ms ease,
    transform 100ms ease;
}
```

---

# 33. Hover do botão sair

```css
@media (hover: hover) {
  .btn-exit:hover {
    background:
      var(--color-danger-hover);

    box-shadow:
      0 4px 10px
      rgba(0, 92, 169, 0.55);
  }
}
```

---

# 34. Active do botão sair

```css
.btn-exit:active {
  background:
    var(--color-danger-active);

  transform:
    scale(0.98);
}
```

---

# 35. Focus do botão sair

```css
.btn-exit:focus-visible {
  outline:
    3px solid
    var(--color-focus);

  outline-offset: 2px;
}
```

---

# 36. Comportamento funcional do botão sair

O botão deve utilizar o mesmo handler existente.

É proibido:

- recriar logout;
- alterar encerramento de sessão;
- introduzir `window.close()`;
- modificar autenticação;
- modificar redirect;
- alterar limpeza de sessão;
- substituir serviço existente.

A SPEC modifica somente a representação visual.

---

# 37. Footer

Contrato:

- altura: 48px;
- fixo;
- base da viewport;
- fundo branco;
- borda superior;
- sombra superior;
- `z-index: 100`.

```css
.app-footer {
  position: fixed;

  left: 0;
  right: 0;
  bottom: 0;

  height:
    var(--footer-height);

  background:
    var(--color-surface);

  border-top:
    1px solid
    var(--color-border);

  box-shadow:
    0 -2px 3px
    rgba(15, 42, 64, 0.18);

  z-index: 100;
}
```

---

# 38. Pilha de camadas

| Camada | Elemento | z-index |
|---|---|---:|
| Base | conteúdo | auto |
| Fixa | header | 100 |
| Fixa | footer | 100 |
| Flutuante | botão sair | 110 |

Nenhum componente desta change deve introduzir arbitrariamente:

```text
999
9999
99999
```

para solucionar colisões.

Se houver conflito com camadas existentes, investigar primeiro a pilha atual da aplicação.

---

# 39. Proteção contra sobreposição

O conteúdo deve possuir espaço inferior suficiente para que os últimos cards possam ser visualizados sem ficarem escondidos atrás do botão sair.

Por isso:

```css
--content-bottom-safe-area: 120px;
```

Esse espaço considera:

- botão de 48px;
- margem;
- área de segurança visual.

Ao atingir o final do scroll, o último item deve permanecer completamente visível e clicável.

---

# 40. Responsividade

O alvo principal é desktop POS.

Resoluções obrigatórias:

```text
1024 × 768
1366 × 768
1920 × 1080
```

---

# 41. Desktop amplo — ≥ 1200px

Mostrar:

- brand;
- título central;
- info box de 380px;
- versão;
- botão sair;
- footer.

Nenhum elemento pode colidir.

---

# 42. Desktop compacto — 1024px até 1199px

A tela deve continuar totalmente funcional.

Neste intervalo:

- manter header com 120px;
- ocultar o título central caso não exista espaço seguro;
- reduzir info box de 380px para 320px;
- reduzir padding lateral do conteúdo para 48px.

```css
@media (min-width: 1024px)
       and (max-width: 1199px) {

  .app-header__title {
    display: none;
  }

  .info-box {
    width: 320px;
  }

  .app-content {
    --content-side-padding: 48px;
  }
}
```

Ocultar o título central é preferível a permitir colisão com dados operacionais.

---

# 43. Abaixo de 1024px

Resoluções inferiores a 1024px não fazem parte do alvo principal de aceite.

Ainda assim, a interface não deve:

- produzir scroll horizontal;
- sobrepor elementos de forma destrutiva;
- tornar o botão sair inacessível.

Fallback recomendado:

```css
@media (max-width: 1023px) {

  .app-header__title {
    display: none;
  }

  .app-header {
    padding: 0 16px;
  }

  .app-header__right {
    gap: 8px;
  }

  .info-box {
    width: min(320px, 45vw);
  }

  .app-content {
    --content-side-padding: 24px;
  }

  .btn-exit {
    right: 16px;
  }
}
```

A aplicação não precisa ser redesenhada como interface mobile nesta change.

---

# 44. Acessibilidade

Todos os elementos de ação devem utilizar controles semanticamente interativos.

Para ações:

```html
<button>
```

Não utilizar:

```html
<div onclick="...">
```

ou equivalente.

---

# 45. Navegação por teclado

A navegação natural deve seguir:

```text
funcionalidade 1
↓
funcionalidade 2
↓
...
↓
botão SAIR
```

Não atribuir `tabindex` positivo.

Evitar:

```html
tabindex="1"
tabindex="2"
```

Utilizar ordem natural do DOM.

Header e footer não são focáveis.

---

# 46. Enter e Space

Botões devem responder naturalmente a:

- Enter;
- Space.

Não implementar listeners manuais para reproduzir comportamento nativo de `<button>` sem necessidade.

---

# 47. Contraste

Deve ser preservado contraste compatível com WCAG AA para textos essenciais.

Combinações principais:

```text
#FFFFFF sobre #005CA9
#FFFFFF sobre #D32F2F
#333333 sobre #FFFFFF
```

Qualquer alteração futura desses tokens exige nova validação de contraste.

---

# 48. Movimento reduzido

Usuários que configurarem redução de movimento não devem receber animações de escala.

```css
@media (prefers-reduced-motion: reduce) {

  .menu-card,
  .btn-exit {
    transition: none;
  }

  .menu-card:active,
  .btn-exit:active {
    transform: none;
  }
}
```

---

# 49. Scroll

Deve existir apenas um scroll vertical principal da tela.

Scroll esperado:

```text
.app-content
```

Não esperado:

```text
body + .app-content
```

O header e footer permanecem visualmente imóveis.

---

# 50. Scrollbar

Não esconder scrollbar através de:

```css
::-webkit-scrollbar {
  display: none;
}
```

ou equivalente.

A scrollbar pode seguir o padrão do sistema/browser.

---

# 51. Overflow horizontal

Não deve existir scroll horizontal nas resoluções de aceite.

Validar explicitamente:

```text
1024×768
1366×768
1920×1080
```

---

# 52. Desvios documentados do mockup

| Elemento | Mockup | SPEC | Razão |
|---|---:|---:|---|
| Footer | 44px | 48px | grid 8px |
| Card | 60px | 64px | grid 8px |
| Botão sair | 50px | 48px | grid 8px |

Os valores desta tabela são normativos.

Não retornar às medidas do SVG durante a implementação.

---

# 53. Hierarquia de fidelidade

Quando houver diferença visual entre mockup e SPEC:

1. preservar funcionalidade;
2. utilizar medida da SPEC;
3. utilizar mockup somente para características não especificadas.

Exemplo:

```text
Mockup card = 60px
SPEC card = 64px
```

Resultado obrigatório:

```text
64px
```

---

# 54. Critérios de aceite funcionais negativos

A change será rejeitada se alterar:

- clique dos cards;
- destino das rotas;
- autenticação;
- autorização;
- logout;
- carregamento dos dados;
- chamada de APIs;
- estado da aplicação;
- ordem funcional definida pelo sistema;
- tratamento de erros funcional.

---

# 55. Critérios de aceite visuais

## AC-01 — Header

O header deve permanecer fixo e medir 120px.

---

## AC-02 — Brand

A identificação visual deve permanecer no lado esquerdo.

---

## AC-03 — Título

Em viewport ≥1200px o título deve permanecer centralizado na viewport, não apenas no espaço restante do flex.

---

## AC-04 — Info box

Em desktop amplo, deve possuir 380px de largura.

---

## AC-05 — Textos longos

Dados excessivamente longos devem apresentar ellipsis sem aumentar a altura do header.

---

## AC-06 — Versão

A versão deve permanecer visível sem quebra de linha.

---

## AC-07 — Conteúdo

Somente a região central pode possuir scroll vertical principal.

---

## AC-08 — Grid

Cards devem ser automaticamente reorganizados conforme o espaço disponível.

---

## AC-09 — Largura dos cards

Cards devem possuir largura nominal de 240px.

Um único card não deve expandir para 100% ou 1200px.

---

## AC-10 — Altura dos cards

Todos os cards devem possuir 64px.

---

## AC-11 — Espaçamento

Gap entre cards deve ser 16px.

---

## AC-12 — Hover

Mouse hover deve alterar fundo e sombra nos dispositivos que suportam hover.

---

## AC-13 — Active

Clique/press deve fornecer feedback visual.

---

## AC-14 — Focus

Navegação via Tab deve exibir ring claramente visível.

---

## AC-15 — Botão sair

Deve possuir:

```text
min-width 140px
height 48px
right 24px
bottom 72px
```

em desktop padrão.

---

## AC-16 — Footer

Footer deve permanecer fixo com 48px de altura.

---

## AC-17 — Z-index

Botão sair deve permanecer acima do header/footer apenas quando suas regiões se relacionarem visualmente, usando nível 110.

---

## AC-18 — Final do scroll

Nenhum card deve ficar escondido atrás do botão sair ou do footer ao final do scroll.

---

## AC-19 — 1024×768

Nenhuma colisão ou scroll horizontal deve ocorrer.

O título central pode ser ocultado segundo a regra desta SPEC.

---

## AC-20 — 1366×768

Todos os elementos desktop devem aparecer conforme o layout principal.

---

## AC-21 — 1920×1080

O conteúdo deve permanecer centralizado, sem crescimento excessivo dos cards.

---

## AC-22 — Teclado

Todas as funcionalidades e o botão sair devem ser alcançáveis por Tab.

---

## AC-23 — Enter/Space

A ativação deve continuar funcionando através do comportamento nativo dos botões.

---

## AC-24 — Semântica

Nenhuma ação nova ou convertida deverá utilizar `<div>` clicável.

---

## AC-25 — Movimento reduzido

`prefers-reduced-motion: reduce` deve remover transformações/animações desnecessárias.

---

## AC-26 — Dados reais

Valores dinâmicos devem continuar vindo das fontes existentes.

---

## AC-27 — Regressão

Nenhuma alteração de regra de negócio ou navegação é permitida.

---

# 56. Matriz de validação visual

Validar no mínimo:

| Cenário | Resolução | Resultado esperado |
|---|---|---|
| Desktop mínimo | 1024×768 | Sem colisão |
| Desktop padrão | 1366×768 | Layout completo |
| Desktop amplo | 1920×1080 | Conteúdo centralizado |
| Strings longas | 1366×768 | Ellipsis |
| Muitos cards | 1366×768 | Grid + scroll |
| Teclado | 1366×768 | Focus correto |
| Reduced motion | qualquer | Sem transform |
| Conteúdo mínimo | 1366×768 | Card não expande excessivamente |

---

# 57. Cenários obrigatórios de dados

Testar visualmente pelo menos:

### Cenário A — dados normais

```text
Proprietário: 123 - JOÃO SILVA
TERMINAL: 001
LOTÉRICA: 123456
```

### Cenário B — proprietário longo

```text
123456789 - NOME EXTREMAMENTE LONGO DE OPERADOR UTILIZADO PARA VALIDAR TRUNCAMENTO
```

### Cenário C — terminal longo

Utilizar valor maior que o normal para validar proteção de layout.

### Cenário D — várias funcionalidades

Renderizar quantidade suficiente de cards para provocar scroll.

---

# 58. Testes e validação técnica

Antes de concluir a change:

- executar build existente;
- executar lint existente, caso configurado;
- executar testes existentes aplicáveis;
- executar testes unitários afetados;
- verificar console do navegador;
- validar ausência de erros;
- validar ausência de warnings novos relevantes;
- executar validação visual nas três resoluções obrigatórias;
- testar teclado;
- testar strings longas;
- testar scroll;
- testar reduced motion quando possível.

Não criar comandos imaginários.

O agente deve identificar no próprio projeto os comandos efetivamente disponíveis.

---

# 59. Evidências

Registrar no fluxo de validação do projeto:

- ambiente;
- branch/change;
- comando executado;
- exit code;
- resultado;
- resolução testada;
- evidências visuais quando o workflow suportar;
- observações sobre divergências;
- confirmação de que regras de negócio não foram modificadas.

---

# 60. Restrições de implementação

É proibido solucionar o layout através de:

- coordenadas absolutas copiadas diretamente do SVG;
- posições específicas para 1280×800;
- `margin-left` gigantes;
- múltiplos `position:absolute` dependentes da resolução;
- `!important` indiscriminado;
- z-index arbitrariamente alto;
- JavaScript para posicionar elementos que podem ser resolvidos em CSS;
- duplicação de componentes;
- hardcode de dados operacionais;
- alteração funcional para facilitar CSS.

---

# 61. Dependências

Nenhuma nova dependência visual deve ser necessária.

Não adicionar biblioteca apenas para:

- grid;
- flex;
- ellipsis;
- focus;
- shadow;
- gradient;
- responsive layout.

CSS nativo é suficiente para esses requisitos.

Se o projeto já utiliza biblioteca de componentes, a implementação pode aproveitar os componentes atuais sem modificar seu contrato funcional.

---

# 62. Performance

Esta change não deve introduzir:

- observers desnecessários;
- timers;
- listeners globais de resize;
- cálculo contínuo de layout via JavaScript;
- bibliotecas adicionais;
- imagens pesadas.

Responsividade deve ser resolvida prioritariamente através de CSS.

---

# 63. Segurança

Esta change não deve:

- exibir novas informações sensíveis;
- registrar credenciais;
- modificar autenticação;
- alterar cookies;
- alterar tokens;
- adicionar informação de sessão ao DOM além do que já existe;
- inserir HTML não sanitizado.

---

# 64. Definition of Done

A change é considerada concluída quando:

1. a tela reproduzir o contrato visual desta SPEC;
2. header estiver estável;
3. footer estiver estável;
4. main possuir scroll isolado;
5. botão sair estiver corretamente posicionado;
6. cards tiverem 240×64px;
7. grid reorganizar automaticamente os cards;
8. strings longas não quebrarem layout;
9. teclado funcionar;
10. focus estiver visível;
11. estados hover/active funcionarem;
12. reduced motion for respeitado;
13. 1024×768 passar sem colisões;
14. 1366×768 passar;
15. 1920×1080 passar;
16. não existir scroll horizontal;
17. build passar;
18. testes aplicáveis passarem;
19. comportamento funcional anterior permanecer intacto;
20. evidências forem registradas segundo o workflow do projeto.

---

# 65. Resultado visual esperado

Hierarquia final:

```text
┌──────────────────────────────────────────────────────┐
│ Buy More         POS - COMPRAS       [INFO]  versão │
├──────────────────────────────────────────────────────┤
│                                                      │
│          SELECIONE A FUNCIONALIDADE DESEJADA         │
│                                                      │
│       [ SACA COM CARTÃO ] [ FUNÇÃO 2 ] [...]         │
│                                                      │
│                      conteúdo scrollável             │
│                                                      │
│                                      [   SAIR   ]    │
├──────────────────────────────────────────────────────┤
│                                                      │
└──────────────────────────────────────────────────────┘
```

O desenho acima é conceitual.

As medidas normativas encontram-se nas seções anteriores.

---

# 66. Decisões consolidadas

## 66.1 Grid

Decisão:

```text
cards possuem largura nominal fixa de 240px
```

em vez de expandirem livremente por `1fr`.

Motivo:

evitar que um único card ocupe uma área desproporcional.

---

## 66.2 Scroll

Decisão:

```text
somente main scrolla
```

Motivo:

evitar scroll duplo causado pela combinação de header/footer fixos com `min-height: 100vh`.

---

## 66.3 Header em 1024px

Decisão:

o título central poderá ser ocultado.

Motivo:

dados operacionais têm prioridade visual sobre um título redundante quando não houver espaço seguro.

---

## 66.4 Dados longos

Decisão:

```text
nowrap + overflow hidden + ellipsis
```

Motivo:

dados de produção não podem alterar a geometria do header.

---

## 66.5 Grid de 8px

Decisão:

```text
card = 64px
exit = 48px
footer = 48px
```

Esses valores prevalecem sobre o mockup bruto.

---

# 67. Observação para revisão automatizada

Uma IA revisora deve procurar especificamente:

- alteração funcional acidental;
- body scroll simultâneo ao main;
- card crescendo além de 240px;
- colisão do header em 1024px;
- valores hardcoded;
- truncamento incompleto;
- ausência de `min-width: 0` em elementos flex;
- botão sair cobrindo último card;
- ausência de focus;
- remoção indevida de outline;
- z-index arbitrário;
- medidas antigas de 44px, 50px ou 60px;
- regressões nas ações já existentes.

---

# 68. Justificativa da revisão

A versão anterior já definia corretamente o conceito visual, tokens, semântica, estados de interação e fidelidade ao grid.

Esta revisão fecha ambiguidades de execução que poderiam produzir implementações diferentes.

### Scroll

A combinação de:

```text
header fixo
main min-height: 100vh
padding-top
overflow-y
footer fixo
```

poderia produzir altura maior que a viewport e dois contextos de scroll.

A nova regra define explicitamente o espaço útil entre header e footer.

### Grid

A combinação:

```css
minmax(240px, 1fr)
```

conflitava conceitualmente com o requisito de largura de 240px.

Agora os cards preservam largura nominal de 240px.

### Responsividade

Regras que antes eram recomendações tornam-se determinísticas.

Em 1024px:

```text
título central escondido
info box = 320px
padding lateral = 48px
```

Isso elimina decisões arbitrárias durante implementação.

### Integração

Os exemplos HTML deixam explicitamente de ser interpretados como substituição da implementação real.

A lógica existente deve ser preservada.

### Acessibilidade

Foram adicionados:

- disabled visual;
- reduced motion;
- hover condicionado ao dispositivo;
- proibição de tabindex positivo;
- preservação de semântica nativa.

### Validação

A implementação agora possui matriz objetiva de resolução, dados longos, quantidade de cards, teclado e regressão funcional.

---

# 69. Regra final

A implementação deve buscar:

```text
fidelidade visual
+
robustez estrutural
+
acessibilidade
+
zero alteração funcional
```

Em caso de dúvida, não modificar lógica funcional para adaptar a tela.

A interface deve adaptar-se à aplicação existente — não o contrário.

</details>

---

## Decisões

| # | Decisão | Motivo |
|---|---|---|
| D-01 | Tokens em `MenuDesignTokens.kt` no mesmo pacote | Evita duplicação de literais; sem nova dependência |
| D-02 | Layout do header via `BoxWithConstraints` existente | Já presente; apenas expande condicionais de largura |
| D-03 | Grid via `FlowRow` (Compose Foundation) ou `LazyVerticalGrid` | `FlowRow` existe em `foundation-layout`; já é dependência transitiva; permite `auto-fit` de 240 dp |
| D-04 | `LazyColumn` para o conteúdo da área de scroll | Substitui `Column` simples sem alterar a lógica de negócio |
| D-05 | `Scaffold` para estrutura header/content/footer | Oferece slots fixos sem gambiarras de `padding-top`/`padding-bottom` manuais |
| D-06 | `interactionSource` + `scale` animada no card | Feedback visual de pressed; suprimido quando `animationScale == 0f` |
| D-07 | Botão sair como `Box` flutuante com `align(Alignment.BottomEnd)` no `Scaffold` | Respeita `z` acima do footer; não interfere com scroll |
| D-08 | Sem alteração de `Business PluginManager`, `MenuTreeBuilder` ou sharedapi | Escopo exclusivamente visual |

---

## Arquitetura e componentes

### Novo arquivo

```
app/src/main/java/br/com/romulopenha/sistemaprototipoandroid/
└── ui/
    └── MenuDesignTokens.kt
```

Conteúdo: `object MenuDesignTokens` com constantes `Color`, `Dp` e `TextStyle` correspondendo 1:1 aos tokens da SPEC.

### Alterações em `AuthenticationHeader.kt`

1. Importar tokens de `MenuDesignTokens`.
2. Substituir literais de cor, tamanho e espaçamento pelos tokens.
3. Ampliar `BoxWithConstraints` para:
   - ocultar título central quando `maxWidth < 860.dp`;
   - reduzir info box para 320 dp quando `maxWidth < 1000.dp`.
4. Adicionar `title`/`contentDescription` nos textos truncados (já presente parcialmente).
5. Preservar `CoreShell`, `AuthenticationHeaderState`, `displayRole()` intactos.

### Alterações em `MainActivity.kt`

1. `BusinessMenuScreen`: substituir `Column` simples por `Scaffold` com:
   - `topBar`: `AuthenticationHeader` (já chamado via `CoreShell` no nível acima — **não duplicar**; usar o slot de conteúdo do `Scaffold` abaixo do header existente).
   - `bottomBar`: `MenuFooter` (novo composable privado, 48 dp, branco, borda superior).
   - `floatingActionButton`: `MenuExitButton` (novo composable privado, botão pill vermelho).
   - `content`: `LazyColumn` com título de seção + `MenuGrid`.
2. `BusinessMenuEntries`: substituir `Button` padrão por `MenuCard` (novo composable privado, 240 × 64 dp, tokens de cor, animação de pressed, focus ring).
3. Preservar todos os parâmetros e handlers existentes.

### Hierarquia de composables resultante

```
CoreShell (Column)
├── AuthenticationHeader          ← alterado (tokens + responsividade)
└── Column (weight=1f)
    └── BusinessMenuScreen        ← alterado (Scaffold + novos sub-composables)
        ├── Scaffold
        │   ├── bottomBar: MenuFooter           (novo, privado)
        │   ├── floatingActionButton: MenuExitButton (novo, privado)
        │   └── content: LazyColumn
        │       ├── MenuSectionTitle            (novo, privado)
        │       └── MenuGrid                   (novo, privado)
        │           └── MenuCard × N           (novo, privado)
        └── AlertDialog (fatal — inalterado)
```

### Pilha de z-index / elevation

| Composable | Elevation |
|---|---|
| `AuthenticationHeader` | `AppBarDefaults.TopAppBarElevation` |
| `MenuFooter` | 3 dp (sombra superior) |
| `MenuExitButton` | FAB padrão (acima do footer) |

---

## Alternativas e consequências

| Alternativa | Rejeitada por |
|---|---|
| Usar `ConstraintLayout` para o header | Complexidade desnecessária; `BoxWithConstraints` já resolve |
| Usar `minmax(240.dp, 1fr)` equivalente no grid | Expansão indesejada com card único; `FlowRow` com largura fixa resolve |
| Mover `CoreShell` para usar `Scaffold` global | Alteraria a estrutura fora do escopo visual desta change |
| Adicionar `accompanist` para flow layout | `FlowRow` já disponível em `androidx.compose.foundation.layout` sem dependência adicional |

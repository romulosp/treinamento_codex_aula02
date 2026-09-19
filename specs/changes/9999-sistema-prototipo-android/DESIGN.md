# Design: 9999-sistema-prototipo-android

**Autor:** Rômulo Penha

## Status

`SPEC_APROVADA`

## Contexto

A origem utiliza Java desktop, desenho por imagens, dimensões fixas e uma configuração XML que instancia estilos por nome. O destino é um aplicativo Android nativo executado em janelas variáveis. A migração deve preservar intenção visual e estados, não a tecnologia nem as coordenadas anteriores.

## Referências

- [SPEC principal](spec.md).
- [Inventário](inventario-origem.md).
- [Fontes e decisões](sources-and-decisions.md).
- Documentação Android oficial consultada em 2026-09-19, registrada em `sources-and-decisions.md`.

## Decisões

### D-001 — Perfil e módulos

Usar perfil `SIMPLE` e somente o módulo `app`. O produto é um protótipo local com uma tela-catálogo e um menu demonstrativo. Múltiplos módulos, banco, rede e framework de injeção aumentariam custo sem evidência de benefício.

### D-002 — Compose estável

Usar Kotlin, Compose Material 3 e BOM estável verificada no início da implementação. Não usar a API experimental de estilos: o catálogo visual será formado por data classes imutáveis e funções Compose estáveis.

### D-003 — Configuração tipada em Kotlin

Substituir leitura XML e construção dinâmica por um registro Kotlin compilado:

```text
ComponentVariant
├── semanticId
├── defaultStyle
├── pressedStyle?
├── disabledStyle?
├── focusedStyle?
└── selectedStyle?

VisualStyle
├── background
├── foreground
├── imageResource?
├── typography
├── shape
├── border
└── spacing
```

Essa decisão mantém a associação configuração-recurso solicitada, fornece segurança de tipo e transforma caminhos inválidos em erros de compilação ou testes de catálogo.

### D-004 — Componentes sem estado interno de produto

Componentes reutilizáveis recebem valor, estado habilitado/selecionado e callbacks. Estado de tela pertence a um `CatalogViewModel`; estado efêmero puramente visual pode usar `rememberSaveable` quando necessário.

### D-005 — Substituição neutra dos recursos legados

Nenhum binário de imagem ou fonte legado será copiado para o APK nesta Change. Os hashes existentes permanecem na matriz somente para rastreabilidade analítica. A implementação deve usar:

- tokens, formas e desenho Compose para superfícies, bordas, estados e fundos;
- ícones neutros do sistema ou vetores novos sem marca de terceiro;
- tipografia padrão do sistema;
- conteúdo demonstrativo neutro para ilustrações ou imagens informativas.

Essa decisão resolve de forma uniforme recursos presentes sem autorização comprovada e as 118 referências ausentes, sem declarar equivalência pixel a pixel.

### D-006 — Tela-catálogo como vertical slice

A primeira entrega funcional será a tela-catálogo. Ela comprova tema, componentes, estado, adaptabilidade, acessibilidade e testes antes do menu demonstrativo.

### D-007 — Layout orientado pela janela

A composição deve consultar o espaço disponível por APIs estáveis de métricas/classificação de janela. Não haverá ramificação por nome de aparelho nem suposição baseada em resolução física. Os blocos usam constraints, pesos, tamanhos mínimos/máximos, grades adaptativas estáveis e contêineres roláveis.

Recursos rasterizados terão estratégia explícita de `ContentScale`, recorte e proporção. Elementos simples serão redesenhados por vetores ou Compose para manter nitidez em qualquer densidade. Barras do sistema, recortes e áreas de gesto serão consumidos por insets, sem valores mágicos.

### D-008 — Plataforma e distribuição

O aplicativo usa `minSdk = 26`, `compileSdk = 36`, `targetSdk = 36`, Java 17 e BOM Compose estável `2026.06.01` (Compose UI/Foundation 1.11.4). A entrega é interna e demonstrativa. Publicação pública, Google Play ou incorporação de ativos legados exige nova Change e nova análise de direitos.

## Arquitetura e componentes

```text
apps/frontend/smartphone/sistema-prototipo-android/
├── app/
│   └── src/
│       ├── main/java/br/com/romulopenha/sistemaprototipoandroid/
│       │   ├── MainActivity.kt
│       │   ├── ui/theme/
│       │   ├── ui/components/
│       │   │   ├── button/
│       │   │   ├── input/
│       │   │   ├── password/
│       │   │   ├── label/
│       │   │   ├── panel/
│       │   │   ├── selection/
│       │   │   ├── data/
│       │   │   ├── feedback/
│       │   │   └── keyboard/
│       │   ├── ui/catalog/
│       │   └── ui/menu/
│       ├── main/res/
│       ├── test/
│       └── androidTest/
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
└── gradlew / gradlew.bat
```

### Fluxo de estado

```text
gesto/tecla -> CatalogEvent -> CatalogViewModel -> CatalogUiState -> Compose
```

Nenhum componente visual acessa diretamente Activity, sistema de arquivos, rede ou singleton global.

### Composição da tela-catálogo

```text
┌──────────────────────────────────────────────────────────────┐
│ Cabeçalho: nome do protótipo | status | ações auxiliares    │
├───────────────────────────────────────────────┬──────────────┤
│ Painel de identificação                       │  7  8  9     │
│ usuário [____________]                        │  4  5  6     │
│ senha   [____________]                        │  1  2  3     │
│ mensagens, seleção e progresso                │  ←  0 limpar │
├───────────────────────────────────────────────┴──────────────┤
│ Q W E R T Y U I O P                                           │
│  A S D F G H J K L                                            │
│ fixar Z X C V [espaço] B N M                                  │
│ cancelar                                      confirmar       │
├──────────────────────────────────────────────────────────────┤
│ Demais seções roláveis: tabela, diálogo, menu e ativos       │
└──────────────────────────────────────────────────────────────┘
```

Em largura compacta, cabeçalho, identificação, teclado numérico, teclado alfanumérico e demais seções passam a uma coluna rolável. Em largura média, os blocos podem ocupar uma ou duas colunas conforme constraints. Em largura expandida, a composição aproxima a referência 800 x 600 e usa o espaço adicional sem esticar controles além de seus limites. A ordem semântica permanece coerente com a ordem visual.

## Contratos públicos propostos

- `PrototypeTheme`: aplica tokens visuais.
- `ComponentCatalog`: resolve variantes por identificador semântico.
- `PrototypeButton`, `PrototypeTextField`, `PrototypePasswordField`, `PrototypeTextArea`.
- `PrototypeLabel`, `PrototypePanel`, `PrototypeImage`.
- `PrototypeToggle`, `PrototypeChoiceGroup`, `PrototypeDropdown`.
- `PrototypeDataTable`, `PrototypeScrollableList`.
- `PrototypeDialog`, `PrototypeProgress`, `PrototypeMessage`.
- `AlphabeticKeyboard`, `NumericKeyboard`, `CalculatorKeyboard`.
- `CatalogScreen`, `CatalogUiState`, `CatalogEvent`.
- `DemoMenuScreen`, `DemoMenuItem`.

Os nomes finais podem ser refinados na implementação sem alterar comportamento, desde que a matriz seja atualizada e não reintroduza nomenclatura do sistema anterior.

## Estratégia de testes

- Unitários: registro de variantes, resolução de estados, edição de texto, seleção e árvore de menu.
- Compose UI: semântica, callbacks, estado desabilitado, foco, diálogo e teclados.
- Screenshot: 320 x 568, 360 x 800, 400 x 500, 600 x 960, 800 x 600, 840 x 900 e 1200 x 800 dp; retrato/paisagem aplicáveis, tema padrão e escala de fonte 1,5.
- Instrumentados: restauração após rotação, mudança de postura e redimensionamento; insets/recortes; validação nos níveis de API mínimo e alvo.
- Exploratórios: escala de fonte 2,0 e redimensionamento contínuo para localizar cortes, saltos e perda de foco.
- Inspeção visual humana: comparação lado a lado da área expandida com a referência, registrando diferenças intencionais.

## Alternativas e consequências

### Interpretar o XML anterior em tempo de execução

Rejeitada. Preservaria nomes e caminhos inválidos, exigiria parser adicional e deslocaria falhas para execução.

### Copiar todas as imagens diretamente

Rejeitada. Há duplicatas, recursos ausentes e direitos de uso não comprovados.

### Usar posicionamento absoluto 800 x 600

Rejeitada. Não se adapta ao smartphone, escala de fonte, rotação ou janelas redimensionáveis.

### Criar biblioteca Android separada por componente

Rejeitada neste incremento. Pastas e APIs separadas dão isolamento suficiente; modularização poderá ser reavaliada com métricas de evolução e build.

### Usar API experimental de estilos

Rejeitada. A candidata oficial exige dependências experimentais e SDK acima do alvo definido. O custo e a instabilidade não se justificam para este protótipo.

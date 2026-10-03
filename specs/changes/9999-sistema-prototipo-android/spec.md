# SPEC: 9999-sistema-prototipo-android

**Autor:** Rômulo Penha

## Status

`SPEC_APROVADA`

## Revisão operacional de 2026-09-24

O catálogo de componentes e o menu demonstrativo remanescentes serão entregues
por `:plugin-negocio`, APK interno assinado e validado pela plataforma da Change
10000. O `:app` não recebe regras, árvore ou conteúdo de negócio; ele só exibe
descritores fornecidos pelo plugin depois de sessão SSO válida.

O baseline operacional desta revisão é `minSdk = 29`, `compileSdk = 37`,
`targetSdk = 36` e Java 17. A confirmação local demonstrativa é substituída pela
sessão opaca de `plugin-login`; nenhum segredo é enviado ao plugin de negócio.

> **Escopo operacional superado em parte.** A tela, os campos, os teclados e
> qualquer comportamento de identificação deixam de ser contrato ativo do
> módulo `:app` desde a Change `10001-plugin-login-autenticacao`. A Change 9999
> permanece como histórico visual e de componentes de origem.

## Baseline histórico do prompt2

A decisão anterior `compileSdk = 36` foi substituída por `compileSdk = 37`,
mantendo `targetSdk = 36`. O baseline exige BOM Compose `2026.09.00`,
Compose 1.12.x stable, AGP 9.4.0, Gradle 9.6.0, KGP 2.2.10 e JDK 17.
Na fundação monolítica original, `minSdk = 26` foi confirmado após análise de
público, dispositivos, bibliotecas, segurança, APIs e custo de testes. A Change
10000 substituiu essa decisão pela API 29 para toda a topologia de plugins.

Estado atual: `SPEC_APROVADA`. A Compatibility Review completa terminou em
`PASS`; `compileSdk` e `targetSdk` permanecem decisões independentes.

## Referências e dependências

- [Proposta](proposal.md).
- [Design](DESIGN.md).
- [Inventário da origem](inventario-origem.md).
- [Matriz de migração](migration-matrix.md).
- [Fontes e decisões](sources-and-decisions.md).
- [Estado do material de origem](source-material/README.md).
- Especificações complementares ligadas nas seções abaixo.

## Premissas e decisões aprovadas para revisão

- `REQUIRED`: aplicativo Android nativo, Kotlin e Compose, com perfil `STANDARD`
  e módulos `:app`, `:shared-api`, `:plugin-login` e plugins de negócio
  independentes. O desenho `SIMPLE` de módulo único é histórico.
- `REQUIRED`: `compileSdk = 37` e `targetSdk = 36`; compilar contra Android 17 não ativa automaticamente os comportamentos de alvo da API 37.
- `REQUIRED`: `minSdk = 29`, conforme a Change 10000 e seu ADR de fronteira de
  segurança. O baseline 26 pertence somente à fundação monolítica superada.
- `REQUIRED`: distribuição exclusivamente interna para desenvolvimento, demonstração e validação. Publicação em Google Play, outra loja ou distribuição pública exige nova Change.
- `REQUIRED`: nenhum arquivo de imagem, fonte ou marca do material legado será copiado para o APK, pois não há licença ou autorização de distribuição comprovada. O material permanece como evidência analítica local.
- `REQUIRED`: o aplicativo deve executar exclusivamente em orientação paisagem; a Activity deve declarar `screenOrientation="landscape"`.
- `REQUIRED`: definições dependentes de imagens, inclusive as 118 referências sem arquivo, serão `SUBSTITUIR` por tokens, formas Compose, ícones do sistema ou conteúdo neutro; as demais serão `ADAPTAR`. A decisão individual está em `migration-matrix.md`.

## Requisitos funcionais

### RF-001 — Fundação do aplicativo

O projeto deve ser criado exatamente em `apps/frontend/smartphone/sistema-prototipo-android/`, com nome e artefato `sistema-prototipo-android`, grupo `br.com.romulopenha` e `namespace`/`applicationId` `br.com.romulopenha.sistemaprototipoandroid`.

O contrato detalhado está em [spec-fundacao-aplicativo.md](spec-fundacao-aplicativo.md).

### RF-002 — Catálogo visual tipado

A aparência deve ser descrita por modelos Kotlin imutáveis que associem identificador semântico, cores, tipografia, forma, espaçamento e recursos aos estados suportados. O aplicativo não deve interpretar a configuração XML anterior em tempo de execução.

O contrato detalhado está em [spec-tema-assets-tipografia.md](spec-tema-assets-tipografia.md).

### RF-003 — Famílias de componentes

Devem ser criadas as famílias descritas nas seguintes especificações:

- [botões e ações](spec-componente-button.md);
- [textos, painéis e imagens](spec-componente-label-panel.md);
- [entradas de texto e área de texto](spec-componente-text-input.md);
- [entrada de senha](spec-componente-text-password.md);
- [seleção, alternância e agrupamento](spec-componente-selection.md);
- [tabelas, listas e rolagem](spec-componente-data-display.md);
- [diálogos, mensagens e progresso](spec-componente-feedback.md);
- [teclados virtuais](spec-componente-virtual-keyboard.md).

Cada componente deve ficar em pasta própria dentro da família correspondente, com API Compose independente do nome das classes de origem.

### RF-004 — Estados visuais e interação

Componentes interativos devem representar, quando aplicável, os estados `default`, `pressed`, `disabled`, `focused` e `selected`. Estados não aplicáveis devem ser documentados na matriz de migração, sem criar comportamento fictício.

### RF-005 — Tela operacional obrigatória

`SUPERADO_PELAS_CHANGES_10001_10006_10007`. A antiga tela operacional única foi
decomposta: `:plugin-login` possui identificação, campos, teclados e rodapé; o
shell de `:app` possui o cabeçalho global; plugins de negócio fornecem conteúdo
após uma sessão válida. O módulo `:app` não reimplementa esses elementos.

O contrato detalhado está em [spec-tela-catalogo.md](spec-tela-catalogo.md).

Os componentes reutilizáveis criados anteriormente permanecem como origem
histórica e só podem ser usados nos módulos proprietários definidos pelas
Changes sucessoras.

### RF-006 — Estrutura de menu demonstrativa

`SUPERADO_PELA_CHANGE_10003`. O host monta uma árvore dinâmica a partir de
plugins `business-menu` verificados. O conteúdo demonstrativo neutro pertence a
`:plugin-negocio`; o host não mantém árvore local nem regras de negócio.

O contrato detalhado está em [spec-menu-demonstrativo.md](spec-menu-demonstrativo.md).

### RF-007 — Migração rastreável

A implementação deve manter uma matriz versionada com uma linha por definição visual ou comportamento reutilizável da origem, contendo família de destino, estado, ativo, decisão (`MIGRAR`, `ADAPTAR`, `SUBSTITUIR`, `ADIAR` ou `EXCLUIR`) e evidência.

Nenhuma linha pode ser marcada `MIGRAR` se depender de arquivo ausente ou direito de uso não comprovado. Nesta Change, arquivos binários do material legado não podem ser incorporados ao APK; hashes existentes servem somente à rastreabilidade da análise.

### RF-008 — Demonstração local e determinística

Os dados demonstrativos de `:plugin-negocio` devem ser locais, determinísticos e
não sensíveis. Esse requisito não se aplica à autenticação: o fluxo vigente usa
`plugin-login`, a API `autenticadorsso` e o Keycloak conforme as Changes 10001,
10003 e 10007. Credenciais e tokens não atravessam a fronteira do plugin de
negócio.

### RF-009 — Autonomia do material de origem

Especificação, implementação, revisão e validação devem usar somente arquivos versionados no repositório. Caminhos absolutos externos são proibidos em código, Gradle, testes, scripts e evidências finais. O diretório `source-material/` desta Change é a fonte canônica para a migração.

## Requisitos não funcionais

### RNF-001 — Plataforma e estabilidade

- Kotlin e Jetpack Compose.
- Java 17 para a cadeia de build.
- Dependências estáveis; bibliotecas alfa, beta ou experimentais exigem retorno à SPEC.
- Gradle Wrapper versionado e catálogo de versões.
- BOM estável do Compose `2026.09.00`, com Compose 1.12.x stable e `compileSdk = 37`; alteração exige nova verificação conjunta do toolchain e registro na matriz de compatibilidade.

### RNF-002 — Arquitetura

- Aplicação `single-activity`.
- Estado imutável e fluxo unidirecional: estado desce, eventos sobem.
- `ViewModel` somente para estado de tela; componentes reutilizáveis recebem estado e callbacks.
- Perfil vigente `STANDARD`, com `:app` como host mínimo, `:shared-api` como
  fronteira contratual e plugins independentes por capacidade. A decisão
  original de injeção manual e módulo único está superada pelas Changes 10000 e
  10001; DI, domínio, persistência e rede continuam contextuais e só devem ser
  introduzidos no módulo que efetivamente os possuir.

### RNF-003 — Adaptabilidade

- Bloquear a orientação em paisagem, mas adaptar proporção, resolução, densidade e tamanho de janela dentro dessa orientação.
- Dimensionar layout em `dp`, tipografia em `sp` e selecionar recursos por densidade; pixels físicos só podem aparecer em processamento interno documentado, nunca como contrato de layout.
- Tomar decisões pela janela efetivamente disponível, não pelo modelo físico do dispositivo.
- Atender larguras compactas, médias e expandidas em paisagem, inclusive redimensionamento em multiwindow e dobráveis.
- Respeitar barras de sistema, recortes de câmera, áreas de gesto, dobradiças e demais insets sem esconder controles essenciais.
- Em largura expandida e orientação horizontal, preservar a hierarquia visual 800 x 600 da referência.
- Em largura compacta ou média, reorganizar blocos, adaptar grades e habilitar rolagem vertical sem cortar ou sobrepor conteúdo.
- Não exigir rolagem horizontal da tela; componentes de dados podem possuir estratégia própria aprovada quando inevitável.
- Preservar estado, foco e posição relevante de rolagem ao rotacionar, redimensionar ou mudar postura do dispositivo.
- Suportar escala de fonte do sistema de pelo menos 1,5 sem perda de operação; a validação também deve observar escala 2,0 para identificar bloqueios críticos.
- Este requisito cobre janelas Android de celulares, tablets e dobráveis no intervalo do `minSdk` ao `targetSdk`, sempre em paisagem. Wear OS, TV, Auto/Automotive e XR estão fora desta Change.

### RNF-004 — Acessibilidade

- Alvos interativos com pelo menos 48 dp em cada dimensão.
- Contraste verificável, foco visível, ordem de navegação previsível e suporte a escala de fonte de 1,5.
- Semântica de função, rótulo, estado e ação para TalkBack.
- Imagens decorativas sem descrição redundante; imagens informativas com descrição localizada.
- O teclado virtual deve ser utilizável por toque, teclado físico e serviço de acessibilidade.

### RNF-005 — Segurança e privacidade

- Não registrar credenciais nem conteúdo de senha.
- Senha mascarada por padrão e apagada quando o fluxo demonstrativo for reiniciado.
- Nenhum componente Android exportado além da atividade inicial, e sua exportação deve ser explícita.
- Nenhuma permissão perigosa.
- Backup e captura de tela de conteúdo de senha devem ser avaliados e registrados na revisão de segurança.

### RNF-006 — Qualidade

- KDoc em português do Brasil para declarações públicas/protegidas e contratos internos não óbvios.
- Cobertura unitária mínima de 80% do código elegível e alvo recomendado de 90%.
- `assembleDebug`, testes unitários, lint, testes de UI, regressão visual e validador estrutural devem ter evidência reproduzível.
- Validação em emulador nos níveis de API mínimo e alvo.

### RNF-007 — Recursos

- Arquivos do conjunto fornecido são evidência analítica e visual, não recursos autorizados para empacotamento.
- Nenhum PNG, JPG, GIF ou TTF legado pode ser copiado para `app/src/main/res` nesta Change.
- Formas simples, superfícies e estados devem ser recriados por Compose; ícones devem usar alternativas neutras do sistema; textos usam a fonte padrão do sistema.
- A regressão visual compara hierarquia, paleta, forma e composição, sem exigir reprodução de marca, fotografia, glifo proprietário ou binário legado.

## Regras de negócio

- RN-001: pressionar uma tecla virtual altera apenas o campo atualmente selecionado.
- RN-002: `Limpar` remove todo o conteúdo do campo; apagar remove um caractere compatível com Unicode.
- RN-003: `SUPERADA_PELAS_CHANGES_10001_E_10007`. A confirmação usa o fluxo SSO
  aprovado, produz sessão opaca e perfil sanitizado; não existe autenticação
  local demonstrativa no estado vigente.
- RN-004: componente desabilitado não dispara callback e deve expor semanticamente seu estado.
- RN-005: seleção exclusiva mantém no máximo um item selecionado por grupo; seleção múltipla mantém cada estado independentemente.
- RN-006: diálogo modal retém foco enquanto aberto e devolve foco ao acionador ao fechar.
- RN-007: linhas, colunas e conteúdo longo permanecem alcançáveis sem rolagem personalizada que esconda a semântica nativa.

## Cenários e critérios de aceite

### CA-001 — Estrutura e identidade

Dado que a implementação foi concluída, quando o diretório do projeto e a configuração Gradle forem inspecionados, então todos os valores de RF-001 devem coincidir exatamente e o validador estrutural deve retornar código `0`.

### CA-002 — Controles operacionais no mesmo enquadramento

`SUPERADO_PELAS_CHANGES_10006_E_10007`. Os critérios vigentes de composição do
login e do cabeçalho são validados nessas Changes; este cenário permanece como
registro visual histórico.

### CA-003 — Semelhança visual em janela expandida

`SUPERADO_PELAS_CHANGES_10006_E_10007`. A comparação visual atual usa os SVGs,
o viewport 1280×800 e a divisão de propriedade entre `CoreShell` e
`:plugin-login` definida nessas Changes.

No registro visual histórico, as teclas da referência eram retangulares com
cantos arredondados e elevação discreta. O contrato vigente de teclados, ações
e cores pertence às Changes 10001 e 10006.

Não é exigida cópia pixel a pixel, uso de marca de terceiro nem manutenção de coordenadas absolutas.

### CA-004 — Layout compacto

Dada uma janela compacta em orientação paisagem, quando o catálogo neutro de
`:plugin-negocio` for percorrido, então nenhum componente deve ficar
inalcançável, sobreposto ou cortado, e não deve haver rolagem horizontal da
tela.

### CA-004A — Faixa de janelas Android

Dadas janelas representativas de celular compacto, celular comum, tablet, dobrável aberto e desktop/multiwindow, quando a tela for renderizada e redimensionada, então o conteúdo deve recompor sem reiniciar o fluxo, respeitar insets, preservar estado/foco e continuar integralmente operável.

As dimensões mínimas de validação são 568 x 320, 800 x 360, 500 x 400, 960 x 600, 800 x 600, 900 x 840 e 1200 x 800 dp, sempre em paisagem.

### CA-005 — Estados

Dado um componente interativo, quando ele for pressionado, focado, selecionado ou desabilitado conforme sua API, então aparência, semântica e callback devem corresponder ao estado e à matriz de migração.

### CA-006 — Entradas e teclados

Dado um campo de `:plugin-login` selecionado, quando letras, números, espaço,
apagar e limpar forem acionados no teclado virtual, então o texto deve mudar
deterministicamente; em campo de senha, o valor não deve ficar visível nem
aparecer em logs. As Changes 10001 e 10006 são o contrato vigente desse fluxo.

### CA-007 — Menu

Dado o menu dinâmico da Change 10003, quando um agrupador for acionado, então o
próximo nível deve ser exibido; quando voltar for acionado, então o nível
anterior deve ser restaurado; uma folha deve abrir exclusivamente a tela do
plugin proprietário.

### CA-008 — Acessibilidade

Dado TalkBack ou a árvore semântica de testes, quando os controles forem percorridos, então cada ação deve ter nome, função, estado e ordem compreensíveis; os testes devem verificar alvos mínimos, escala de fonte de 1,5 e ausência de bloqueio crítico em escala 2,0.

### CA-009 — Rastreabilidade dos recursos

Dada a matriz de migração, quando uma linha referenciar imagem ou fonte existente, então deve registrar origem e hash apenas para rastreabilidade; o direito de uso deve indicar que o binário não será incorporado e o destino deve ser uma substituição neutra. Referências ausentes devem registrar `AUSENTE` e a mesma decisão de substituição.

### CA-010 — Qualidade reproduzível

Dada a implementação revisada, quando os comandos definidos em `validation.md` forem executados no ambiente registrado, então build, testes, lint, cobertura e testes de UI devem concluir com código `0`, ressalvadas somente limitações formalmente aprovadas.

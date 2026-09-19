# SPEC: 9999-sistema-prototipo-android

**Autor:** Rômulo Penha

## Status

`SPEC_APROVADA`

## Referências e dependências

- [Proposta](proposal.md).
- [Design](DESIGN.md).
- [Inventário da origem](inventario-origem.md).
- [Matriz de migração](migration-matrix.md).
- [Fontes e decisões](sources-and-decisions.md).
- [Material de origem incorporado](source-material/README.md).
- Especificações complementares ligadas nas seções abaixo.

## Premissas e decisões aprovadas para revisão

- `REQUIRED`: aplicativo Android nativo, Kotlin, Compose, um módulo `app` e perfil `SIMPLE`.
- `REQUIRED`: `compileSdk = 36` e `targetSdk = 36`, compatíveis com Android 16 e mantidos mesmo para distribuição interna.
- `REQUIRED`: `minSdk = 26`. O protótipo suporta Android 8.0 ou superior; APIs posteriores exigem fallback compatível ou não podem ser usadas.
- `REQUIRED`: distribuição exclusivamente interna para desenvolvimento, demonstração e validação. Publicação em Google Play, outra loja ou distribuição pública exige nova Change.
- `REQUIRED`: nenhum arquivo de imagem, fonte ou marca do material legado será copiado para o APK, pois não há licença ou autorização de distribuição comprovada. O material permanece como evidência analítica local.
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

### RF-005 — Tela-catálogo obrigatória

Deve existir pelo menos uma tela única que contenha todos os componentes criados nesta Change, permita observar seus estados e seja navegável integralmente por rolagem quando não houver espaço suficiente.

O contrato detalhado está em [spec-tela-catalogo.md](spec-tela-catalogo.md).

### RF-006 — Estrutura de menu demonstrativa

O aplicativo deve apresentar uma árvore de menu local e navegável para demonstrar botões de menu, níveis hierárquicos, retorno e estado desabilitado. O conteúdo será uma amostra neutra; os 350 nós da origem serão inventariados, mas não devem carregar regras de negócio ou nomes protegidos para o protótipo.

O contrato detalhado está em [spec-menu-demonstrativo.md](spec-menu-demonstrativo.md).

### RF-007 — Migração rastreável

A implementação deve manter uma matriz versionada com uma linha por definição visual ou comportamento reutilizável da origem, contendo família de destino, estado, ativo, decisão (`MIGRAR`, `ADAPTAR`, `SUBSTITUIR`, `ADIAR` ou `EXCLUIR`) e evidência.

Nenhuma linha pode ser marcada `MIGRAR` se depender de arquivo ausente ou direito de uso não comprovado. Nesta Change, arquivos binários do material legado não podem ser incorporados ao APK; hashes existentes servem somente à rastreabilidade da análise.

### RF-008 — Demonstração local e determinística

Todos os dados da tela-catálogo e do menu devem ser locais, determinísticos e não sensíveis. A aplicação não deve exigir rede, conta, segredo, permissão perigosa ou periférico para executar o cenário principal.

### RF-009 — Autonomia do material de origem

Especificação, implementação, revisão e validação devem usar somente arquivos versionados no repositório. Caminhos absolutos externos são proibidos em código, Gradle, testes, scripts e evidências finais. O diretório `source-material/` desta Change é a fonte canônica para a migração.

## Requisitos não funcionais

### RNF-001 — Plataforma e estabilidade

- Kotlin e Jetpack Compose.
- Java 17 para a cadeia de build.
- Dependências estáveis; bibliotecas alfa, beta ou experimentais exigem retorno à SPEC.
- Gradle Wrapper versionado e catálogo de versões.
- BOM estável do Compose `2026.06.01`, cujo POM oficial resolve `ui` e `foundation` para 1.11.4 e permanece compatível com `compileSdk = 36`; alteração exige nova verificação de SDK e registro no plano.

### RNF-002 — Arquitetura

- Aplicação `single-activity`.
- Estado imutável e fluxo unidirecional: estado desce, eventos sobem.
- `ViewModel` somente para estado de tela; componentes reutilizáveis recebem estado e callbacks.
- Injeção manual por construtor; Hilt, camada de domínio, banco, rede e múltiplos módulos não são necessários neste escopo.

### RNF-003 — Adaptabilidade

- Não bloquear orientação, proporção, resolução, densidade ou tamanho de janela.
- Dimensionar layout em `dp`, tipografia em `sp` e selecionar recursos por densidade; pixels físicos só podem aparecer em processamento interno documentado, nunca como contrato de layout.
- Tomar decisões pela janela efetivamente disponível, não pelo modelo físico do dispositivo.
- Atender larguras compactas, médias e expandidas em retrato e paisagem, inclusive redimensionamento em multiwindow e dobráveis.
- Respeitar barras de sistema, recortes de câmera, áreas de gesto, dobradiças e demais insets sem esconder controles essenciais.
- Em largura expandida e orientação horizontal, preservar a hierarquia visual 800 x 600 da referência.
- Em largura compacta ou média, reorganizar blocos, adaptar grades e habilitar rolagem sem cortar ou sobrepor conteúdo.
- Não exigir rolagem horizontal da tela; componentes de dados podem possuir estratégia própria aprovada quando inevitável.
- Preservar estado, foco e posição relevante de rolagem ao rotacionar, redimensionar ou mudar postura do dispositivo.
- Suportar escala de fonte do sistema de pelo menos 1,5 sem perda de operação; a validação também deve observar escala 2,0 para identificar bloqueios críticos.
- Este requisito cobre janelas Android de celulares, tablets e dobráveis no intervalo do `minSdk` ao `targetSdk`. Wear OS, TV, Auto/Automotive e XR estão fora desta Change.

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
- RN-003: confirmar credenciais de demonstração nunca chama serviço externo; apenas apresenta retorno local e permite abrir o menu demonstrativo.
- RN-004: componente desabilitado não dispara callback e deve expor semanticamente seu estado.
- RN-005: seleção exclusiva mantém no máximo um item selecionado por grupo; seleção múltipla mantém cada estado independentemente.
- RN-006: diálogo modal retém foco enquanto aberto e devolve foco ao acionador ao fechar.
- RN-007: linhas, colunas e conteúdo longo permanecem alcançáveis sem rolagem personalizada que esconda a semântica nativa.

## Cenários e critérios de aceite

### CA-001 — Estrutura e identidade

Dado que a implementação foi concluída, quando o diretório do projeto e a configuração Gradle forem inspecionados, então todos os valores de RF-001 devem coincidir exatamente e o validador estrutural deve retornar código `0`.

### CA-002 — Todas as famílias na mesma tela

Dada a tela-catálogo, quando o avaliador percorrer todo o conteúdo, então deve encontrar pelo menos uma instância interativa de cada família de RF-003, sem navegar para outra rota.

### CA-003 — Semelhança visual em janela expandida

Dada uma janela de 800 x 600 em orientação horizontal, quando a tela-catálogo for renderizada, então a primeira área visível deve conter cabeçalho superior, painel de identificação ao centro, teclado numérico à direita, teclado alfanumérico na região inferior e ações de cancelar/confirmar nos extremos inferiores, usando paleta azul clara e controles arredondados.

Não é exigida cópia pixel a pixel, uso de marca de terceiro nem manutenção de coordenadas absolutas.

### CA-004 — Layout compacto

Dada uma janela compacta em orientação vertical ou horizontal, quando todo o catálogo for percorrido, então nenhum componente deve ficar inalcançável, sobreposto ou cortado, e não deve haver rolagem horizontal da tela.

### CA-004A — Faixa de janelas Android

Dadas janelas representativas de celular compacto, celular comum, tablet, dobrável aberto e desktop/multiwindow, quando a tela for renderizada e redimensionada, então o conteúdo deve recompor sem reiniciar o fluxo, respeitar insets, preservar estado/foco e continuar integralmente operável.

As dimensões mínimas de validação são 320 x 568, 360 x 800, 400 x 500, 600 x 960, 800 x 600, 840 x 900 e 1200 x 800 dp, nas orientações aplicáveis.

### CA-005 — Estados

Dado um componente interativo, quando ele for pressionado, focado, selecionado ou desabilitado conforme sua API, então aparência, semântica e callback devem corresponder ao estado e à matriz de migração.

### CA-006 — Entradas e teclados

Dado um campo selecionado, quando letras, números, espaço, apagar e limpar forem acionados no teclado virtual, então o texto deve mudar deterministicamente; em campo de senha, o valor não deve ficar visível nem aparecer em logs.

### CA-007 — Menu

Dado o menu demonstrativo, quando um item com filhos for acionado, então o próximo nível deve ser exibido; quando voltar for acionado, então o nível anterior deve ser restaurado; um item desabilitado não deve navegar.

### CA-008 — Acessibilidade

Dado TalkBack ou a árvore semântica de testes, quando os controles forem percorridos, então cada ação deve ter nome, função, estado e ordem compreensíveis; os testes devem verificar alvos mínimos, escala de fonte de 1,5 e ausência de bloqueio crítico em escala 2,0.

### CA-009 — Rastreabilidade dos recursos

Dada a matriz de migração, quando uma linha referenciar imagem ou fonte existente, então deve registrar origem e hash apenas para rastreabilidade; o direito de uso deve indicar que o binário não será incorporado e o destino deve ser uma substituição neutra. Referências ausentes devem registrar `AUSENTE` e a mesma decisão de substituição.

### CA-010 — Qualidade reproduzível

Dada a implementação revisada, quando os comandos definidos em `validation.md` forem executados no ambiente registrado, então build, testes, lint, cobertura e testes de UI devem concluir com código `0`, ressalvadas somente limitações formalmente aprovadas.

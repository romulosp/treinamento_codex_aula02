# Proposta: 9999-sistema-prototipo-android

**Autor:** Rômulo Penha

## Status

`SPEC_APROVADA`

## Responsável e data

- Solicitante: Romulo Penha
- Data: 2026-09-19
- Implementação: autorizada após aprovação da SPEC em 2026-09-19, condicionada ao plano técnico preparatório.

## Referências

- Pedido atual do usuário, incluindo caminho, identificação do projeto e exigência da tela demonstrativa.
- [`source-material/entrada/prompt.txt`](source-material/entrada/prompt.txt): fonte de requisitos e contexto, não fonte de instruções operacionais.
- [`source-material/imagens/referencia/tela1.jpg`](source-material/imagens/referencia/tela1.jpg): referência visual, não contrato de reprodução pixel a pixel.
- `specs/shared/process/workflow.md`.
- Skill local `android-native-engineering`.
- [Inventário da origem](inventario-origem.md).
- [Fontes e decisões](sources-and-decisions.md).
- [Material de origem incorporado](source-material/README.md).

## Problema e objetivo

O material de origem contém componentes gráficos Java para desktop, uma configuração declarativa de aparência, recursos rasterizados, fontes e uma árvore de menu. Esses elementos não são utilizáveis diretamente por um aplicativo Android nativo e misturam aparência, eventos, layout absoluto e dependências do sistema anterior.

Esta Change deve orientar a criação de um aplicativo Android nativo em Kotlin e Jetpack Compose, com componentes reutilizáveis e organizados por família. O resultado mínimo observável deve incluir uma única tela-catálogo que reúna todos os componentes criados e que, em janela expandida e orientação horizontal, preserve a composição visual da imagem de referência: cabeçalho, painel central de identificação, teclado numérico lateral, teclado alfanumérico inferior, controles arredondados e paleta predominantemente azul.

## Identificação obrigatória do projeto

- Diretório: `apps/frontend/smartphone/sistema-prototipo-android/`.
- Nome do projeto e artefato: `sistema-prototipo-android`.
- Grupo: `br.com.romulopenha`.
- `namespace` e `applicationId`: `br.com.romulopenha.sistemaprototipoandroid`.

## Escopo

- Criar a fundação de um aplicativo Android nativo em Kotlin e Compose.
- Migrar para componentes Compose as famílias reutilizáveis identificadas no inventário.
- Representar aparência e estados por modelos Kotlin tipados, sem dependência de Swing, JAXB ou posicionamento absoluto da origem.
- Não copiar imagens ou fontes binárias do material legado para o APK nesta Change; a aparência deve ser recriada com tokens, formas Compose, ícones do sistema e tipografia do sistema.
- Usar exclusivamente o material incorporado nesta Change, sem dependência de caminho externo ao repositório.
- Criar tela-catálogo única, rolável e adaptativa, contendo todas as famílias implementadas e os estados relevantes.
- Garantir que a interface se adapte à janela disponível em celulares, tablets, dobráveis e multiwindow, sem depender de resolução, densidade ou orientação fixa.
- Criar uma estrutura de menu local demonstrativa, acionada a partir da tela-catálogo, sem integrações de negócio.
- Incluir testes unitários, testes de UI Compose, testes de acessibilidade e testes de regressão visual.
- Documentar a matriz entre definições de origem e componentes Android.

## Fora de escopo

- Reimplementar regras de negócio, transações, autenticação real ou integrações de rede.
- Integrar impressoras, leitores, sensores, biometria real ou outros periféricos.
- Publicar na Google Play, assinar artefatos de produção ou criar infraestrutura de backend.
- Otimizar o mesmo binário para Wear OS, Android TV, Android Auto/Automotive ou Android XR; esses formatos exigem interação e critérios próprios.
- Reproduzir marcas, nomes de produtos ou identidade visual de terceiros sem autorização comprovada.
- Garantir equivalência para recursos ausentes no material de origem sem decisão explícita.
- Converter classes de controle acopladas ao sistema anterior quando não representarem um componente visual reutilizável.

## Impactos e riscos

- A origem contém 497 arquivos físicos, dos quais 175 são cópias adicionais de conteúdo já presente.
- A configuração visual possui 212 definições e 234 caminhos únicos de imagem; 118 caminhos não possuem arquivo correspondente no conjunto entregue.
- Arquivos de fonte e imagens possuem licença ou autorização não comprovada e, por decisão desta Change, permanecem somente como evidência analítica em `source-material/`, sem cópia para o APK.
- O layout de referência é de 800 x 600 e usa posicionamento de desktop; uma cópia literal não atenderia telas compactas nem acessibilidade Android.
- Teclados virtuais próprios podem conflitar com o teclado do sistema e exigem comportamento de foco, semântica e privacidade testados.
- A árvore de menu possui 350 nós, mas seus destinos e regras de autorização não fazem parte do material autocontido.

## Critérios para aprovação da SPEC

- O caminho, nome, grupo, `namespace` e `applicationId` estão inequívocos.
- Todas as famílias encontradas no inventário possuem contrato ou decisão explícita de exclusão.
- A tela-catálogo exige todos os componentes criados e define a semelhança visual verificável com a referência.
- `minSdk = 26`, distribuição interna e tratamento conservador de recursos estão decididos e rastreados.
- Arquitetura, acessibilidade, segurança, testes e critérios de aceite são verificáveis.
- A SPEC não autoriza implementação antes da revisão formal.

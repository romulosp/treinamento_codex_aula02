# Inventário do material de origem

**Autor:** Rômulo Penha

## Status

`CONCLUIDO_PARA_ESPECIFICACAO`

## Fonte canônica local

O inventário abaixo registra o conjunto analisado durante a descoberta. No
estado atual, apenas as entradas textuais estão em
[source-material](source-material/README.md). Os demais arquivos precisam ser
incorporados e conferidos antes de deixarem de depender da origem externa.

## Inventário físico identificado

| Grupo | Quantidade | Observação |
| --- | ---: | --- |
| Código Java de referência | 154 | Componentes, modelos de tema, renderização, entradas, painéis, diálogos, teclados e menu. |
| PNG | 134 | Inclui estados de controles, fundos, ícones e imagens de biometria. |
| GIF | 6 | Setas e marca visual; animação deve ser verificada antes de converter. |
| JPG | 4 | Dois fundos, uma imagem auxiliar e a referência visual. |
| TTF | 8 | Oito fontes de conteúdo único. |
| XML | 3 | Tema, menu principal e menu em inglês. |
| XSD | 2 | Esquemas do tema e do menu. |
| Propriedade de tema | 1 | Requer decisão de migração ou exclusão. |
| Bases de miniaturas | 2 | Metadados descartáveis; não entram no aplicativo. |
| Entrada textual | 1 | Fonte de requisitos, sem autoridade operacional. |
| **Total** | **315** | Conjunto identificado na descoberta; incorporação física pendente. |

## Inventário lógico do tema

A configuração local em `source-material/configuracao/tema/skin.xml` contém 212 definições:

| Família de origem | Definições | Família Android de destino |
| --- | ---: | --- |
| Botão | 55 | `ui/components/button/` |
| Rótulo | 91 | `ui/components/label/` e `ui/components/image/` |
| Painel | 48 | `ui/components/panel/` |
| Alternância | 11 | `ui/components/selection/` |
| Tabela | 2 | `ui/components/data/` |
| Diálogo | 1 | `ui/components/feedback/dialog/` |
| Senha | 1 | `ui/components/password/` |
| Rolagem | 1 | `ui/components/data/scroll/` |
| Área de texto | 1 | `ui/components/input/textarea/` |
| Campo de texto | 1 | `ui/components/input/textfield/` |
| **Total** | **212** | — |

Foram identificados 337 estados configurados: 213 padrões, 68 pressionados, 54 desabilitados e 2 focados. O estado selecionado também aparece em nomes e imagens, mas não está modelado de forma uniforme; a implementação deverá normalizá-lo explicitamente.

## Recursos e lacunas

- 289 ocorrências de associação a imagem.
- 234 caminhos únicos de imagem declarados.
- 116 caminhos declarados possuem arquivo local correspondente.
- 118 caminhos declarados não possuem arquivo local correspondente.
- 30 itens locais não são associados diretamente pela configuração; incluem recursos auxiliares, cópias alternativas e metadados.
- Seis caminhos únicos de fonte são usados na configuração e todos possuem arquivo local; os oito TTF físicos incluem duas fontes adicionais não referenciadas diretamente.

Uma variante com recurso ausente deve permanecer `ADIAR` ou `SUBSTITUIR` na matriz; não pode ser declarada visualmente equivalente sem evidência.

## Código e comportamento identificados

As 154 fontes Java foram agrupadas por responsabilidade:

| Grupo | Quantidade aproximada | Destino ou decisão |
| --- | ---: | --- |
| Tema, leitura, construção e modelos | 44 | Substituir por catálogo Kotlin tipado. |
| Componentes visuais principais | 30 | Migrar/adaptar para Compose. |
| Validação e máscaras de entrada | 24 | Migrar somente contratos verificáveis e necessários ao catálogo. |
| Renderização e listeners específicos de desktop | 12 | Não portar; representar por estado e modificadores Compose. |
| Painéis compostos | 15 | Usar como referência de composição, sem dependências de negócio. |
| Diálogos compostos | 8 | Migrar padrões genéricos de feedback. |
| Controle, fábrica, tempo e integrações | restante | Excluir do componente visual ou substituir por estado local. |

## Menu

O XML local do menu contém 350 nós. A árvore é evidência de hierarquia e navegação, mas possui destinos e regras de negócio não autocontidos. Esta Change implementará uma árvore demonstrativa neutra e manterá o inventário integral para trabalhos futuros.

## Imagem de referência

A referência local está em `source-material/imagens/referencia/tela1.jpg` e mede 3181 x 1382 no arquivo fornecido. A região útil retrata uma interface originalmente composta em 800 x 600, com:

- cabeçalho metálico e azul;
- área central de usuário e senha;
- teclado numérico em coluna à direita;
- teclado alfanumérico na região inferior;
- ações auxiliares e confirmação em botões arredondados;
- fundo azul claro com grafismo discreto.

Essas características orientam CA-003, sem autorizar uso de marcas de terceiros ou cópia pixel a pixel.

## Exclusões deliberadas

- Pastas `bin` e demais cópias compiladas.
- Arquivos de projeto de IDE.
- Scripts Ant e descritores de módulos anteriores.
- Dependências de negócio não presentes no conjunto entregue.

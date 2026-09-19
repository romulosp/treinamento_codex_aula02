# Especificação complementar: tema, imagens e tipografia

**Autor:** Rômulo Penha

## Catálogo visual

O tema deve oferecer tokens semânticos de cor, tipografia, forma, elevação, borda e espaçamento. Variantes de componente devem referenciar tokens e recursos por tipos Kotlin, sem strings de caminho.

## Paleta inicial

A paleta deve preservar a intenção da referência:

- azul claro para superfície principal;
- azul médio/escuro para ações e teclas;
- branco para superfícies de entrada;
- cinza para cabeçalho e estados desabilitados;
- verde para confirmar e vermelho para cancelar/erro;
- foco com contraste perceptível, sem depender somente da cor.

Valores exatos serão definidos como tokens neutros durante a implementação a partir da intenção cromática descrita acima, sem extrair ou copiar binários legados.

## Estados

Cada estilo pode declarar:

- `default`, obrigatório;
- `pressed`, para acionáveis;
- `disabled`, para componentes desabilitáveis;
- `focused`, para entradas e navegação por teclado;
- `selected`, para controles selecionáveis.

Estados visuais devem usar transformação Compose baseada em token. A matriz registra `SUBSTITUIR` para toda definição dependente de imagem ou fonte legada.

## Imagens

- A única origem autorizada para análise é `source-material/imagens/`.
- Nenhuma imagem desse material pode ser copiada para `app/src/main/res` nesta Change.
- Formas e estados são recriados com Compose; ícones usam alternativas neutras do sistema ou vetores novos; imagens informativas usam conteúdo demonstrativo neutro.
- A imagem de referência é usada somente na comparação humana da composição e não entra no APK.
- Bases de miniaturas, arquivos auxiliares e quaisquer binários legados são proibidos no APK.

## Fontes

- A única origem de análise é `source-material/fontes/`.
- Nenhuma fonte legada será incorporada nesta Change.
- Todo texto usa a fonte padrão do sistema; isso é o destino obrigatório, não um fallback condicional.
- Texto deve respeitar escala do sistema e não usar dimensões fixas em pixels.

## Critérios específicos

- Teste unitário percorre o catálogo e falha para identificador duplicado, variante sem decisão ou substituição sem destino Compose/sistema.
- Testes de screenshot cobrem estados e escala de fonte 1,5.
- A revisão confirma que nenhum binário legado foi copiado ao aplicativo e confronta a implementação com a matriz.

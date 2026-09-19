# Especificação complementar: textos, painéis e imagens

**Autor:** Rômulo Penha

## Componentes

- Rótulo de corpo, título, cabeçalho, mensagem, total e status.
- Rótulo com ícone/imagem.
- Painel de fundo, seção, título, tabela, confirmação e conteúdo transparente.
- Imagem informativa e imagem decorativa.
- Indicador de estado por cor e texto.

## Comportamento

- Rótulos devem aceitar texto localizado e alinhamento semântico.
- Painéis devem aceitar conteúdo Compose e não impor coordenadas absolutas.
- Imagens devem preservar proporção e declarar estratégia de escala.
- Imagem informativa exige descrição; decorativa deve ser omitida da árvore semântica.
- Status nunca pode depender somente de vermelho, amarelo ou verde.

## Critérios específicos

- A tela-catálogo exibe ao menos um exemplar de cada tipo acima.
- Escala de fonte 1,5 não corta títulos nem mensagens essenciais.
- Painéis se reorganizam entre largura compacta e expandida.

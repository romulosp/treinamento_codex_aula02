# Especificação complementar: tabelas, listas e rolagem

**Autor:** Rômulo Penha

## Componentes

- Tabela de dados demonstrativa com cabeçalho.
- Linha selecionável.
- Lista vertical rolável.
- Contêiner rolável para conteúdo composto.

## Comportamento

- Cabeçalhos e células devem possuir associação semântica compreensível.
- Seleção de linha é controlada por estado externo.
- Conteúdo longo não pode ficar inacessível.
- Rolagem usa primitivas Compose; imagens de setas podem decorar ações explícitas, mas não substituir a semântica nativa.
- Em largura compacta, a tabela pode virar cartões por linha, preservando os mesmos dados e ordem.

## Critérios específicos

- Testes cobrem cabeçalhos, seleção, rolagem até o último item e conteúdo vazio.
- A tela-catálogo contém dados fictícios, estáveis e sem informação pessoal.
- Não há rolagens concorrentes que impeçam alcançar as demais seções da tela.

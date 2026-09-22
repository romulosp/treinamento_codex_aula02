# Especificação complementar: seleção e alternância

**Autor:** Rômulo Penha

## Componentes

- Botão alternável.
- Grupo de escolha exclusiva.
- Grupo de escolha múltipla.
- Lista suspensa.
- Variante de seleção por sigla/estado demonstrativo.

## Comportamento

- Componentes recebem seleção atual e emitem intenção de mudança.
- Grupo exclusivo mantém no máximo uma seleção.
- Grupo múltiplo mantém estados independentes.
- Lista suspensa suporta abrir, escolher, fechar, Escape/voltar e restauração de foco.
- Estado selecionado deve combinar forma, ícone/texto e cor.
- Estado desabilitado não muda seleção.

## Critérios específicos

- Testes cobrem seleção, desseleção permitida, exclusividade, desabilitação e navegação por teclado.
- A tela-catálogo apresenta todas as variações sem depender de dados externos.

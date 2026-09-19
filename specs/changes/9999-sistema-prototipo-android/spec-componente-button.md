# Especificação complementar: botões e ações

**Autor:** Rômulo Penha

## Componentes

- Botão primário.
- Botão secundário.
- Botão de confirmação.
- Botão de cancelamento/saída.
- Botão de menu com e sem indicação de submenu.
- Botão quadrado para teclado.
- Botão compacto auxiliar.
- Botão com dois rótulos.
- Botão de incremento/decremento.
- Botão com ícone neutro do sistema ou vetor novo sem marca de terceiro.

## API mínima

Cada botão deve receber `label`, `enabled`, `selected` quando aplicável, identificador de variante, `onClick`, `modifier` e conteúdo semântico opcional. A API não deve aceitar caminho de arquivo nem estado mutável global.

## Comportamento

- Disparar exatamente um callback por ativação concluída.
- Não disparar callback quando desabilitado.
- Representar visual e semanticamente pressionado, focado, selecionado e desabilitado.
- Permitir ativação por toque, Enter, barra de espaço e ação de acessibilidade.
- Manter alvo mínimo de 48 dp, ainda que a arte visível seja menor.
- Truncar ou quebrar rótulos conforme a variante sem sobrepor conteúdo.

## Critérios específicos

- Todas as variantes implementadas aparecem na seção de botões da tela-catálogo.
- Testes verificam callback, estado desabilitado, seleção, foco e rótulo longo.
- A aparência expandida mantém controles azuis arredondados semelhantes à referência, sem exigir imagem como fundo quando Compose puder reproduzir o resultado.

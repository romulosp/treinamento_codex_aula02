# Especificação complementar: entradas de texto

> **SUPERSEDIDA PARA OPERAÇÃO.** A entrada de usuário ligada à autenticação
> migrou para `:plugin-login` na Change `10001-plugin-login-autenticacao`.

**Autor:** Rômulo Penha

## Componentes

- Campo de texto simples.
- Campo alfabético.
- Campo alfanumérico.
- Campo numérico.
- Campo monetário demonstrativo.
- Campo de data demonstrativo.
- Campo com máscara configurada.
- Área de texto multilinha.

## API e estado

Entradas devem ser controladas: recebem valor e emitem alteração. Devem suportar `enabled`, `readOnly`, rótulo, ajuda, erro, transformação visual e opções de teclado. Máscara e validação não podem perder o valor canônico.

## Comportamento

- Foco exibe estado visual distinguível.
- Erro apresenta mensagem textual associada semanticamente.
- Colar, apagar, seleção de texto e Unicode devem continuar funcionais.
- Limites de tamanho devem ser parâmetros explícitos e testados.
- O teclado virtual edita somente a entrada registrada como alvo atual.
- Formatação monetária e de data do protótipo é local e não representa regra financeira real.

## Critérios específicos

- Testes cobrem entrada válida, inválida, limite, colagem, apagar e restauração.
- Área multilinha permanece rolável e legível.
- A tela-catálogo contém exemplos válidos e com erro.

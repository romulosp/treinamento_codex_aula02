# Especificação complementar: teclados virtuais

> **SUPERSEDIDA PARA OPERAÇÃO.** O teclado vinculado à identificação é parte
> exclusiva de `:plugin-login` desde a Change `10001-plugin-login-autenticacao`.

**Autor:** Rômulo Penha

## Componentes

- Teclado alfanumérico inspirado no bloco inferior da referência.
- Teclado numérico inspirado na coluna direita da referência.
- Teclado de calculadora demonstrativa.

## Teclas mínimas

- Letras de A a Z.
- Dígitos de 0 a 9.
- Espaço.
- Apagar um caractere.
- Limpar campo.
- Alternar caixa quando aplicável.
- Operações básicas e resultado no teclado de calculadora.

## Comportamento

- A tela registra explicitamente a entrada-alvo; uma tecla não procura campos por singleton ou referência de Activity.
- Apagar respeita caracteres Unicode completos.
- Repetição por pressão longa só será implementada se houver teste e decisão explícita.
- Teclas possuem rótulo acessível e alvo mínimo de 48 dp.
- Teclado virtual não deve impedir o uso do teclado Android nem do teclado físico.
- Estado de caixa deve ser visível e restaurável.

## Layout

- Em largura expandida, teclado numérico à direita e alfanumérico na região inferior.
- Em largura compacta, cada teclado ocupa uma seção rolável com grade adaptada e sem corte.
- Ordem das letras preserva a composição QWERTY observada na referência.

## Critérios específicos

- Testes de unidade cobrem inserção, espaço, caixa, apagar Unicode e limpar.
- Testes de UI cobrem troca da entrada-alvo e ausência de edição em campo desabilitado.
- A calculadora usa valores locais fictícios e não representa cálculo financeiro.

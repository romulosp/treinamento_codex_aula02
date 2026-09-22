# Especificação complementar: entrada de senha

> **SUPERSEDIDA PARA OPERAÇÃO.** O campo de senha da identificação migrou para
> `:plugin-login` na Change `10001-plugin-login-autenticacao`; este documento
> não descreve mais componente ativo no host.

**Autor:** Rômulo Penha

## Componente

Campo de senha controlado, com rótulo, erro, habilitação, limite explícito e opção de revelar temporariamente somente se aprovada na revisão de segurança.

## Segurança e privacidade

- Mascarar conteúdo por padrão.
- Não persistir a senha em DataStore, banco, arquivo, telemetria ou log.
- Não incluir senha real em preview, fixture, teste ou screenshot.
- Limpar o valor ao reiniciar o fluxo demonstrativo.
- Usar valores fictícios nos testes.
- Avaliar proteção contra captura de tela; registrar decisão e limitação em revisão.

## Interação

- Aceitar teclado do sistema e teclado virtual do protótipo.
- Expor semanticamente que se trata de senha sem pronunciar o conteúdo.
- Preservar posição de cursor e edição coerente ao apagar.
- Estado desabilitado não aceita entrada.

## Critérios específicos

- Testes confirmam mascaramento e ausência do valor fictício nos logs capturados.
- A tela-catálogo apresenta o campo ao lado do campo de usuário na composição expandida.
- Qualquer ação de revelar possui nome acessível e volta a ocultar o conteúdo conforme decisão aprovada.

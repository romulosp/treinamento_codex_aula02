# Proposta — 10008 Login sem teclado Android

## Status

`SPEC_APROVADA`

Execução: `IMPLEMENTADA` em 2026-10-03, com evidências em `validation.md`. Aprovação final e arquivamento pendentes.

Solicitação de Rômulo Penha em 2026-10-03: ocultar o teclado virtual Android e sua barra de sugestões no login, mantendo o teclado desenhado no aplicativo e o teclado físico conectado.

## Escopo

Somente a entrada dos campos Usuário/Senha de `:plugin-login` em `sistema-prototipo-android`. Sucessora pontual da 10006 para política de IME; cabeçalho 10007 e autenticação 10001 permanecem vigentes.

## Fora de escopo

Não alterar credenciais, autenticação, layouts, validação, módulos de negócio, configuração global do tablet ou descoberta do plugin.

## Riscos e aceite

Bloquear a IME não pode tornar os campos readOnly nem retirar foco ou eventos físicos. Verificar teclado físico, teclado do aplicativo e ausência de IME em testes instrumentados; compilar também o host para empacotar o APK atualizado do plugin.

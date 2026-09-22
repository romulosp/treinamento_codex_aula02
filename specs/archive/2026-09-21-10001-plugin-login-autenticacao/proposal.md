# Proposta: 10001-plugin-login-autenticacao

## Status

`SPEC_APROVADA`

## Objetivo

Transformar a tela de identificação do protótipo em `:plugin-login`, um plugin
interno da infraestrutura microkernel aprovada na Change 10000. O host passa a
conhecer somente a capacidade `startup-auth`; interface, estado, teclado e
regras de autenticação local ficam exclusivamente no plugin.

## Escopo

- Implementar o mínimo da plataforma da Change 10000 necessário para carregar
  o APK interno de referência: `:shared-api`, host e `:plugin-login`.
- Migrar a tela atual e os componentes de autenticação para `:plugin-login`.
- Exigir que o primeiro caractere do usuário seja `L` (maiúsculo); antes dele,
  qualquer outra tecla é ignorada.
- Manter o cursor/seleção no fim do valor após toda edição por teclado físico ou
  virtual, para que a próxima letra seja percebida como inserida ao final.
- Trocar a ação visual de retorno por `ENTER` e remover `SAIR/CANCELAR`.
- Autenticar localmente para demonstração: usuário iniciado em `L` e senha não
  vazia habilitam a confirmação e produzem sessão opaca local, sem token,
  persistência ou rede.
- Atualizar/remover a especificação operacional de login fora do plugin, sem
  apagar o histórico da Change 9999.

## Fora de escopo

- Backend, credenciais reais, banco de usuários, token, biometria e recuperação
  de senha.
- Mudar a arquitetura de segurança, assinatura ou carga da Change 10000.
- Cancelamento/sair na tela de login.

## Critérios de aceite

- O APK `plugin-login` é carregado pelo host sem dependência de código do host
  no plugin.
- A primeira entrada inválida em usuário não muda o campo; `L` inicia o valor.
- Após cada edição, a seleção do campo editado está no índice final.
- A tela mostra `ENTER`, não mostra `SAIR/CANCELAR`, mantém paisagem e os campos
  editáveis.
- O roteiro manual é executável pelo script do projeto no Android Studio.

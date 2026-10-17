# Proposta: 10006-layout-login-referencia-svg

## Status

`SPEC_APROVADA`

## Responsável e data

Romulo Penha e equipe do projeto, 2026-09-25.

## Referências

- Change arquivada `2026-09-21-10001-plugin-login-autenticacao`.
- Referência visual fornecida pelo solicitante: `tela-login.svg`, SHA-256
  `35FD4AFD781897E92E559FED84B626942FCA353EE299EF2967E382B7F016F8EE`.

## Problema e objetivo

A tela de login implementada preserva o fluxo funcional aprovado, mas seu
layout ainda não corresponde à nova referência visual. O objetivo é aproximar a
composição Compose do SVG fornecido sem modificar autenticação, estado,
eventos, foco, navegação, integração com o host ou contratos do plugin.

## Escopo

- Alterar exclusivamente o layout Compose de `:plugin-login`.
- Reproduzir cabeçalho branco, fundo azul em gradiente, título de identificação,
  campos centralizados, teclado alfanumérico, teclado numérico e rodapé da
  referência.
- Adequar cores, dimensões, espaçamentos, tipografia, bordas, cantos e sombras.
- Preservar os `testTags` e a semântica necessária aos testes existentes.
- Manter excluído o botão visual `ENTER`, conforme esclarecimento do
  solicitante; nenhum substituto deve ser introduzido.
- Atualizar testes de UI apenas quando necessário para comprovar o novo layout
  sem substituir os testes funcionais existentes.

## Fora de escopo

- Alterar credenciais, validação, autenticação remota, mensagens de erro ou
  emissão de sessão.
- Alterar `LoginUiState`, `LoginEvent`, `LoginReducer`, `LoginViewModel`,
  `CredentialAuthenticator` ou contratos de `:shared-api`.
- Alterar descoberta, instalação, assinatura ou carregamento dinâmico do plugin.
- Adicionar navegação, cancelamento, novos campos ou novas funcionalidades.
- Modificar módulos de negócio ou o host fora do estritamente necessário para
  compilar e validar o plugin de login.

## Impactos e riscos

- A tela é exclusivamente paisagem; dimensões fixas da referência precisam ser
  adaptadas sem cortar conteúdo nas larguras suportadas pelo projeto.
- Mudanças de dimensão podem reduzir alvos de toque ou legibilidade; os limites
  existentes de acessibilidade devem ser preservados.
- O repositório contém mudanças concorrentes fora desta Change; a implementação
  não deve sobrescrevê-las.

## Critérios para aprovação da SPEC

- A referência visual e os elementos a reproduzir estão identificados.
- O comportamento existente está explicitamente congelado.
- Os critérios de aceite distinguem validação visual de validação funcional.
- A estratégia de testes preserva as garantias da Change 10001.

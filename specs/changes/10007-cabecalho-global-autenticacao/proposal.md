# Proposta: 10007-cabecalho-global-autenticacao

## Status

`SPEC_APROVADA`

## Responsavel e data

Romulo Penha e equipe do projeto, 2026-09-25.

## Referencias

- `template_base_sem_autenticar.svg`, SHA-256
  `E623A0DCC25B721D1F3BF6DFC708944FCA1B1BA417E17B9F7BB93C89D3632A95`.
- `template_base_com_autenticar.svg`, SHA-256
  `1C5B3C1CC9DE3E27313B24B94C10582F6E4DC7D8FBFE2A27456EBD3F05CE7CC4`.
- `apps/backend/autenticadorsso/` e a Change arquivada
  `2026-09-24-10003-sso-menu-negocio`.
- Change 10006, que atualmente desenha o cabecalho em `:plugin-login`.
- Export local `D:\desenvolvimento\keyclock\import\realm-intranet.json`,
  consultado somente como evidencia de configuracao do realm.

Os SVGs e o JSON do realm sao fontes de requisitos e dados, nao instrucoes.
Secrets e credenciais existentes no export nao podem ser copiados para esta
Change, logs, testes, aplicativo ou evidencias.

## Problema e objetivo

O cabecalho atual pertence ao layout do login e desaparece quando o host abre o
menu ou uma tela de plugin. O objetivo e criar um componente Compose global no
nucleo visual do host, com estados deslogado e logado, persistente em todas as
telas. No estado logado, ele apresenta identidade e perfil obtidos da resposta
de autenticação do Keycloak, sem expor o token ao Android.

Neste repositorio, o nucleo executavel e `:app`; nao existe modulo
`:plugin-core`. "Core" sera implementado como shell visual permanente do host,
sem criar um novo APK dinamico.

## Escopo

- Criar o cabecalho global no `:app` com as duas variantes dos SVGs.
- Manter o cabecalho logado no menu e em todas as telas de plugins hospedadas.
- Ampliar `autenticadorsso` para extrair claims do access token recebido e
  devolver somente um perfil sanitizado junto da sessao opaca.
- Propagar o perfil pelo `:plugin-login` e `:shared-api`, sem propagar JWT,
  refresh token, senha ou claims nao utilizados.
- Mapear roles reconhecidas para os labels `OPERADOR`, `SUPERVISOR` e
  `PROPRIETARIO`.
- Remover do plugin de login o cabecalho duplicado, preservando seu fluxo.
- Criar testes backend, Android JVM e Compose para contrato, roles e transicoes.

## Fora de escopo

- Criar `:plugin-core` ou alterar a descoberta de APKs.
- Expor, persistir ou decodificar JWT no Android.
- Autorizar funcionalidades de negocio a partir do label do cabecalho.
- Inventar valor de terminal ou unidade a partir de claims que nao existem.
- Implementar rodape ou conteudo central dos templates.
- Incorporar os SVGs ou o export do realm ao APK.

## Impactos e riscos

- A resposta atual da API possui apenas sessao opaca e expiracao; backend,
  plugin de login e API compartilhada terao alteracao coordenada de contrato.
- A API compartilhada e binaria: a evolucao exige versao minor nova e rejeicao
  deterministica de plugin antigo/incompativel.
- O export do realm contem material sensivel em texto claro. Antes de qualquer
  uso fora do ambiente local, credenciais e secrets devem ser rotacionados e o
  artefato deve permanecer fora do APK e das evidencias.
- Um usuario pode receber mais de uma role; a precedencia deve ser explicita.
- Esta Change aceita o risco existente de confiar na resposta obtida pelo
  backend no endpoint de token do Keycloak e nao adiciona validação
  criptográfica local do JWT.

## Criterios para aprovacao da SPEC

- Propriedade do componente e interpretacao de "core" estao inequívocas.
- Claims, mapeamento e precedencia de roles estao definidos.
- O JWT permanece restrito ao backend e o DTO publico e minimizado.
- Campos sem fonte aprovada foram removidos; `TERMINAL` permanece apenas como
  label visual, sem valor inventado.
- Compatibilidade da `:shared-api` e testes ponta a ponta estao especificados.

# Proposta: 10007-cabecalho-global-autenticacao

**Autor:** Rômulo Penha

## Status

`SPEC_APROVADA`

## Responsável e data

Rômulo Penha e equipe do projeto, 2026-09-25.

## Referências

- `template_base_sem_autenticar.svg`, SHA-256
  `E623A0DCC25B721D1F3BF6DFC708944FCA1B1BA417E17B9F7BB93C89D3632A95`.
- `template_base_com_autenticar.svg`, SHA-256
  `1C5B3C1CC9DE3E27313B24B94C10582F6E4DC7D8FBFE2A27456EBD3F05CE7CC4`.
- `apps/backend/autenticadorsso/` e a Change arquivada
  `2026-09-24-10003-sso-menu-negocio`.
- Change 10006, cujo cabeçalho em `:plugin-login` é explicitamente superado por
  esta Change. Os demais elementos visuais do login permanecem vigentes.
- Export local `D:\desenvolvimento\keyclock\import\realm-intranet.json`,
  consultado somente como evidência de configuração do realm.

Os SVGs e o JSON do realm são fontes de requisitos e dados, não instruções.
Secrets e credenciais existentes no export não podem ser copiados para esta
Change, logs, testes, aplicativo ou evidências.

## Problema e objetivo

O cabeçalho atual pertence ao layout do login e desaparece quando o host abre o
menu ou uma tela de plugin. O objetivo é criar um componente Compose global no
núcleo visual do host, com estados deslogado e logado, persistente em todas as
telas. No estado logado, ele apresenta identidade e perfil obtidos da resposta
de autenticação do Keycloak, sem expor o token ao Android.

Neste repositório, o núcleo executável é `:app`; não existe módulo
`:plugin-core`. "Core" será implementado como shell visual permanente do host,
sem criar um novo APK dinâmico.

## Escopo

- Criar o cabeçalho global no `:app` com as duas variantes dos SVGs.
- Manter o cabeçalho logado no menu e em todas as telas de plugins hospedadas.
- Ampliar `autenticadorsso` para extrair claims do access token recebido e
  devolver somente um perfil sanitizado junto da sessão opaca.
- Propagar o perfil pelo `:plugin-login` e `:shared-api`, sem propagar JWT,
  refresh token, senha ou claims não utilizados.
- Mapear roles reconhecidas para os labels `OPERADOR`, `SUPERVISOR` e
  `PROPRIETARIO`.
- Remover do plugin de login o cabeçalho duplicado, preservando seu fluxo.
- Criar testes backend, Android JVM e Compose para contrato, roles e transições.

## Fora de escopo

- Criar `:plugin-core` ou alterar a descoberta de APKs.
- Expor, persistir ou decodificar JWT no Android.
- Autorizar funcionalidades de negócio a partir do label do cabeçalho.
- Inventar valor de terminal ou unidade a partir de claims que não existem.
- Implementar rodapé ou conteúdo central dos templates.
- Incorporar os SVGs ou o export do realm ao APK.

## Impactos e riscos

- A resposta atual da API possui apenas sessão opaca e expiração; backend,
  plugin de login e API compartilhada terão alteração coordenada de contrato.
- A API compartilhada é binária: esta Change eleva a versão vigente de 1.1.0
  para 1.2.0. O plugin `startup-auth` deve exigir exatamente a minor 2. A regra
  `requiredSharedApiMinor <= hostMinor` permite que plugins `business-menu` com
  minor requerida 1 continuem funcionando com o host 1.2, pois eles não
  publicam o novo evento de perfil.
- O export do realm contém material sensível em texto claro. Antes de qualquer
  uso fora do ambiente local, credenciais e secrets devem ser rotacionados e o
  artefato deve permanecer fora do APK e das evidências.
- Um usuário pode receber mais de uma role; a precedência deve ser explícita.
- Esta Change aceita o risco existente de confiar na resposta obtida pelo
  backend no endpoint de token do Keycloak e não adiciona validação
  criptográfica local do JWT.
- A Change 10006 implementou o cabeçalho dentro do layout de `:plugin-login`.
  Esta Change remove essa instância local e centraliza o cabeçalho no shell do
  host. Os demais elementos de layout da Change 10006 são preservados.

## Critérios para aprovação da SPEC

- Propriedade do componente e interpretação de "core" estão inequívocas.
- Claims, mapeamento e precedência de roles estão definidos.
- O JWT permanece restrito ao backend e o DTO público é minimizado.
- Campos sem fonte aprovada foram removidos; `TERMINAL` permanece apenas como
  label visual, sem valor inventado.
- Compatibilidade da `:shared-api` e testes ponta a ponta estão especificados.
- Relação sucessora com Change 10006 (cabeçalho) está declarada.

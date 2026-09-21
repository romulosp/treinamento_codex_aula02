# Proposta: microkernel observando pasta dinâmica

## Status

`SPEC_APROVADA`

## Contexto

A Change 10000 aprovou descoberta por `FileObserver`, mas a implementação
inicial da Change 10001 ainda incorpora o APK `plugin-login` aos assets do host
durante o build. Isso mantém o host funcional, porém não demonstra instalação
ou descoberta dinâmica de plugins.

## Objetivo

Substituir o asset embutido por uma área de entrada observada em runtime. O host
deve iniciar sem conhecer o APK concreto, detectar um arquivo depositado na
pasta dinâmica, validá-lo em armazenamento privado e ativar a capacidade
`startup-auth` sem reinstalar ou recompilar o host.

## Escopo

- Criar pasta app-specific `files/plugins/inbox` no armazenamento externo do app.
- Fazer varredura no início e observação contínua com `FileObserver`.
- Aplicar debounce e processar descoberta fora da thread principal.
- Copiar candidatos para quarentena privada antes de validar ou carregar.
- Validar caminho, extensão, tamanho, SHA-256, assinatura, pacote, manifesto,
  API compartilhada, entry class e capacidade `startup-auth`.
- Promover somente APK validado e somente leitura ao repositório privado.
- Registrar os estados `DISCOVERED`, `STAGED`, `VERIFIED`, `LOADED`,
  `ATTACHED`, `ACTIVE`, `REJECTED`, `ERROR` e `PENDING_RESTART`.
- Carregar no boot a revisão mais recente do repositório privado verificado.
- Executar recuperação cooperativa quando callback, lifecycle ou UI falhar.
- Atualizar o host quando o primeiro plugin válido for ativado.
- Manter plugin ativo estável e marcar revisão posterior como
  `PENDING_RESTART`.
- Atualizar o script de geração para compilar host/plugin separadamente,
  instalar o host e depositar o plugin na pasta observada.

## Fora de escopo

- Plugins de terceiros, download por rede, loja ou console administrativo.
- Hot swap ou descarregamento de classes de plugin já ativo.
- Alteração visual ou funcional da tela de login.
- Persistência de credenciais, backend ou autenticação real.

## Dependências

- Change `10000-arquitetura-microkernel-plugins-apk`.
- Change `10001-plugin-login-autenticacao`.
- Diagramas `diagrama-sequencia-arquitetura.jfif` e
  `Gemini_Generated_Image_q0hwlaq0hwlaq0hw.jfif`, tratados como referências de
  requisitos fornecidas pelo usuário.

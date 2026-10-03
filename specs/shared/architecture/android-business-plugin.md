# Padrão obrigatório para plugins de negócio Android

## Finalidade

Esta especificação compartilhada define a estrutura mínima de todos os APKs que implementam `IPluginNegocioApp`. Uma Change de plugin PODE acrescentar requisitos, mas NÃO PODE enfraquecer estas regras sem ADR e nova revisão da SPEC.

## Topologia modular

- Cada plugin DEVE ser um módulo Android application independente `:plugin-<nome>`.
- O plugin DEVE usar `compileOnly(project(":shared-api"))` em produção.
- O plugin NÃO PODE depender de `:app` nem de outro plugin concreto.
- Testes PODEM usar `testImplementation(project(":shared-api"))`.
- O host NÃO PODE ter dependência `implementation` no plugin.

## Contrato público

O ponto de entrada DEVE implementar `IPluginNegocioApp` e publicar:

- `manifest` compatível com a versão vigente de `SharedApi`;
- exatamente um item-folha em `businessMenuItems`;
- `getCaminhoMenu()` iniciado por `Principal`;
- `createBusinessScreen(Context)` retornando a View raiz do plugin;
- lifecycle cooperativo `onLoad`, `onAttach`, `onActivate` e `onDetach`.

Posição do menu vem exclusivamente de `getCaminhoMenu()`. O arquivo `META-INF/services/<FQCN-IPluginNegocioApp>` DEVE declarar a entry class sem varredura reflexiva.

## Compatibilidade da SharedApi

- A versão vigente após a Change `10007-cabecalho-global-autenticacao` é
  `1.2.0`.
- O host DEVE rejeitar major diferente da sua própria major.
- Para a capacidade `business-menu`, o manifesto é compatível quando declara a
  mesma major e `requiredSharedApiMinor` menor ou igual à minor do host. Assim,
  plugins de negócio compilados para 1.1 continuam elegíveis no host 1.2.
- Para a capacidade `startup-auth`, o manifesto DEVE declarar exatamente a
  minor vigente, pois o evento de sessão da 1.2 inclui o perfil autenticado.
- Um plugin só pode declarar uma minor anterior quando não usa contratos
  introduzidos por minor posterior. A compatibilidade deve ser coberta por teste
  de manifesto e carregamento.

## Manifesto do plugin

`assets/plugin-manifest.json` DEVE conter `schemaVersion`, `pluginId`, `displayName`, `pluginVersion`, versão mínima da API, `entryClass`, `declaredPackageName`, `priority`, capacidade `business-menu` e `dependencies`. Identidades usam domínio reverso e devem ser únicas.

## UI e execução

- A UI DEVE ser Compose hospedada por `ComposeView` em `createBusinessScreen`.
- A rota Compose de conteúdo DEVE receber `Modifier` e ser renderizada pelo host via `createBusinessScreen`.
- Nenhuma variante do plugin DEVE declarar Activity launcher.
- O teste humano DEVE executar `:app`, concluir o login, selecionar o item do menu dinâmico e validar a tela hospedada pelo microkernel.
- O retorno ao menu DEVE usar `IPluginRouter`; o plugin não inicia Activity nem conhece classes do host.

## Estado, segurança e privacidade

- O estado pertence ao menor owner capaz de coordenar o fluxo e não pode ser duplicado no host.
- Senhas, tokens, PAN completo e credenciais NÃO PODEM sair da UI/plugin, aparecer em rota, log, manifesto ou item de menu.
- Campos de senha DEVEM usar transformação visual e semântica apropriadas e ser limpos ao concluir, cancelar ou sair do fluxo.
- O plugin NÃO PODE simular autorização real quando não existe contrato de sessão/roles; nesse caso a UI deve ser explicitamente demonstrativa.
- Trabalho bloqueante DEVE ocorrer fora da main thread e ser cancelável conforme o lifecycle.

## Testes e quality gates

Cada plugin DEVE ter, no mínimo:

1. teste JVM de identidade, caminho e item de menu;
2. teste do estado/reducer quando houver fluxo com mais de uma etapa;
3. teste Compose dos ramos e callbacks observáveis;
4. verificação de que o APK contém manifesto e descritor de serviço;
5. `test`, `lintDebug`, `assembleDebug` e compilação dos testes instrumentados;
6. instalação do host, login, descoberta, clique no menu e abertura do plugin em emulador/dispositivo;
7. inventário KDoc e evidências reproduzíveis em `validation.md`.

O artefato é validado, assinado e carregado pelo host com `DexClassLoader`; execução direta do módulo de plugin é proibida.

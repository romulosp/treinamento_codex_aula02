# ADR-001: componente mediado pelo host

Status: aceito na revisão da SPEC de 2026-10-30.

Complemento r3: o teste de atualização detectou duas revisões legítimas do mesmo componente no repositório. A revisão recém-verificada no staging é selecionada e seu digest fica em arquivo privado atômico; boots seguintes revalidam a seleção. Isso preserva histórico sem ambiguidade e sem escolher por data do arquivo. Múltiplas revisões concorrentes no mesmo staging continuam sendo rejeitadas.

O saque sai do contrato IPluginNegocioApp e mantém telas e integração. Usar contrato aditivo na shared-api e mediação do host evita dependência concreta entre plugins e preserva a verificação dos APKs. O consumidor demonstrativo existente evita novo módulo sem necessidade.

Alternativas: chamada direta entre classloaders aumentaria acoplamento; uma Activity exportada criaria superfície IPC e navegação diferente. A decisão usa View hospedada no processo atual, com cancelamento cooperativo e retorno após cleanup.

A SharedApi 1.3 mantém o evento de autenticação 1.2 sem mudanças. Por isso a compatibilidade startup-auth passa a aceitar minors a partir de 1.2, até a vigente, preservando o plugin de login existente. A mudança do saque deixa de se enquadrar no padrão de plugins de negócio; esse padrão continua obrigatório para o consumidor.

Fontes consultadas em 2026-10-30: arquitetura e loader locais; catálogo https://developer.android.com/tools/agents/android-skills/browse. Candidata Use Android CLI: REJECT para incorporação nesta Change, pois o ambiente já possui Wrapper/ADB e a tarefa é contrato interno de microkernel; nenhuma skill externa foi instalada. A consulta à página de riscos de carga dinâmica expirou; segurança é sustentada pelo pipeline local existente e testado, sem nova alegação normativa externa.

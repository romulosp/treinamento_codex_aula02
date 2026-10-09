# Plano de implementação — 10010

**Entrada:** proposal.md e spec.md em SPEC_APROVADA, confirmados em 2026-10-03.

## Estratégia e impactos

- MainActivity seleciona uma variante visual opcional de CoreShell somente quando apresenta o menu autenticado; login e telas dos plugins continuam com o header anterior.
- Extrair apresentação do menu para BusinessMenuScreen.kt. Receber estado imutável e callbacks existentes; não alterar sessão, plugins ou SharedApi.
- MenuDesignTokens centraliza cores e medidas; renderização recursiva mantém ordenação por ordem/id e agrupa apenas folhas consecutivas em linhas. Um único scroll ocupa a região acima do footer; saída fica sobreposta a 24 dp do footer.
- Header do menu aplica os breakpoints 1200/1024 dp, borda tracejada e ellipsis com texto integral na semântica do próprio Text. Em largura compacta, brand/versão e informações ocupam regiões distintas sem colisão.
- Botões nativos observam InteractionSource para foco/hover/press. Escala de pressão 0,98 e transição de cores/sombra de 150 ms e escala de 100 ms respeitam ANIMATOR_DURATION_SCALE; observação limitada à composição do menu e removida ao sair. Escala zero remove a transformação.

## Testes e qualidade

Atualizar testes Compose de saída para SAIR; acrescentar logout desabilitado, fatal, agrupadores/ordenação, teclado, geometria responsiva e interações. Preservar testes de header antigo. Descobrir tarefas reais; executar validador estrutural, assembleDebug, testDebugUnitTest, lintDebug, compilação e execução instrumentada quando dispositivo disponível. Registrar saídas, APK, inventário KDoc e limitações de cobertura/matriz visual em validation.md. Não confundir verificações técnicas com validação formal.

## Segurança, riscos e decisões

Sem nova dependência, API, fonte de dados ou log. Preservar alterações preexistentes em plugin-login e Change 10008. SDK/package/módulos conforme build atual: app br.com.romulopenha.sistemaprototipoandroid, min 29, target 36, compile 37, Java 17. Valores do mockup não serão dados de produção. O texto '240 é 64' na consolidação é interpretado como dimensões 240 × 64, comprovadas na origem.

Skills locais lidas: android-native-engineering, spec-implement e Compose component-design, animations, focus-navigation, state-and-effects, ui-testing-patterns (incluindo referências acionadas de layout, estado e efeitos). Catálogo oficial consultado em https://developer.android.com/tools/agents/android-skills/browse: candidata Make app adaptive classificada REJECT para incorporação nesta Change por sobreposição com skills locais e vedação explícita da proposta. Settings.Global.ANIMATOR_DURATION_SCALE confirmado em https://developer.android.com/reference/android/provider/Settings.Global#ANIMATOR_DURATION_SCALE. Classificação: REQUIRED para tokens/contratos; CONTEXTUAL para seleção das APIs locais. Nenhuma versão será atualizada.

## Limite da entrega

Encerrar em IMPLEMENTADA para teste humano; não executar revisão da implementação, validação formal, aprovação, atualização de system, archive ou commit.

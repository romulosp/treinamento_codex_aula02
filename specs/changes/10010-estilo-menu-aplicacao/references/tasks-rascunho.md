# Tarefas: 10010-estilo-menu-aplicacao

## Pré-condições

- [ ] `proposal.md` e `spec.md` com status `SPEC_APROVADA`.
- [ ] `DESIGN.md` revisado e sem pendências abertas.
- [ ] Branch de trabalho criada a partir do HEAD atual.

---

## Implementação

### T-01 — Criar `MenuDesignTokens.kt`
- [ ] Criar `ui/MenuDesignTokens.kt` no pacote `br.com.romulopenha.sistemaprototipoandroid.ui`.
- [ ] Declarar `object MenuDesignTokens` com:
  - Cores: `Primary`, `PrimaryHover`, `PrimaryActive`, `Danger`, `DangerHover`, `DangerActive`, `Surface`, `InfoBg`, `InfoBorder`, `Border`, `Text`, `TextMuted`, `Focus`, `DisabledBg`, `DisabledText`.
  - Dimensões: `HeaderHeight`, `FooterHeight`, `CardWidth`, `CardHeight`, `ExitButtonHeight`, `ExitButtonMinWidth`, `InfoBoxWidthWide`, `InfoBoxWidthCompact`, `ContentSidePadding`, `ContentSidePaddingCompact`, `ContentBottomSafeArea`, `GapCards`.
  - Limiares: `BreakpointWide`, `BreakpointCompact`.
- [ ] Adicionar KDoc ao objeto e a cada constante.

### T-02 — Atualizar `AuthenticationHeader.kt`
- [ ] Substituir literais de cor (`Color(0xFF005CA9)` etc.) pelos tokens de `MenuDesignTokens`.
- [ ] Ajustar altura do header logado para `MenuDesignTokens.HeaderHeight` (já em 120 dp; confirmar).
- [ ] Ampliar condicionais de `BoxWithConstraints`:
  - `maxWidth >= MenuDesignTokens.BreakpointWide` → info box 380 dp + título visível.
  - `maxWidth in MenuDesignTokens.BreakpointCompact..MenuDesignTokens.BreakpointWide` → info box 320 dp + título oculto.
  - `maxWidth < MenuDesignTokens.BreakpointCompact` → info box `min(320.dp, 0.45f * maxWidth)` + título oculto.
- [ ] Adicionar borda tracejada na info box (atualmente é sólida).
- [ ] Adicionar `title` / `contentDescription` nos textos truncados onde ausentes.
- [ ] Atualizar KDoc de `AuthenticationHeader`, `CoreShell` e `AuthenticationHeaderState`.

### T-03 — Criar sub-composables privados em `MainActivity.kt`
- [ ] `MenuDesignTokens` importado.
- [ ] `MenuSectionTitle`: `Text` centralizado, 18 sp, peso 600, cor `Primary`, margem inferior 32 dp.
- [ ] `MenuCard`: `Button` 240 × 64 dp, cor `Primary`, texto branco 16 sp peso 700, borda 8 dp, sombra sutil, animação de pressed (supprimível).
- [ ] `MenuGrid`: `FlowRow` (ou `LazyVerticalGrid`) com itens de 240 dp e gap 16 dp, centralizado, `maxWidth 1200 dp`.
- [ ] `MenuFooter`: `Spacer`/`Box` 48 dp, fundo branco, borda superior 1 dp.
- [ ] `MenuExitButton`: `Button` pill 48 dp altura, min-width 140 dp, cor `Danger`, texto branco 16 sp.
- [ ] KDoc em todos os composables criados.

### T-04 — Reestruturar `BusinessMenuScreen`
- [ ] Substituir `Column` simples por `Scaffold` com `bottomBar = { MenuFooter() }` e `floatingActionButton = { MenuExitButton(...) }`.
- [ ] Substituir `LazyColumn` (ou `Column`) de conteúdo por: `MenuSectionTitle` + `MenuGrid`.
- [ ] Garantir `contentPadding` do `LazyColumn` com `bottom = ContentBottomSafeArea`.
- [ ] Preservar `AlertDialog` de erro fatal inalterado.
- [ ] Preservar todos os parâmetros e handlers existentes da função.
- [ ] Atualizar KDoc de `BusinessMenuScreen`.

### T-05 — Atualizar `BusinessMenuEntries`
- [ ] Substituir `Button` padrão por `MenuCard` para itens folha.
- [ ] Preservar `Text` para agrupadores (nível > 0).
- [ ] Preservar `sortedWith(compareBy(BusinessMenuItem::ordem, BusinessMenuItem::id))`.
- [ ] Preservar recorrência para `item.filhos`.
- [ ] Atualizar KDoc de `BusinessMenuEntries`.

### T-06 — Suporte a animação reduzida
- [ ] Ler `Settings.Global.TRANSITION_ANIMATION_SCALE` no composable ou via `LocalContext`.
- [ ] Suprimir animação de `scale` em `MenuCard` quando `animationScale == 0f`.

### T-07 — Build e lint
- [ ] Executar `./gradlew :app:assembleDebug` — exit code 0.
- [ ] Executar `./gradlew :app:lintDebug` — sem novos warnings relevantes.

### T-08 — Testes
- [ ] Executar testes unitários existentes: `./gradlew :app:testDebugUnitTest` — exit code 0.
- [ ] Executar testes instrumentados existentes (se ambiente disponível).
- [ ] Criar testes de composable para `BusinessMenuScreen`:
  - card renderiza com `item.titulo`;
  - clique invoca `onItemSelected` com o id correto;
  - botão sair invoca `onLogout`;
  - botão sair desabilitado quando `logoutInProgress = true`.
- [ ] Criar testes de composable para `AuthenticationHeader`:
  - info box exibe `displayRole + username + displayName`;
  - textos longos apresentam ellipsis (maxLines = 1 verificado via semantics).

---

## Revisão e validação

- [ ] Revisar implementação contra `spec.md` (todos os AC-01 a AC-21).
- [ ] Revisar implementação contra `DESIGN.md` (hierarquia de composables e decisões).
- [ ] Validar ausência de scroll horizontal em 1024 × 768, 1366 × 768, 1920 × 1080 (emulador ou device).
- [ ] Validar que clicar em card e sair continuam funcionando corretamente.
- [ ] Registrar evidências em `validation.md`.

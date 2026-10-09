# SPEC — CHANGE 10010 — Estilo do Menu da Aplicação

**Versão:** 2.1 — contrato Android consolidado
**Status:** SPEC_APROVADA
**Data:** 2026-10-03

## Fontes e precedência
A versão 2.0 fornecida está integralmente em `references/spec-origem-v2.md`. Seus tokens, estados e critérios AC-01 a AC-27 são normativos, com as adaptações Android explícitas abaixo. Precedência: comportamento existente - este contrato Android - SPEC de origem - SVG. Os rascunhos em `references/*-rascunho.md` são históricos, não normativos.

O SVG representa a aparência. Prevalecem cards 240 × 64, saída de altura 48, rodapé 48, grade centralizada e saída a 24 da borda direita e 72 da base útil. As coordenadas do SVG e seus valores 60/50/44 não são contratos de posicionamento.

## Plataforma e unidades
Preservar Kotlin, Jetpack Compose Material 3, módulos, SDKs e versões atuais. Medidas de layout em px da origem correspondem numericamente a dp; fontes correspondem a sp. Breakpoints medidos pela largura útil da janela em dp, nunca por pixels físicos. Não converter 1200/1024 para 1000/860. Insets do sistema não fazem parte da área útil; não aplicar duas vezes.

## Contratos preservados
Manter eventos e dados existentes: `onItemSelected(item.id)`, `onLogout`, `onFatalAcknowledged`, navegação de retorno e carregamento de plugins. Preservar ordenação por `ordem` e `id` em cada nível, hierarquia e rótulos dos agrupadores; agrupadores não viram ações. Preservar mensagem de menu vazio, diálogo fatal e saída desabilitada durante logout, com texto `Saindo...`. O texto ocioso pode ser apresentado como `SAIR`.

Perfil vem de `AuthenticatedProfile`, usando `displayRole()`, `username` e `displayName`. Preservar o rótulo atual `TERMINAL`, sem inventar identificador. Não adicionar LOTÉRICA sem fonte existente. A versão atualmente literal `v1.0.0.0` permanece até outra Change fornecer fonte real. Dados exemplificativos do SVG não entram em produção.

## Layout normativo
- Cabeçalho fixo de 120 dp no menu, branco, divisória inferior de 1 dp, padding lateral 24 dp. Brand 22 sp, título central 14 sp, informações 12 sp e versão 14 sp. Posicionamento vertical conforme contrato flex da origem, sem copiar coordenadas do SVG.
- Largura ≥1200 dp: título centralizado na janela, info box 380 dp, gap até a versão 24 dp; garantir ausência de colisão. Info box com altura mínima 76 dp, padding 12/16 dp, raio 4 dp e borda tracejada 1,5 dp.
- 1024–1199 dp: título oculto, info box 320 dp e padding lateral do conteúdo 48 dp.
- Abaixo de 1024 dp: título oculto, padding do header 16 dp, gap 8 dp, info box limitada a min(320 dp, 45% da largura útil), conteúdo com padding lateral 24 dp e saída a 16 dp da direita. Em janelas insuficientes, reorganizar informações sem sobreposição; cards podem reduzir largura até caber, mantendo alvo de toque e ações acessíveis.
- Fundo em gradiente #EBF4FA - #B3D4E6. Conteúdo inicia abaixo do cabeçalho; padding superior 60 dp, inferior de segurança 120 dp. Apenas o conteúdo central rola; header, saída e footer permanecem fixos.
- Título `SELECIONE A FUNCIONALIDADE DESEJADA`, 18 sp, peso 600, #005CA9, centralizado, espaço inferior 32 dp.
- Grade centralizada, máximo 1200 dp, cards 240 × 64 dp, gap 16 dp, raio 8 dp, padding horizontal 24 dp, texto branco 16 sp peso 700. Card único mantém largura nominal. Agrupadores ocupam linha própria e seus filhos seguem na sequência hierárquica, com indicação visual do nível sem aumentar largura do card.
- Saída pill: mínimo 140 dp de largura, altura 48 dp, direita 24 dp, base 24 dp acima do footer. Footer branco de 48 dp com divisória superior. Camadas equivalentes à origem (conteúdo < header/footer < saída); números CSS não precisam ser usados como elevação Android.

## Interação e acessibilidade
Usar botões Compose com semântica nativa. Tab percorre ações em ordem visual; Enter/Space ativam uma vez. Foco com contorno explícito #FFB300 de 3 dp e afastamento equivalente a 2 dp; ripple sozinho não satisfaz o critério. Dados longos têm uma linha, ellipsis e acesso ao valor completo por semântica sem anúncio duplicado.

Cores e tokens seguem integralmente a seção 8 da origem. Hover em ponteiro compatível usa #004A88 (cards) e #B71C1C (saída); pressionado usa #003D70/#8E0000 e escala 0,98 quando movimento permitido. Desabilitado usa #D5DCE2/#6B737A sem hover/press. Não criar estados funcionais novos.

Respeitar redução de movimento do sistema Android para as animações efetivamente utilizadas; duração zero remove também a transformação decorativa de escala. Não equiparar apenas TRANSITION_ANIMATION_SCALE a todas as animações Compose. Sem timers ou listeners globais de redimensionamento.

## Aceite e evidências
Aplicam-se AC-01 a AC-27 da origem nas unidades acima. AC-26 deve comprovar apenas fontes atualmente existentes; AC-04 usa o breakpoint de 1200 dp. Verificar adicionalmente estado vazio, agrupadores aninhados, ordenação, erro fatal e ausência de regressão no login e nas telas dos plugins.

Validar áreas úteis 1024×768, 1366×768 e 1920×1080 dp com densidade registrada, mais cenário de densidade diferente, janela compacta e fonte ampliada. Registrar resolução física, densidade, área útil, fontScale, insets e capturas. Dados normais/longos e muitos cards devem ser fixtures de teste, nunca constantes de produção.

Executar validador estrutural da skill base, Gradle Wrapper, build, testes JVM, lint, compilação e execução de testes instrumentados afetados. Descobrir tarefas reais antes da execução. Inventariar KDoc e cobertura (mínimo 80%, alvo 90% no código elegível, conforme gate base). KDoc para todas as declarações criadas/alteradas e contratos privados relevantes. Registrar comandos, ambiente, resultado, exit code e artefatos em validation.md. Falta de dispositivo não permite declarar validação visual concluída.

Não introduzir novos dados sensíveis, logs pessoais, HTML/WebView, bibliotecas ou alterações de arquitetura. A implementação requer leitura das sub-skills de componentes, animações, foco e testes Compose; estado/efeitos quando acionados. Encerramento exige revisão da implementação, validação, aprovação formal e atualização de specs/system conforme workflow.

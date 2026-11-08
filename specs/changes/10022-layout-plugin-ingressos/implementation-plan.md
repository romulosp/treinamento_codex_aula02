# Plano técnico preparatório

Entrada: proposal.md e spec.md em `SPEC_APROVADA` após revisão de 08/11/2026.

## Correção visual r4
Após rejeição do primeiro visual, seguir a imagem do usuário e a revisão r4. Reescrever somente CinemaScreen: carrossel horizontal largo com LazyRow e setas, cadastro em diálogo, horários/mapa à esquerda, resumo branco à direita. Desenhar poltronas em Canvas com controles nativos focáveis. Galeria apenas preenche rascunho; pedido/venda bloqueiam substituição. Preservar CinemaJourney, rota, pagamentos e assets existentes. Atualizar testes para abrir cadastro, provar atalho sem cadastro automático, verificar poltronas e capturar composição ampla/compacta antes da reinstalação do host.

1. Preservar `CinemaJourney.kt`, contratos compartilhados, lifecycle e callback de pagamento. Registrar hashes baseline desses arquivos/áreas para comparação.
2. Mover exclusivamente apresentação de `PluginIngressosApp.kt` para `CinemaScreen.kt`. Manter assinatura de `CinemaScreen` e formatter consumidos pelos testes; adicionar Modifier opcional na rota sem alterar chamadores existentes.
3. Construir cartaz local, apresentação de sessão, editor recolhível, mapa com semântica de seleção e resumo. Usar somente dependências já declaradas. Layout amplo e compacto recebem os mesmos callbacks.
4. Preservar teste de ponta a ponta sintético do plugin e acrescentar testes de estados de assento, teclado e dimensões compacta/ampla. Capturar imagens determinísticas; sem API de cartão real.
5. Executar validador estrutural antes dos gates. Rodar Wrapper com JBR local, JVM, cobertura, lint, APK/test APK e instrumentação do plugin. Compilar host para conter APK atualizado. Inspecionar imagens e registrar resultados exatos, incluindo gates preexistentes reprovados.
6. Entregar `IMPLEMENTADA` para teste humano, sem arquivar, commitar, aprovar implementação ou declarar validação formal.

Riscos: rolagem em altura reduzida, textos longos, teste ambíguo por conteúdo repetido e dependências de build locais. Mitigar com estados determinísticos, semântica de assentos, rolagem explícita e testes instrumentados no emulador disponível. Sem novas permissões, logs, rede, dados pessoais ou acionamento automático de hardware.

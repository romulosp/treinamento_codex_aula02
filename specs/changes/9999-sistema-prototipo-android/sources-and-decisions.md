# Fontes e decisões

**Autor:** Rômulo Penha

## Regras de evidência

- Consulta realizada em 2026-09-19.
- Fontes Android/Google oficiais têm prioridade.
- Links externos abaixo são citações de autoridade temporal, não dependências de arquivo ou build.
- As entradas textuais estão em `source-material/`; os demais materiais identificados no inventário ainda aguardam incorporação e conferência antes do uso na implementação.

## Decisões Android

### Política permanente de versionamento

- Regra: em novos projetos ou atualizações relevantes, usar o stack stable mais recente oficialmente compatível e aprovado na data da decisão.
- Limitação: as versões desta Change não se tornam regra permanente da skill ou do harness.
- Estabilidade: stable é permitido; RC ou beta exige necessidade e aprovação explícita; alpha, preview ou experimental exige classificação `EXPERIMENTAL`, ADR, risco, validação adicional e aprovação.
- Fonte de verdade desta Change: [compatibility-matrix.md](compatibility-matrix.md).

### Toolchain aprovado

- Fonte: [notas oficiais do AGP 9.4.0](https://developer.android.com/build/releases/agp-9-4-0-release-notes), consultadas em 2026-09-19.
- Decisão: AGP 9.4.0, Gradle 9.6.0, JDK 17 e KGP 2.2.10.
- Evidência: o AGP 9.4.0 suporta API 37 e publica essas versões na tabela de compatibilidade.
- Classificação: `REQUIRED` nesta Change.

### SDK alvo

- Fonte: [requisito de API alvo da Google Play](https://developer.android.com/google/play/requirements/target-sdk).
- Fato temporal: desde 2026-08-31, novos aplicativos e atualizações para telefones devem mirar Android 16, API 36, ou superior.
- Decisão: `compileSdk = 37` e `targetSdk = 36`; são decisões independentes.
- Classificação: requisito da Google Play para publicação pública e baseline técnico `REQUIRED` por decisão desta Change interna.
- Limitação: a escolha do alvo não autoriza publicação; o canal permanece exclusivamente interno.

### SDK mínimo

- Problema: não foi fornecida matriz corporativa de aparelhos.
- Alternativas avaliadas: API 23, 26 ou uma versão mais recente.
- Decisão: API 26, correspondente a Android 8.0, para equilibrar compatibilidade e custo de validação do protótipo.
- Classificação: `CONTEXTUAL` e `REQUIRED` nesta Change.
- Validação: emulador API 26 e emulador API 36, além das janelas definidas nos critérios de aceite.

### Compose e BOM

- Fontes: [BOM do Jetpack Compose](https://developer.android.com/develop/ui/compose/bom), [configuração do Compose](https://developer.android.com/develop/ui/compose/setup-compose-dependencies-and-compiler) e POM oficial do BOM no Google Maven.
- Fato temporal: o BOM mais recente `2026.09.00` resolve Compose 1.12.1, e a documentação oficial exige `compileSdk = 37` a partir do Compose 1.12.0.
- Evidência de compatibilidade: a documentação oficial do Compose indica BOM `2026.09.00`, Compose 1.12.x, `compileSdk = 37` e AGP 9; as notas oficiais do AGP 9.4.0 suportam API 37 e registram Gradle 9.6.0, JDK 17 e KGP 2.2.10.
- Decisão: usar BOM estável `2026.09.00` com AGP 9.4.0, Gradle 9.6.0, JDK 17 e KGP 2.2.10.
- Classificação: `REQUIRED` nesta Change.
- Limitação: qualquer atualização posterior exige nova verificação conjunta e registro em `compatibility-matrix.md`.

### Distribuição

- Decisão: uso interno para desenvolvimento, demonstração e validação.
- Classificação: `REQUIRED` nesta Change.
- Consequência: Google Play, lojas e distribuição pública estão fora de escopo; `targetSdk = 36` é mantido como baseline técnico atual, não como declaração de publicação.
- Validação: não criar configuração de assinatura de produção nem tarefas de publicação.

### Arquitetura

- Fontes: [recomendações de arquitetura](https://developer.android.com/topic/architecture/recommendations) e [camada de UI](https://developer.android.com/topic/architecture/ui-layer).
- Decisão: single-activity, Compose, estado imutável, fluxo unidirecional, ViewModel na tela e componentes sem ViewModel próprio.
- Classificação: `RECOMMENDED`.
- Adaptação local: perfil `SIMPLE`, sem repository porque não há fonte de dados externa ou persistente neste escopo.

### Adaptabilidade

- Fonte: [suporte a tamanhos de tela](https://developer.android.com/develop/adaptive-apps/guides/support-different-display-sizes).
- Decisão: executar somente em paisagem, reagir ao espaço real da janela, preservar estado, respeitar insets e não fixar resolução, densidade ou proporção.
- Classificação: `RECOMMENDED`.
- Escopo interpretado: celulares, tablets, dobráveis e multiwindow. Wear OS, TV, Auto/Automotive e XR exigem Changes próprias.

### Acessibilidade

- Fonte: [padrões acessíveis nas APIs Compose](https://developer.android.com/develop/ui/compose/accessibility/api-defaults).
- Fato: controles interativos devem possuir alvo mínimo de 48 dp.
- Decisão: requisito RNF-004 e testes semânticos.
- Classificação: `REQUIRED` nesta Change.

### Testes de UI

- Fonte: [semântica em testes Compose](https://developer.android.com/develop/ui/compose/testing/semantics).
- Decisão: priorizar seletores semânticos; usar tags somente quando a semântica não identificar o elemento de forma estável.
- Classificação: `RECOMMENDED`.

## Candidatas oficiais avaliadas

Repositório oficial consultado no commit `b1f707d90904129b5972b3cc6436b568583effe5`, licença Apache-2.0.

### SKILL-CANDIDATE-001 — Interface adaptativa

- Classificação: `REJECT` para incorporação nesta Change.
- Origem: catálogo oficial Android, caminho `jetpack-compose/adaptive`.
- Mantenedor: Google LLC.
- Versão/commit analisado: commit acima; atualização declarada em 2026-08-27.
- Problema que resolve: adaptação avançada a múltiplos dispositivos.
- Sobreposição: a skill local já define adaptabilidade e o escopo possui somente duas telas simples.
- Aderência arquitetural: parcial; a candidata pressupõe Navigation 3 e inclui APIs experimentais que não são necessárias.
- Aderência ao processo: não substitui os gates locais.
- Impacto no contexto: alto para o ganho deste protótipo.
- Riscos: adicionar navegação e APIs experimentais sem necessidade.
- Evidência: catálogo e arquivo de skill oficiais consultados.
- Decisão: aplicar diretamente as orientações estáveis da documentação; não instalar a candidata.

### SKILL-CANDIDATE-002 — Estilos Compose

- Classificação: `REJECT`.
- Origem: catálogo oficial Android, caminho `jetpack-compose/theming/styles`.
- Mantenedor: Google LLC.
- Versão/commit analisado: commit acima; atualização declarada em 2026-09-08.
- Problema que resolve: estilo tipado de componentes personalizados.
- Sobreposição: coincide com o catálogo visual proposto.
- Aderência arquitetural: conceitualmente boa.
- Aderência ao processo: neutra.
- Impacto no contexto: médio.
- Riscos: a candidata é experimental, requer Compose alfa e `compileSdk` 37 ou superior.
- Evidência: limitações e pré-requisitos declarados na skill oficial.
- Decisão: usar data classes Kotlin e APIs Compose estáveis.

### SKILL-CANDIDATE-003 — Configuração de testes

- Classificação: `REJECT` para incorporação automática.
- Origem: catálogo oficial Android, caminho `testing/testing-setup`.
- Mantenedor: Google LLC.
- Versão/commit analisado: commit acima; atualização declarada em 2026-09-03.
- Problema que resolve: criação ampla de infraestrutura de testes.
- Sobreposição: a skill local já exige quality gates e cobertura.
- Aderência arquitetural: parcial; recomenda instalar Hilt quando não há DI, contrariando a decisão `SIMPLE` desta Change.
- Aderência ao processo: não conhece o workflow local.
- Impacto no contexto: alto.
- Riscos: dependências e frameworks desnecessários.
- Evidência: arquivo de skill oficial consultado.
- Decisão: adotar somente conceitos compatíveis registrados na SPEC, sem instalar a candidata.

## Decisões sobre o material incorporado

### Direitos de uso

- Estado: `DECIDED`.
- Regra: estar presente em `source-material/` permite análise e versionamento da Change, mas não comprova direito de distribuição no APK.
- Decisão: nenhum binário legado de imagem ou fonte será incorporado; a UI usará Compose, ícones neutros e tipografia do sistema.
- Gate: a revisão e a validação devem falhar se qualquer hash do material legado aparecer em `app/src/main/res` ou no APK.

### Recursos ausentes

- Estado: `DECIDED`.
- Evidência: 118 dos 234 caminhos únicos declarados não possuem arquivo correspondente.
- Decisão: cada definição afetada está marcada `SUBSTITUIR` em `migration-matrix.md` e deve usar representação neutra em Compose ou recurso do sistema.
- Limitação: a substituição preserva função, estado, hierarquia e intenção cromática; não promete equivalência de marca ou pixel a pixel.

### Nomenclatura

- Estado: decidido.
- Decisão: componentes, pacotes, documentos e recursos de destino recebem nomes semânticos novos e neutros. Identificadores anteriores permanecem somente no material de origem incorporado e na matriz técnica necessária à rastreabilidade.

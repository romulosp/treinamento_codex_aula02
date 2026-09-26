# Tarefas: 9999-sistema-prototipo-android

**Autor:** Rômulo Penha

## Status

`SPEC_APROVADA — IMPLEMENTACAO_EM_ANDAMENTO`

## Atualização prompt2 — compatibilidade resolvida

- [x] Confirmar `minSdk = 26` pela análise de suporte e custo de testes.
- [x] Validar `compileSdk = 37` com AGP 9.4.0, Gradle 9.6.0, KGP 2.2.10 e JDK 17.
- [x] Obter `PASS` na Compatibility Review do stack completo.

## Pré-condições

- [x] Revisar `proposal.md`, `spec.md`, `DESIGN.md` e todas as especificações complementares.
- [x] Definir `minSdk = 26` e distribuição interna.
- [x] Proibir incorporação de binários legados e definir substituições neutras por Compose/sistema.
- [x] Marcar definições dependentes das 118 referências ausentes como `SUBSTITUIR` na matriz completa.
- [x] Alterar `proposal.md` e `spec.md` para `SPEC_APROVADA` somente após relatório formal sem pendência material.
- [x] Criar `implementation-plan.md` completo após `SPEC_APROVADA`.

## Implementação

### Fundação

- [x] Criar o projeto no caminho e com a identidade definidos em RF-001.
- [x] Configurar Java 17, Kotlin, Gradle Wrapper, catálogo de versões e Compose estável.
- [x] Ler `compose-component-design` antes de criar ou estilizar componentes Compose.
- [x] Ler `compose-state-and-effects` antes de implementar estado, ViewModel ou efeitos.
- [x] Ler `compose-focus-navigation` antes de implementar foco, teclado, acessibilidade ou navegação.
- [x] Ler `compose-animations` antes de implementar transições, gestos ou motion previstos pela SPEC.
- [x] Ler `compose-ui-testing-patterns` antes dos testes Compose e instrumentados.
- [x] Executar o validador estrutural; resultado `0`.
- [x] Disponibilizar Android SDK API 26/API 37 e Build Tools 36.0.0 para desbloquear o build.

### Inventário e recursos

- [ ] Incorporar código de referência, configurações, imagens, fontes e imagem de referência em `source-material/`; atualmente somente as entradas textuais estão presentes.
- [x] Gerar matriz completa das 212 definições visuais.
- [ ] Inventariar os 350 nós de menu sem importar regras de negócio.
- [ ] Calcular hashes, remover cópias e registrar renomeações Android.
- [x] Resolver cada recurso ausente por substituição neutra rastreada.
- [x] Registrar a decisão de não copiar ativos sem licença ou autoridade comprovada.

### Tema e componentes

- [x] Implementar tokens visuais Kotlin da tela-catálogo.
- [x] Implementar botões e ações, incluindo variações primária, secundária, confirmar, cancelar, menu, auxiliar, dupla, quadrada, incremento, ícone, selecionada e desabilitada.
- [x] Implementar textos, painéis, indicador textual de estado e ilustração neutra em Compose.
- [x] Implementar campos simples, alfabético, alfanumérico, numérico, monetário, data, máscara, erro e área multilinha.
- [x] Implementar campo de senha mascarado, somente em memória e sem logs.
- [x] Implementar seleção, alternância, grupos exclusivo/múltiplo e lista suspensa.
- [x] Implementar tabela selecionável e conteúdo na rolagem nativa da tela.
- [x] Implementar diálogos, mensagens de sucesso/alerta/erro e progresso determinado/indeterminado.
- [x] Implementar teclados alfanumérico e numérico da primeira vertical slice.
- [x] Implementar teclado de calculadora local com operações básicas.
- [ ] Adicionar KDoc aplicável durante cada vertical slice.

### Telas

- [x] Implementar a tela operacional sem galeria técnica visível, conforme referência aprovada em 2026-09-20.
- [x] Implementar o primeiro enquadramento expandido semelhante à referência.
- [ ] Implementar reorganização compacta rolável.
- [ ] Implementar composição média e expandida orientada pela janela.
- [ ] Tratar insets, recortes, gestos e separações de dobráveis.
- [ ] Implementar menu demonstrativo e navegação de retorno.
- [ ] Preservar estado em rotação e redimensionamento.

### Testes e qualidade

- [x] Criar testes unitários para entrada virtual, Unicode e limpeza de credenciais.
- [x] Criar testes Compose da hierarquia inicial e callback de tecla.
- [ ] Criar testes de screenshot nos tamanhos definidos.
- [ ] Validar paisagem e redimensionamento nas sete dimensões mínimas.
- [ ] Validar escala de fonte 1,5, inspecionar escala 2,0, foco e alvos mínimos.
- [ ] Medir cobertura e justificar exclusões.
- [ ] Executar emuladores nos níveis de API mínimo e alvo.
- [x] Executar build, testes JVM, lint e validador estrutural pelo Gradle Wrapper.

## Revisão e validação

- [x] Registrar checkpoint parcial de implementação aprovado pelo solicitante.
- [ ] Executar revisão de implementação contra toda a SPEC.
- [ ] Executar revisão de segurança e privacidade.
- [ ] Registrar comandos, ambiente, resultados, códigos de saída e evidências em `validation.md`.
- [ ] Obter aprovação formal antes de atualizar `specs/system/` ou arquivar.

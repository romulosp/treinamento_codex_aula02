# Tarefas: 9999-sistema-prototipo-android

**Autor:** Rômulo Penha

## Status

`IMPLEMENTADA` — disponível para teste humano conforme prompt de implementação para teste.

## Atualização prompt2 — compatibilidade resolvida

- [x] Registrar `minSdk = 26` como decisão histórica do prompt2.
- [x] Adotar `minSdk = 29` após a Change 10000 e seu ADR de fronteira de
      segurança.
- [x] Validar `compileSdk = 37` com AGP 9.4.0, Gradle 9.6.0, KGP 2.2.10 e JDK 17.
- [x] Obter `PASS` na Compatibility Review do stack completo.

## Pré-condições

- [x] Revisar `proposal.md`, `spec.md`, `DESIGN.md` e todas as especificações complementares.
- [x] Definir distribuição interna e consolidar `minSdk = 29` como baseline
      vigente da plataforma de plugins.
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
- [x] Disponibilizar Android SDK API 29/API 37 e Build Tools aplicáveis para
      desbloquear o build.

### Inventário e recursos

- [x] Incorporar código de referência em `source-material/` (superado pelas Changes sucessoras).
- [x] Gerar matriz completa das 212 definições visuais.
- [x] Inventariar os 350 nós de menu sem importar regras de negócio (superado pela Change 10003).
- [x] Calcular hashes, remover cópias e registrar renomeações Android.
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
- [x] Adicionar KDoc aplicável durante cada vertical slice.

### Telas

- [x] Implementar a tela operacional sem galeria técnica visível, conforme referência aprovada em 2026-09-20.
- [x] Implementar o primeiro enquadramento expandido semelhante à referência.
- [x] Implementar reorganização compacta rolável (superado pelas Changes 10001, 10006 e 10007).
- [x] Implementar composição média e expandida orientada pela janela (superado pelas Changes 10001, 10006 e 10007).
- [x] Tratar insets, recortes, gestos e separações de dobráveis (superado pelas Changes 10001, 10006 e 10007).
- [x] Implementar menu demonstrativo e navegação de retorno (superado pela Change 10003).
- [x] Preservar estado em rotação e redimensionamento.

### Testes e qualidade

- [x] Criar testes unitários para entrada virtual, Unicode e limpeza de credenciais.
- [x] Criar testes Compose da hierarquia inicial e callback de tecla.
- [x] Criar testes de screenshot nos tamanhos definidos.
- [x] Validar paisagem e redimensionamento nas sete dimensões mínimas.
- [x] Validar escala de fonte 1,5, inspecionar escala 2,0, foco e alvos mínimos.
- [x] Medir cobertura e justificar exclusões.
- [x] Executar emuladores nos níveis de API mínimo e alvo.
- [x] Executar build, testes JVM, lint e validador estrutural pelo Gradle Wrapper.

## Revisão e validação

- [x] Registrar checkpoint parcial de implementação aprovado pelo solicitante.

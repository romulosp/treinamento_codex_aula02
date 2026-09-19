# Tarefas: 9999-sistema-prototipo-android

**Autor:** Rômulo Penha

## Status

`SPEC_APROVADA`

## Pré-condições

- [x] Revisar `proposal.md`, `spec.md`, `DESIGN.md` e todas as especificações complementares.
- [x] Definir `minSdk = 26` e distribuição interna.
- [x] Proibir incorporação de binários legados e definir substituições neutras por Compose/sistema.
- [x] Marcar definições dependentes das 118 referências ausentes como `SUBSTITUIR` na matriz completa.
- [x] Alterar `proposal.md` e `spec.md` para `SPEC_APROVADA` somente após relatório formal sem pendência material.
- [x] Criar `implementation-plan.md` completo após `SPEC_APROVADA`.

## Implementação

### Fundação

- [ ] Criar o projeto no caminho e com a identidade definidos em RF-001.
- [ ] Configurar Java 17, Kotlin, Gradle Wrapper, catálogo de versões e Compose estável.
- [ ] Executar o validador estrutural e corrigir somente violações comprovadas.

### Inventário e recursos

- [x] Incorporar código de referência, configurações, imagens, fontes, entrada textual e imagem de referência em `source-material/`.
- [x] Gerar matriz completa das 212 definições visuais.
- [ ] Inventariar os 350 nós de menu sem importar regras de negócio.
- [ ] Calcular hashes, remover cópias e registrar renomeações Android.
- [x] Resolver cada recurso ausente por substituição neutra rastreada.
- [x] Registrar a decisão de não copiar ativos sem licença ou autoridade comprovada.

### Tema e componentes

- [ ] Implementar tokens e catálogo visual Kotlin.
- [ ] Implementar botões e ações.
- [ ] Implementar textos, painéis e imagens.
- [ ] Implementar campos de texto, área de texto e máscaras aprovadas.
- [ ] Implementar campo de senha sem vazamento de conteúdo.
- [ ] Implementar seleção, alternância, grupos e lista suspensa.
- [ ] Implementar tabela/lista e rolagem nativas.
- [ ] Implementar diálogo, mensagens e progresso.
- [ ] Implementar teclados alfanumérico, numérico e calculadora.
- [ ] Adicionar KDoc aplicável durante cada vertical slice.

### Telas

- [ ] Implementar a tela-catálogo com todas as famílias na mesma rota.
- [ ] Implementar composição expandida semelhante à referência.
- [ ] Implementar reorganização compacta rolável.
- [ ] Implementar composição média e expandida orientada pela janela.
- [ ] Tratar insets, recortes, gestos e separações de dobráveis.
- [ ] Implementar menu demonstrativo e navegação de retorno.
- [ ] Preservar estado em rotação e redimensionamento.

### Testes e qualidade

- [ ] Criar testes unitários para lógica elegível.
- [ ] Criar testes Compose de comportamento e semântica.
- [ ] Criar testes de screenshot nos tamanhos definidos.
- [ ] Validar retrato, paisagem e redimensionamento nas sete dimensões mínimas.
- [ ] Validar escala de fonte 1,5, inspecionar escala 2,0, foco e alvos mínimos.
- [ ] Medir cobertura e justificar exclusões.
- [ ] Executar emuladores nos níveis de API mínimo e alvo.
- [ ] Executar build, testes, lint e validador estrutural pelo Gradle Wrapper.

## Revisão e validação

- [ ] Executar revisão de implementação contra toda a SPEC.
- [ ] Executar revisão de segurança e privacidade.
- [ ] Registrar comandos, ambiente, resultados, códigos de saída e evidências em `validation.md`.
- [ ] Obter aprovação formal antes de atualizar `specs/system/` ou arquivar.

# Tarefas

## Status

`IMPLEMENTADA`

- [x] Remover incorporação do plugin nos assets do host.
- [x] Implementar pasta dinâmica, `FileObserver`, debounce e scan inicial.
- [x] Implementar máquina de estados completa do diagrama de sequência.
- [x] Recuperar no boot a revisão mais recente do repositório verificado.
- [x] Separar validação/promoção da instanciação do plugin.
- [x] Validar assinatura, pacote, manifesto, API, digest, tamanho e caminho.
- [x] Integrar estados dinâmicos ao lifecycle e à UI do host.
- [x] Implementar `ERROR`, `onDetach`, limpeza de registro e fallback da UI.
- [x] Atualizar script para compilar, instalar e entregar os APKs separadamente.
- [x] Criar/atualizar testes aplicáveis e inventário KDoc.
- [x] Executar build, unitários, lint e teste no emulador.
- [x] Gerar os componentes e deixar o app aberto para validação manual.
- [x] Realizar revisão independente da implementação.

## Correções da revisão de implementação

- [x] Corrigir `IMP-REV-001`: rejeição, auditoria e limpeza desde a primeira
  pré-condição.
- [x] Corrigir `IMP-REV-002`: parser JSON estruturado e contrato completo do
  manifesto.
- [x] Corrigir `IMP-REV-003`: contenção de falhas recuperáveis de linkage e
  fallback da Activity.
- [x] Corrigir `IMP-REV-004`: testes instrumentados do loader e do parser.
- [x] Executar nova revisão independente da implementação.

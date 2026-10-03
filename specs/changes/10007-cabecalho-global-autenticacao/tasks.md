# Tarefas: 10007-cabecalho-global-autenticacao

**Autor:** Rômulo Penha

## Status

`IMPLEMENTADA`

## Pré-condições

- [x] Registrar proposta, SPEC, design, tarefas e hashes visuais.
- [x] Inspecionar contratos atuais do backend, Android e realm local.
- [x] Revisar a SPEC e resolver achados materiais.
- [x] Promover os contratos para `SPEC_APROVADA` após revisão formal.
- [x] Criar `implementation-plan.md` depois da aprovação.

## Implementação

- [x] Decodificar os claims permitidos da resposta do Keycloak no
      `autenticadorsso`, registrando o risco aceito de não validar assinatura.
- [x] Ampliar domínio e resposta REST com perfil sanitizado.
- [x] Atualizar testes unitários e de integração Java conforme a estratégia do
      projeto e revisar JavaDoc.
- [x] Ampliar resultado do plugin de login e `SessionStateChanged`.
- [x] Incrementar `SharedApi` de 1.1.0 para 1.2.0, exigir minor 2 em
      `startup-auth` e preservar minor requerida 1 nos plugins
      `business-menu` compatíveis.
- [x] Criar estado tipado, cabeçalho Compose e `CoreShell` no `:app`.
- [x] Mostrar somente o label `TERMINAL` e remover `LOTERICA`, sem configuração
      ou valor inventado.
- [x] Remover cabeçalho duplicado do `:plugin-login`.
- [x] Criar testes JVM e Compose para variantes, roles e navegação.
- [x] Revisar KDoc de todas as declarações Kotlin alteradas.

## Revisão e validação

- [ ] Revisar implementação contra a SPEC em `reviews/`.
- [x] Executar Maven tests no backend.
- [x] Executar validador Android, Gradle tests, lint e builds aplicáveis; o
      validador registrou a limitação preexistente de `local.properties`.
- [ ] Testar integração real Keycloak -> API -> Android sem registrar token.
- [ ] Comparar ambos os estados com os SVGs em 1280×800.
- [ ] Validar viewport menor e font scale 1,5.
- [x] Registrar ambiente, comandos, resultados e códigos de saída.
- [ ] Obter aprovação formal antes de arquivar ou preparar commit.

# Tarefas: 10007-cabecalho-global-autenticacao

## Status

`IMPLEMENTADA`

## Pre-condicoes

- [x] Registrar proposta, SPEC, design, tarefas e hashes visuais.
- [x] Inspecionar contratos atuais do backend, Android e realm local.
- [x] Revisar a SPEC e resolver achados materiais.
- [x] Promover os contratos para `SPEC_APROVADA` apos revisao formal.
- [x] Criar `implementation-plan.md` depois da aprovacao.

## Implementacao

- [x] Decodificar os claims permitidos da resposta do Keycloak no
      `autenticadorsso`, registrando o risco aceito de nao validar assinatura.
- [x] Ampliar dominio e resposta REST com perfil sanitizado.
- [x] Atualizar testes unitarios e de integracao Java conforme a estrategia do
      projeto e revisar JavaDoc.
- [x] Ampliar resultado do plugin de login e `SessionStateChanged`.
- [x] Incrementar `SharedApi` minor e atualizar manifests compativeis.
- [x] Criar estado tipado, cabecalho Compose e `CoreShell` no `:app`.
- [x] Mostrar somente o label `TERMINAL` e remover `LOTERICA`, sem configuracao
      ou valor inventado.
- [x] Remover cabecalho duplicado do `:plugin-login`.
- [x] Criar testes JVM e Compose para variantes, roles e navegacao.
- [x] Revisar KDoc de todas as declaracoes Kotlin alteradas.

## Revisao e validacao

- [ ] Revisar implementacao contra a SPEC em `reviews/`.
- [x] Executar Maven tests no backend.
- [x] Executar validador Android, Gradle tests, lint e builds aplicaveis; o
      validador registrou a limitacao preexistente de `local.properties`.
- [ ] Testar integracao real Keycloak -> API -> Android sem registrar token.
- [ ] Comparar ambos os estados com os SVGs em 1280x800.
- [ ] Validar viewport menor e font scale 1,5.
- [x] Registrar ambiente, comandos, resultados e codigos de saida.
- [ ] Obter aprovacao formal antes de arquivar ou preparar commit.

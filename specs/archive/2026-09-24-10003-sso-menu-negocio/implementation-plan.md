# Plano de implementação: 10003-sso-menu-negocio

## Impactos

- Novo backend local independente e ignorado pela política de versionamento de
  `apps/backend/`.
- Alterações binárias compatíveis por nova minor da `:shared-api`.
- Rede no plugin de login e nova tela inicial no host.

## Estratégia

1. Criar gerador/template que injeta as chaves externas no processo Quarkus.
2. Implementar gateway Keycloak, store volátil e casos de uso testáveis.
3. Expor os dois endpoints REST com DTOs e mapeamento de falhas.
4. Evoluir a shared API para autenticação/logout e plugin de negócio.
5. Trocar autenticação local do plugin pela API com estado UDF e evento único.
6. Renderizar menu vazio após sessão e voltar ao login no logout.
7. Priorizar o plugin incorporado da revisão debug atual antes da recuperação
   de uma revisão verificada anterior, preservando o fallback seguro.

## Testes

- Java unitário: sucesso, credencial inválida, indisponibilidade e logout.
- Quarkus: contratos 200/400/401/503/204 com gateway/store simulados.
- Kotlin JVM: redutor e parser dos resultados.
- Compose: campos obrigatórios, loading e tela inicial vazia/logout.
- Kotlin JVM: ordenação determinística do candidato incorporado.
- Emulador: reinstalação incremental com revisão antiga preservada e ativação
  da revisão incorporada atual.

## Qualidade e segurança

- Executar Maven test/package e JaCoCo quando disponível.
- Executar validador estrutural, testes Gradle, assemble e lint.
- Buscar segredo literal, tokens e logs de credenciais.
- Inventariar JavaDoc/KDoc e registrar qualquer limitação de ambiente.

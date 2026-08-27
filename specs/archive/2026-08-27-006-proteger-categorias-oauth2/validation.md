# Validação: 006-proteger-categorias-oauth2

## Ambiente

- Windows.
- Java 17.0.11 em `C:\Desenvolvimento\jdk-17.0.11`.
- Maven e Quarkus 3.2.10.Final.
- Perfil de testes Quarkus com H2 em memória e OIDC desabilitado, usando `quarkus-test-security` para identidades simuladas.

## Comandos e códigos de saída

1. `mvn test` usando o JDK disponível inicialmente — código `1`; interrompido antes dos testes porque o compilador não suportava `--release 17`.
2. `JAVA_HOME=C:\Desenvolvimento\jdk-17.0.11` e `mvn test` — código `1`; identificou a injeção CDI não resolvida de `JsonWebToken` e, após a correção, o mapeamento obrigatório de lista vazia.
3. `JAVA_HOME=C:\Desenvolvimento\jdk-17.0.11` e `mvn test` — código `0`; execução final aprovada.

## Cenários executados

- Autenticação: chamada sem token para `/categorias/` retorna HTTP 401.
- Autorização: cliente com `azp` autorizado acessa todas as operações já existentes de categorias.
- Autorização: `azp` ausente ou não autorizado retorna HTTP 403 com `mensagem`.
- Configuração: lista de clientes vazia retorna HTTP 403; lista com espaços e itens vazios é normalizada, mantendo comparação exata.
- Integração de saída: `FiservFiltroAutenticacao` adiciona `Content-Type: application/json` e `Capture-Network-Code: local`.
- Documentação: o OpenAPI contém as descrições de 401 e 403.

## Evidências

- `VAL-001` — a execução final informou `Tests run: 11, Failures: 0, Errors: 0, Skipped: 0` e `BUILD SUCCESS`.
- `VAL-002` — a aplicação foi iniciada nos perfis de teste e respondeu HTTP 401 e HTTP 403 nos cenários de segurança.
- `VAL-003` — o relatório Jacoco foi gerado em `apps/backend/target/site/jacoco/`. O Maven exibiu advertência de dados de execução de uma classe não correspondentes durante a geração do relatório; não houve falha de teste nem impacto na execução da suíte.

## Veredito
`VALIDADA`

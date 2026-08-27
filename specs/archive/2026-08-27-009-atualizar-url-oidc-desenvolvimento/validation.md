# Validação: 009-atualizar-url-oidc-desenvolvimento

## Ambiente

- Windows.
- Java 17.0.11 em `C:\Desenvolvimento\jdk-17.0.11`.
- Maven 3.8.8 e Quarkus 3.2.10.Final.
- Perfil de testes Quarkus com H2 em memória e OIDC desabilitado, sem contato com serviços externos.

## Comandos e códigos de saída

1. Verificação inicial por PowerShell — código `1`; a expressão regular ancorada não considerava o caractere de retorno de carro do arquivo `.bat`. Nenhuma alteração de implementação foi necessária.
2. Verificação corrigida do endereço, ordem da variável, escopo do diff e parametrização de `application.properties` — código `0`.
3. `mvn test` com Java 17.0.11 — código `0`.

## Cenários executados

- O script contém a nova URL OIDC antes de `mvn quarkus:dev` e não contém o host anterior.
- O diff funcional do script contém exclusivamente a substituição da linha `AUTH-SERVER-URL`.
- `application.properties` permanece parametrizado com `AUTH-SERVER-URL` e não foi alterado.
- A suíte de integração é executada no perfil de testes, sem conectar ao OIDC ou DB2 remotos.

## Evidências

- `VAL-001` — a verificação corrigida confirmou a nova URL, a ausência do endereço anterior, o posicionamento antes de Maven e a preservação do escopo.
- `VAL-002` — `mvn test` concluiu com `Tests run: 11, Failures: 0, Errors: 0, Skipped: 0` e `BUILD SUCCESS`.
- `VAL-003` — a geração do relatório JaCoCo apresentou a advertência preexistente de incompatibilidade de dados de execução para `FiservFiltroAutenticacao`; não houve falha de teste nem impacto nesta mudança, que não alterou classes Java.

## Veredito
`VALIDADA`

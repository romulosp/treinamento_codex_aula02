# Validação: 007-configurar-inicializacao-local-segura

## Ambiente

- Windows.
- Java 17.0.11 em `C:\Desenvolvimento\jdk-17.0.11`.
- Maven 3.8.8 e Quarkus 3.2.10.Final.
- Configuração concreta em arquivo local ignorado pelo Git; valores sensíveis não foram incluídos nesta evidência.

## Comandos e códigos de saída

1. Verificação por PowerShell do conteúdo do script, da configuração local e do `.gitignore` — código `0`.
2. Execução de cópia temporária do script sem configuração local — código `1`, sem invocar Maven.
3. `mvn test` com Java 17.0.11 — código `0`.

## Cenários executados

- A configuração local define as sete variáveis requeridas, sem revelar seus valores.
- O arquivo local é reconhecido por `git check-ignore`.
- O script contém validação das sete variáveis e falha antecipadamente quando estão ausentes.
- O script preserva Java 17.0.11, Maven 3.8.8 e `mvn quarkus:dev` no diretório do backend.
- A suíte de integração Quarkus permanece aprovada no perfil de testes, que não acessa o DB2 nem o provedor OIDC real.

## Evidências

- `VAL-001` — a verificação de estrutura e proteção local foi aprovada; a execução sem configuração exibiu somente nomes das variáveis ausentes e retornou código diferente de zero.
- `VAL-002` — revisão de diffs e auditoria independente confirmaram que nenhum valor concreto de OIDC ou DB2 aparece em arquivos rastreados.
- `VAL-003` — `mvn test` concluiu com `Tests run: 11, Failures: 0, Errors: 0, Skipped: 0` e `BUILD SUCCESS`.
- `VAL-004` — a geração do relatório JaCoCo apresentou a advertência preexistente de incompatibilidade de dados de execução para `FiservFiltroAutenticacao`; não houve falha de testes nem impacto nesta mudança, que não alterou classes Java.

## Veredito
`VALIDADA`

# Validação: 008-unificar-configuracao-inicializacao-local

## Ambiente

- Windows.
- Java 17.0.11 em `C:\Desenvolvimento\jdk-17.0.11`.
- Maven 3.8.8 e Quarkus 3.2.10.Final.

## Comandos e códigos de saída

1. Verificação estrutural por PowerShell do script e da inexistência do arquivo separado — código `0`.
2. `mvn test` com Java 17.0.11 — código `0`.

## Cenários executados

- O script único contém as sete variáveis antes de `mvn quarkus:dev`.
- O script, `.gitignore` e o diretório do backend não referenciam `start_aplicacao.local.bat`.
- Java, Maven e o diretório de execução são mantidos.

## Evidências

- `VAL-001` — a verificação estrutural foi aprovada e confirmou a remoção do segundo script.
- `VAL-002` — a suíte Maven foi aprovada com `Tests run: 11, Failures: 0, Errors: 0, Skipped: 0` e `BUILD SUCCESS`.
- `VAL-003` — a geração do relatório JaCoCo apresentou a advertência preexistente de incompatibilidade de dados de execução para `FiservFiltroAutenticacao`; não houve falha de teste ou impacto nesta mudança, que não alterou classes Java.
- `VAL-004` — a documentação vigente foi atualizada para declarar `start_aplicacao.bat` como único script e não contém referência funcional a um arquivo de configuração separado.

## Veredito
`VALIDADA`

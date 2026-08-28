# Validação: 022-corrigir-inicializacao-quarkus

## Ambiente

- Windows 10
- Java 17.0.11
- Maven 3.8.8
- Quarkus 3.2.10.Final
- Projeto: `apps/backend/gerenciarcategorias`

## Veredito

## Comandos e resultados

| Comando | Resultado | Código |
| --- | --- | --- |
| `mvn test` | 15 testes executados, 0 falhas e 0 erros | `0` |
| `mvn quarkus:dev` | Quarkus iniciou em `http://localhost:8080`, perfil `dev`, live coding ativo; processo encerrado manualmente após a confirmação | `0` |

## Evidências

- `VAL-001` — o POM contém `quarkus-maven-plugin` com execução do goal `build`.
- `VAL-002` — a suíte Maven foi aprovada com 15 testes.
- `VAL-003` — o log de desenvolvimento exibiu `gerenciar-categorias 1.0.0.1 ... started` e `Listening on: http://localhost:8080`.
- `VAL-004` — o warning original `Skipping quarkus:dev as this is assumed to be a support library` não ocorreu após a correção.

## Veredito

`VALIDADA`

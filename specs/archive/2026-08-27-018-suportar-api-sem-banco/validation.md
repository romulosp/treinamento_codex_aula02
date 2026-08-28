# Validação: 018-suportar-api-sem-banco

## Ambiente

- Data/hora: 2026-08-27 22:20 -03:00.
- Sistema: Windows 10 amd64.
- Java: `17.0.11`, em `C:\Desenvolvimento\jdk-17.0.11`.
- Maven: `3.8.8`, em `C:\Desenvolvimento\apache-maven-3.8.8`.
- Sessão: `MAVEN_OPTS=-Duser.home=D:/desenvolvimento/ia/aula02/.maven-home` e settings público explícito (`.mvn/settings-public.xml`).
- PowerShell: executado com `-NoProfile -ExecutionPolicy Bypass`.

## Comandos e códigos de saída

| Comando | Resultado | Código |
| --- | --- | --- |
| `powershell -NoProfile -ExecutionPolicy Bypass -File .\apps\backend\testar-geracao-configuracao-banco.ps1` | Validação de geração de banco e ausência de persistência para SEM_BANCO aprovada | `0` |
| `mvn -s .mvn/settings-public.xml test` (em `apps/backend`) | 12 testes executados com 0 falhas e 0 erros | `0` |

## Testes unitários e cobertura

- Ferramenta e versão: JUnit 5 (Quarkus 3.2.10.Final) e JaCoCo 0.8.12.
- Escopo de classes aplicáveis: 13 classes do módulo `gerenciar-categorias` (recursos, serviços, armazenamento em memória e exception mappers).
- Classes excluídas e justificativas: Nenhuma classe do pacote foi excluída.
- Cobertura de linhas: JaCoCo executado via `jacoco-maven-plugin:0.8.12:report`.
- Cobertura de branches: Validada nas regras de negócio e validações em memória.
- Comando executado: `mvn -s .mvn/settings-public.xml test`.
- Resultado: Sucesso (BUILD SUCCESS), 12 testes executados (2 em CategoriaResourceTest, 2 em CategoriaExceptionMapperTest, 2 em CategoriaResourceUnitTest, 5 em CategoriaServiceTest, 1 em ArmazenamentoCategoriasEmMemoriaTest).
- Código de saída: `0`.
- Indisponibilidade de aferição ou observações: Nenhuma.

## Cenários executados

- `VAL-001` — Geração com `SEM_BANCO`: `pom.xml` não contém dependências de banco (`quarkus-jdbc-*`, `quarkus-hibernate-orm-panache`) e `application.properties` não contém propriedades ou variáveis de datasource (`quarkus.datasource`, `quarkus.hibernate-orm`, `DB2_*`, `POSTGRESQL_*`, `MYSQL_*`).
- `VAL-002` — Preservação dos bancos suportados: ausência de parâmetro continua gerando DB2 como padrão; `DB2`, `POSTGRESQL` e `MYSQL` continuam gerando suas dependências e variáveis exclusivas.
- `VAL-003` — Tratamento de valor inválido: valor não suportado (ex.: `ORACLE`) falha com mensagem indicando os quatro valores aceitos (`DB2, POSTGRESQL, MYSQL, SEM_BANCO`) e sem alterar arquivos.
- `VAL-004` — Execução da suíte de testes Quarkus da API de categorias: 12 testes executados e aprovados sem dependência de banco de dados externo ou H2 em memória.

## Evidências

- O script de teste PowerShell emitiu `Teste de geração de configuração de banco aprovado.` com código de saída `0`.
- A execução do Maven (`mvn -s .mvn/settings-public.xml test`) compilou e executou todos os 12 testes com status `BUILD SUCCESS` e código de saída `0`.
- `apps/backend/start_aplicacao.bat` não solicita nem exporta variáveis de banco de dados.

## Veredito

`VALIDADA`

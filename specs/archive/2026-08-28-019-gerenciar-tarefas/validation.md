# Validação: 019-gerenciar-tarefas

## Ambiente

- Data/hora: 2026-08-28 07:30 -03:00.
- Sistema: Windows 10 amd64.
- Java: `17.0.11`, em `C:\Desenvolvimento\jdk-17.0.11`.
- Maven: `3.8.8`, em `C:\Desenvolvimento\apache-maven-3.8.8`.
- Sessão: `MAVEN_OPTS=-Duser.home=D:/desenvolvimento/ia/aula02/.maven-home` e settings público (`.mvn/settings-public.xml`).
- PowerShell: executado com `-NoProfile -ExecutionPolicy Bypass`.

## Comandos e códigos de saída

| Comando | Resultado | Código |
| --- | --- | --- |
| `powershell -NoProfile -ExecutionPolicy Bypass -File .\apps\backend\testar-geracao-configuracao-banco.ps1` | Matriz de configuração de banco (PostgreSQL, DB2, MySQL, SEM_BANCO e inválido) testada e aprovada | `0` |
| `mvn -s .mvn/settings-public.xml test` (em `apps/backend`) | 23 testes unitários e de integração executados com 0 falhas e 0 erros | `0` |

## Testes unitários e cobertura

- Ferramenta e versão: JUnit 5 (Quarkus 3.2.10.Final), Mockito, Rest Assured e JaCoCo 0.8.12.
- Escopo de classes aplicáveis: 15 classes do módulo `gerenciar-tarefas` (recursos, serviços, domínio, exceções, repositórios e DTOs).
- Classes excluídas e justificativas: Nenhuma classe foi excluída do escopo.
- Cobertura de linhas e branches: JaCoCo executado via `jacoco-maven-plugin:0.8.12:report`.
- Comando executado: `mvn -s .mvn/settings-public.xml test`.
- Resultado: Sucesso (`BUILD SUCCESS`), 23 testes executados com 100% de aprovação:
  - `TarefaResourceTest`: 7 testes de integração Rest Assured.
  - `TarefaServiceTest`: 7 testes unitários com Mockito.
  - `TarefaResourceUnitTest`: 4 testes unitários com Mockito.
  - `StatusTarefaTest`: 3 testes unitários de regras de enum.
  - `TarefaExceptionMapperTest`: 2 testes unitários de Exception Mappers.
- Código de saída: `0`.
- Indisponibilidade de aferição ou observações: Nenhuma.

## Cenários executados

- `VAL-001` — Listagem de tarefas: `GET /tarefas` retorna HTTP 200 e lista no atributo `tarefas`.
- `VAL-002` — Criação de tarefa: `POST /tarefas` cria registro com status `PENDENTE`, registra `data_criacao` e retorna HTTP 201.
- `VAL-003` — Detalhamento por ID: `GET /tarefas/{id}` retorna dados da tarefa com HTTP 200.
- `VAL-004` — Validação de campos inválidos: `POST /tarefas` com título em branco retorna HTTP 400 e mensagem de erro.
- `VAL-005` — Atualização: `PUT /tarefas/{id}` atualiza título, descrição e altera status para `CONCLUIDA`, preenchendo automaticamente `data_conclusao`.
- `VAL-006` — Exclusão: `DELETE /tarefas/{id}` remove a tarefa com HTTP 200; busca posterior confirma HTTP 404.
- `VAL-007` — Consulta de ID inexistente: `GET /tarefas/99999` retorna HTTP 404.
- `VAL-008` — Testes unitários isolados: regras de negócio de `StatusTarefa`, `TarefaService`, `TarefaResource` e Exception Mappers validadas sem container.
- `VAL-009` — Persistência e banco: Suíte executada com banco H2 em memória em ambiente de teste e configuração produtiva PostgreSQL renderizada corretamente.

## Evidências

- O script `testar-geracao-configuracao-banco.ps1` retornou `Teste de geração de configuração de banco aprovado.` com código `0`.
- A compilação e suíte Maven executou 23 testes com status `BUILD SUCCESS` e código `0`.
- Nenhuma URL ou senha de banco foi versionada.

## Veredito

`VALIDADA`

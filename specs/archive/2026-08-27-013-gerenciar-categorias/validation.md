# Validação: 013-gerenciar-categorias

## Ambiente

- Data/hora: 2026-08-27 21:27 -03:00.
- Sistema: Windows 10 amd64.
- Java: `17.0.11`, em `C:\Desenvolvimento\jdk-17.0.11`.
- Maven: `3.8.8`, em `C:\Desenvolvimento\apache-maven-3.8.8`.
- Perfil Quarkus: `test`, com H2 e sem banco externo.

## Comandos e códigos de saída

| Comando | Resultado | Código |
| --- | --- | --- |
| `mvn -s .mvn/settings-public.xml clean test` | Compilação, testes unitários, integração Quarkus e relatório JaCoCo concluídos | `0` |

## Testes unitários e cobertura

- Ferramentas: JUnit 5, Rest Assured, Quarkus Test e JaCoCo `0.8.12`.
- Classes aplicáveis inventariadas: `Categoria`, `CategoriaService`, `ArmazenamentoCategoriasEmMemoria`, recursos, mapeadores e DTOs com lógica de conversão.
- Classes declarativas excluídas: nenhuma; DTOs simples foram exercitados pelos testes de recurso e conversão.
- Cobertura JaCoCo: `100%` de linhas (`0` perdidas) e `100%` de branches (`0` perdidos), conforme `target/site/jacoco/jacoco.csv`.
- Resultado: `12` testes executados, `0` falhas, `0` erros e `0` ignorados.

## Cenários executados

- `VAL-001` — Massa inicial e `GET /categorias/` com os atributos e valores aprovados.
- `VAL-002` — Inclusão, consulta, atualização e exclusão com os contratos HTTP aprovados.
- `VAL-003` — Entradas inválidas e categoria inexistente com respostas `400` e `404` contendo `mensagem`.
- `VAL-004` — Regras unitárias de nome, quantidade nula/negativa, identificadores, armazenamento e mapeadores.

## Evidências

- O Quarkus iniciou no perfil `test` em `http://localhost:8083` e a suíte de integração passou.
- A massa inicial é reinicializada entre cenários e a primeira inclusão recebe identificador `4`.
- O aviso de Hibernate ORM sem entidades JPA é esperado: esta mudança usa somente armazenamento em memória.

## Veredito

`VALIDADA`

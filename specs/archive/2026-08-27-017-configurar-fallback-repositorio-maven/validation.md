# Validação: 017-configurar-fallback-repositorio-maven

## Ambiente

- Data/hora: 2026-08-27 21:27 -03:00.
- Sistema: Windows 10 amd64.
- Java: `17.0.11`, em `C:\Desenvolvimento\jdk-17.0.11`.
- Maven: `3.8.8`, em `C:\Desenvolvimento\apache-maven-3.8.8`.
- Sessão: `MAVEN_OPTS=-Duser.home=D:/desenvolvimento/ia/aula02/.maven-home` e settings público explícito.

## Comandos e códigos de saída

| Comando | Resultado | Código |
| --- | --- | --- |
| `mvn -s .mvn/settings-public.xml clean test` | Dependências resolvidas pelo Maven Central e suíte concluída | `0` |
| `powershell -NoProfile -ExecutionPolicy Bypass -File .\testar-geracao-configuracao-banco.ps1` | Renderizador e Maven do projeto executados com a configuração local | `0` |

## Cenários executados

- `VAL-001` — Caminho público: `settings-public.xml` não possui mirror Nexus e o Maven resolveu as dependências pelo Maven Central.
- `VAL-002` — Repositório local: a sessão usou `user.home` temporário sob o projeto, sem modificação de `settings.xml` global.
- `VAL-003` — Testes Java/Quarkus: `12` testes concluídos sem falhas ou erros.
- `VAL-004` — Seleção Nexus, pública e automática: verificada estaticamente no script, incluindo `Test-NetConnection`, argumentos e preservação do código de saída.

## Evidências

- A falha CDI anterior foi eliminada com a injeção explícita de `CategoriaService`.
- A execução fora do sandbox eliminou o acesso negado ao JAR do repositório do usuário e completou a suíte com sucesso.
- `settings-nexus.xml` contém somente o mirror interno e `settings-public.xml` não contém credenciais nem mirror corporativo.
- A conectividade real com `binario.caixa:8081` não foi exercitada porque a VPN/Nexus não estava disponível nesta sessão; a seleção automática e o fallback foram verificados por inspeção do script.

## Veredito

`VALIDADA`

O cenário de Nexus acessível deve ser repetido no ambiente corporativo antes de uma liberação que dependa desse repositório, mas não bloqueia a validação da implementação do fallback público nesta estação.

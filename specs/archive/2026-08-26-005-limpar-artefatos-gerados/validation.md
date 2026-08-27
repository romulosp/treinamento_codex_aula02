# Validação: 005-limpar-artefatos-gerados

## Ambiente

- Data: 2026-08-26.
- Sistema operacional: Windows 11, arquitetura amd64.
- Workspace validado: `aula02`.

## Comandos e códigos de saída

1. Criação de `apps/frontend/.quarkus` e `apps/frontend/limpeza-validacao.log`, seguida de `cmd /c "(echo S)|call deletar-arquivos-gerados.bat"` — código de saída `1`.
	- Resultado: os artefatos de teste do frontend foram removidos e os arquivos protegidos permaneceram presentes. A remoção de `apps/backend/target` falhou porque `gerenciar-categorias-dev.jar` estava em uso por outro processo.

## Cenários executados

- Confirmação positiva: o script iniciou a limpeza após receber `S`.
- Remoção de `.quarkus` e `.log`: aprovada; os artefatos temporários do frontend foram removidos.
- Preservação: aprovada; `pom.xml`, `start_aplicacao.bat`, `application.properties`, `specs/README.md` e `.git` permaneceram presentes.
- Remoção de `apps/backend/target`: reprovada; o diretório permaneceu porque um processo externo bloqueou `gerenciar-categorias-dev.jar`.

## Evidências

### VAL-001 — Proteção de escopo e tratamento de falha — Aprovada

- Resultado observado: o script limitou a limpeza a `apps/`, não removeu os arquivos protegidos e retornou código `1` ao detectar que o diretório `target` não pôde ser removido.

### VAL-002 — Limpeza completa de artefatos — Reprovada

- Resultado observado: `apps/backend/target` não foi removido porque `gerenciar-categorias-dev.jar` estava aberto por outro processo.
- Impacto: o critério de aceite de remoção de um diretório `target` não possui evidência aprovada.
- Ação necessária: encerrar o processo que mantém o arquivo bloqueado e executar novamente a validação; não há alteração de código autorizada durante esta fase.

## Veredito
`REPROVADA`

## Revalidação

- Comando: criação de `.quarkus` e `limpeza-validacao.log` temporários, seguida de `cmd /c "(echo S)|call deletar-arquivos-gerados.bat"`.
- Código de saída: `1`.
- Resultado: o script removeu novamente os artefatos temporários do frontend e preservou os itens protegidos, mas `apps/backend/target/gerenciar-categorias-dev.jar` continuou em uso por outro processo.
- Conclusão: a validação permanece `REPROVADA`. O fluxo não pode avançar para aprovação até que o processo seja encerrado e a remoção de `target` seja validada com código `0`.

## Revalidação após liberação do processo

- Comando: criação de `apps/frontend/.quarkus` e `apps/frontend/limpeza-validacao.log` temporários, seguida de `cmd /c "(echo S)|call deletar-arquivos-gerados.bat"`.
- Código de saída: `0`.
- Resultado: `apps/backend/target`, `apps/frontend/.quarkus` e `apps/frontend/limpeza-validacao.log` foram removidos. `pom.xml`, `start_aplicacao.bat`, `application.properties`, `specs/README.md` e `.git` foram preservados.
- Evidência: saída `RESULTADO_LIMPEZA=0 REMOVIDOS=True PRESERVADOS=True` e contador de três artefatos removidos.

## Veredito atual

`VALIDADA`
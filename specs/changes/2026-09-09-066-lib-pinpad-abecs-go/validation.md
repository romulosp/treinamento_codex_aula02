# Validação — 066-lib-pinpad-abecs-go

**Data:** 2026-09-11  
**Estado:** `BLOQUEADA`  
**Fase atual:** validação.

## Ambiente

- Windows `windows/386`; Go `go1.26.5 windows/386`.
- Pinpad físico e porta serial real: indisponíveis nesta execução.
- Sonar/scanner e `govulncheck`: indisponíveis no módulo; aplicada Auditoria de Qualidade Assistida por LLM.
- Docker Desktop não está em execução. O WSL `docker-desktop` não possui Go instalado, portanto não oferece alternativa para `-race`.

## Comandos e resultados

| Comando | Código | Resultado |
| --- | ---: | --- |
| `gofmt -w ...` | 0 | Formatação concluída. |
| `go vet ./...` | 0 | Aprovado. |
| `go test ./...` | 0 | Todos os testes passaram. |
| `go test ./... -coverprofile=coverage` | 0 | Perfil de cobertura gerado. |
| `go tool cover -func=coverage` | 0 | Cobertura total aferida em **81,7%**. |
| `go build ./...` | 0 | Build aprovado. |
| `go build -o libpinpadabecsgo.exe ./cmd/libpinpadabecsgo` | 0 | Executável Windows compilado no diretório do módulo. |
| `go test -race ./...` | 2 | Não suportado em `windows/386`. |
| `docker run ... go test -race ./...` | 1 | Docker Desktop não estava em execução. |
| `wsl -d docker-desktop -- go version` | 127 | Distribuição sem Go instalado. |
| `go test ./...` após ampliação do menu local | 0 | Menu e todos os pacotes aprovados. |
| `go vet ./...` após ampliação do menu local | 0 | Aprovado. |
| `go build -o libpinpadabecsgo.exe ./cmd/libpinpadabecsgo` após ampliação do menu local | 0 | Executável atualizado. |

## Evidências VAL

- **VAL-001:** a cobertura mínima de 80% foi atendida com 81,7%.
- **VAL-002:** testes adicionais cobrem CLI local, falhas de transporte serial, escrita parcial, cópias defensivas, sessão expirada, cancelamento, resposta inválida e limites de byte stream.
- **VAL-003:** a execução de `-race` foi tentada nos ambientes disponíveis; não há ambiente compatível ativo para essa verificação.
- **VAL-004:** a validação física de comunicação, status, timeout, cancelamento e redaction continua pendente de pinpad e porta serial reais.

## Auditoria de segurança

Escopo inspecionado: módulo Go, configuração por ambiente, adaptador serial, framing, parser, fila, sessão, logging e dependência `go.bug.st/serial`.

- Nenhum segredo confirmado foi encontrado pela busca estática.
- Não foram encontrados listener de rede, API HTTP, execução de comando do sistema, SQL ou renderização HTML no módulo.
- O log aplica redaction a payloads de GCX, GTK, GOX, FCX e GPN.
- Autenticação, autorização, tenant e IDOR são não aplicáveis: a entrega é uma biblioteca local sem servidor ou identidade de usuário.

O gerador foi atualizado com a opção `--change-066`, destinada a criar `docs/security-audit/relatorio-066-lib-pinpad-abecs-go.pdf`. O PDF ainda não foi produzido porque a skill `pdf` exige a execução prévia do utilitário `container_tools/mark_artifact_operation_started.mjs`, ausente neste ambiente. O relatório histórico da Change 010 não foi reutilizado.

## Conclusão

Os bloqueios de implementação foram resolvidos. Para concluir a validação sem retornar à implementação, faltam evidência no pinpad físico e a geração do PDF atual de segurança após disponibilizar o utilitário obrigatório da skill. Não avançar para aprovação, arquivamento ou commit.

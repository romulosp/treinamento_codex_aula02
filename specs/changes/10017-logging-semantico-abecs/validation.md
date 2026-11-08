# Evidências técnicas de implementação — 10017

Data: 2026-10-15 (America/Sao_Paulo). Estado: `IMPLEMENTADA`, para teste humano.
Este registro não constitui validação formal nem aprovação da implementação.

## Ambiente e execução

Windows, PowerShell, `go version go1.26.5 windows/386`.
Diretório: `apps/desktop/libpinpadabecsgo`.

| Comando | Resultado | Código de saída |
| --- | --- | --- |
| `go test ./... -count=1` | PASS; pacotes com testes aprovados, incluindo logging, serial, service, bridge e mobile | 0 |
| `go vet ./...` | PASS | 0 |
| `go build ./...` | PASS | 0 |

Os comandos foram executados nesta retomada sobre as alterações locais já existentes.
Não houve alteração de código de produção, acesso a payloads reais ou teste físico.
As outras Changes com contrato estão registradas como IMPLEMENTADA ou
IMPLEMENTACAO_APROVADA em seus respectivos tasks.md; seus gates posteriores
não fazem parte do prompt implementar-mudanca-para-teste.

## Pendências

- Teste humano do formato DETAILED e manutenção de LEGACY.
- Matriz de cobertura dos 46 comandos e gates formais posteriores, sem declaração de conclusão nesta execução.
- `implementation-plan.md` estava ausente ao retomar a implementação já marcada
  como concluída. A ausência do planejamento prévio é uma lacuna de processo;
  não foi produzido um plano retroativo como se fosse preparatório.
- A pasta 10009 contém somente reviews/, sem proposal.md/spec.md/DESIGN.md/tasks.md;
  não há contrato aprovado que autorize implementação dessa pasta.

Sem revisão de implementação, validação formal, aprovação, commit ou arquivamento.

## Revisão 3 — caminho BAT/Bridge, 2026-10-15

Estado: `IMPLEMENTADA`, para teste humano. Este marco substitui a conclusão
anterior de que bastava integrar o utilitário local. Ambiente Windows 10,
PowerShell, Go 1.26.5 windows/386; comandos no módulo libpinpadabecsgo.

| Comando/verificação | Resultado | Exit |
| --- | --- | --- |
| `go test ./... -count=1` | Todos os pacotes passaram | 0 |
| `go test -tags integration ./... -count=1` | Integração completa; composição main + cliente mobile TCP nos dois formatos; BAT default/override; demais jornadas sintéticas | 0 |
| `go vet ./...` | PASS | 0 |
| `go build ./...` | PASS | 0 |
| `go build -o build/10017/libpinpadabecsgo-bridge.exe ./cmd/libpinpadabecsgo-bridge` | PASS | 0 |
| `go test ./internal/infrastructure/logging '-coverprofile=build/10017/logging.cover'` | PASS, 82,3% statements no pacote logging | 0 |
| `go tool cover '-func=build/10017/logging.cover'` | Total 82,3% | 0 |
| `git diff --check` | Sem erros de whitespace; avisos LF/CRLF | 0 |
| Binário compilado + cliente mobile TCP + serial scripted | OPN/GIX/DSP/CLO completos; 8 blocos HEX (4 SPE e 4 PP) | 0 |
| `powershell -NoProfile -ExecutionPolicy Bypass -File ./preparar_emulador.ps1` | Reverse-only, emulator-5554, TCP39100 | 0 |

A primeira tentativa de go tool cover com argumento sem aspas falhou (exit 1)
porque o PowerShell perdeu o sufixo .cover. A execução corrigida acima passou.

Binário: `build/10017/libpinpadabecsgo-bridge.exe`; SHA-256:
`BEFF95771D8071984469A922788825274BC87ED9C422E3B9362C5D92FC286411`.
Smoke iniciou um processo scripted próprio e encerrou somente esse processo após
CLO/liberação. Sem COM ou pagamento nesse smoke. Amostra integral sintética:
[log de exemplo](evidence/2026-10-15-bridge-detailed-synthetic.txt).
[Matriz documental](evidence/matriz-comandos.md) lista as 46 entradas; comandos
sem catálogo continuam redigidos. Não alegar suporte universal ao protocolo.

### Verificações de comportamento e proteção

O decorador mantém bytes e limites dos chunks. Testes verificam fragmentação,
controles agregados, correlação por requisição válida, reset entre sessões,
limite 4102 bytes, falha de escrita, CRC inválido, campos desconhecidos,
PIN block/KSN/PAN sintéticos, GOX sensível, OPN seguro e DC2.
IDs/comprimentos/sensibilidade são verificados antes de formatar HEX/ASCII.
Decoder corrigido para resposta curta (sem panic), DEX com comprimento interno
real, OPN/GKY sem parâmetros, CLO S32 e TLI posicional. Campos/offsets/encoding,
descrição de status, controles, substituição DC3 e CRC aparecem no detalhamento.
O rastro bruto mantém `SetOpaqueTrace(true)`.

Falhas de conteúdo diagnóstico não alteram comunicação. Falhas de persistência
seguem a regra preexistente do rastro obrigatório do Bridge. Sem alteração
Android/AAR, API mobile, wire protocol, perfil externo ou configuração de chaves.
Proteção demonstrada por testes sintéticos e leitura do fluxo; isso não substitui
auditoria independente do stack nem os gates físicos anteriores. Race não
executado: toolchain atual windows/386; limitação Go/CGO+C preexistente na memória
10011. Sem mudança de toolchain ou dependências.

### Disponibilização

Bridge antigo já encerrado: log com 2 sessões adquiridas e 2 liberadas,
terminando em bridge_stopped, sem listener na preparação final. Novo binário
iniciado oculto, PID 4828, listener `127.0.0.1:39100`, COM7/19200/30s,
transporte physical e DETAILED. Inicialização não abre a COM e não executa
compra/captura. PID é evidência histórica, não configuração.
Destino preservado: `logs/LogPinpadAbecs.txt`, append, sem reprocessar histórico.

Próximo passo: teste humano no aplicativo. Não iniciar outro listener enquanto
o atual estiver ativo. A próxima execução do BAT compila o fonte corrigido e
seleciona DETAILED por padrão, respeitando override LEGACY.
Plano preparatório desta revisão registrado antes de código; a ausência do
plano original permanece como fato histórico. Sem revisão formal da implementação,
validação formal, aprovação final, commit, atualização de system ou arquivamento.

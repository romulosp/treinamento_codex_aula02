# Aula 02 — Prototipo com Integração lib pinpad abecs

> Projeto educacional para construção de APIs Java por meio de especificações, evidências e ciclos de entrega rastreáveis.

## Visão geral

Este repositório demonstra uma abordagem **Spec Driven** para criação de APIs. Antes de qualquer implementação, os requisitos, decisões técnicas, tarefas, critérios de aceite e evidências de validação são registrados e revisados.

O projeto mantém a documentação como fonte de verdade. Módulos executáveis são gerados a partir de mudanças aprovadas e, quando a própria mudança exigir rastreabilidade da implementação, os arquivos necessários podem ser versionados explicitamente.

- framing e controle de transmissão ABECS;
- envio e recebimento por porta serial real;
- comandos tipados e parsers de resposta;
- fila serializada para impedir operações concorrentes no mesmo pinpad;
- cancelamento, timeout, retransmissão e encerramento seguro;
- logging detalhado SPE/PP/RSP, com redaction de dados sensíveis;
- fluxos de display, multimídia, tabelas EMV, transação, teclas, PIN e reset, conforme escopo aprovado;
- comunicação segura ABECS, quando especificada e aprovada para a Change correspondente.

A conversão integral ainda deve ser considerada concluída somente após a implementação, revisão e validação dos comandos e funcionalidades do legado. A existência de builders ou constantes Go não é suficiente para declarar equivalência funcional.

O workspace preserva aplicações independentes em pastas próprias sob `apps/backend/`, como `apps/backend/gerenciarcategorias/`, e organiza o frontend por plataforma em `apps/frontend/web/`, `apps/frontend/smartphone/` e `apps/frontend/desktop/`. A vitrine demonstrativa Terra & Torra está em `apps/frontend/web/exemplo-site-web-001/`.

A política padrão do repositório permite versionar documentos `.md` e `.txt`, além do próprio `.gitignore`. Código, configurações, scripts e artefatos gerados permanecem locais por padrão; uma mudança aprovada pode selecionar explicitamente os arquivos necessários para sua rastreabilidade. Segredos reais nunca são versionados.

## Principais características

- Windows e Linux;
- porta serial por `go.bug.st/serial`;
- ausência de CGO no módulo Go;
- configuração por ambiente e por `PinpadConfig`;
- protocolo ABECS, CRC-16-CCITT e substitution;
- parser de resposta ABECS e BER-TLV;
- modelos de dispositivo, estado, resposta, capacidades e transação;
- catálogo individual de comandos ABECS;
- fila FIFO e worker único por instância;
- `context.Context` em operações bloqueantes;
- `SessionManager` para posse lógica do pinpad;
- logging operacional com `log/slog`;
- rastreamento bruto de comunicação configurável;
- testes unitários, concorrência e validação com hardware real.

- **Processo rastreável:** cada mudança possui proposta, SPEC, design, tarefas, revisões, validação e aprovação.
- **Arquitetura de referência:** Java 17, Maven, Quarkus e separação entre `api`, `application`, `domain` e `infrastructure`.
- **Qualidade desde o início:** critérios de aceite verificáveis, testes unitários e integração Quarkus como parte do ciclo de mudança.
- **Segurança por configuração:** credenciais e dados de infraestrutura devem ser fornecidos pelo ambiente local, nunca registrados em documentos ou commits.
- **Reprodutibilidade:** a documentação permite gerar novamente o mesmo módulo e comprovar o resultado com evidências objetivas.
- **Skills locais:** o acervo em [.agents/skills/](.agents/skills/) inclui skills de processo, backend Java e estudo reutilizável.

- REST, HTTP, WebSocket, gRPC ou listener TCP;
- bridge de rede do legado;
- UI web, desktop ou smartphone;
- Windows Service, Linux daemon ou instalador;
- persistência de PAN, PIN, KSN ou dados EMV;
- API pública de comando hexadecimal bruto;
- tradução literal de tipos, handles, mutexes e chamadas de sistema C/C++;
- mocks como substitutos da validação final com pinpad físico.

## Estrutura do repositório

A documentação funcional desta branch utiliza o manual ABECS fornecido para a Change, além dos artefatos legados como referência comportamental.

Parâmetros seriais padrão:

| Parâmetro | Valor |
| --- | --- |
| [NotasProjeto.md](NotasProjeto.md) | Visão detalhada do projeto, arquitetura, histórico e guia de reprodução. |
| [AGENTS.md](AGENTS.md) | Regras obrigatórias para mudanças no repositório. |
| [specs/changes/](specs/changes/) | Mudanças em andamento antes do arquivamento. |
| [specs/archive/](specs/archive/) | Histórico de mudanças concluídas, com decisões e evidências. |
| [specs/shared/](specs/shared/) | Convenções de arquitetura, API, testes e processo. |
| [specs/system/](specs/system/) | Descrição vigente do estado do sistema. |
| [apps/frontend/](apps/frontend/) | Aplicações frontend organizadas por web, smartphone e desktop. |
| [docs/adr/](docs/adr/) | Decisões arquiteturais duradouras. |
| [.agents/skills/](.agents/skills/) | Catálogo completo das skills locais utilizadas pelo agente. |

## Ciclo de entrega

Cada alteração segue o fluxo abaixo:

1. **Especificação** — definição da proposta, SPEC, design e tarefas.
2. **Revisão da SPEC** — confirmação de escopo e critérios verificáveis.
3. **Implementação** — produção limitada ao que foi aprovado.
4. **Revisão da implementação** — verificação de aderência à SPEC.
5. **Validação** — execução de testes e registro de evidências.
6. **Aprovação** — consolidação dos gates de qualidade.
7. **Encerramento** — atualização do estado vigente, arquivamento e commit rastreável.

O frame utiliza payload com substitution, `ETB` e CRC-16-CCITT. Uma leitura da biblioteca serial pode conter ACK, NAK, EOT, um frame inteiro ou partes de vários frames; o leitor deve preservar bytes excedentes.

## Comandos cobertos

Cada comando deve possuir uma SPEC própria em [specs/changes/2026-09-09-066-lib-pinpad-abecs-go/](specs/changes/2026-09-09-066-lib-pinpad-abecs-go/), com payload, resposta, estados, timeout, cancelamento, logging, redaction e critérios de aceite.

| Grupo | Comandos |
| --- | --- |
| Ciclo de vida | `CAN`, `OPN`, `CLO`, `CLX`, `RST` |
| Informações | `GIX` |
| Display | `DSP`, `DEX`, `MNU`, `DSI` |
| Multimídia | `MLI`, `MLR`, `MLE` |
| Tabelas EMV | `TLI`, `TLR`, `TLE` |
| Teclas | `GKY` |
| Transação/cartão | `GCX`, `GTK`, `GOX`, `FCX` |
| PIN | `GPN` |
| Infraestrutura | cancelamento de leitura serial e logging SPE/PP/RSP |

Quando um comando estiver apenas parcialmente convertido, a SPEC deve declarar explicitamente o subconjunto implementado e o que permanece pendente. Não devem ser inventados campos que o manual atribui a outro comando.

## Segurança e redaction

Nenhum log, erro, fixture ou documento deve conter dados reais de cartão ou material criptográfico.

São sempre sensíveis:

- PAN;
- trilhas e dados de tarja;
- PIN e PIN block;
- KSN e WKENC;
- dados EMV sensíveis;
- chaves, KSEC, IV e material RSA/AES.

O rastro de comunicação é opt-in. Quando habilitado:

- `SPE` registra bytes enviados pelo software;
- `PP` registra bytes recebidos do pinpad;
- `RSP` correlaciona comando e status após o parse completo;
- `GPN` e respostas sensíveis de `GCX` têm payload integralmente redigido;
- abertura, fechamento e erros de I/O são registrados sem expor segredos.

## Arquitetura

```text
Consumidor Go
	|
PinpadService
	|
CommandQueue / Worker único
	|
SerialPort (porta própria)
	|
go.bug.st/serial
	|
Pinpad ABECS físico
```

Estrutura principal:

```text
apps/desktop/libpinpadabecsgo/
├── cmd/libpinpadabecsgo/        executável de validação
├── internal/domain/
│   ├── model/                   modelos e contratos
│   ├── protocol/                framing, CRC e controles ABECS
│   ├── parser/                  respostas ABECS e BER-TLV
│   ├── command/                 catálogo e builders
│   ├── queue/                   contratos de fila
│   ├── session/                 posse lógica
│   ├── state/                   status ABECS
│   └── error/                   erros tipados
├── internal/application/service/ fachada de orquestração
├── internal/infrastructure/
│   ├── serial/                  adaptador real e adaptadores de teste
│   ├── config/                  configuração
│   ├── logging/                 slog e tracer SPE/PP/RSP
│   └── worker/                  fila e worker
└── internal/utilitario/         CRC, TLV e bytes
```

## Configuração local

Variáveis:

| Variável | Obrigatória | Padrão | Descrição |
| --- | --- | --- | --- |
| `PORTA_PINPAD` | recomendada | `COM3` no modelo | porta serial do pinpad |
| `PINPAD_BAUDRATE` | não | `19200` | velocidade serial |
| `PINPAD_TIMEOUT` | não | `30s` | timeout de leitura/operação |
| `LOG_LEVEL` | não | `INFO` | nível do `slog` |

No Windows, a porta real pode ser `COM1` até `COMn`. No Linux, pode ser `/dev/ttyS*`, `/dev/ttyUSB*` ou `/dev/ttyACM*`.

Para execução local, consultar o README interno do módulo e o script [start_aplicacao.bat](apps/desktop/libpinpadabecsgo/start_aplicacao.bat), quando disponível. A configuração é temporária para o processo; não gravar credenciais ou configurações permanentes no sistema.

## Desenvolvimento e validação

Executar os comandos a partir de `apps/desktop/libpinpadabecsgo/`:

```text
gofmt -w .
go build ./...
go vet ./...
go test ./...
go test ./... -coverprofile=coverage.out

go test -race ./...
```

Os testes unitários podem usar adaptadores determinísticos para CRC, framing, parser, fila e validação de erros. Isso não substitui a validação de integração:

1. conectar um pinpad físico compatível;
2. identificar a porta serial correta;
3. executar abertura e comunicação real;
4. exercitar o comando da SPEC correspondente;
5. capturar somente logs redigidos;
6. registrar versão do dispositivo, porta, comando, resultado e código de saída em `validation.md`;
7. nunca registrar PAN, PIN, KSN, trilhas ou chaves.

Se o hardware não estiver disponível, registrar a limitação. Não marcar cenário físico como aprovado com base somente em fake ou mock.

## Processo Spec Driven

A Change principal está em [specs/changes/2026-09-09-066-lib-pinpad-abecs-go/](specs/changes/2026-09-09-066-lib-pinpad-abecs-go/).

Fluxo obrigatório:

1. inventariar o legado Java/JNI/C;
2. criar ou atualizar a SPEC individual do comando;
3. revisar e aprovar a SPEC;
4. implementar apenas o contrato aprovado;
5. executar testes unitários e verificações Go;
6. revisar a implementação contra a SPEC;
7. validar com pinpad físico quando o cenário exigir hardware;
8. registrar evidências em `reviews/` e `validation.md`;
9. aprovar, atualizar `specs/system/` e somente então arquivar/commitar.

Arquivos importantes:

Depois do seu teste, use [Executar mudança Spec Driven](.github/prompts/executar-mudanca-spec-driven.prompt.md) para continuar a mesma mudança pelas fases restantes até o commit.

## Como contribuir ou iniciar uma nova API
| Arquivo | Finalidade |
| --- | --- |
| [AGENTS.md](AGENTS.md) | regras obrigatórias do repositório |
| [spec.md](specs/changes/2026-09-09-066-lib-pinpad-abecs-go/spec.md) | contrato geral da biblioteca |
| [DESIGN.md](specs/changes/2026-09-09-066-lib-pinpad-abecs-go/DESIGN.md) | arquitetura e decisões |
| [tasks.md](specs/changes/2026-09-09-066-lib-pinpad-abecs-go/tasks.md) | execução e pendências |
| [resumo_continuar_projeto.txt](specs/resumo_continuar_projeto.txt) | contexto consolidado para continuidade por outro modelo |
| [specs/shared/process/workflow.md](specs/shared/process/workflow.md) | etapas e gates da Change |
| [apps/desktop/libpinpadabecsgo/](apps/desktop/libpinpadabecsgo/) | implementação Go |

1. Crie uma pasta em [specs/changes/](specs/changes/) com o próximo identificador e um nome descritivo.
2. Use os modelos em [specs/templates/](specs/templates/) para registrar a proposta, a SPEC, o design e as tarefas.
3. Solicite e registre a revisão da SPEC antes de gerar qualquer código.
4. Gere o módulo localmente, implemente somente o escopo aprovado e crie os testes previstos.
5. Registre revisão, validação, aprovação e evidências antes de arquivar a mudança.

Consulte [NotasProjeto.md](NotasProjeto.md) para instruções completas de criação, testes, execução e limpeza de um módulo Java Quarkus.

- conversão integral das funções Java/JNI/C;
- logging SPE/PP/RSP completo;
- parser funcional de GCX;
- GIX e capacidades de display;
- cancelamento de leitura com contexto;
- status especial de TLI;
- TransactionGCX completo, quando especificado;
- GTK, GOX e FCX;
- OPN/CLO/CLX e comunicação segura RSA/AES;
- resolução dos achados IMP-REV-001 a IMP-REV-015;
- testes de cobertura, race e validação física.

- Frontend: http://localhost:2000/login (login demonstrativo root/root).
- API: http://localhost:1000/produtos.
- Backend: executar apps/backend/produtobase/testar_aplicacao.bat.
- Frontend: executar apps/frontend/web/produtobase/start_aplicacao_frontend.bat.
- Testes backend: configurar JAVA_HOME para a instalação Java 17 e executar Maven verify no módulo.
- Testes frontend: npm test e npm run build no projeto frontend.
- Smoke com os serviços ativos: node scripts/smoke.mjs no frontend. Cria e remove somente um produto temporário.

Evidências da conclusão real: specs/archive/2026-09-06-003-concluir-execucao-produto-base/validation.md. A API permanece demonstrativa, sem autenticação de servidor.

## Referências

- [Arquitetura do backend Java](specs/shared/architecture/backend-java.md)
- [Convenções REST](specs/shared/api/rest-conventions.md)
- [Estratégia de testes](specs/shared/testing/testing-strategy.md)
- [Catálogo de skills locais](.agents/skills/README.md)
- [Estado vigente do sistema](specs/system/README.md)
- [Histórico das mudanças](specs/archive/)
- [Auditoria de segurança](docs/security-audit/relatorio-auditoria-seguranca.pdf)
- [Change 066 — lib-pinpad-abecs-go](specs/changes/2026-09-09-066-lib-pinpad-abecs-go/)
- [Resumo para continuidade](specs/resumo_continuar_projeto.txt)
- [Regras do agente](AGENTS.md)
- [Skills locais](.agents/skills/)
- [Histórico de mudanças](specs/archive/)
## Créditos

**Responsável:** Rômulo Penha


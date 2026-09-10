# Proposta: 066-lib-pinpad-abecs-go

## Status
`RASCUNHO`

## Responsável e data

- Responsável: equipe do projeto
- Data: 2026-09-09

## Referências

- `AGENTS.md`
- `specs/shared/process/workflow.md`
- `specs/shared/architecture/backend-golang.md`
- `specs/shared/testing/golang-testing.md`
- Skills Golang de layout, concorrência, contexto, segurança, segurança defensiva e tratamento de erros.
- Requisitos comportamentais do protocolo ABECS fornecidos para esta Change; nenhuma fonte legada será armazenada no repositório.
- Manual ABECS v2.12, de 11-abr-2019, fornecido como fonte normativa para esta Change. Uma revisão posterior somente poderá ser usada quando registrada e aprovada na SPEC do comando correspondente.

## Problema e objetivo

Criar a biblioteca Go reutilizável `lib-pinpad-abecs-go`, responsável exclusivamente pelo domínio de comunicação com dispositivos compatíveis com o protocolo ABECS através de comunicação serial Windows/Linux, convertendo integralmente o comportamento que estiver dentro do escopo do legado Java + JNI + C.

A biblioteca será a base para futuras Changes de bridge HTTP, WebSocket, UI e integrações. Esta Change não criará servidor, API ou interface de usuário.

## Escopo

- Módulo Go `br.com.romulopenha/lib-pinpad-abecs-go` em `apps/desktop/libpinpadabecsgo/`.
- Arquitetura Clean Architecture, DDD e Ports and Adapters.
- Adaptador serial com `go.bug.st/serial`, sem CGO.
- Configuração por ambiente, modelo de pinpad, estados, erros e catálogo de comandos/status.
- CRC-16-CCITT, substitution, packet builder, leitura de resposta, parser ABECS e parser BER-TLV.
- Implementação completa de serial, CRC, framing, parsers, `SessionManager`, `CommandQueue`, estados e comandos CAN/OPN/GIX/CLO.
- Implementação Go dos builders, parsers e fluxos de comunicação especificados individualmente para CAN, OPN, CLO/CLX, GIX, DSP, DEX, MNU, DSI, MLI, MLR, MLE, TLI, TLR, TLE, GCX, GTK, GOX, FCX, GKY, GPN e RST, preservando contratos, status, cancelamento e redaction sem expor dados sensíveis.
- Fachada de biblioteca equivalente ao `PinpadService`, com configuração, ciclo de vida, estados, comandos de display, imagem, tabelas EMV, transações GCX, leitura de teclas, reset e captura de PIN.
- Executável de validação em `cmd/libpinpadabecsgo`.
- Script `start_aplication.bat` para teste local sem privilégios administrativos, com configuração temporária da porta serial e binário gerado no diretório do módulo.
- Testes unitários, testes com transporte serial determinístico para componentes puros, cobertura mínima de 80% e validação de integração com pinpad físico real para os critérios de comunicação.

## Fora de escopo

REST, HTTP, WebSocket, DNS, Windows Service, Linux daemon, instalador, Docker, UI, SDK cliente, CORS, TLS, upload/download, multipart, autenticação, rate limit e API de comando hexadecimal bruto.

## Impactos e riscos

- Os anexos C++ serão referência comportamental, não tradução literal.
- O acesso serial será isolado atrás de interface para permitir testes sem hardware.
- Operações do hardware serão serializadas por uma fila FIFO com um worker por instância.
- Dados PAN, trilhas, PIN e EMV sensível não serão registrados.
- A compatibilidade real com pinpad físico exige validação manual registrada. Fakes determinísticos podem testar framing, CRC, parsers, fila e erros, mas não podem ser usados para declarar que uma transação ou comando físico foi convertido com sucesso.

## Rastreabilidade por comando

Cada comando incluído no escopo deverá ter uma SPEC própria, com payload, resposta, status, timeout, cancelamento, retransmissão, logging/redaction e critérios de aceite. A lista mínima é: `CAN`, `OPN`, `CLO`, `CLX`, `GIX`, `DSP`, `DEX`, `MNU`, `DSI`, `MLI`, `MLR`, `MLE`, `TLI`, `TLR`, `TLE`, `GKY`, `GCX`, `GTK`, `GOX`, `FCX`, `GPN` e `RST`. Logging e cancelamento serial possuem SPEC transversal própria.

Nenhum comando poderá ser considerado convertido apenas porque seu nome existe no catálogo Go. O status da conversão deve distinguir builder, parser, fluxo serial, fachada e validação física.

## Critérios para aprovação da SPEC

- [ ] Contrato desta proposta refletido em `spec.md`, `DESIGN.md` e `tasks.md`.
- [ ] Módulo, diretório, dependência serial e escopo confirmados.
- [ ] Interfaces, modelos, erros, comandos e critérios de aceite definidos.
- [ ] Nenhum componente de servidor ou transporte HTTP incluído.
- [ ] Revisão da SPEC aprovada formalmente.

## Revisão de escopo solicitada

Os requisitos detalhados fornecidos pelo usuário ampliam a Change além dos contratos anteriormente previstos. A nova SPEC deverá ser revisada e aprovada novamente antes da implementação desses fluxos. A autorização final para arquivamento continuará dependendo da validação manual do usuário.

Nenhum arquivo legado `.h`, `.hpp`, `.c`, `.cpp`, `.cc` ou `.cxx` fará parte desta Change. A fonte de verdade será exclusivamente a documentação da Change, especialmente `spec.md`, `DESIGN.md` e `tasks.md`. Os diretórios locais `.gocache`, `.gomodcache` e `.bin` são artefatos de execução e não deverão ser versionados.

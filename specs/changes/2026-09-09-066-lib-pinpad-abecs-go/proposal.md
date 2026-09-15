# Proposta: 066-lib-pinpad-abecs-go

Autor: Rômulo Penha

## Status
`SPEC_APROVADA`

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

Criar a biblioteca Go reutilizável `lib-pinpad-abecs-go`, responsável exclusivamente pela integração de comunicação com dispositivos compatíveis com o protocolo ABECS v2.12, através de comunicação serial Windows/Linux.

A biblioteca será a base para futuras Changes de bridge HTTP, WebSocket, UI e integrações. Esta Change não criará servidor, API ou interface de usuário.

## Escopo

- Módulo Go `br.com.romulopenha/lib-pinpad-abecs-go` em `apps/desktop/libpinpadabecsgo/`.
- Arquitetura Clean Architecture, DDD e Ports and Adapters.
- Adaptador serial com `go.bug.st/serial`, sem CGO.
- Configuração por ambiente, modelo de pinpad, estados, erros e catálogo de comandos/status.
- Logging operacional estruturado e rastro serial ABECS persistido em destino
  canônico e observável, com `open`, `close`, `SPE`, `PP` e `RSP`, caminho
  absoluto informado ao operador e redação obrigatória de dados sensíveis.
- Comunicação segura ABECS conforme `spec-protocolo-seguro.md`: OPN seguro,
  negociação RSA de 2048 bits, `KSEC` temporária, pacotes AES-CBC e
  encerramento por CLO, sempre dependentes de confirmação no dispositivo.
- CRC-16-CCITT, substitution, packet builder, leitura de resposta, parser ABECS e parser BER-TLV.
- Implementação completa de serial, CRC, framing, parsers, `SessionManager`, `CommandQueue`, estados e comandos CAN/OPN/GIX/CLO.
- Implementação Go dos comandos listados em `spec-conformidade-abecs-v212.md`; RST fica excluído por não existir no ABECS 2.12.
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
- Um caminho de log relativo ao diretório de trabalho pode criar arquivos
  homônimos e induzir diagnóstico incorreto; o utilitário local deverá resolver
  um único caminho absoluto a partir da raiz do módulo e exibi-lo antes do menu.
- A compatibilidade real com pinpad físico exige validação manual registrada. Fakes determinísticos podem testar framing, CRC, parsers, fila e erros, mas não podem ser usados para declarar que uma transação ou comando físico foi convertido com sucesso.

## Rastreabilidade por comando

Cada comando incluído possui SPEC própria. A lista normativa e a exclusão de RST constam em `spec-conformidade-abecs-v212.md`.

Nenhum comando poderá ser considerado convertido apenas porque seu nome existe no catálogo Go. O status da conversão deve distinguir builder, parser, fluxo serial, fachada e validação física.

## Critérios para aprovação da SPEC

- [ ] Contrato desta proposta refletido em `spec.md`, `DESIGN.md` e `tasks.md`.
- [ ] Módulo, diretório, dependência serial e escopo confirmados.
- [ ] Interfaces, modelos, erros, comandos e critérios de aceite definidos.
- [ ] Nenhum componente de servidor ou transporte HTTP incluído.
- [ ] Revisão da SPEC aprovada formalmente.

## Aditivo multimídia de 2026-09-14

Este aditivo inclui os comandos ABECS `LMF` e `DMF` na fachada e no menu local,
e recuperação da comunicação após timeout para impedir que respostas atrasadas
sejam atribuídas ao comando seguinte. Os limites normativos de ACK (2 segundos,
três tentativas) e resposta não bloqueante (10 segundos) permanecem inalterados.
Os comandos adicionais e a recuperação só poderão ser implementados após a
revisão formal deste aditivo.

Os requisitos adicionais de LMF, DMF e recuperação foram revisados em
`reviews/2026-09-14-spec-review.md`; o padding A8 de LMF em
`reviews/2026-09-14-spec-review-2.md`; e a compatibilidade de tipo desconhecido
em MLI em `reviews/2026-09-14-spec-review-3.md`. Os contratos estão
`SPEC_APROVADA`. O encerramento e arquivamento continuam condicionados à
validação física e aprovação formal da Change.

### Correção do tipo desconhecido em MLI

Em conformidade com a seção 6.6.1 do manual, o host deverá permitir o envio de
um arquivo cujo tipo não seja reconhecido, preenchendo `SPE_MFINFO.B1` com um
valor reservado (`RUF`, `00h`). A compatibilidade do conteúdo só é avaliada pelo
pinpad no uso via DSI. Nome, tamanho e CRC continuam sujeitos à validação local.

### Recuperação escalonada e evidência visual — revisão de 2026-09-14

O teste físico mostrou que, depois de um timeout real de MLE, as três tentativas
automáticas de CAN e as três tentativas posteriores de Reset podem terminar sem
EOT. Fechar e abrir a porta restabeleceu o diálogo: a nova abertura recebeu EOT
no CAN inicial e concluiu OPN com status `000`. Para evitar que o consumidor
precise reiniciar manualmente a conexão, a recuperação deverá escalar de CAN/EOT
para uma reconexão serial controlada quando o handshake falhar.

A operação que sofreu timeout não será reenviada automaticamente, pois seu
resultado no firmware é indeterminado. A reconexão somente restabelece o canal
para uma consulta explícita posterior, como LMF ou DSI.

Uma resposta `DSI000` comprova a aceitação do comando pelo firmware, mas não
comprova isoladamente que os pixels esperados apareceram. A validação física de
DSI exige também confirmação visual do operador; o CLI deverá distinguir
"comando aceito" de "imagem visualmente confirmada" em sua orientação.

Nenhum arquivo legado `.h`, `.hpp`, `.c`, `.cpp`, `.cc` ou `.cxx` fará parte desta Change. A fonte de verdade será exclusivamente a documentação da Change, especialmente `spec.md`, `DESIGN.md` e `tasks.md`. Os diretórios locais `.gocache`, `.gomodcache` e `.bin` são artefatos de execução e não deverão ser versionados.


## Correção normativa de 2026-09-13

A implementação deve seguir `spec-conformidade-abecs-v212.md`. O comando RST
foi excluído porque não existe no manual ABECS 2.12; cancelamento e limpeza de
comunicação usam CAN/EOT.

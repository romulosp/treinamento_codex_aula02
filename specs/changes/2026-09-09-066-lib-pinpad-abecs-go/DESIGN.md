# Design: 066-lib-pinpad-abecs-go

Autor: Rômulo Penha

## Finalidade da Change

Esta Change integra a comunicação com pinpad ABECS v2.12 em uma biblioteca local, headless e sem rede, destinada a ser consumida por outro processo Go. A biblioteca conversa diretamente com um pinpad ABECS físico por porta serial; não existe bridge HTTP, WebSocket, UI ou servidor dentro deste módulo.

O manual ABECS v2.12 fornecido para a Change é a fonte normativa do protocolo. A documentação e o comportamento validado no pinpad são a base de referência, não devem ser copiados nem traduzidos literalmente. Cada comando deve ser rastreado para sua própria SPEC, implementação, teste e evidência de hardware.

## Contexto

A Change integra a comunicação com pinpad ABECS v2.12 em uma biblioteca Go headless, reutilizável e independente de HTTP, WebSocket, UI e frameworks de desktop.

## Referências

- `spec.md` e `proposal.md` desta Change.
- `specs/shared/architecture/backend-golang.md`.
- `specs/shared/testing/golang-testing.md`.
- Skills Golang de concorrência, contexto, segurança, safety, erros e layout.
- Manual ABECS v2.12 fornecido para a Change e requisitos funcionais documentados nesta Change. Revisões diferentes do manual devem ser registradas na SPEC do comando e não podem ser misturadas silenciosamente.

## Decisões

1. O perfil é `desktop`, porém o produto desta Change é biblioteca interna reutilizável, consumida por import Go. "Sem service" refere-se exclusivamente à ausência de processo servidor externo; a fachada de aplicação (`internal/application/service`), equivalente a `PinpadService`, existe como único ponto de orquestração interno e não abre rede.
2. O módulo será `br.com.romulopenha/lib-pinpad-abecs-go` em `apps/desktop/libpinpadabecsgo/`.
3. A composição será feita por construtores Go e injeção manual, sem contêiner DI.
4. O domínio será dividido em `model`, `protocol`, `parser`, `command`, `queue`, `session`, `state` e `error`.
5. A porta serial será uma interface própria; `go.bug.st/serial` ficará restrita ao adaptador de infraestrutura.
6. Um worker por instância será o proprietário do transporte. A fila usa capacidade 100, FIFO e retorno imediato de `ErrQueueFull`. `Enqueue` nunca bloqueia o produtor; `Submit` é a única operação bloqueante, aguardando o resultado do comando ou o cancelamento do contexto.
7. Contextos serão propagados a todas as operações potencialmente bloqueantes. O shutdown cancelará o worker e fechará a porta de forma idempotente.
8. O relógio do `SessionManager` será injetável para testes determinísticos de expiração.
9. `slog` será usado na infraestrutura de logging com redaction por padrão. O
   tracer SPE/PP/RSP será uma instância injetada e desabilitada por padrão; a
   composição do utilitário local usará uma única instância. Sem
   `PINPAD_LOG_FILE`, o destino será o caminho absoluto
   `<raiz-do-módulo>/logs/LogPinpadAbecs.txt`, nunca um caminho relativo ao
   diretório de trabalho. A raiz do módulo é o ancestral que contém o `go.mod`
   cujo `module` é `br.com.romulopenha/lib-pinpad-abecs-go`; a busca parte do
   diretório de trabalho e do diretório do executável. Se a raiz não puder ser
   determinada, o CLI falhará antes do menu e solicitará um
   `PINPAD_LOG_FILE` explícito.
10. Comandos avançados terão builders, parsers e fluxos seriais definidos na RF-012, sem acoplar o núcleo a bridges HTTP, WebSocket ou UI.
11. O catálogo de comandos será documentado uma SPEC por comando. Uma implementação parcial deverá declarar explicitamente o subconjunto suportado e não poderá simular campos de resposta que pertençam a outro comando.
12. A validação automatizada de componentes puros não substitui a validação de transporte e comportamento com pinpad físico real.
13. A comunicação segura ABECS será isolada do framing em claro e seguirá `spec-protocolo-seguro.md`: OPN negocia `KSEC` temporária pelo perfil RSA de 2048 bits comprovado no legado; pacotes protegidos seguem o formato normativo AES-CBC; CLO encerra e limpa a sessão. CLX é visual e não fecha a porta, mas também encerra a sessão segura no pinpad, conforme manual ABECS v2.12, seção 6.4.5.
14. `PinpadConfig` inicia em `COM7`; uma `PORTA_PINPAD` não vazia no ambiente do processo tem precedência. O script local não deve sobrescrever essa escolha.
15. `GCX` é a própria iniciação da captura transacional. A fachada não envia
    um GCX preliminar com data/hora zeradas. O builder usa parâmetros ABECS
    tipados e codifica `SPE_GCXOPT` como N5, conforme a seção 3.7.1 do manual.
16. O loop de recepção de um comando blocante reconhece `NTM000` pelo envelope
    próprio, sem interpretar sua mensagem como TLV, e continua até o `RSP_ID`
    do comando solicitado. O CLI traduz explicitamente as escolhas de interface
    e visibilidade do valor para `SPE_GCXOPT`.
17. O GCX usa diretamente o contexto fornecido pelo consumidor, pois é um
    comando blocante cuja duração pode superar o timeout genérico da
    configuração. No CLI, esse contexto começa depois que todas as entradas da
    opção 11 forem coletadas e validadas.
18. A opção 19 do CLI cria `GTKRequest` a partir de uma escolha explícita. O
    modo em claro omite método, trilhas e índice, produzindo `GTK000`; o modo
    criptografado usa DUKPT TDES DAT#3/ECB (`50`) e exige índice 00..99 já
    provisionado no pinpad.
19. Após GTK bem-sucedido no modo em claro, somente o CLI local registra os
    campos de trilha interpretados em uma linha `GTK_CLEAR`. O frame `PP`
    permanece redigido e o modo criptografado não produz essa linha.
20. Para esse registro, trilha 1 é ASCII. Trilhas 2 e 3 são expandidas da
    representação BCD/nibble do ABECS 2.12; `D` é exibido como `=` e filler
    `F` final é removido. Dados inválidos interrompem o registro.
21. A opção 20 deriva as redes disponíveis das entradas N6 de
    `GCXResponse.AidTableInfo`, conserva o valor N12 do GCX e coleta método,
    índice e WKENC condicional antes de criar o contexto de 60 segundos do GOX.
22. A opção 21 conserva a resposta GOX válida e cria `FCXRequest` a partir do
    resultado real da comunicação com a rede. Aprovação e negação exigem ARC
    A2; falha de comunicação omite ARC. Dados EMV, lista de tags e timeout são
    entradas opcionais validadas antes de iniciar o contexto de 60 segundos.
    `PP_FCXRES` permanece exclusivamente no modelo de resposta.
23. LMF/DMF são operações ABECS tipadas na mesma fila; o parser conserva todos
    os valores repetidos de `PP_MFNAME`, e a apresentação de nomes normaliza
    ASCII para maiúsculas.
24. O timeout ou uma resposta de comando incompatível inicia recuperação CAN/EOT
    antes da próxima operação; falha de EOT marca a conexão fora de sincronia e
    inicia uma única reconexão serial controlada, sem repetir o comando original.
25. DSI é independente da carga na sessão atual. O status `000` confirma que o
    firmware aceitou o comando; a comprovação da renderização é uma evidência
    física separada fornecida pelo operador.

## Arquitetura e componentes

```text
apps/desktop/libpinpadabecsgo/
├── cmd/libpinpadabecsgo/       executável de validação
├── internal/domain/
│   ├── model/                  PinpadConfig, DeviceInfo, Response e modelos
│   ├── protocol/               constantes, framing e protocolo ABECS
│   ├── parser/                 resposta ABECS e BER-TLV
│   ├── command/                catálogo e contratos de comandos
│   ├── queue/                  contratos da fila
│   ├── session/                posse e expiração
│   ├── state/                  estados
│   └── error/                  erros sentinela/tipos
├── internal/application/service/ serviço principal
├── internal/infrastructure/
│   ├── serial/                 go.bug.st/serial e fake
│   ├── config/                 ambiente e defaults
│   ├── logging/                slog, tracer e redaction
│   └── worker/                 implementação FIFO/worker
└── internal/utilitario/        crc, tlv e bytes
```

Fluxo de uma operação:

```text
PinpadService -> CommandQueue -> Worker único -> SerialPort -> Pinpad
       |              |               |
    contexto       FIFO 100       resposta/parser
```

Fluxo físico detalhado:

```text
API Go tipada
    -> validação de argumentos
    -> fila FIFO da instância
    -> worker único
    -> montagem do payload do comando
    -> framing, substitution e CRC
    -> SPE pela porta serial
    -> PP em um ou mais chunks
    -> ACK/NAK/EOT e leitura do frame
    -> validação de CRC e parser
    -> RSP correlacionado com comando/status
    -> modelo Go ou erro tipado
```

O rastro SPE/PP/RSP é transversal e está especificado em `spec-logging.md`. Dados sensíveis devem ser redigidos antes de qualquer persistência do rastro.

O adaptador serial real é o único ponto que conhece os bytes efetivamente
aceitos na escrita e devolvidos na leitura; por isso, ele emite `SPE` e `PP`,
além de `open`, `close` e erros de I/O. O serviço informa ao adaptador o comando
ativo antes da escrita para correlação e redaction, e emite somente o `RSP`
após o parser conhecer o status. O CLI configura o destino, valida o marcador
de ativação e mostra o caminho absoluto antes de disponibilizar o menu.

Nenhum pacote de domínio importa a biblioteca serial, sistema operacional, logger concreto ou framework externo.

## Recuperação após timeout de comando

O serviço mantém o limite ABECS de 10 segundos para comandos não bloqueantes.
Ao esgotá-lo, ou detectar resposta de outro comando, o worker envia CAN e espera
EOT antes de liberar a fila. Se não confirmar EOT em três tentativas, marca a
sessão como fora de sincronia e executa uma única reconexão: fecha a porta, abre,
realiza o CAN/EOT inicial e envia OPN. A fila só volta ao estado aberto quando
todas essas etapas terminam com sucesso. O comando interrompido não é reenviado,
pois uma resposta ausente não prova que o firmware deixou de processá-lo. Se a
reconexão falhar, somente uma nova abertura explícita poderá restaurar o canal.

Esse desenho reproduz automaticamente o caminho físico que recuperou a COM7 em
2026-09-14, preservando a barreira contra respostas atrasadas e evitando exigir
do operador a sequência manual Close/Open.

## Alternativas e consequências

- **Biblioteca serial aprovada versus syscall próprio:** `go.bug.st/serial` reduz código específico de SO e mantém ausência de CGO; o adaptador preserva possibilidade de substituição.
- **Worker único versus goroutine por comando:** worker único garante ordenação e evita interleaving no pinpad; a capacidade limitada impede crescimento sem controle.
- **Mutex versus canais:** canais transportam comandos e resultados; mutexes protegem estado curto do serviço e sessão; não haverá mutex mantido durante I/O além do necessário para propriedade.
- **Biblioteca pública em `pkg` versus `internal`:** nesta estrutura a API de domínio permanece interna ao módulo para evitar compromisso público prematuro; futuras Changes podem extrair um pacote público.


## Correção de desenho normativo de 2026-09-13

A implementação técnica obedece `spec-conformidade-abecs-v212.md`. O transporte
passa a possuir política explícita de três tentativas, ACK/NAK de 2 segundos,
resposta não bloqueante de 10 segundos, NAK para frame inválido e handshake
CAN/EOT. OPN clássico e seguro são caminhos alternativos; CLO e CLX são
cifrados durante KSEC, embora suas respostas sejam claras. Builders deixam de
usar formatos posicionais inventados para comandos ABECS parametrizados. RST é
removido.

## Prazo da carga multimídia local

Separar a coleta de caminho/nome da execução da opção 16 em uma função testável
com callback de carga. Criar o contexto somente depois das perguntas, respeitar
o prazo do consumidor na biblioteca e reservar 100% de progresso à confirmação
MLE.

## Tipo desconhecido durante MLI — 2026-09-14

`BuildMLICommand` identifica PNG, JPG e GIF pela assinatura. Para conteúdo sem
assinatura reconhecida, preencherá `SPE_MFINFO.B1` com `00h` (RUF), preservando
nome, tamanho, CRC e os três bytes RUF finais. Isso mantém o carregamento opaco
ao formato; a validação de suporte pertence ao firmware no comando DSI.

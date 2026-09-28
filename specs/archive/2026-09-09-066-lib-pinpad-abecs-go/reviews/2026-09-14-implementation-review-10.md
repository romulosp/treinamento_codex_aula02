# Revisão da implementação: multimídia ABECS

**Data:** 2026-09-14  
**Escopo:** MLI/MLR/MLE/DSI, LMF/DMF, recuperação CAN/EOT e menu local.  
**SPECs:** `spec-command-mli.md`, `spec-command-lmf.md`,
`spec-command-dmf.md`, `spec-infra-serial-cancel.md` e
`spec-conformidade-abecs-v212.md`.

## Comparação com a SPEC

- MLI gera tamanho X4, CRC16, tipo B1 e três bytes RUF; assinatura desconhecida
  usa `B1=00h` e não é rejeitada antes da carga
  (`internal/domain/command/advanced.go:184-203`).
- MLR mantém blocos de até 995 bytes e MLE só finaliza a carga após sucesso dos
  blocos anteriores (`internal/application/service/service.go:708-750`). O
  progresso total é emitido depois de uma resposta MLE bem-sucedida.
- DSI transmite somente o nome A8; tamanho e suporte da imagem são verificados
  pelo dispositivo. Não há parâmetro ABECS de largura ou altura no DSI.
- Parser preserva cada valor repetido de `PP_MFNAME`, aceita o padding à direita
  do A8 publicado e devolve nomes válidos em maiúsculas
  (`internal/domain/parser/abecs.go:111-153`).
- LMF literal e DMF plural estão disponíveis no serviço e menu; nomes inválidos
  e lista vazia são rejeitados antes da serial, enquanto nomes válidos
  desconhecidos são enviados ao dispositivo (`internal/domain/command/advanced.go:165-181`,
  `internal/application/service/service.go:769-787`, `cmd/libpinpadabecsgo/main.go:371-395`).
- Timeout, exaustão de ACK, frame inválido e resposta incompatível acionam
  CAN/EOT; falha de recuperação bloqueia comandos comuns até Reset ou ciclo de
  fechamento/abertura. Operações já enfileiradas também revalidam o estado no
  worker (`internal/application/service/service.go:344-381, 961-1015, 1101-1210`).
- As opções 26/27 foram adicionadas. A opção 16 valida nome A8 e só inicia o
  prazo de carga após a entrada do caminho e do nome (`cmd/libpinpadabecsgo/main.go:371-395, 503-514`).

## Arquitetura e exposição de dados

O módulo segue a arquitetura aprovada, sem HTTP, WebSocket, banco ou endpoint de
rede. Os comandos passam pela fila serial existente. O conteúdo de mídia não é
incluído em logs; a matriz de logging inclui LMF/DMF e mantém MLR redigido.
Nenhum código gerado ou segredo foi introduzido nesta mudança.

## Divergências e limites

Não foi identificada divergência material no código em relação às SPECs citadas.
Os testes determinísticos não comprovam armazenamento, capacidade disponível,
limpeza de temporários, formato de imagem nem apresentação/centralização no
firmware; a validação em pinpad físico permanece pendente.

## Decisão

`IMPLEMENTACAO_APROVADA`

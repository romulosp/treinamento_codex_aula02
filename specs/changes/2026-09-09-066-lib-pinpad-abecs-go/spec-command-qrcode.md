# SPEC: 066-lib-pinpad-abecs-go — DisplayQRCode: aviso estruturado de posição

Autor: Rômulo Penha

## Status
`SPEC_APROVADA`

## Papel do recurso no projeto

Nesta Change, `DisplayQRCode` é uma fachada de geração/entrega de imagem. A geração do PNG é delegada a `QRCodeGenerator`; isso não comprova, por si só, que o pinpad exibirá o QR Code. A exibição física somente poderá ser declarada validada quando o PNG for carregado pelo fluxo de multimídia ABECS (`MLI`/`MLR`/`MLE`) e exibido pelo comando `DSI`, com SPEC e evidência próprias.

## Identificação

- pacote afetado: `internal/application/service` (`DisplayQRCode`, `QRCodeResult`)
- Esta SPEC é complementar a `spec.md` (RF-013 seção "Imagens e QR Code", CA-016a) e não substitui nenhum RF já aprovado; ela detalha e corrige exclusivamente o aviso de posicionamento não suportado.

## Motivação

**IMP-REV-012** — `QRCodeResult` possui o campo `PositionSupported`, mas não expõe um aviso ou motivo estruturado para `xPos`/`yPos` não suportados, contrariando o texto de CA-016a de `spec.md` ("`xPos`/`yPos` como não suportados de forma explícita no resultado").

## Referências e dependências

- `spec.md` RF-013 ("posicionamento customizado (`xPos`, `yPos`) não é suportado pelo protocolo ABECS documentado nesta SPEC; `DisplayQRCode` deverá ignorar esses parâmetros e retornar um aviso estruturado (campo específico do resultado), nunca de forma silenciosa no log").

## Limite de responsabilidade

- `DisplayQRCode` não deve abrir porta, enviar bytes ou simular uma resposta do pinpad.
- O método deve validar entrada, chamar o gerador injetado e devolver o PNG e o aviso de posicionamento.
- A integração com hardware pertence ao fluxo de multimídia e ao comando `DSI`.
- O aviso de posição não deve ser confundido com erro de comunicação.

## Requisitos funcionais

### RF-QR-001 — Campo de aviso estruturado

`QRCodeResult` deverá incluir um campo adicional, por exemplo `PositionWarning string`, preenchido sempre que `xPos != 0` ou `yPos != 0` forem informados pelo chamador, com um texto fixo e estável (não uma mensagem de log solta) descrevendo que o protocolo ABECS documentado nesta Change não suporta posicionamento customizado e que os valores foram ignorados. Quando `xPos == 0 && yPos == 0`, o campo permanece vazio.

### RF-QR-002 — Consistência com `PositionSupported`

`PositionSupported` permanece `false` nesta Change (nenhuma forma de posicionamento é suportada), independentemente dos valores de `xPos`/`yPos` informados. `PositionWarning` é o campo que diferencia "não suportado, mas o chamador não pediu posicionamento" de "não suportado, e o chamador pediu posicionamento".

### RF-QR-003 — Sem log implícito

Esta SPEC não adiciona nenhum log automático para o aviso; o aviso é somente um valor de retorno estruturado, cabendo ao consumidor decidir se e como registrar.

## Requisitos não funcionais

- Nenhuma mudança de comportamento para os demais campos já validados de `DisplayQRCode` (tamanho 50–320, margem 0–10, `ErrQRCodeGeneratorNotConfigured`).

## Regras de negócio

- `PositionWarning` só é preenchido quando `xPos` ou `yPos` forem diferentes de zero.

## Cenários e critérios de aceite

Os critérios de geração e validação de retorno podem ser testados sem hardware. O critério de exibição física deve ser validado separadamente com pinpad real através das SPECs de multimídia/`DSI`; não se deve afirmar que a geração local ocorreu no dispositivo apenas por executar `QRCodeGenerator`.

- [ ] **CA-QR-001:** `DisplayQRCode`, executado com gerador configurado e `xPos=0, yPos=0`, devolve `PositionWarning` vazio.
- [ ] **CA-QR-002:** `DisplayQRCode`, executado com `xPos=10` ou `yPos=10`, devolve `PositionWarning` não vazio, com `PositionSupported=false`.
- [ ] **CA-QR-003:** tamanho, margem e `ErrQRCodeGeneratorNotConfigured` são validados por teste automatizado do contrato da fachada.
- [ ] **CA-QR-004:** a exibição do PNG em pinpad físico é validada na SPEC de `MLI`/`MLR`/`MLE`/`DSI`, sem atribuir essa evidência exclusivamente ao método `DisplayQRCode`.

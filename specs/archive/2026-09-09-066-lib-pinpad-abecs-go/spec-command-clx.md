# SPEC: 066-lib-pinpad-abecs-go — Comando CLX

Autor: Rômulo Penha

## Status
`SPEC_APROVADA`

## Objetivo
Definir o comando de encerramento visual `CLX`, equivalente funcional a `CLO`,
mas capaz de deixar mensagem de formato livre ou mídia no display. `CLX` não
fecha a porta serial; no equipamento, porém, desativa comunicação segura e
limpa `KSEC`, conforme o manual ABECS v2.12, seção 6.4.5.

## Contrato

- O payload contém o identificador `CLX` e pode incluir `SPE_DSPMSG` (mensagem
  livre) e `SPE_MFNAME` (nome de mídia previamente carregada).
- Sem parâmetros, o comando limpa o display. Quando os dois parâmetros forem
  enviados e a mídia existir no dispositivo, `SPE_MFNAME` prevalece sobre
  `SPE_DSPMSG`.
- A resposta deve conter `RSP_ID=CLX` e `RSP_STAT`; o comando é não bloqueante
  mesmo quando a mídia possui animação ou vídeo.
- A mídia ou mensagem permanece no display até o próximo comando aplicável. A
  conclusão de `CLX` não altera o estado da porta ou da fila. Quando a
  comunicação segura estiver ativa, o pinpad a encerra e limpa `KSEC`; a
  resposta de `CLX` é devolvida em claro.
- A fachada deverá expor operação tipada, validar o resultado ABECS e propagar
  timeout, cancelamento, NAK e status como erros distintos. Não expor comando
  hexadecimal bruto.

## Dependências

`spec-command-clo.md`, `spec-command-dsi.md`, `spec-logging.md` e o manual
ABECS v2.12. A comunicação segura é definida exclusivamente em
`spec-protocolo-seguro.md`.

## Critérios de aceite

- [ ] Builder cobre display vazio, somente mensagem, somente mídia e a
  prioridade da mídia sobre a mensagem.
- [ ] Parser exige `RSP_ID=CLX`, interpreta `RSP_STAT` e distingue NAK,
  timeout, cancelamento e resposta inválida.
- [ ] Fake serial comprova que o comando retorna sem aguardar a duração da mídia.
- [ ] Teste com pinpad físico confirma mensagem e mídia compatível, quando esta
  capacidade estiver disponível no equipamento.
- [ ] Logs registram somente metadados não sensíveis.

## Referências
Manual ABECS v2.12, seção 3.2.7; `spec-command-clo.md`,
`spec-command-dsi.md` e `spec-logging.md`.

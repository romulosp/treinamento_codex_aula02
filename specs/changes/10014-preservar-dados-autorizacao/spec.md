# SPEC — 10014

Status: `SPEC_APROVADA` pela [REV-001](reviews/2026-10-14-spec-review.md).

## Requisitos REQUIRED

RF-01: manter GCX→GTK→GOX para 03/06 e GCX→GTK→GPN para 00/05.
GTK é obrigatório pelo contrato da aplicação, embora opcional no protocolo
ABECS (páginas 84–86 e 142). Não aplicar a hipótese de remover GTK.

RF-02: contexto Go deve reter os bytes validados de GTK: PAN criptografado
quando devolvido, trilhas 1/2/3, seus KSNs, KSN PAN e chave aleatória quando
existente. Preservar os dados disponíveis; não fabricar campos ausentes.
Manter seleção de trilhas existente 0111 nesta Change, sem ampliar o pedido PAN.
Reter também decisão/dados EMV GOX e PIN block/KSN de GPN ou GOX online.

RF-03: disponibilizar snapshot tipado no pacote interno Go apenas após GPN/GOX
concluído, para mesma operação/sessão. Consumidor futuro deve solicitar os dados
antes de FCX/fechamento. Não implementar chamada ao autorizador ou nova API pública
JSON/gomobile nesta Change. Campos privados, getters por cópia e Clear sincronizado;
nenhuma serialização automática, UI, arquivo ou log novo para esses dados.

RF-04: retenção limitada à sessão ativa. Sobrescrever dados controlados na
invalidação, falha, cancelamento, perda de transporte e fechamento. Resposta
tardia não restaura contexto. Snapshot transferido deve ser limpo pelo consumidor;
documentar que limpar o contexto não apaga cópias já transferidas.

RF-05: PIN segue três campos do properties; DATA GTK segue perfil externo.
Nenhum método/índice/WK fixo ou substituição automática. GTK011 não é resolvido
pela retenção; registrar configuração 30/08 e GIX anterior sem DATA08. Não
atribuir causa exclusiva sem evidência do equipamento. Homologação física
continua pendente da configuração/carga autorizada de chaves.

## Aceite

- AC-01: modos 00/03/05/06 preservam GTK e dados sintéticos exatos no snapshot
  após GPN/GOX, com PIN online e ausência legítima offline.
- AC-02: getters e snapshots sem alias; JSON vazio; Clear idempotente; rejeitar
  consulta prematura, operação alheia, contexto invalidado e GTK011/042.
- AC-03: limpeza real dos buffers controlados e resposta tardia cancelada;
  nenhuma trilha/PAN em logs e contratos de resumo existentes preservados.
- AC-04: testes/vet/integração e artefatos AAR/host verificados, revisão e
  validação registradas, memória atualizada. Não declarar hardware aprovado.

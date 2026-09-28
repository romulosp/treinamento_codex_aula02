# SPEC: 066-lib-pinpad-abecs-go — Comando DMF

## Status
`SPEC_APROVADA`

## Objetivo
Solicitar ao pinpad a exclusão de um ou mais arquivos multimídia pelo nome.

## Contrato
- A solicitação contém um ou mais parâmetros `SPE_MFNAME` (tag `0x001E`),
  cada um com nome A8 de oito caracteres alfanuméricos.
- O host rejeita lista vazia, nomes inválidos e payload que exceda os limites
  ABECS antes de escrever na porta serial.
- Nomes desconhecidos são permitidos: o pinpad os ignora e não retorna erro
  somente por estarem ausentes.
- O serviço interpreta e devolve o status ABECS do comando; o CLI aceita vários
  nomes separados por ponto e vírgula.
- O comando é serializado pela fila existente, respeita timeout/cancelamento e
  recupera a comunicação conforme `spec-infra-serial-cancel.md`.

## Critérios de aceite
- [ ] Builder reproduz byte a byte o vetor publicado com dois nomes.
- [ ] Lista vazia, nome inválido e excesso de tamanho são rejeitados antes da
  serial.
- [ ] Nomes inexistentes não são transformados em erro local.
- [ ] Status ABECS, timeout e falha de transporte são propagados.
- [ ] A opção do menu confirma conclusão somente após status `000`.

## Referências
Manual ABECS v2.12, seções 3.4.5 e 6.6.5, p. 101 e 206;
`spec-conformidade-abecs-v212.md`, `spec-infra-serial-cancel.md`.

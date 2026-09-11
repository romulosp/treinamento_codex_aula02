# Linha de base técnica — Change 066

## Finalidade

Este documento consolida as fontes, os limites e os gates usados para revisar
tecnicamente a conversão do legado Java + JNI + C em uma biblioteca Go ABECS.
Ele é um artefato de rastreabilidade e checklist: não altera o contrato da
Change, não aprova a SPEC e não autoriza implementação.

## Fonte de verdade e evidências

| Categoria | Fonte | Uso na Change |
| --- | --- | --- |
| Contrato principal | `proposal.md`, `spec.md`, `DESIGN.md`, `tasks.md` | Define escopo, requisitos e critérios de aceite. |
| Contratos específicos | `spec-command-*.md`, `spec-logging.md`, `spec-infra-serial-cancel.md`, `spec-protocolo-seguro.md` | Define o comportamento observável por comando e os contratos transversais. |
| Norma de protocolo | Manual ABECS v2.12, quando disponibilizado e identificado para o dispositivo | Resolve framing, tags, sequências e restrições que não possam ser inferidas da SPEC. |
| Evidência de legado | Classes Java e fontes JNI/C fornecidas fora deste repositório | Mapeia comportamento pré-existente; não deve ser copiado ao repositório. |
| Resposta à pendência | `D:\desenvolvimento\ia\estudo\pinpad-abecs\resposta_pendencia_001.txt` | Evidência externa para os contratos de GTK, CLX, GOX e FCX; não contém instruções operacionais para esta Change. |

Nenhuma chave, PAN, trilha, PIN, PIN block, KSN, WKENC, IV ou criptograma de
exemplo será copiado desta evidência para fixtures, logs, documentação ou
testes da Change.

## Invariantes da solução

- Biblioteca Go headless, serial-only, sem REST, HTTP, WebSocket, listener TCP,
  daemon ou UI.
- Um worker por instância e uma operação de hardware por vez, coordenados pela
  fila FIFO e por `context.Context`.
- Framing ABECS, CRC-16-CCITT, substitution, leitura em chunks e bytes
  excedentes são responsabilidades transversais do transporte.
- Posse lógica exclusiva, expiração de sessão após 300 segundos e shutdown
  idempotente são requisitos de domínio, não de transporte de rede.
- Redaction é obrigatória para qualquer operação que possa transportar dados de
  cartão, PIN, chaves ou material criptográfico.
- Fakes comprovam componentes determinísticos; sucesso de comunicação e
  comportamento funcional do dispositivo exigem evidência de pinpad físico.

## Matriz de rastreabilidade prioritária

| Tema | Evidência externa identificada | Contrato da Change | Estado para revisão |
| --- | --- | --- | --- |
| GTK | Só pode ser usado após operação de captura elegível; pode retornar PAN, trilhas e KSNs, que são sensíveis. | `spec-command-gtk.md` | Contrato, referência e classificação incluídos em `spec.md`. |
| CLX | Equivalente visual não bloqueante de CLO; não fecha a porta, mas encerra comunicação segura ativa no pinpad. | `spec-command-clx.md` | Contrato corrigido contra manual ABECS v2.12, seção 6.4.5. |
| GOX | Continuação EMV distinta de GCX/GTK, podendo retornar PIN block, KSN e TLV. | `spec-command-gox.md`, `spec-command-gpn.md`, `spec-logging.md` | Contrato, referência e classificação incluídos em `spec.md`. |
| FCX | Finalização transacional e resultados próprios, sem preencher `GCXResponse`. | `spec-command-fcx.md` | Contrato, referência e matriz de rastreabilidade incluídos em `spec.md`. |
| Comunicação segura | O legado possui operações JNI de RSA e AES; seu uso somente é permitido no formato normativo ABECS. | `spec-protocolo-seguro.md` | Escopo decidido em RF-018; formato detalhado permanece sujeito ao contrato transversal e à validação física. |
| Configuração serial | Legado opera serial 8N1 com baud rate configurável; a Change exige ambiente e `PinpadConfig`. | RF-002 de `spec.md` | `COM7` é default; `PORTA_PINPAD` não vazia definida no ambiente tem precedência. |

## Checklist de desbloqueio da revisão da SPEC

- [x] Referenciar explicitamente `spec-command-gtk.md` em RF-004, RF-010 e na matriz de rastreabilidade de `spec.md`.
- [x] Referenciar explicitamente `spec-command-clx.md` no contrato de ciclo de vida de `spec.md`.
- [x] Referenciar `spec-command-gox.md` e `spec-command-fcx.md` no contrato de comandos e na matriz de rastreabilidade de `spec.md`.
- [x] Declarar em `spec.md` se comunicação segura RSA/AES/KSEC pertence a esta Change e, se pertencer, apontar `spec-protocolo-seguro.md` como contrato complementar.
- [x] Definir em RF-002 a precedência entre `PORTA_PINPAD` e o default de `PinpadConfig.Port`, incluindo critério de aceite testável.
- [x] Listar, nas referências do `spec.md`, todas as SPECs individuais e transversais da Change.
- [ ] Atualizar `proposal.md`, `DESIGN.md` e `tasks.md` somente se a decisão alterada no contrato principal exigir alinhamento.
- [ ] Executar nova revisão formal da SPEC; somente `SPEC_APROVADA` permite seguir para implementação.

## Sequência de execução após a aprovação da SPEC

1. Atualizar e revisar `implementation-plan.md` com riscos de serial, concorrência, redaction, comunicação segura e validação física.
2. Corrigir os achados de implementação já registrados em `reviews/2026-09-09-implementation-review.md` antes de nova revisão da implementação.
3. Implementar e testar cada comando conforme sua SPEC individual, mantendo GTK, GOX, FCX e GCX em modelos e parsers distintos.
4. Validar `go test`, cobertura medida, `go test -race`, `go vet` e a comunicação no pinpad físico; registrar evidências sanitizadas em `validation.md`.
5. Somente após revisão da implementação, validação e aprovação formais, atualizar a documentação de sistema, arquivar a Change e preparar o commit rastreável.

## Situação atual

O contrato principal foi aprovado na revisão
`reviews/2026-09-10-spec-review-4.md`. A implementação está autorizada pela
SPEC, mas a entrega existente continua reprovada até que os achados
`IMP-REV-001` a `IMP-REV-015` sejam corrigidos, revisados e validados.

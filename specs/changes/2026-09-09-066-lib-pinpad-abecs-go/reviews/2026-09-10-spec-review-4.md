# Revisão da SPEC — 066-lib-pinpad-abecs-go

Autor: Rômulo Penha

**Data:** 2026-09-10  
**Skill aplicada:** `spec-review`  
**Escopo:** `proposal.md`, `spec.md`, `DESIGN.md`, `tasks.md`,
`implementation-plan.md`, SPECs individuais afetadas e a resolução dos achados
de `2026-09-10-spec-review-3.md`.

## Método

Revisão cruzada do contrato principal, das SPECs de GTK, CLX, GOX, FCX e
comunicação segura, do mapeamento para o protocolo ABECS v2.12 e da evidência
externa sanitizada fornecida para a Change. Foram conferidos escopo, sequência,
dependências, dados sensíveis, critérios de aceite e rastreabilidade. Não houve
alteração de código, testes, dependências ou artefatos de validação.

## Resolução dos achados anteriores

### REV-001 — GTK sem definição técnica no contrato principal — Resolvido

- **Evidência:** `spec.md` agora referencia `spec-command-gtk.md` nas
  dependências, em RF-004, RF-010, RF-012, na matriz do legado e na fachada.
  A SPEC individual define pré-condição, seleção de trilhas, criptografia,
  resposta, KSN e redaction.
- **Impacto:** GTK passa a possuir contrato rastreável e separado de GCX.
- **Recomendação:** nenhuma pendência de SPEC; validar em implementação e
  hardware conforme CA-024.

### REV-002 — CLX sem definição técnica no contrato principal — Resolvido

- **Evidência:** `spec-command-clx.md` passou a definir `CLX` como comando
  visual não bloqueante, com mensagem/mídia, prioridade de mídia e resposta
  `RSP_ID`/`RSP_STAT`. RF-010 e RF-012 fazem remissão explícita.
- **Impacto:** foi eliminada a classificação incorreta de CLX como encerramento
  seguro; a operação não fecha porta nem sessão criptográfica.
- **Recomendação:** implementar e testar independentemente do fluxo seguro.

### REV-003 — GOX e FCX sem subseção e rastreabilidade — Resolvido

- **Evidência:** RF-012.10, as matrizes de legado/fachada e as SPECs
  `spec-command-gox.md` e `spec-command-fcx.md` definem pré-condições,
  parâmetros, respostas, TLV, PIN/KSN, Issuer Script Results e redaction.
- **Impacto:** GOX, FCX e GCX possuem modelos e parsers semanticamente
  separados.
- **Recomendação:** manter CA-024 como evidência obrigatória da implementação.

### REV-004 — Comunicação segura sem decisão de escopo — Resolvido

- **Evidência:** RF-018 inclui comunicação segura no escopo e aponta
  `spec-protocolo-seguro.md` como contrato prevalente. O perfil inicial é RSA
  de 2048 bits e expoente público 65537, conforme legado e manual adotado;
  formato, IV, padding e sequência continuam condicionados ao contrato
  transversal e à validação no dispositivo.
- **Impacto:** não é permitida criptografia inferida, nem uso de CLX como
  encerramento seguro; CLO realiza o encerramento aplicável.
- **Recomendação:** validar o perfil somente com pinpad de laboratório e
  evidência sanitizada, conforme CA-025.

### REV-005 — Precedência da porta serial ambígua — Resolvido

- **Evidência:** RF-002 e CA-020 definem `COM7` como default de
  `PinpadConfig`; `PORTA_PINPAD` não vazia no ambiente do processo tem
  precedência. Valor vazio ou demais valores inválidos retornam erro de
  configuração.
- **Impacto:** a configuração é determinística e testável, inclusive pelo
  script local sem persistência no sistema.
- **Recomendação:** alinhar a implementação existente e o README ao contrato
  aprovado durante a fase de implementação.

### REV-006 — Referências incompletas — Resolvido

- **Evidência:** a seção "Referências e dependências" de `spec.md` enumera as
  SPECs individuais e transversais da Change.
- **Impacto:** a rastreabilidade formal entre contrato principal e contratos
  complementares está completa.
- **Recomendação:** incluir novos contratos na mesma lista quando forem
  criados em Change futura.

## Pontos verificados sem achados

- A biblioteca permanece headless, serial-only e sem transporte de rede.
- A fila, posse de sessão, cancelamento, redaction e validação física continuam
  requisitos explícitos e verificáveis.
- Os novos critérios CA-024 e CA-025 cobrem a separação entre os fluxos de
  cartão e o protocolo seguro.
- `TransactionGCX` completo continua deliberadamente fora da implementação
  desta Change, como `ErrNotImplemented`, sem serialização inventada.

## Conclusão

Não restaram ambiguidades ou lacunas bloqueantes no contrato revisado. Os
achados de implementação `IMP-REV-001` a `IMP-REV-015` permanecem abertos e
não são resolvidos por esta revisão; deverão ser tratados na fase de
implementação antes de nova revisão da entrega.

**Resultado:** `SPEC_APROVADA`

**Próxima fase autorizada:** implementação, com atualização do plano técnico
e correção dos achados de implementação existentes.

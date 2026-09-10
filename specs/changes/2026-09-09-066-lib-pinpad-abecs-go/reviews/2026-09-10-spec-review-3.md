# Revisão da SPEC (execução do orquestrador Spec Driven) — 066-lib-pinpad-abecs-go

**Data:** 2026-09-10
**Skill aplicada:** `spec-review`
**Escopo:** `spec.md`, `proposal.md`, `DESIGN.md`, `tasks.md`, `implementation-plan.md` e consistência com as SPECs individuais referenciadas (`spec-command-*.md`, `spec-logging.md`, `spec-infra-serial-cancel.md`, `spec-protocolo-seguro.md`).
**Motivo da reavaliação:** `spec.md` teve conteúdo alterado após a aprovação anterior (`2026-09-09-spec-review-2.md`, `SPEC_APROVADA`) — foram adicionadas a descrição executiva, a matriz RF-015.1, o RF-016 e o RF-017 — e retornou a `RASCUNHO`, exigindo nova revisão formal antes de qualquer implementação.

## Método

Leitura integral de `spec.md`, `proposal.md`, `DESIGN.md`, `tasks.md` e `implementation-plan.md`, com verificação cruzada de completude entre: (a) o escopo declarado em `proposal.md`; (b) a matriz obrigatória de comandos do RF-015.1; (c) as seções técnicas RF-004, RF-010 e RF-012, que deveriam detalhar contratos, builders, parsers e fluxos de cada comando em escopo; (d) as SPECs individuais já existentes no diretório da Change.

## Achados

### REV-001 — `GTK` não possui nenhuma definição técnica em `spec.md`

- **Severidade:** Alta.
- **Evidência:** `GTK` aparece exclusivamente na tabela do RF-015.1 (grupo "Transação/cartão"). Não há menção a `GTK` em RF-004 (lista de artefatos `command`), em RF-010 (lista de comandos que devem ter "contratos, tipos, builders, parsers e fluxos"), em nenhuma das dez subseções do RF-012, nem na "Matriz de especificação dos arquivos fornecidos". `spec-command-gtk.md` existe como arquivo próprio, mas `spec.md` não o referencia em nenhum ponto.
- **Impacto:** `spec.md` alega, na própria descrição executiva, que "cada comportamento relevante do legado deverá ser classificado como convertido, parcialmente convertido, fora de escopo formal ou pendente de uma Change específica". `GTK` não recebe nenhuma dessas classificações no corpo da SPEC principal, apesar de estar no escopo declarado em `proposal.md` e em `tasks.md` ("Implementar GTK, GOX e FCX em modelos próprios, sem misturar dados com GCX").
- **Recomendação:** adicionar `GTK` a RF-004 e RF-010, criar uma referência explícita em RF-012 apontando `spec-command-gtk.md` como fonte de verdade do contrato (no mesmo padrão usado em RF-007 para `spec-infra-serial-cancel.md`), e incluir `GTK` na "Matriz de especificação dos arquivos fornecidos".

### REV-002 — `CLX` não possui nenhuma definição técnica em `spec.md`

- **Severidade:** Alta.
- **Evidência:** `CLX` aparece somente na tabela do RF-015.1 (grupo "Controle") e no escopo de `proposal.md`. RF-010, que detalha explicitamente `CAN`, `OPN`, `GIX` e `CLO`, não menciona `CLX`. `spec-command-clx.md` existe, mas não é referenciado por `spec.md`.
- **Impacto:** mesmo problema do REV-001: um comando em escopo fica sem classificação e sem contrato de referência no documento principal.
- **Recomendação:** adicionar `CLX` a RF-010 (mesmo grupo de `CAN`/`OPN`/`GIX`/`CLO`) e referenciar `spec-command-clx.md` como fonte de verdade do contrato detalhado, no mesmo padrão de RF-007.

### REV-003 — `GOX` e `FCX` são citados de passagem, mas não possuem subseção técnica em RF-012 nem linha na matriz de rastreabilidade

- **Severidade:** Média.
- **Evidência:** `GOX` e `FCX` aparecem em RF-004 ("... `GCX`, `GOX`, `FCX`, `GKY`, `GPN` e `RST`") e em RF-010 ("Para DSP, DEX, MNU, DSI, MLI, MLR, MLE, GCX, GOX, FCX, GKY, GPN, RST, TLI, TLR e TLE criar os contratos..."). Diferente de `GKY` (subseção 6), `GCX` (subseção 7), `GPN` (subseção 8) e `RST` (subseção 9), RF-012 não possui nenhuma subseção dedicada a `GOX` ou `FCX`, e a "Matriz de especificação dos arquivos fornecidos" não tem nenhuma linha equivalente a `sendGOXCommand`/`sendFCXCommand`.
- **Impacto:** inconsistência de nível de detalhe entre comandos do mesmo grupo funcional ("Transação/cartão"); um implementador não encontra, no corpo principal, nenhum ponto de entrada padronizado para o contrato de `GOX`/`FCX`, apenas o nome do comando.
- **Recomendação:** adicionar referência explícita, em RF-012, a `spec-command-gox.md` e `spec-command-fcx.md` como fonte de verdade dos contratos (mesmo padrão de RF-007), e incluir as duas linhas correspondentes na matriz de rastreabilidade.

### REV-004 — Comunicação segura (RSA/AES/OPN seguro/KSEC) não possui nenhuma decisão de escopo em `spec.md`

- **Severidade:** Alta.
- **Evidência:** `spec-protocolo-seguro.md` existe no mesmo diretório da Change e `tasks.md` já prevê "Implementar OPN/CLO/CLX e o protocolo seguro RSA/AES conforme `spec-protocolo-seguro.md`" na seção "Conversão integral do legado". Nenhum RF de `spec.md` (RF-001 a RF-017) menciona comunicação segura, RSA, AES, KSEC ou `OPN` seguro. O documento também não lista `spec-protocolo-seguro.md` em "Referências e dependências".
- **Impacto:** viola o próprio critério de completude declarado na descrição executiva de `spec.md` (toda funcionalidade do legado deve ser classificada como convertida, parcial, fora de escopo ou pendente de Change futura). Diferente do tratamento dado a `TransactionGCX` — que foi explicitamente excluído do escopo desta Change, com justificativa registrada em RF-013 — a comunicação segura simplesmente não é mencionada, deixando ambíguo se está dentro do escopo (e portanto exige contrato) ou fora dele (e portanto exige uma exclusão explícita, como a de RF-015 para a bridge HTTP).
- **Recomendação:** adicionar um RF específico que decida explicitamente o escopo da comunicação segura nesta Change — dentro do escopo com contrato mínimo e remissão a `spec-protocolo-seguro.md`, ou formalmente deferida para uma Change futura, com o mesmo nível de justificativa usado para `TransactionGCX`.

### REV-005 — RF-002 não resolve a regra entre `PORTA_PINPAD` obrigatória e o default `COM3` de `PinpadConfig`

- **Severidade:** Média.
- **Evidência:** RF-002 declara `PORTA_PINPAD` como variável obrigatória, mas também define que `PinpadConfig.Port` tem default `COM3`. O próprio RF-002 reconhece a tensão e apenas adia a decisão: "A obrigatoriedade de `PORTA_PINPAD` na execução deverá ser resolvida de modo consistente com o default do modelo e documentada no README." Nenhum critério de aceite (`CA-001` a `CA-023`) cobre esse comportamento.
- **Impacto:** um requisito funcional não pode delegar sua própria definição para a fase de implementação/README sem critério de aceite objetivo; isso impede um teste verificável do comportamento de configuração.
- **Recomendação:** decidir agora, em RF-002, qual comportamento prevalece (por exemplo: "obrigatória" significa que a ausência da variável é erro de configuração, e o default `COM3` do modelo aplica-se apenas quando `PinpadConfig` for construído programaticamente sem passar pelo carregamento de ambiente) e adicionar um critério de aceite correspondente.

### REV-006 — `spec.md` não lista, em "Referências e dependências", as SPECs individuais que cita no corpo do texto

- **Severidade:** Baixa.
- **Evidência:** o corpo de `spec.md` cita nominalmente `spec-command-gcx.md` (RF-012.7) e `spec-infra-serial-cancel.md` (RF-007), mas a seção "Referências e dependências" não inclui nenhum arquivo `spec-command-*.md`, `spec-logging.md`, `spec-infra-serial-cancel.md` ou `spec-protocolo-seguro.md`.
- **Impacto:** reduz a rastreabilidade formal entre o documento principal e as SPECs individuais que efetivamente compõem o contrato da Change.
- **Recomendação:** adicionar essas referências à lista, mesmo que de forma agregada (por exemplo, "todas as SPECs individuais em `spec-command-*.md`, `spec-logging.md`, `spec-infra-serial-cancel.md` e `spec-protocolo-seguro.md` desta Change").

## Pontos verificados sem achados

- RF-001 mantém a distinção clara entre "sem service" (ausência de servidor externo) e a fachada interna `PinpadService`, consistente com `DESIGN.md`.
- RF-009/RF-014 mantêm a semântica única de `Enqueue`/`Submit` sem duplicidade, como corrigido na revisão anterior.
- RF-013 mantém a exclusão de `SendRawCommand`, o contrato `QRCodeGenerator` injetável e o stub `ErrNotImplemented` de `TransactionGCX`, todos consistentes com `RF-006`, `RF-015` e as SPECs individuais correspondentes.
- RF-016 (execução local sem privilégios administrativos) e RF-017 (documentação Go conforme `golang-documentation`) são objetivos, verificáveis e cobertos por critérios de aceite próprios (`CA-020` a `CA-023`).
- A matriz RF-015.1 é internamente consistente com a lista de rastreabilidade de `proposal.md`.

## Conclusão

`spec.md` evoluiu de forma consistente na maior parte do documento, mas quatro comandos em escopo (`GTK`, `CLX`, `GOX`, `FCX`) e a comunicação segura carecem de definição técnica própria ou de referência explícita às SPECs individuais já existentes, e RF-002 deixa uma regra de configuração sem decisão. Isso contraria o próprio padrão de completude que a SPEC declara adotar (classificação obrigatória de todo comportamento do legado).

A implementação desta Change permanece bloqueada até a resolução dos achados REV-001 a REV-006. Nenhuma alteração de código, mudança de status para `SPEC_APROVADA`, implementação, validação, aprovação ou commit foi executada nesta fase, conforme as regras da Skill `spec-review`.

**Resultado:** `REPROVADA`

**Fase de retorno:** Fase 1 — SPEC (`spec.md`), para incorporar as correções acima antes de nova revisão.

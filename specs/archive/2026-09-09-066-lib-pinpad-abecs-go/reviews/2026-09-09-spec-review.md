# Revisão da SPEC — 066-lib-pinpad-abecs-go

**Data:** 2026-09-09  
**Resultado:** `REPROVADA`  
**Fase de retorno:** SPEC

## Escopo revisado

Foram revisados `proposal.md`, `spec.md`, `DESIGN.md`, `tasks.md`, `implementation-plan.md` existente e o workflow canônico. A SPEC ampliada agora cobre protocolo, serial, parsers, builders, fachada, fila, sessão e exclusão da bridge HTTP.

## Achados

### REV-001 — Contradição sobre a existência de service

- **Severidade:** Bloqueante
- **Evidência:** RF-001 declara que a entrega será “sem ... service”, mas RF-013 exige uma fachada equivalente a `PinpadService` e o DESIGN mantém `internal/application/service`.
- **Impacto:** não é possível determinar se a implementação deve criar ou não a fachada de aplicação.
- **Recomendação:** esclarecer RF-001 para distinguir “sem servidor/serviço externo” de “fachada de biblioteca”, ou remover RF-013 e o componente de serviço.

### REV-002 — Dependência para geração de QR Code não definida

- **Severidade:** Alta
- **Evidência:** RF-013 exige `DisplayQRCode` e permite “dependência Go aprovada ou adaptador injetável”, mas não define biblioteca, interface mínima, formato de saída, política de erro, licenciamento ou testes.
- **Impacto:** a implementação pode tomar decisões incompatíveis ou adicionar dependência sem aprovação.
- **Recomendação:** decidir entre uma interface `QRCodeGenerator` sem dependência nesta Change ou aprovar uma dependência concreta e documentar contrato e testes.

### REV-003 — Contrato de `SendRawCommand` permanece ambíguo

- **Severidade:** Alta
- **Evidência:** RF-013 inclui `SendRawCommand`, mas afirma que não será criada API genérica de bytes para consumidores externos; RF-015 também exclui API de comando hexadecimal bruto. Não está definido se o método será interno, restrito a comandos tipados ou removido.
- **Impacto:** risco de expor uma superfície fora do escopo aprovado.
- **Recomendação:** definir visibilidade e validação do método, ou removê-lo da fachada pública.

### REV-004 — Semântica de `CommandQueue.Enqueue` e `Dequeue` precisa ser única

- **Severidade:** Alta
- **Evidência:** RF-009 descreve `Enqueue`, worker e `Command.Execute`, enquanto RF-014 menciona `Dequeue`/worker interno, callback e resultado tipado. Não está definido se `Enqueue` aguarda execução, retorna um handle/futuro ou apenas confirma inserção.
- **Impacto:** contratos de cancelamento, capacidade e shutdown não podem ser testados de forma determinística.
- **Recomendação:** definir assinaturas Go, propriedade de resultados, comportamento de contexto cancelado e erro de comandos removidos por `Clear`/`Stop`.

### REV-005 — Contrato de GCX possui campos sem serialização especificada

- **Severidade:** Alta
- **Evidência:** RF-013 lista `TransactionGCX` com AIDs, cashback, moeda, EMV, lista de tags, máscara e mensagem, mas RF-012 detalha apenas builder simplificado de valor, data, hora e opções. Não há ordem, formato, comprimento, tags, regras de validação ou resposta para os campos adicionais.
- **Impacto:** não é possível implementar a transação completa sem inventar protocolo.
- **Recomendação:** especificar tabela de parâmetros GCX, tags, tipos, limites, serialização e mapeamento de resposta; ou limitar a Change ao subconjunto comprovado.

### REV-006 — Critérios de aceite não refletem a nova matriz completa

- **Severidade:** Média
- **Evidência:** CA-014, CA-016 e CA-018 são genéricos e não definem cenários verificáveis para QR Code, GCX completo, fachada de configuração, `Clear`, `Stop`, `ForceRelease`, status TLI `020` e proteção de respostas sensíveis.
- **Impacto:** validação poderá declarar sucesso sem comprovar todos os métodos especificados.
- **Recomendação:** adicionar cenários de aceite por grupo funcional com entradas, saída esperada e erros.

### REV-007 — Plano de implementação está desatualizado

- **Severidade:** Média
- **Evidência:** `implementation-plan.md` ainda descreve “somente contratos/modelos/builders para comandos avançados” e não contempla RF-013, RF-014 e RF-015.
- **Impacto:** o plano preparatório não representa a SPEC atual e não pode orientar implementação ou validação.
- **Recomendação:** atualizar o plano somente após a SPEC ser corrigida e aprovada.

## Conclusão

`REPROVADA`

A implementação está bloqueada até a resolução dos achados REV-001 a REV-005. Nenhuma alteração de código, mudança de status para `SPEC_APROVADA`, implementação, validação, aprovação ou commit foi executada nesta fase.

# Revisão da SPEC — comando GCX ABECS

Autor: Rômulo Penha

**Data:** 2026-09-12

**Skill aplicada:** `spec-review`

**Escopo:** `proposal.md`, `spec.md`, `spec-command-gcx.md`, `DESIGN.md`,
`tasks.md`, `implementation-plan.md`, log físico de 2026-09-12 e manual ABECS
v2.12, seção 3.7.1, páginas 129 a 134.

## Método

O contrato foi confrontado com o frame descrito no manual, o status físico
`011`, a construção atual do comando e a sequência executada pela fachada.
Foram preservados o escopo reduzido de valor/data/hora/opções e a redação
integral de GCX.

## Achados tratados

### REV-GCX-001 — GCX preliminar não existe no protocolo

- **Severidade:** bloqueante.
- **Evidência:** o manual define o próprio GCX como início da transação e torna
  `SPE_TRNDATE` e `SPE_TRNTIME` mandatórios. A fachada enviava antes um GCX com
  ambos como `000000`, que recebeu `ST_INVPAR` (`011`).
- **Impacto:** a compra era interrompida antes de enviar os dados digitados.
- **Tratamento:** `PurchaseGCX` passa a emitir exatamente um GCX; API,
  configuração e menu de inicialização artificial foram removidos do contrato.

### REV-GCX-002 — Builder não emitia parâmetros ABECS

- **Severidade:** bloqueante.
- **Evidência:** a seção 3.7.1 e os exemplos do manual codificam cada parâmetro
  com ID e comprimento binários. O builder concatenava opção, data, hora e
  valor como texto posicional.
- **Impacto:** o pinpad não reconhecia os campos e podia responder `011` mesmo
  com valores válidos.
- **Tratamento:** RF-GCX-000 exige `SPE_AMOUNT=0x0013`,
  `SPE_TRNDATE=0x0015`, `SPE_TRNTIME=0x0016` e `SPE_GCXOPT=0x0017` no envelope
  ABECS.

### REV-GCX-003 — Representação de GCXOPT era ambígua

- **Severidade:** importante.
- **Evidência:** o manual define `SPE_GCXOPT` como N5 (`0xxxx`, `1xxxx`,
  `x0xxx`, `x1xxx`, `xx000`); o contrato aceitava um byte sem definir a
  serialização.
- **Impacto:** CTLS e exibição do valor poderiam gerar valor incompatível.
- **Tratamento:** o contrato define `00000` sem CTLS e `10000` com CTLS,
  preservando os três bits RUF como zero.

### REV-GCX-004 — Estratégia de validação impedia regressão unitária

- **Severidade:** importante.
- **Evidência:** a SPEC exigia pinpad real para todos os cenários, embora
  construção byte a byte e quantidade de escritas sejam componentes puros.
- **Impacto:** a serialização incorreta não possuía critério automatizado.
- **Tratamento:** CA-GCX-008 a CA-GCX-010 exigem testes unitários; os cenários
  que dependem de cartão continuam exigindo hardware real.

## Verificação final

- A proposta permanece coerente e não precisa de alteração de escopo.
- O contrato atualizado define entradas, bytes esperados, sequência e erros
  antes da implementação.
- Dados GCX continuam integralmente redigidos no rastro.
- `TransactionGCX` completo permanece fora do escopo e retorna
  `ErrNotImplemented`.

## Conclusão

Os achados bloqueantes foram resolvidos documentalmente. O contrato é
verificável e aderente à seção 3.7.1 do manual ABECS v2.12.

**Resultado:** `SPEC_APROVADA`

**Próxima fase autorizada:** implementar RF-GCX-000/RF-GCX-000.1 e executar
CA-GCX-008 a CA-GCX-010.

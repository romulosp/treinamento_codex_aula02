# Proposta: 10017-logging-semantico-abecs

## Objetivo

Evoluir o logging da biblioteca ABECS — **sem** alterar protocolo, comunicação,
builders, parsers ou cálculo de CRC — para adicionar interpretação semântica dos
frames SPE/PP: HEX + ASCII/texto + detalhamento campo a campo, preservando a
redação de dados sensíveis e criptografados.

## Escopo

- Decoração **observacional** do tracer existente (`internal/infrastructure/logging`).
- Catálogo semântico **estendido** a partir do PDF ABECS 2.12 (nomes oficiais).
- Modelo de classificação **A/B/C** (visível / sensível / criptografado).
- Sinalização de substituição DC3 e de sessão segura DC2.
- Matriz de cobertura dos **46 comandos** documentados no PDF.

## Fora de escopo

- Alterar protocolo, enquadramento, CRC, builders, parsers ou comunicação.
- Reimplementar a máquina de estados ACK/NAK/retransmissão no logger.
- Decodificar/interpretar payload de comandos sensíveis (MLR, TLR, GCX, GTK, GOX, FCX, GPN) ou criptografados (DC2).
- Expor PIN, chaves, KSN, trilhas, PAN, dados EMV.

## Baseline

- SPEC 066 arquivada: `specs/archive/2026-09-09-066-lib-pinpad-abecs-go/` (inclui `spec-logging.md` e `spec-conformidade-abecs-v212.md`).
- Código: `apps/desktop/libpinpadabecsgo/`.
- Memória do protocolo: `docs/Pinpad-Abecs-Protocolo-de-Comunicacao-e-Funcionamento-v212190411212/abecs-212-referencia-protocolo.md`.

## Estado

`IMPLEMENTADA` — SPEC v2.0.0 aprovada (`2026-10-15-spec-review-2.md`) e implementada
em `apps/desktop/libpinpadabecsgo/internal/infrastructure/logging/` (catálogo,
classificador, hexdump, decodificador e integração `Tracer`). Testes/vet/build aprovados.


## Revisão 3

Status do contrato: `SPEC_APROVADA`. Completar logging detalhado no caminho BAT/Bridge solicitado pelo usuário, com transporte bruto opaco e observador limitado, conforme spec.md RF-016..020 e ADR-001. Execução desta revisão: `IMPLEMENTADA`, com evidências em validation.md e teste humano pendente.

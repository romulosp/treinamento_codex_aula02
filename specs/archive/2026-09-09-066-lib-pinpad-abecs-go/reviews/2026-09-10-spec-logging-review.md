# Revisão da SPEC — logging detalhado de comunicação (spec-logging.md)

**Data:** 2026-09-10
**Escopo:** `spec-logging.md`, complementar a `spec.md` (RF-011, RF-012.1) da Change `2026-09-09-066-lib-pinpad-abecs-go`.

## Verificação

- Não contradiz nenhum requisito de `spec.md`; apenas detalha RF-011/RF-012.1 já aprovados.
- Não introduz servidor, rede ou transporte fora de escopo (RF-015 permanece intacto).
- Define claramente o identificador de correlação (`<id>` lógico de sessão de porta), já que não há handle de SO portátil em Go.
- Resolve a ambiguidade entre rastro cru por chamada de leitura (fidelidade ao legado) e a necessidade de status decodificado, introduzindo a linha `RSP CMD=<cmd> STATUS=<status>` como correlação sem duplicar bytes já registrados.
- Adota redação total do payload para comandos sensíveis (`GPN`, `GCX` e respostas com tags de trilha/PAN/PIN block/KSN) em vez de redação parcial por campo — decisão registrada explicitamente no próprio RF-L006, reduzindo risco de vazamento por erro de cálculo de offset.
- Critérios de aceite (CA-L001 a CA-L010) são objetivos e verificáveis por teste, incluindo concorrência (`go test -race`) e ausência de overhead quando desligado.

## Conclusão

Nenhum achado bloqueante. Contrato é claro, testável, e consistente com o restante da Change.

**Resultado:** `SPEC_APROVADA`

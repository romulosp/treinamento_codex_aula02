# Análise de consolidação — 075 diagnosticopinpad

## Fontes e responsabilidades

| Fonte | Responsabilidade consolidada |
|---|---|
| 068 | laboratório mínimo, AAR, endpoint, lifecycle, operação e gates |
| 069 | catálogo de 28 opções, fachada nomeada, formulários e redaction |
| 072, via complementos 068/069 | preflight, estado fiel, erros, visibilidade, logs e reabertura |
| 074 | baseline canônica de core, PBRG, Bridge, serial, ownership, launcher, tracer e REST |

## Fronteira final

A 075 não substitui a 074. Ela substitui 068/069 como contrato do aplicativo e
consome a 074 como plataforma. Assim:

- UI/repository/estado do laboratório pertencem à 075;
- protocolo, Bridge e recursos Windows pertencem à 074;
- os efeitos observáveis atravessam a fronteira e são critérios da 075;
- REST permanece existente na 074, mas não é usado pelo Android.

## Correções de consolidação

### C-075-01 — 072 não era apenas uma referência

Os complementos das fontes tornam normativos preflight, estado triplo,
categorias, painel de erro, correlação, logger metadata-only, cleanup e gates
scripted/físico. Esses itens agora aparecem expressamente na SPEC.

### C-075-02 — 074 não deve ser duplicada

Duplicar a baseline inteira criaria duas fontes para PBRG, REST, serial e
ownership. A solução é dependência normativa, Gate P0 e critérios end-to-end.

### C-075-03 — Precedência da porta

A fonte 072 distinguia BAT físico e entrypoint direto. A aprovação humana da
074 adotou regra posterior mais estrita: `PORTA_PINPAD` obrigatória em todo
processo físico, sem COM fixa/fallback. A 075 segue essa supersessão explícita.

### C-075-04 — Evidência antiga não valida contrato novo

Testes executados contra a primeira revisão continuam registrados, mas não
comprovam os novos CA-075-06 a CA-075-13 e Gates 3–7.

## Divergências a reconciliar

| ID | Situação | Tratamento |
|---|---|---|
| D-075-001 | `config.Load()` exige `PORTA_PINPAD` e `DefaultConfig()` não possui COM | conforme decisão C-003 da 074; validação formal pendente |
| D-075-002 | evidência anterior não prova preflight sem Acquire/COM | inspecionar, corrigir se necessário e testar end-to-end |
| D-075-003 | evidência anterior não prova estado triplo e painel de erro completo | confrontar ViewModel/UI com seções 7 e 8 |
| D-075-004 | Gates 3/4/5 estavam pendentes e Gate 7 não existia | executar após reconciliação |
| D-075-005 | AAR consumido não foi regenerado no ambiente | resolver toolchain/NDK ou registrar bloqueio sem declarar reprodução |

## Decisão de estado

SPEC corrigida: `SPEC_APROVADA`.  
Implementação: `IMPLEMENTAÇÃO A RECONCILIAR`.  
Validação: pendente de novas evidências.

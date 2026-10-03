# Rastreabilidade — 075 diagnosticopinpad consolidado

## Fontes

| ID | Fonte |
|---|---|
| SRC-068 | `specs/archive/2026-09-27-068-diagnosticopinpad/` |
| SRC-069 | `specs/archive/2026-09-27-069-diagnosticopinpad-functional-lab/` |
| SRC-072 | `specs/archive/2026-09-27-072-corrigir-abertura-bridge-android/` |
| DEP-074 | `specs/changes/074-pinpad-android-bridge-stack/` |

## Requisitos consolidados

| Tema | Origem | Destino 075 |
|---|---|---|
| estrutura Android e endpoint | SRC-068 RF-001/002 | seções 2 e 4; CA-075-01/06 |
| ações mínimas e fachada | SRC-068 RF-003/005 | seções 5/6; CA-075-02/03 |
| correlação e lifecycle | SRC-068 RF-006/007 | seções 8/10; CA-075-05/11 |
| scripted, observabilidade e docs | SRC-068 RF-008/009/010 | seções 9/11/12; CA-075-12/18/20 |
| catálogo de 28 opções | SRC-069 seção 2 | seção 5; CA-075-02/03 |
| fachada ampliada e parâmetros | SRC-069 seções 3/4 | seções 5/6/12; CA-075-03/15 |
| privacidade | SRC-069 seção 5 | seções 9/12; CA-075-16 |
| stack Go/Bridge | DEP-074 seções 3–14 | Gate P0; CA-075-04/05/06/12/13 |

## Critérios 072 aplicáveis

| Fonte 072 | Tratamento 075 |
|---|---|
| CA-072-01/02 | Gate P0 e CA-075-06; 074 supersede o fallback direto e exige ambiente em todo processo físico |
| CA-072-03 | Gate P0/3; readiness somente após bind |
| CA-072-04 | Gate 3; seleção/reverse explícitos |
| CA-072-05 | seção 4.3 e CA-075-07 |
| CA-072-06 | seção 9 e CA-075-12 |
| CA-072-07 | seção 8 e CA-075-10/16 |
| CA-072-08 | seção 7 e CA-075-08 |
| CA-072-09 | seções 7/8 e CA-075-09/10 |
| CA-072-10/11 | seção 10, Gate 7 e CA-075-13/17 |
| CA-072-12 | Gate 4 e CA-075-18 |
| CA-072-13 | Gate 5 e CA-075-19 |
| CA-072-14 | Gates 1/2 e CA-075-14/17 |
| CA-072-15 | análise, rastreabilidade e CA-075-20 |

## Evidências

Evidências das fontes são históricas. Evidências da primeira revisão da 075
permanecem em `validation.md`, mas não fecham critérios alterados. Cada CA atual
deve apontar para execução nova antes da validação formal.

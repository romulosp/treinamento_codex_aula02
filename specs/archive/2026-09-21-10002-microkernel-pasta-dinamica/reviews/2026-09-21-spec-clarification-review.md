# Revisão da clarificação da SPEC — 10002

## Resultado

`SPEC_APROVADA`

## Decisão

As alterações em `spec.md` e `DESIGN.md` apenas tornam explícitos requisitos já
presentes nas Changes 10000/10002 e no diagrama aprovado: validação estruturada
do manifesto, rejeição auditável desde a primeira pré-condição e contenção de
falhas recuperáveis de carregamento. Não houve ampliação funcional, mudança de
módulos ou alteração do modelo de confiança.

## Rastreabilidade

- `spec.md` mantém a fronteira externa → quarentena → repositório privado →
  classloader.
- `schemaVersion`, SemVer, `priority`, `capabilities` e `dependencies` passam a
  ser campos verificáveis do manifesto, conforme a Change 10000.
- `LinkageError` é tratado como falha recuperável de carga; erros fatais de
  processo permanecem fora do fallback de UI.
- A correção continua limitada à Change 10002 e ao plugin de referência.

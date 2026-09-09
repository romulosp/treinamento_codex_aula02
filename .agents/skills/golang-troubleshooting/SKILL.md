---
name: golang-troubleshooting
description: 'Solucione problemas em programas Golang de forma sistemática — encontre e corrija a causa raiz. Use ao encontrar bugs, travamentos, impasses ou comportamentos inesperados em código Go. Abrange metodologia de depuração, armadilhas comuns em Go, depuração orientada a testes, configuração e captura do pprof, depurador Delve, detecção de condições de corrida, rastreamento com GODEBUG e depuração em produção. Comece por aqui em qualquer situação de "algo está errado". Não se destina à interpretação de perfis ou benchmarking (consulte a habilidade golang-benchmark) ou à aplicação de padrões de otimização (consulte a habilidade golang-performance).'
---

Solucione problemas em programas Golang de forma sistemática — encontre e corrija a causa raiz. Use ao encontrar bugs, travamentos, impasses ou comportamentos inesperados em código Go. Abrange metodologia de depuração, armadilhas comuns em Go, depuração orientada a testes, configuração e captura do pprof, depurador Delve, detecção de condições de corrida, rastreamento com GODEBUG e depuração em produção. Comece por aqui em qualquer situação de "algo está errado". Não se destina à interpretação de perfis ou benchmarking (consulte a habilidade golang-benchmark) ou à aplicação de padrões de otimização (consulte a habilidade golang-performance).

**Persona:** You are a Go systems debugger. You follow evidence, not intuition — instrument, reproduce, and trace root causes systematically.

**Thinking mode:** Use `ultrathink` for debugging and root cause analysis. Rushed reasoning leads to symptom fixes — deep thinking finds the actual root cause.

**Modes:**

- **Single-issue debug** (default): Follow the sequential Golden Rules — read the error, reproduce, one hypothesis at a time. Do not launch sub-agents; focused sequential investigation is faster for a single known symptom.
- **Codebase bug hunt** (explicit audit of a large codebase): Launch up to 5 parallel sub-agents, one per bug category (ni

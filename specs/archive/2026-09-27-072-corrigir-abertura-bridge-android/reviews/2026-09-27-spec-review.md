# Revisão de SPEC — 072

Data: 27/09/2026. Revisão técnica documental pela Skill `spec-review`.

## Escopo lido

`proposal.md`, `spec.md`, `DESIGN.md`, `tasks.md`, `diagnostico.md` e
`validation.md`; SPECs relacionadas 067–071; regras compartilhadas de workflow,
evidências, arquitetura/convenções Go, testes Go e segurança Go. Fontes de
baseline foram inspecionadas sem alteração de implementação.

## Achados e resolução

| ID | Severidade | Evidência / impacto | Recomendação e resolução no contrato |
|---|---|---|---|
| REV-001 | Importante | 071 fixava COM14, contrariando consumo da variável solicitada | RF-072-01 preserva variável e distingue launcher do fallback do entrypoint; 071 retificada |
| REV-002 | Importante | Ping e Estado CLOSED viram OPEN no ViewModel; DSP depois falha fechado | RF-072-06 separa ação/conectividade/sessão; CA-08/09 exigem regressão |
| REV-003 | Importante | 070 provava ativação de arquivo; falha ownership anterior a Open podia não deixar linha | RF-072-04 exige eventos de infraestrutura no mesmo tracer e crescimento em execução |
| REV-004 | Importante | readiness/reverse/enumeração COM poderiam ser interpretados como Open válido | RF-072-02/03/06 e CA-13 distinguem disponibilidade, conectividade e OPN confirmado |
| REV-005 | Importante | causa original foi atribuída a snapshot posterior sem reprodução | diagnostico.md separa fatos/hipóteses; CA-13 exige evidência física nova |
| REV-006 | Importante | erros descartados, mutex sem afinidade explícita, read serial fatal sem wakeup do peer | RF-072-07/DESIGN definem correção e testes ociosos/multiprocesso Windows |
| REV-007 | Importante | adicionar log/error poderia ecoar dados sensíveis ou quebrar Bridge opaco | RF-072-04/05 limitam metadados/redação; layout e payload PBRG preservados |
| REV-008 | Importante | preflight poderia dobrar timeout ou operar COM desnecessariamente | DESIGN impõe orçamento único e Ping sem aquisição de serial |

Achados resolvidos no texto revisado; não significam que o código baseline
tenha sido corrigido. Não há ressalva material de especificação em aberto.

## Verificação dos requisitos de revisão

- Objetivo e escopo rastreados ao relato e fontes locais; fora de escopo explícito.
- Contratos funcionais RF-072-01 a 07 e critérios CA-072-01 a 15 verificáveis.
- Dependências de ambiente/hardware, risco de resultados indeterminados e
  evidências históricas separados de sucesso atual.
- DESIGN mantém fronteiras do core/Bridge/Kotlin e contratos PBRG/gomobile;
  não exige novo ADR para troca de arquitetura, protocolo ou tecnologia.
- Plano de teste inclui hardware real, não apenas fake/scripted/criação do log.
- `specs/system/`, archive e commits só serão alterados no encerramento formal.

Verificação documental pós-revisão: artefatos, 24 links locais e 15 critérios
consistentes; verificador PowerShell e `git diff --check -- specs` com código
0. Resultados registrados em VAL-072-D05/D06. Não são testes de implementação.

## Decisão

`SPEC_APROVADA`

Autoriza planejamento preparatório e implementação desta SPEC. Não aprova
implementação, validação física, encerramento das changes anteriores ou commit.

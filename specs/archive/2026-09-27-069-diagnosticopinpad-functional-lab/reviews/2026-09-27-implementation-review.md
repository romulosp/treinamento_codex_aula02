# Revisão da implementação — Change 069

## Escopo

Comparação da implementação Go/mobile e Android com `spec.md`, `DESIGN.md`,
`implementation-plan.md` e os critérios CA-069-01 a CA-069-10. Esta revisão não
alterou código nem executou correções.

## Evidências examinadas

- fachada gomobile em `apps/desktop/libpinpadabecsgo/mobile/`;
- catálogo, ViewModel, repositório e tela Compose do
  `diagnosticopinpad`;
- testes Go e testes JVM Android;
- AAR gerado e classe Java inspecionada com `javap`;
- README Android, `tasks.md` e `validation.md`;
- auditoria de segurança atual da Change 069.

## Achados

### IMP-REV-001 — ABI gomobile para listas

- Severidade: baixa, resolvido durante a implementação.
- Evidência: `[]string` não era exportado pela ABI Java do gomobile.
- Tratamento: os métodos nomeados MNU, TLI/TLR/TLE e DMF recebem listas
  delimitadas e fazem a conversão exclusivamente no Go; a classe Java final
  foi inspecionada e contém os métodos exportados.

### IMP-REV-002 — Dados sensíveis na fronteira Android

- Severidade: alta, mitigado.
- Evidência: GTK, GPN, GCX, GOX e FCX usam summaries allowlisted ou retornam
  somente status; campos secretos são mascarados no formulário.
- Tratamento: PAN, trilhas, PIN block, KSN, chaves, EMV bruto e bytes raw não
  chegam ao estado Compose, aos logs ou aos summaries públicos.

### IMP-REV-003 — Opções sem contrato executável

- Severidade: baixa, conforme a SPEC.
- Evidência: opções 6 e 25 aparecem desabilitadas com justificativa; opção 28
  cancela, fecha o cliente e encerra a Activity.

Não foram identificadas divergências materiais ou escopo indevido.

## Matriz de conformidade

| Critério | Resultado da revisão |
|---|---|
| CA-069-01 | Conforme; validação visual no Emulator pendente de registro formal |
| CA-069-02 a CA-069-04 | Conforme; métodos nomeados, GoDoc e testes presentes |
| CA-069-05 | Conforme; validação básica Android e validação final Go |
| CA-069-06 e CA-069-07 | Conforme por inspeção estática e testes |
| CA-069-08 | Conforme; comandos registrados em `validation.md` |
| CA-069-09 | Requer evidência de validação; não é divergência de implementação |
| CA-069-10 | Conforme; README e documentação da change atualizados |

## Veredito

`IMPLEMENTACAO_APROVADA`

A implementação corresponde à SPEC aprovada e está apta para a fase de
validação. A validação deve manter explícita a limitação de hardware físico e
não declarar sucesso de operações ABECS que não tenham sido executadas.

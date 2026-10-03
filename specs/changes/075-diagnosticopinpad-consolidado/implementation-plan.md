# Plano de implementação — 075 diagnosticopinpad consolidado

## Status

`RECONCILIAÇÃO EXECUTADA — COMPLEMENTO DE TESTES EM 2026-10-03`

O plano anterior foi parcialmente executado. Esta revisão não reinicia o
projeto; ela compara a implementação existente com o contrato corrigido e
altera somente o necessário para obter conformidade.

O complemento de implementação habilitou cobertura JVM do AGP, ampliou os
testes de lifecycle e corrigiu o wrapper Windows e a disponibilidade da opção
Sair. Resultados e escopo de cobertura estão em `validation.md` e
`coverage-inventory.md`. Cobertura Android/JNI integrada permanece pendente.

## Ordem de execução

1. Executar Gate P0 contra a Change 074 e confirmar a configuração aprovada:
   todo processo físico exige `PORTA_PINPAD`; ausência, vazio e inválido falham;
   não existe COM fixa ou fallback.
2. Inventariar no código atual preflight, estados, erros, logs, lifecycle e
   testes, classificando cada item como conforme, divergente ou ausente.
3. Corrigir primeiro contratos Go/mobile que alimentam categoria, fase,
   correlação e estado confirmado.
4. Regenerar o AAR e registrar versão, commit, dirty state, SHA-256, ABI e
   minSdk. Falha de NDK/gomobile é bloqueio explícito, não autorização para usar
   artefato sem proveniência.
5. Reconciliar repository/ViewModel/UI: estado triplo, preflight, painel de
   erro, endpoint, cancelamento, Close e saída.
6. Atualizar testes Go, JVM e Compose para cada divergência corrigida.
7. Executar validador, testes, lint e assemble com
   `JAVA_HOME=C:\Desenvolvimento\jdk-17.0.11`. Disponibilizar Maven em
   `C:\Desenvolvimento\apache-maven-3.8.8`, sem alegar uso se o comando não o
   invocar.
8. Executar Gate 3 e Gate 4 end-to-end com Bridge real e transporte scripted.
9. Executar Gate 7 de cleanup, multiprocesso, eventos e redaction.
10. Executar Gate 5 somente com COM/pinpad reais; caso contrário registrar
    `não executado`.
11. Atualizar `tasks.md` e `validation.md`, marcar `IMPLEMENTADA` e parar antes
    da revisão da implementação, conforme o prompt de implementação para teste.

## Incrementos

- R1: configuração e Gate P0;
- R2: fachada/erros/preflight;
- R3: estado triplo e painel operacional;
- R4: lifecycle/endpoint/logs;
- R5: AAR e quality gates Android;
- R6: scripted end-to-end e observabilidade;
- R7: físico, se disponível.

Cada incremento mantém os testes aplicáveis verdes. Não atualizar arquitetura,
toolchain ou catálogo fora da SPEC; divergência nova retorna à especificação.

## Qualidade e segurança

- usar Gradle Wrapper;
- executar o validador estrutural antes dos gates Android;
- não versionar AAR, APK, segredos, logs completos ou capturas sensíveis;
- preservar loopback, PBRG v1, redaction e ausência de retry indeterminado;
- registrar comando, diretório, ambiente, duração, resultado e código de saída;
- manter GoDoc/KDoc e inventário de cobertura elegível.

## Prontidão para o prompt de implementação

`proposal.md` e `spec.md` estão `SPEC_APROVADA`. O prompt
`.github/prompts/implementar-mudanca-para-teste.prompt.md` poderá ser executado
para esta reconciliação e deverá parar em `IMPLEMENTADA`, sem realizar revisão,
validação independente, archive ou commit.

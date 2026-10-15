# Revisão da implementação — 10015

IMP-REV-001 — `IMPLEMENTACAO_APROVADA`. Revisão local após IMPLEMENTADA,
sem edição de código nesta fase; não constitui auditoria independente.

RF-01/02: Kotlin/launcher exigem string explícita vazio/30 e índice inteiro0..99.
Binding omite ponteiroKeyIndex em claro e valida índice da fonte antes da chamada;
core aceita apenas vazio/semíndice ou30/comíndice. Builder inalterado faz validação
de demais parâmetros. Não usar00 ou downgrade automático.

RF-03: evidência set-des-gtk-clear.py compara dicionários e só altera método
no arquivo externo DES, preservando08 e demais campos; backup temporário.
CapturePIN/ContinueEMV/parseproperties não alterados. Perfil por operação
continua congelado, mudança futura exige provisionamento/nova jornada.

RF-04/05: GTK obrigatório mantido, snapshot recebe modo e índice enviados,
getters sincronizados e Clear mantém contrato. Trilhas em claro não chegam
ao Android/resumos/logs. GPN/GOX seguem criptografiaPIN independente. Nenhuma
nova persistência/log de cartão/alteração de redaction, UI, manifesto ou stack.

TestsGo development service/mobile PASS; integração28cenários duasAPIs/doisGTK,
vetor literal GTK008 contendo somente SPE_TRACKS; core cobre quatro modos com
PINonline, trilhas em claro, semKSNDATA, e escolha inválida bloqueia I/O.
Testes Kotlin JVM/modelo, parser instrumental/preflight e9cenários PowerShell
estão previstos/implementados; PowerShell ValidateOnly9casos PASS.
KDoc/godoc e README/ADR confrontados com contrato. Sem divergência material.
Seguir gates reais e empacotamento; nenhuma compra física nesta revisão.

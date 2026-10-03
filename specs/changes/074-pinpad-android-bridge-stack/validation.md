# Validation — 074 Pinpad Android Bridge Stack

## Status

`IMPLEMENTADA — TESTE HUMANO PENDENTE`

As evidências de 2026-09-29 abaixo validam somente a inspeção documental e o
estado então observado no workspace. Houve implementação posterior, registrada
na seção do aditivo 071, mas ainda sem revisão e validação formais.

Em 2026-09-30, a auditoria confirmou a supersessão aprovada do fallback da 072:
todo processo físico exige `PORTA_PINPAD`. O ambiente atual possui Java em
`C:\Desenvolvimento\jdk-17.0.11` e Maven em
`C:\Desenvolvimento\apache-maven-3.8.8`; qualquer gate novo deve usar esses
caminhos explicitamente e registrar comando e código de saída.

## Ambiente

- Windows, PowerShell.
- Workspace: D:\desenvolvimento\ia\aula02.
- Go observado: go1.26.5 windows/386.
- Módulo: apps/desktop/libpinpadabecsgo.
- Android consumidor: apps/frontend/smartphone/diagnosticopinpad.
- Data: 2026-09-29.
- Alterações preexistentes no worktree foram preservadas.

## Verificações de identificação e fontes

| Evidência | Procedimento | Resultado |
|---|---|---|
| VAL-074-D01 | Inventário de specs/changes e specs/archive para o ID 073 | 073 ocupado por 2026-09-28-073-remover-acoes-duplicadas-catalogo. |
| VAL-074-D02 | Verificação do próximo ID livre | 074 livre; pasta criada em specs/changes/074-pinpad-android-bridge-stack. |
| VAL-074-D03 | Leitura de proposal, spec, DESIGN, tasks, validation, reviews e contratos transversais das quatro fontes | Concluída; análise registrada em analysis.md. |
| VAL-074-D04 | Inventário do código Go, scripts, mobile e laboratório Android | Concluído; divergências D-001 a D-008 registradas. |

## Comandos executados

| Evidência | Comando | Resultado/código |
|---|---|---|
| VAL-074-R01 | go version | go1.26.5 windows/386; código 0. |
| VAL-074-R02 | go test ./... no módulo Go | Passou; código 0. Pacotes Go aplicáveis passaram. |
| VAL-074-R03 | go vet ./... no módulo Go | Passou; código 0. |
| VAL-074-R04 | go test -race ./... no módulo Go | Não executado pelo ambiente: -race is not supported on windows/386; código 1. Não declarar race aprovado. |
| VAL-074-R05 | go test -tags=integration ./... no módulo Go | Falhou em internal/infrastructure/transport/emulator, teste TestCloseAckVariants/legacy_eof, erro set bridge read deadline: io: read/write on closed pipe; código 1. |
| VAL-074-R06 | python .agents/skills/android-native-engineering/scripts/validate_android_project.py apps/frontend/smartphone/diagnosticopinpad | Falhou antes dos gates: arquivo local potencialmente sensível local.properties; código 1. O arquivo não foi alterado. |
| VAL-074-R07 | python .agents/skills/android-native-engineering/scripts/validate_android_project.py apps/frontend/smartphone/sistema-prototipo-android | Falhou pela mesma presença de local.properties; código 1. Projeto não é o consumidor do pinpad e não foi alterado. |

## Interpretação

- VAL-074-R02 e VAL-074-R03 demonstram apenas que o estado atual dos testes
  unitários e vet não falhou no ambiente observado; não validam a nova SPEC.
- VAL-074-R05 é uma divergência atual relevante e bloqueia declaração de
  validação completa do transporte.
- VAL-074-R04 registra uma limitação de arquitetura/ambiente, não uma aprovação
  de concorrência.
- VAL-074-R06 e VAL-074-R07 não autorizam remover local.properties, pois o
  arquivo é preexistente e local. O bloqueio deve ser resolvido no ambiente ou
  por procedimento seguro antes da validação Android futura.
- Nenhum teste físico foi executado nesta Change.
- Nenhum AAR/APK foi gerado ou aprovado nesta Change.
- O código de produção foi modificado durante a implementação aprovada; a
  revisão da implementação e a validação formal ainda não foram executadas.

## Critérios de aceite

CA-074-01 a CA-074-18 permanecem pendentes. Eles só poderão ser marcados após
SPEC_APROVADA, implementação, revisão da implementação e validação formal.

## Evidência técnica do aditivo 071 — 2026-09-30

Esta seção registra testes da fase `IMPLEMENTADA` para avaliação humana; não é
validação formal da Change. Ambiente: Windows, PowerShell, Go 1.26.5
windows/386; módulo `apps/desktop/libpinpadabecsgo`.

| Evidência | Comando e diretório | Resultado | Código |
|---|---|---|---:|
| VAL-074-T071-01 | `go test -tags=integration ./cmd/libpinpadabecsgo-bridge -count=1`, módulo Go | Passou; inclui COM herdada, vazia, espaços, valor inválido e exit code do BAT | 0 |
| VAL-074-T071-02 | `go test ./...`, módulo Go | Passou | 0 |
| VAL-074-T071-03 | `go vet ./...`, módulo Go | Passou | 0 |
| VAL-074-T071-04 | `go test -tags=integration ./... -count=1`, módulo Go | Passou, inclusive Bridge/launcher | 0 |
| VAL-074-T071-05 | `python .agents/skills/android-native-engineering/scripts/validate_android_project.py apps/frontend/smartphone/diagnosticopinpad`, raiz do workspace | Bloqueado por `local.properties` preexistente; arquivo preservado | 1 |

O launcher agora rejeita valor COM malformado antes de chamar Go, além de
rejeitar ausência e espaços, sem atribuir porta fixa. Não foram executados
Gradle, AAR/APK, emulador ou pinpad físico. CA-074-18 tem evidência técnica
parcial, mas não está formalmente validado. A seção histórica acima continua
descrevendo a consolidação de 2026-09-29, não o estado atual completo do código.

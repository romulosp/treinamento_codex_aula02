# Tasks — 075 diagnosticopinpad consolidado

## Consolidação documental corrigida

- [x] Inventariar 068 e 069 como fontes históricas diretas.
- [x] Confirmar que 068/069 incorporam efeitos corretivos da 072.
- [x] Declarar 074 como baseline normativa do stack Go/Bridge.
- [x] Consolidar readiness, preflight, estado, erros, logs, cleanup e reabertura.
- [x] Confirmar a supersessão aprovada da 074: `PORTA_PINPAD` obrigatória em
  todo processo físico, sem COM fixa/fallback.
- [x] Criar análise, rastreabilidade e ADR da dependência 074.
- [x] Atualizar critérios de aceite e Gates P0–7.
- [x] Registrar re-revisão técnica da SPEC corrigida.

## Implementação anterior preservada como histórico

- [x] Projeto Android single-module e catálogo de 28 opções criados.
- [x] Fachada nomeada, repository, ViewModel/UDF e logger Android criados.
- [x] Testes Go, JVM, lint, assemble e instrumentados executados conforme
  `validation.md` da revisão anterior.
- [x] Ambiente Java registrado em `C:\Desenvolvimento\jdk-17.0.11`.
- [x] Maven registrado em `C:\Desenvolvimento\apache-maven-3.8.8` sem alegar
  uso em comandos Gradle.

Esses itens não comprovam conformidade com a revisão atual.

## Reconciliação de implementação — executada em 2026-10-03

- [x] Confirmar que `DefaultConfig()`/`config.Load()` não usam COM fixa/fallback
  e rejeitam ausência/vazio/inválido; validação formal permanece pendente.
- [x] Confirmar preflight Ping antes de Open sem Acquire/COM em falha.
- [x] Separar actionState, bridgeState e sessionState na UI.
- [x] Usar `Mobile.errorCode/errorPhase` no adapter e normalizar erros remotos no Go.
- [x] Manter erro e correlação acima do catálogo com foco/semântica testados.
- [x] Reconciliar endpoint, Close, Cancel, onCleared e saída; rotação preserva ViewModel.
- [x] Logger interno metadata-only e correlação inclusive no Close Windows.
- [x] Provar scripted `Open → GIX → DSP → Close → Open` por JNI no Emulator.
- [x] Executar testes Go de cleanup, read fatal e mutex multiprocesso.
- [x] Atualizar documentação e scripts do AAR.

## Gates técnicos a reexecutar

- [x] Gate P0: testes Go, integração, vet e build; aprovação formal da 074 não inferida.
- [x] Gate 1: AAR regenerado/inspecionado com Go amd64 e proveniência.
- [x] Gate 2: validador em cópia de fontes, 20 testes JVM, lint e assemble.
- [x] Gate 3: listener/reverse/Ping real e testes de preparação do módulo Go.
- [x] Gate 4: seis testes instrumentados, incluindo DSP e reabertura scripted.
- [ ] Gate 5: físico somente com COM/pinpad reais.
- [x] Gate 6: testes de estado/erros/endpoint/foco; testes humanos de rotação/saída ainda necessários.
- [x] Gate 7: testes Go e log scripted com correlação/redaction.
- [x] Registrar ambiente, comandos, resultados e códigos de saída novos.
- [x] Aferir cobertura Go agregada incluindo integração: 80,1% (alvo 90% não atingido).
- [x] Aferir cobertura Kotlin JVM com JaCoCo: 97,22% no escopo JVM inventariado;
  52,72% no módulo inteiro executado somente em JVM. Ver `coverage-inventory.md`.
- [x] Testar recriação, saída e Cancel na UI com operação pendente no Emulator.
- [x] Corrigir Sair desabilitado durante operação e código de saída do wrapper Windows.
- [ ] Aferir cobertura integrada das partes Android/JNI; não inferir mínimo global
  a partir do percentual do escopo JVM.

## Gates formais posteriores

- [ ] Revisar implementação reconciliada.
- [ ] Registrar validação independente.
- [ ] Obter aprovação formal.
- [ ] Atualizar `specs/system/`.
- [ ] Preparar archive e commit somente após aprovação.

## Estado atual

`IMPLEMENTADA` — disponível para teste humano; não revisada nem aprovada formalmente.

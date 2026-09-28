# Tasks — 072 Corrigir abertura, estado e log Android/Bridge

## Especificação

- [x] Inspecionar launcher, Bridge, tracer, ownership e estado Android.
- [x] Registrar evidências confirmadas e hipóteses separadamente.
- [x] Criar proposal, SPEC, DESIGN, tasks e matriz de validação.
- [x] Referenciar contrato corretivo nas SPECs 067–071 e índice.
- [x] Revisar a SPEC e registrar decisão em `reviews/`.
- [x] Preparar `implementation-plan.md` após aprovação da SPEC.

## Implementação prevista

- [ ] Preservar PORTA_PINPAD e valores de ambiente, checar launcher e exit code.
- [ ] Criar helper ADB/reverse com seleção explícita e casos de indisponibilidade.
- [ ] Registrar readiness real, configuração segura e falhas de startup.
- [ ] Registrar eventos de sessão/ownership no mesmo tracer; não descartar erros.
- [ ] Preservar correlação e implementar categorias seguras na fronteira mobile.
- [ ] Implementar preflight dentro do orçamento da ação, sem abrir COM.
- [ ] Corrigir estado da sessão, habilitação das ações e apresentação de erro.
- [ ] Corrigir ownership/thread Windows, read fatal ocioso e cleanup/concorrência.
- [ ] Acrescentar testes de regressão e GoDoc/KDoc para contratos modificados.
- [ ] Atualizar READMEs com configuração Windows, reverse, execução e diagnóstico.

## Verificação e gates

- [ ] Executar testes Go, vet, build, cobertura/inventário e race se suportado.
- [ ] Executar launcher/helper com ambiente e ADB controlados.
- [ ] Gerar AAR quando aplicável e registrar origem/hash.
- [ ] Executar testes Android, lint, build e testes Compose instrumentados.
- [ ] Instalar APK corrigido no emulador e provar fluxo scripted/reabertura.
- [ ] Provar fluxo COM14 físico e alimentação real do log.
- [ ] Revisar implementação contra todos os CA-072-*.
- [ ] Registrar validação e auditoria atual, sem reutilizar evidência histórica.
- [ ] Obter aprovação formal; atualizar system, preparar archive e commit no encerramento.

Não marcar implementação, hardware ou aprovação como concluídos por revisão
documental, teste unitário ou pela criação do arquivo de log.

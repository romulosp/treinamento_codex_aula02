# Tasks — 072 Corrigir abertura, estado e log Android/Bridge

## Estado da implementação

`IMPLEMENTADA`

A implementação e os testes técnicos previstos foram concluídos. O gate físico
foi executado usando a porta herdada de `PORTA_PINPAD`; falta somente a
confirmação visual do texto no display para concluir a validação CA-072-13.

## Especificação

- [x] Inspecionar launcher, Bridge, tracer, ownership e estado Android.
- [x] Registrar evidências confirmadas e hipóteses separadamente.
- [x] Criar proposal, SPEC, DESIGN, tasks e matriz de validação.
- [x] Referenciar contrato corretivo nas SPECs 067–071 e índice.
- [x] Revisar a SPEC e registrar decisão em `reviews/`.
- [x] Preparar `implementation-plan.md` após aprovação da SPEC.

## Implementação prevista

- [x] Preservar PORTA_PINPAD e valores de ambiente, checar launcher e exit code.
- [x] Criar helper ADB/reverse com seleção explícita e casos de indisponibilidade.
- [x] Registrar readiness real, configuração segura e falhas de startup.
- [x] Registrar eventos de sessão/ownership no mesmo tracer; não descartar erros.
- [x] Preservar correlação e implementar categorias seguras na fronteira mobile.
- [x] Implementar preflight dentro do orçamento da ação, sem abrir COM.
- [x] Corrigir estado da sessão, habilitação das ações e apresentação de erro.
- [x] Corrigir ownership/thread Windows, read fatal ocioso e cleanup/concorrência.
- [x] Acrescentar testes de regressão e GoDoc/KDoc para contratos modificados.
- [x] Atualizar READMEs com configuração Windows, reverse, execução e diagnóstico.

## Verificação e gates

- [x] Executar testes Go, vet, build, cobertura/inventário e race se suportado.
- [x] Executar launcher/helper com ambiente e ADB controlados.
- [x] Gerar AAR quando aplicável e registrar origem/hash.
- [x] Executar testes Android, lint, build e testes Compose instrumentados.
- [x] Instalar APK corrigido no emulador e provar fluxo scripted/reabertura.
- [x] Provar fluxo físico na porta efetiva de `PORTA_PINPAD`, alimentação real do log e confirmação visual.
- [x] Revisar implementação contra todos os CA-072-*.
- [x] Registrar validação e auditoria atual, sem reutilizar evidência histórica.
- [ ] Obter aprovação formal; atualizar system, preparar archive e commit no encerramento.

Não marcar implementação, hardware ou aprovação como concluídos por revisão
documental, teste unitário ou pela criação do arquivo de log.

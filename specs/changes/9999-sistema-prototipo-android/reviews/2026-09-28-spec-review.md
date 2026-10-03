# Revisão de SPEC — auditoria pós-merge Android

## Escopo

Foram revisadas a Change `9999-sistema-prototipo-android`, as Changes ativas
`10003` a `10007`, `specs/shared/architecture/android-business-plugin.md`,
`specs/shared/process/workflow.md`, `specs/system/` e as referências novas
adicionadas em `specs/README.md`. Também foi conferida a implementação atual da
`SharedApi` apenas para verificar a coerência documental do contrato.

## Achados

| ID | Severidade | Evidência | Impacto | Recomendação |
| --- | --- | --- | --- | --- |
| `REV-001` | Bloqueante | `specs/README.md:13,23,29,35-37,41` aponta as Changes 066–072 em `changes/...`, mas os diretórios encontrados estão em `specs/archive/...`; os destinos sob `specs/changes/...` não existem. | A documentação pós-merge contém links quebrados e direciona a leitura para uma localização inexistente. | Corrigir os links para `archive/...` ou mover/registrar as Changes conforme a convenção oficial. Só arquivar/commitar depois de confirmar que os diretórios recebidos pelo merge estão rastreados. |
| `REV-002` | Bloqueante | `9999/spec.md:11-23` diz que catálogo/menu vêm de `:plugin-negocio`, que a identificação saiu do `:app` e que SSO substituiu a confirmação local; porém `9999/spec.md:89-114` ainda exige tela-catálogo e menu local no aplicativo, e `9999/spec.md:183-186` ainda exige confirmação local sem serviço externo. | Um implementador pode seguir dois produtos incompatíveis: host com plugins/SSO ou aplicativo monolítico com catálogo/menu/confirmação local. A Change não tem um contrato único. | Marcar explicitamente RF-005, RF-006, RF-008 e RN-003 como superados, ou reescrevê-los para o fluxo atual e apontar `10001`/`10003`/`10007` como contratos sucessores. |
| `REV-003` | Importante | `9999/proposal.md:16-18` e `9999/spec.md:16-18` fixam `minSdk = 29` no baseline operacional, enquanto `9999/proposal.md:49-53`, `9999/spec.md:30` e `9999/spec.md:50` fixam `minSdk = 26`; `10003/spec.md:9` usa 29. | Toolchain, matriz de testes, manifestos e compatibilidade podem divergir entre o host e os plugins. | Escolher um único `minSdk` vigente, remover o baseline histórico conflitante e alinhar 10003, DESIGN, planos e matriz de compatibilidade. |
| `REV-004` | Importante | `10003/spec.md:15` e `10003/DESIGN.md:9` dizem preservar `SharedApi 1.1.0`, e `10003/tasks.md:14` fala em elevar a major; `10007/spec.md:104-107` exige incremento da minor. O código atual já declara `SharedApi(1, 2, 0)`, enquanto os manifestos de negócio ainda requerem minor 1. | Não está definido se 10003 mantém 1.1, se 10003 cria uma major, ou se 10007 é a evolução compatível para 1.2. A compatibilidade dos plugins antigos fica implícita. | Definir uma sequência/versionamento canônico: versão antes/depois, regra `requiredMinor <= hostMinor` ou igualdade, plugins que devem ser recompilados e quais manifestos são compatíveis. Atualizar SPEC, tarefas, sistema e evidências de forma coordenada. |
| `REV-005` | Importante | `10003/spec.md:41` exige isolamento para timeout de discovery/instanciação/caminho; `10003/DESIGN.md:21,32` também menciona timeouts, mas `10003/reviews/2026-09-25-spec-revision.md:12` afirma que o requisito foi substituído por geração/cancelamento. | A SPEC aprovada ainda exige um comportamento temporal não definido, enquanto a revisão afirma que ele deixou de ser requisito. O aceite para APK lento não é reproduzível. | Escolher uma única decisão: remover MENU-007 e o timeout do design, ou definir prazo, relógio, cancelamento, código/log e teste determinísticos. |
| `REV-006` | Importante | `10006/spec.md:25-30` exige cabeçalho dentro de `:plugin-login`; `10007/proposal.md:29-33,41-49` e `10007/spec.md:93-100` transferem o cabeçalho ao shell de `:app` e proíbem a segunda instância no login. Ambas permanecem `SPEC_APROVADA`. | As duas Changes não podem ser consideradas simultaneamente vigentes após a implementação: uma exige a instância local e a outra a remove. | Declarar 10007 como sucessora explícita de 10006, atualizar o critério de aceite de 10006 ou marcar o trecho do cabeçalho como superado, preservando apenas o layout de login que continua válido. |
| `REV-007` | Média | `10003/implementation-plan.md:5` ainda está `PRONTO_PARA_IMPLEMENTACAO`, embora `10003/tasks.md:5` esteja `IMPLEMENTACAO_APROVADA` e `validation.md:5` esteja `IMPLEMENTADA`; `10006/implementation-plan.md:5` ainda está `SPEC_APROVADA`, embora `10006/tasks.md:5` esteja `IMPLEMENTADA`. | O estado operacional da Change fica ambíguo para automações e revisores, especialmente depois do merge. | Atualizar os status dos planos ou declarar que são snapshots históricos; manter um único status operacional por Change conforme o workflow. |

## Observações de merge

- Os diretórios `specs/archive/2026-09-27-067...` até `specs/archive/2026-09-28-073...` aparecem como não rastreados no worktree, enquanto `specs/README.md` já os referencia. Isso precisa ser resolvido antes do commit para não deixar a documentação apontando para artefatos fora do histórico.
- As alterações massivas em `.agents/` e outros arquivos do worktree foram preservadas e não foram tratadas como parte da revisão de contrato.
- Os relatórios históricos `REPROVADA` e `SUPERADA` encontrados nas pastas de revisão não foram considerados o estado vigente quando há uma revisão posterior formalmente aprovada.

## Veredito

`REPROVADA`

Há contradições bloqueantes/importantes entre a Change 9999, as Changes de
plugin/autenticação e o índice de especificações. A implementação ou o
arquivamento não deve prosseguir até que o contrato sucessor, o versionamento
da API, o `minSdk`, os links e os requisitos superados sejam consolidados.

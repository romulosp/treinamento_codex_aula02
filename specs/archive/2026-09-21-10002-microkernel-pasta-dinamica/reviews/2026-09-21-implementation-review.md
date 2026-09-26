# Revisão da implementação — 10002

## Resultado

`REPROVADA`

## Escopo revisado

Foram comparados `proposal.md`, `spec.md`, `DESIGN.md`, o ADR da Change, as
Changes dependentes 10000 e 10001, o código dos módulos `:app`, `:shared-api` e
`:plugin-login`, o script de entrega, os testes existentes e as evidências
preliminares de `validation.md`.

## Conformidades observadas

- O host não possui dependência de implementação em `:plugin-login` nem embute
  seu APK nos assets.
- `FileObserver`, debounce de 300 ms, executor serial, scan de boot e entrega
  por arquivo temporário seguido de `mv` estão implementados.
- O caminho feliz registra `DISCOVERED → STAGED → VERIFIED → LOADED → ATTACHED
  → ACTIVE` e a atualização de digest ativo produz `PENDING_RESTART`.
- O classloader recebe o APK promovido no repositório privado, não o arquivo do
  staging externo.
- O lifecycle do watcher está conectado a `onStart`, `onStop` e `onDestroy`.
- As evidências preliminares demonstram build, lint, ativação dinâmica,
  recuperação no boot, rejeição de arquivo não APK e atualização pendente.

## Achados bloqueantes

### IMP-REV-001 — Alta — nem toda falha do pipeline gera `REJECTED` auditável

**Evidência:** `spec.md:35-51` exige que a falha em qualquer etapa anterior ao
classloader gere `REJECTED` auditável. Em
`app/.../platform/LoginPluginLoader.kt:90-98`, as validações de caminho,
extensão e tamanho, além da criação da quarentena, acontecem antes do bloco
`try` iniciado na linha 99. Portanto, uma falha nessas operações escapa sem a
transição registrada nas linhas 113-121. O `catch` do manager em
`DynamicLoginPluginManager.kt:169-178` apenas publica estado para a UI; ele não
audita a transição nem remove o candidato inválido do staging.

**Impacto:** candidato vazio, maior que 64 MiB, fora da raiz ou falha ao criar a
quarentena pode registrar apenas `DISCOVERED`, permanecer no staging e ser
reprocessado. RF-03 e o critério de aceite 4 não estão integralmente atendidos.

**Ação necessária:** proteger todo o pipeline após `DISCOVERED` por uma única
fronteira de rejeição, garantir auditoria `REJECTED` e uma política determinística
para limpeza do staging/quarentena, com testes para cada pré-condição.

### IMP-REV-002 — Alta — o manifesto não é validado como JSON nem pelo contrato completo

**Evidência:** a Change dependente 10000, `spec.md:44-48` e `spec.md:58-68`,
exige JSON válido e os campos `schemaVersion`, `displayName`, versão SemVer,
API, `entryClass`, pacote, prioridade, capacidades e dependências antes do
classloader. O `PluginDescriptor` em `LoginPluginLoader.kt:42-51` representa
somente parte desses campos. A leitura em `LoginPluginLoader.kt:275-289` usa
expressões regulares auxiliares das linhas 331-345 e ignora `schemaVersion`,
`displayName`, formato SemVer, prioridade e dependências.

**Impacto:** texto que não seja JSON válido, mas contenha padrões compatíveis
com as expressões regulares, pode ser promovido e chegar ao classloader. Campos
obrigatórios do contrato arquitetural também podem estar ausentes ou inválidos
sem rejeição. A fronteira de confiança definida em RF-03 fica incompleta.

**Ação necessária:** usar parser JSON estrito, representar e validar todos os
campos obrigatórios e suas regras, incluindo schema, SemVer, identidade,
prioridade e dependências, antes da promoção.

### IMP-REV-003 — Alta — falhas próprias de carga dinâmica podem escapar e derrubar a Activity

**Evidência:** RF-06, `spec.md:81-84`, determina que falhas do
`DexClassLoader`, construção, lifecycle, View ou callback convirjam para
`ERROR`, `onDetach` e fallback sem derrubar a Activity. Entretanto,
`LoginPluginLoader.kt:179`, `DynamicLoginPluginManager.kt:169,193` e
`MainActivity.kt:101,112` capturam somente `Exception`. Falhas típicas de carga
e inicialização de classes, como subclasses de `LinkageError`, não pertencem a
`Exception` e atravessam essas fronteiras.

**Impacto:** APK assinado e previamente verificado que produza erro de linkage,
verificação ou inicialização pode encerrar o fluxo/UI sem registrar `ERROR` nem
executar a recuperação cooperativa exigida.

**Ação necessária:** definir e implementar uma política explícita para falhas
recuperáveis de carga dinâmica, cobrindo `LinkageError` e equivalentes sem
capturar indiscriminadamente erros fatais da VM, e comprovar `ERROR`,
`onDetach` e fallback.

### IMP-REV-004 — Alta — não existem testes do loader, manager ou recuperação cooperativa

**Evidência:** a busca por `LoginPluginLoader`, `DynamicLoginPluginManager`,
`stageAndVerify`, `PluginRuntimeStatus`, `PENDING_RESTART` e
`reportRuntimeFailure` nos arquivos `*Test.kt` não retorna ocorrências. Os
testes existentes cobrem somente reducer e semântica visual do `:plugin-login`,
comportamento herdado da Change 10001. `validation.md:28-34` confirma que não
foi injetada falha de callback/lifecycle. Apesar disso, `tasks.md:16` marca como
concluída a criação/atualização dos testes aplicáveis.

**Impacto:** os ramos de maior risco introduzidos por esta Change — rejeições
antes da quarentena, validação do manifesto, preservação da instância ativa,
recuperação cooperativa e limpeza — não possuem regressão automatizada. O
critério de aceite 7 não foi demonstrado e o critério 8 está incompleto.

**Ação necessária:** criar testes do pipeline e da máquina de estados com
artefatos/fixtures controlados, incluindo candidato vazio e grande, manifesto
inválido/incompleto, API/pacote/assinatura incompatíveis, atualização ativa e
falhas em construção, lifecycle, View e callback.

## Decisão do gate

Os achados `IMP-REV-001` a `IMP-REV-004` afetam requisitos obrigatórios e a
fronteira de confiança anterior ao classloader. A Change retorna para a fase de
implementação. Validação independente, auditoria de segurança, PDF, aprovação,
arquivamento e commit não podem prosseguir até nova revisão sem pendências
materiais.

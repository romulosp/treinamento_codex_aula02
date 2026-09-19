# SPEC: Android Native Engineering

## Status

`SPEC_APROVADA`

## Requisitos funcionais

### RF-001 — Governança antes de geração

A skill deve exigir uma Change aprovada antes de criar ou alterar código Android de produção. Deve identificar perfil, módulos, package name, SDKs, distribuição, dados sensíveis, integrações e critérios de aceite.

### RF-002 — Fontes oficiais e validade temporal

APIs, versões e recomendações Android sujeitas a mudança devem ser consultadas em documentação oficial atual. A skill não deve inventar versões. Conclusões locais devem distinguir `REQUIRED`, `RECOMMENDED`, `OPTIONAL`, `CONTEXTUAL`, `EXPERIMENTAL` e `RESEARCH_CANDIDATE`.

### RF-003 — Composição com skills oficiais

Antes de criar orientação especializada, a skill deve verificar se existe Android Skill oficial aplicável. Skills oficiais resolvem fluxos específicos; a skill local governa processo, arquitetura, evidência e gaps do projeto. Customização de skill oficial exige nome local diferente.

### RF-004 — Perfis

- `SIMPLE`: app pequeno, poucos fluxos, um módulo de aplicação quando suficiente.
- `STANDARD`: camadas UI/data, repositories, UDF, testes e DI manual ou Hilt conforme complexidade.
- `ENTERPRISE`: modularização por features/capacidades, contratos explícitos, CI e observabilidade.
- `HIGH_ASSURANCE`: controles adicionais de ameaça, supply chain, privacidade, rastreabilidade e revisão independente.

A skill deve escolher o perfil mínimo que atende riscos e requisitos, evitando modularização prematura.

### RF-005 — Baseline arquitetural

O padrão inicial é Kotlin, Jetpack Compose para novas UIs, arquitetura adaptativa, UI e data layers, repositories, single source of truth, UDF, coroutines/Flow e dependências por construtor. Domain layer, Hilt, Room, WorkManager e modularização são contextuais e exigem justificativa.

### RF-006 — Quality gates

A skill deve orientar e registrar, conforme aplicável: Gradle Wrapper, build, unit tests, lint, testes de UI, cobertura do código elegível, análise de dependências, acessibilidade e validação em dispositivo/emulador. Cobertura unitária mínima de 80% é requisito quando a SPEC da aplicação não definir regra mais forte; 90% é alvo recomendado. Exclusões precisam ser justificadas.

### RF-007 — Segurança e privacidade

Segredos não podem ser versionados. Componentes exportados, intents, deep links, WebView, armazenamento, logs, backup, rede e permissões devem ser revisados conforme a superfície real. A skill não deve declarar segurança com base apenas em ausência de achados.

### RF-008 — Recuperação controlada

Correções automáticas devem ter limite de tentativas e evidência de causa. A skill não pode repetir indefinidamente alterações ou comandos. Mudança arquitetural, instalação global, acesso a segredo, publicação, exclusão ou alteração de segurança exige autorização correspondente.

### RF-009 — Evidência

Cada execução deve registrar comandos realmente executados, ambiente, código de saída, resultado e limitações. Afirmações não verificadas devem ser marcadas como `NEEDS_EVIDENCE`; decisões ausentes, como `NEEDS_DECISION`.

### RF-010 — Validador

O script `scripts/validate_android_project.py` deve receber o caminho de um projeto, verificar invariantes estruturais sem modificar arquivos e devolver código `0` quando não houver violações obrigatórias, ou código diferente de zero com diagnósticos claros.

### RF-011 — Política de importação de skills

Durante a pesquisa, skills externas devem permanecer apenas como fontes ou candidatas e não podem ser importadas automaticamente. Cada candidata deve ser classificada como `REUSE`, `EXTEND`, `CREATE` ou `REJECT` e registrar origem, mantenedor, versão ou commit analisado, licença, problema resolvido, sobreposição local, aderência arquitetural, aderência ao processo Spec Driven, impacto no contexto, riscos, evidências e decisão.

Somente candidatas `REUSE` ou `EXTEND` aprovadas pela SPEC podem ser incorporadas ao harness. Importar uma skill não aprova automaticamente suas recomendações arquiteturais; cada recomendação continua sujeita à SPEC do projeto consumidor.

### RF-012 — Documentação KDoc obrigatória

A skill deve exigir KDoc, em português do Brasil, para toda declaração Kotlin pública ou protegida criada ou alterada. Declarações internas ou privadas também devem possuir KDoc quando expressarem regra de negócio, contrato arquitetural, concorrência, segurança, efeitos colaterais ou comportamento não óbvio.

O KDoc deve documentar somente contratos comprovados pelo código e pela SPEC, incluindo, conforme aplicável, propósito, parâmetros e propriedades relevantes, retorno, erros ou exceções observáveis, receiver de extensões, efeitos colaterais, requisitos de thread ou coroutine, ciclo de vida e exemplos úteis. Alterações de comportamento devem atualizar o KDoc correspondente.

Overrides podem reutilizar a documentação herdada somente quando não adicionarem nem modificarem contrato. Código gerado e implementações privadas triviais podem ser excluídos mediante justificativa na validação. Não é permitido criar comentários redundantes que apenas repitam nomes, tipos ou a implementação.

## Requisitos não funcionais

### RNF-001 — Execução segura e determinística

O validador deve operar somente em leitura, não instalar dependências, não alterar o projeto analisado e produzir o mesmo resultado para a mesma estrutura de entrada.

### RNF-002 — Contexto mínimo

O `SKILL.md` deve manter apenas regras de roteamento e operação frequente. Detalhes condicionais devem permanecer em referências ligadas por progressive disclosure.

### RNF-003 — Auditabilidade temporal

Toda decisão dependente do ecossistema Android deve registrar fonte oficial, data da consulta e versão ou commit quando disponível. Uma consulta anterior não substitui nova verificação em uma Change consumidora.

### RNF-004 — Qualidade e manutenção da documentação

KDoc deve usar sintaxe válida, links Kotlin quando úteis e permanecer consistente com o comportamento observável. A geração com Dokka deve ser executada quando o projeto já a possuir ou quando a SPEC consumidora exigir documentação publicada; a skill não deve adicionar Dokka automaticamente apenas para satisfazer este requisito.

## Dependências e riscos

- A validação do pacote da skill depende do validador oficial disponível no ambiente Codex e de PyYAML em ambiente isolado.
- A composição com Android Skills oficiais depende da disponibilidade do catálogo ou do repositório oficial no momento de cada Change consumidora.
- O Android CLI é opcional neste incremento e não pode ser instalado automaticamente.
- O principal risco é duplicar ou congelar recomendações oficiais; a mitigação é consulta temporal, classificação de candidatas e composição sob demanda.
- Skills oficiais podem conter decisões contextuais incompatíveis com uma aplicação específica; toda recomendação continua subordinada à SPEC consumidora.
- KDoc desatualizado pode induzir consumidores a erro; a mitigação é revisar documentação junto com cada alteração Kotlin e registrar o inventário das declarações aplicáveis.

## Critérios de aceite

- CA-001: `SKILL.md` é aceito pelo validador de skills com código `0` e a skill é descoberta pelo catálogo local da sessão.
- CA-002: referências de arquitetura, perfis, qualidade e fontes são ligadas por progressive disclosure.
- CA-003: a skill prioriza skills Android oficiais para tarefas especializadas.
- CA-004: comandos reproduzíveis demonstram que o validador aceita um fixture Android mínimo válido com código `0` e rejeita um fixture equivalente sem Gradle Wrapper com código diferente de zero e diagnóstico específico.
- CA-005: nenhuma versão Android instável é fixada como regra universal.
- CA-006: a Change registra revisão, validação e limitações.
- CA-007: `research.md` registra fonte, data, commit, licença, classificação e decisão para cada candidata efetivamente analisada; nenhuma skill externa é importada sem classificação `REUSE` ou `EXTEND` e aprovação explícita na SPEC.
- CA-008: a skill liga uma referência específica de KDoc, exige inventário das declarações Kotlin alteradas, documentação das declarações aplicáveis e justificativa para exclusões; Dokka é executado quando já configurado ou exigido pela SPEC consumidora.

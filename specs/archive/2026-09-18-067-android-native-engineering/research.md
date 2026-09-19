# Pesquisa consolidada

## DEC-001 — Compor antes de duplicar

**Classificação:** REQUIRED.  
**Decisão:** usar Android Skills oficiais para procedimentos especializados e manter a skill local focada em governança do projeto.  
**Evidência:** a documentação Android declara que suas skills seguem o padrão aberto de Agent Skills e podem ser usadas por ferramentas compatíveis.  
**Referências:** https://developer.android.com/tools/agents/android-skills e https://github.com/android/skills.

## DEC-002 — Arquitetura adaptada ao contexto

**Classificação:** REQUIRED.  
**Decisão:** UI e data layers, repositories, UDF e coroutines/Flow formam o baseline; domain layer, Hilt e modularização dependem da complexidade.  
**Evidência:** as recomendações oficiais distinguem práticas altamente recomendadas, recomendadas e opcionais.  
**Referências:** https://developer.android.com/topic/architecture e https://developer.android.com/topic/architecture/recommendations.

## DEC-003 — Skills externas permanecem candidatas

**Classificação:** REQUIRED.  
**Decisão:** nenhuma skill oficial ou comunitária será importada durante a pesquisa. Cada candidata será avaliada como `REUSE`, `EXTEND`, `CREATE` ou `REJECT` antes de eventual aprovação na SPEC.  
**Evidência:** skills oficiais existem e são atualizadas separadamente; importação prematura aumentaria duplicação, contexto e risco de congelar recomendações temporais.  
**Situação atual:** o catálogo e o repositório `android/skills` foram consultados como fontes. Nenhuma skill externa foi instalada, copiada ou incorporada.

## DEC-004 — KDoc obrigatório para contratos Kotlin

**Classificação:** REQUIRED.  
**Decisão:** exigir KDoc para APIs públicas/protegidas e para declarações não públicas com contratos relevantes, preservando a possibilidade de excluir overrides sem contrato novo, código gerado e implementações privadas triviais mediante justificativa.  
**Evidência:** a documentação oficial define KDoc como o equivalente do JavaDoc para Kotlin; as convenções oficiais recomendam KDoc para membros públicos e orientam evitar tags ou texto redundante quando o contrato pode ser descrito claramente no corpo. Dokka compreende KDoc e gera documentação de API.  
**Referências consultadas em 2026-09-18:** https://kotlinlang.org/docs/kotlin-doc.html, https://kotlinlang.org/docs/coding-conventions.html e https://kotlinlang.org/docs/dokka-introduction.html.  
**Limite:** a obrigatoriedade é uma regra local desta skill. A instalação de Dokka permanece contextual e depende da SPEC da aplicação consumidora.

## Consulta temporal ao catálogo oficial

- **Data da consulta:** 2026-09-18.
- **Catálogo:** https://developer.android.com/tools/agents/android-skills/browse, atualizado em 2026-09-04 conforme a própria página.
- **Repositório:** https://github.com/android/skills.
- **Commit analisado:** `b1f707d90904129b5972b3cc6436b568583effe5`, de 2026-09-18T10:47:56Z.
- **Licença do repositório:** Apache-2.0, registrada em https://github.com/android/skills/blob/b1f707d90904129b5972b3cc6436b568583effe5/LICENSE.txt.
- **Método:** listagem recursiva dos arquivos `SKILL.md` no commit e análise das candidatas relacionadas ao escopo deste primeiro incremento.
- **Resultado:** foram identificadas quatro skills oficiais diretamente relacionadas aos fluxos cobertos pela orquestradora local. Nenhuma foi importada; todas permanecem disponíveis para nova avaliação no contexto de um projeto Android consumidor.

## SKILL-CANDIDATE-001 — Android CLI

- **Classificação:** `REJECT`
- **Origem:** https://github.com/android/skills/blob/b1f707d90904129b5972b3cc6436b568583effe5/devtools/android-cli/SKILL.md
- **Mantenedor:** Google LLC.
- **Versão/commit analisado:** `b1f707d90904129b5972b3cc6436b568583effe5`; metadado `last-updated: 2026-09-12`.
- **Licença:** Apache-2.0.
- **Problema que resolve:** instalação e uso do Android CLI para criar projetos, consultar documentação, executar apps, operar emuladores e gerenciar skills.
- **Sobreposição com skills existentes:** sobrepõe parcialmente descoberta de skills, consulta temporal e execução em dispositivo; não cobre o workflow Spec Driven local.
- **Aderência à arquitetura:** alta para ferramentas e execução; neutra quanto aos perfis arquiteturais locais.
- **Aderência ao processo Spec Driven:** parcial, pois a skill oficial não implementa os gates locais desta Change.
- **Impacto no contexto do agente:** alto, por incluir instalação, SDK, emuladores, documentação e interação com dispositivos.
- **Riscos:** instalação automática fora do escopo, alteração do ambiente e dependência de uma CLI ausente nesta máquina.
- **Evidências:** frontmatter e conteúdo do arquivo oficial no commit analisado; `android` não está disponível no `PATH` deste ambiente.
- **Decisão:** rejeitada para incorporação no harness neste incremento. Deve ser reavaliada e usada sob demanda em Change de aplicativo que autorize instalação ou já disponha do Android CLI.

## SKILL-CANDIDATE-002 — Android Intent Security

- **Classificação:** `REJECT`
- **Origem:** https://github.com/android/skills/blob/b1f707d90904129b5972b3cc6436b568583effe5/security/android-intent-security/SKILL.md
- **Mantenedor:** Google LLC.
- **Versão/commit analisado:** `b1f707d90904129b5972b3cc6436b568583effe5`; metadado `last-updated: 2026-08-14`.
- **Licença:** Apache-2.0.
- **Problema que resolve:** auditoria de componentes, Intents, PendingIntents e vetores de redirecionamento ou acesso não autorizado.
- **Sobreposição com skills existentes:** detalha parte especializada de RF-007; a skill local apenas exige que essa superfície seja revisada.
- **Aderência à arquitetura:** alta quando o projeto possui componentes Android e comunicação por Intents.
- **Aderência ao processo Spec Driven:** parcial; precisa ser executada dentro dos gates locais e com evidências na Change consumidora.
- **Impacto no contexto do agente:** médio e especializado em segurança.
- **Riscos:** aplicar controles sem uma superfície real ou tratar ausência de achados como prova de segurança.
- **Evidências:** catálogo oficial e frontmatter do arquivo no commit analisado.
- **Decisão:** rejeitada para incorporação genérica neste incremento, que não contém aplicativo nem Manifest de produção. Deve ser priorizada sob demanda quando a Change consumidora possuir componentes ou Intents no escopo.

## SKILL-CANDIDATE-003 — Testing Setup

- **Classificação:** `REJECT`
- **Origem:** https://github.com/android/skills/blob/b1f707d90904129b5972b3cc6436b568583effe5/testing/testing-setup/SKILL.md
- **Mantenedor:** Google LLC.
- **Versão/commit analisado:** `b1f707d90904129b5972b3cc6436b568583effe5`; metadado `last-updated: 2026-09-03`.
- **Licença:** Apache-2.0.
- **Problema que resolve:** análise e configuração de estratégia de testes Android, incluindo testes unitários, UI, screenshots e ponta a ponta.
- **Sobreposição com skills existentes:** sobrepõe os quality gates de RF-006, mas é prescritiva sobre frameworks e infraestrutura que dependem do projeto consumidor.
- **Aderência à arquitetura:** alta para aplicativos Android; contextual quanto a DI, frameworks e tipos de teste.
- **Aderência ao processo Spec Driven:** parcial; os comandos, exclusões e critérios ainda precisam ser aprovados e registrados na Change consumidora.
- **Impacto no contexto do agente:** alto, por cobrir diversas ferramentas e referências de teste.
- **Riscos:** instalar frameworks desnecessários ou impor Hilt, Robolectric, screenshots e outras ferramentas sem decisão de produto/projeto.
- **Evidências:** catálogo oficial e conteúdo do arquivo no commit analisado.
- **Decisão:** rejeitada para incorporação no harness deste incremento. Deve ser reavaliada por projeto e composta quando a SPEC da aplicação exigir criação ou revisão da estratégia de testes.

## SKILL-CANDIDATE-004 — Adaptive

- **Classificação:** `REJECT`
- **Origem:** https://github.com/android/skills/blob/b1f707d90904129b5972b3cc6436b568583effe5/jetpack-compose/adaptive/SKILL.md
- **Mantenedor:** Google LLC.
- **Versão/commit analisado:** `b1f707d90904129b5972b3cc6436b568583effe5`; metadado `last-updated: 2026-08-27`.
- **Licença:** Apache-2.0.
- **Problema que resolve:** adaptação de UI Compose a diferentes tamanhos, dispositivos e métodos de entrada.
- **Sobreposição com skills existentes:** detalha o baseline adaptativo citado em RF-005 e nos critérios de qualidade.
- **Aderência à arquitetura:** alta para projetos integralmente em Compose e Navigation 3; baixa quando os pré-requisitos não são atendidos.
- **Aderência ao processo Spec Driven:** parcial; APIs experimentais e migrações exigem decisão explícita da Change consumidora.
- **Impacto no contexto do agente:** alto, com referências e fluxos específicos de UI adaptativa.
- **Riscos:** introduzir Navigation 3 ou APIs experimentais sem aprovação e presumir Compose em telas legadas.
- **Evidências:** catálogo oficial e conteúdo do arquivo no commit analisado.
- **Decisão:** rejeitada para incorporação genérica neste incremento. Deve ser composta sob demanda após confirmar stack, form factors e critérios de aceite da aplicação.

## SKILL-CANDIDATE-005 — Android Native Engineering local

- **Classificação:** `CREATE`
- **Origem:** `.agents/skills/android-native-engineering/` neste repositório.
- **Mantenedor:** projeto local.
- **Versão/commit analisado:** Change `2026-09-18-067-android-native-engineering`; ainda sem commit de encerramento.
- **Licença:** aplica-se a licença do repositório local; nenhuma fonte oficial foi copiada.
- **Problema que resolve:** governança transversal de processo Spec Driven, perfis, decisões, evidências e roteamento para skills especializadas.
- **Sobreposição com skills existentes:** limitada; as skills oficiais analisadas são especializadas e não implementam os gates locais.
- **Aderência à arquitetura:** alta, porque preserva decisões contextuais e o menor perfil suficiente.
- **Aderência ao processo Spec Driven:** integral para o workflow local definido em `specs/shared/process/workflow.md`.
- **Impacto no contexto do agente:** baixo a médio, com progressive disclosure em cinco referências.
- **Riscos:** duplicar conhecimento oficial ou congelar recomendações temporais; mitigados por roteamento e consulta atualizada.
- **Evidências:** comparação entre o catálogo oficial, o workflow local, `DESIGN.md` e os artefatos implementados.
- **Decisão:** criar e manter a skill local como orquestradora, sem incorporar o conteúdo das candidatas oficiais neste incremento.

## Limitações atuais

- Android CLI não foi encontrado no PATH deste ambiente.
- Nenhum projeto laboratório Android foi criado nesta Change.
- Versões de SDK, AGP, Kotlin e Compose serão decididas por projeto mediante consulta atualizada.

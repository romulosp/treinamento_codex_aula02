# Planejar implementação da Change

Produza um plano técnico curto e rastreável para uma Change antes de sua implementação.

## Objetivo

Definir e registrar como a Change aprovada será implementada neste projeto, em `specs/changes/<id>-<nome>/implementation-plan.md`, sem alterar o contrato da Change ou da SPEC.

## Pré-condição

Realize este planejamento somente depois de a Change estar formalmente `SPEC_APROVADA` e antes da implementação. Esta é uma cerimônia preparatória e não substitui, renomeia ou acrescenta uma etapa ao workflow 01-06.

## Regras

1. Leia a Change, a SPEC, a estratégia de testes, as convenções de segurança e os documentos de contexto aplicáveis.
2. Analise o projeto e seus padrões existentes antes de propor o plano.
3. Identifique os componentes, módulos, camadas e arquivos provavelmente impactados.
4. Identifique integrações com banco de dados, APIs, contratos ou infraestrutura, quando aplicável.
5. Planeje testes unitários, testes de integração e validações compatíveis com os critérios de aceite da SPEC e com a Definition of Done da Sprint.
6. Para Java, relacione cada classe de produção impactada ao teste correspondente e preveja o uso da Skill `java-unit-test`, quando disponível. Para front-end, identifique os itens testáveis e, se não houver estrutura de testes, registre o impedimento ou a decisão necessária.
7. Identifique a cobertura esperada, respeitando o mínimo de 80%, a meta de 100% nas áreas novas ou alteradas quando viável, as métricas configuradas no projeto e o comando ou evidência reproduzível de aferição.
8. Planeje JavaDoc ou a documentação de código aplicável, bem como a atualização de README ou documentação de uso quando houver impacto relevante.
9. Planeje as verificações de qualidade, Sonar e cobertura configuradas no módulo, incluindo pré-requisitos operacionais, comando, token, dependências e evidências necessárias para confirmar os critérios da Definition of Done. Se não houver Sonar ou cobertura, ou se Docker, SonarQube, scanner, token ou dependência necessária estiverem indisponíveis, planeje a Auditoria de Qualidade Assistida por LLM: build, tipo, lint e testes disponíveis; mapeamento de artefatos alterados para testes aplicáveis; revisão de bugs, vulnerabilidades e hotspots de segurança, defeitos, tratamento de erro, duplicação, código morto, complexidade e documentação; e registro completo do motivo de fallback em `validation.md`. Falha de build, scanner ou Quality Gate após disponibilidade do Sonar não pode ser convertida em fallback.
10. Delimite a aplicabilidade da Skill `security-audit` antes da aprovação final. Execute-a quando a Change possuir artefato de frontend/backend, API, autenticação, autorização, configuração, dependência, segredo ou integração no escopo; para Change exclusivamente documental, planeje o registro da não aplicabilidade.
11. Planeje o registro das evidências em `validation.md` e, quando a auditoria for aplicável, do relatório PDF atual em `docs/security-audit/`, gerado por `docs/security-audit/gerar_relatorio.py`. Preveja a conferência do conteúdo e a redação de segredos; relatório anterior ou estático não substitui a auditoria atual e gerador incompatível com os resultados atuais é um bloqueio.
12. Preveja o ciclo de correção autônoma de achados confirmados: corrigir dentro do escopo, repetir revisões e validações necessárias e executar a auditoria novamente. Se a correção exigir mudança de SPEC, ação externa ou decisão fora do escopo, registre o bloqueio.
13. Registre riscos de segurança, de negócio e técnicos, suas evidências, impacto, tratamento proposto e decisões necessárias antes da implementação.
14. Diferencie fatos observados no projeto de suposições ou pontos a confirmar.
15. Crie ou atualize somente `implementation-plan.md` para registrar este resultado. Não implemente código, não altere automaticamente `proposal.md`, `spec.md` ou `DESIGN.md` e não crie requisitos novos.

## Resultado esperado

Crie ou atualize `implementation-plan.md` e apresente o plano curto e específico, identificado pela Change analisada, com as seções:

- **Fatos observados:** padrões e artefatos efetivamente encontrados;
- **Impactos prováveis:** componentes, arquivos e integrações afetados;
- **Estratégia de implementação:** sequência técnica proposta;
- **Testes, cobertura e qualidade:** testes unitários, testes de integração, cobertura esperada, documentação de código, Sonar configurado ou Auditoria de Qualidade Assistida por LLM planejada;
- **Auditoria de segurança:** escopo, evidências, relatório e ciclo de correção e reauditoria planejados;
- **Riscos, dúvidas e decisões necessárias:** incluindo impacto de negócio, segurança e suposições que precisam de confirmação.

# SPEC: 034-automatizar-sonar-local

## Status

`VALIDADA`

## Referências e dependências

- `AGENTS.md`
- `specs/shared/process/workflow.md`
- `specs/sprint/templates/template-sprint.md`
- `D:\desenvolvimento\sonar\docker-compose.yml`
- `D:\desenvolvimento\banco_dados\postgresql\docker-compose.yml`

## Requisitos funcionais

1. Deve existir `scripts/sonar/validar-codigo.ps1`, com ações `Tudo`, `Subir`, `Analisar`, `Status` e `Parar`; a ação padrão é `Tudo`.
2. O script deve verificar se o comando Docker e seu daemon estão acessíveis antes de invocar Docker Compose ou Docker Run.
3. Quando Docker estiver ausente ou inacessível, o script deve imprimir `SONAR_FALLBACK_LLM_REQUIRED: <motivo>` e encerrar com código `20`, sem tentar declarar a análise aprovada.
4. Antes de subir o Sonar, o script deve consultar a saúde do serviço PostgreSQL administrado pela composição em `D:\desenvolvimento\banco_dados\postgresql`. Se não estiver saudável, deve executar `docker compose up -d` nessa pasta e aguardar a saúde até o limite configurado.
5. Após o PostgreSQL estar saudável, o script deve garantir de forma idempotente o usuário e o banco `sonar` usados pelo SonarQube. A senha deve ser aleatória, persistida somente em `D:\desenvolvimento\sonar\.env` e nunca escrita nos logs, documentos ou código-fonte.
6. `D:\desenvolvimento\sonar\docker-compose.yml` deve obter usuário e senha JDBC exclusivamente de variáveis carregadas pelo arquivo `.env`; não pode conter senha JDBC fixa.
7. O script deve subir o SonarQube por Docker Compose, aguardar `http://localhost:9000/api/system/status` retornar estado `UP` e disponibilizar a ação `Status` para consultar a situação sem iniciar serviços.
8. A ação `Analisar` deve exigir `SONAR_TOKEN` apenas do ambiente de execução, sem persistir ou mostrar o valor. Ela deve identificar automaticamente os módulos em `apps/backend/*` com `pom.xml` e os módulos em `apps/frontend/web/*` com `package.json`.
9. Para cada módulo Java, a análise deve compilar o módulo antes do scanner e informar fontes de produção, testes e binários Java ao Sonar. Para cada front-end, deve executar o build disponível e analisar fontes HTML, CSS, JavaScript, JSX, TypeScript e TSX, excluindo dependências e artefatos gerados.
10. O scanner deve aguardar o Quality Gate. Quality Gate reprovado ou falha de build/scanner após Sonar estar disponível deve encerrar com erro diferente de `20` e impedir aprovação da Change.
11. Docker inacessível, PostgreSQL indisponível após a espera, SonarQube que não atinja `UP` ou token ausente devem emitir o marcador e código `20`, que obrigam a Auditoria de Qualidade Assistida por LLM.
12. A governança do Sprint Planner e o prompt de execução Spec Driven devem exigir a Auditoria de Qualidade Assistida por LLM quando Sonar/cobertura não estiverem configurados **ou** estiverem indisponíveis operacionalmente. A auditoria deve registrar escopo, arquivos, comandos, resultados, mapeamento de testes, achados e correções em `validation.md`; não pode alegar percentual de cobertura nem execução do Sonar.
13. Achado da auditoria LLM ou falha do Quality Gate impede `DONE` até correção e nova validação. A indisponibilidade operacional não é exceção automática nem aprovação.

## Requisitos não funcionais

1. O script e os documentos devem estar em português do Brasil e devem funcionar em Windows PowerShell 5.1.
2. A automação deve usar somente Docker Compose, conforme a infraestrutura existente, e não deve iniciar serviços visíveis adicionais.
3. Segredos não podem ser armazenados no repositório, exibidos no console ou registrados em `validation.md`.
4. O script deve reportar erros de modo objetivo e não mascarar falhas de build, scanner ou Quality Gate como fallback.

## Cenários e critérios de aceite

- [ ] CA-001: `validar-codigo.ps1` expõe as cinco ações previstas e pode ser analisado pelo parser do PowerShell sem erro.
- [ ] CA-002: com Docker inacessível, `validar-codigo.ps1 -Acao Tudo` retorna `20` e escreve o marcador `SONAR_FALLBACK_LLM_REQUIRED`, sem exibir segredo.
- [ ] CA-003: a composição do Sonar não contém senha JDBC literal e o script cria ou reutiliza `.env` sem imprimir sua senha.
- [ ] CA-004: o fluxo `Subir` verifica PostgreSQL, sobe a composição indicada se necessário, prepara o banco do Sonar e só inicia Sonar após a dependência ficar saudável.
- [ ] CA-005: o fluxo `Analisar` descobre os dois módulos Java e os dois front-ends existentes, usando configurações adequadas às suas linguagens e excluindo `node_modules`, `dist`, `coverage` e `target` da análise de fontes.
- [ ] CA-006: Quality Gate, build ou scanner reprovados retornam código diferente de `20`.
- [ ] CA-007: template, README e prompts do Sprint Planner, além do orquestrador Spec Driven, exigem auditoria LLM também para indisponibilidade operacional de Docker/Sonar e impedem `DONE` se ela estiver ausente ou com achado aberto.
- [ ] CA-008: teste Pester estrutural do script passa; a validação registra que a execução integrada ficou pendente se o daemon Docker permanecer inacessível.
- [ ] CA-009: nenhum arquivo de aplicação em `apps/` é alterado e `git diff --check` termina com código `0`.

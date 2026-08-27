---
name: "Executar mudança Spec Driven"
description: "Use quando: executar uma mudança Spec Driven de ponta a ponta, respeitando todos os gates e registrando evidências."
argument-hint: "Informe o caminho da mudança, por exemplo specs/changes/003-gerenciar-categorias"
agent: "agent"
---

# Execução integral de mudança Spec Driven

Execute a mudança indicada em `${input:changePath}` de ponta a ponta, obedecendo estritamente a [AGENTS.md](../../AGENTS.md) e ao [workflow canônico](../../specs/shared/process/workflow.md). Trabalhe em português do Brasil.

## Objetivo

Conduza a mudança pelas sete fases obrigatórias, sempre respeitando os gates:

`SPEC → revisão da SPEC → implementação → revisão da implementação → validação → aprovação → commit`.

A SPEC é a fonte da verdade. Execute somente a primeira fase ainda pendente e, quando ela for aprovada, continue para a fase seguinte. Interrompa imediatamente se um gate for reprovado, falhar ou estiver bloqueado.

## Preparação obrigatória

1. Leia `proposal.md`, `spec.md`, `DESIGN.md`, `tasks.md`, `validation.md`, todos os relatórios em `reviews/` e as regras compartilhadas aplicáveis.
2. Identifique o estado atual usando os documentos da mudança e [STATUS.md](../../STATUS.md).
3. Antes de executar qualquer comando Maven, configure a sessão do terminal para usar Java 17 e Maven 3.8.8. A configuração é temporária, vale somente para a sessão e não deve ser gravada em `pom.xml`, arquivos gerados ou variáveis permanentes do sistema.
	 - No Prompt de Comando do Windows, execute:

		 ```bat
		 set JAVA_HOME=C:\Desenvolvimento\jdk-17.0.11
		 set MAVEN_HOME=C:\Desenvolvimento\apache-maven-3.8.8
		 set PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%
		 ```

	 - No PowerShell, use a sintaxe equivalente:

		 ```powershell
		 $env:JAVA_HOME = 'C:\Desenvolvimento\jdk-17.0.11'
		 $env:MAVEN_HOME = 'C:\Desenvolvimento\apache-maven-3.8.8'
		 $env:PATH = "$env:JAVA_HOME\bin;$env:MAVEN_HOME\bin;$env:PATH"
		 ```

	 - Confirme as versões efetivas com `java -version` e `mvn -version` e registre-as em `validation.md` quando a fase de validação for executada.
4. Verifique as Skills exigidas para a próxima fase antes de agir.
5. Não altere código, contratos ou testes fora da fase permitida.

## Regras inegociáveis

- Nunca implemente antes de `proposal.md` e `spec.md` estarem ambos em `SPEC_APROVADA`.
- Não invente decisões, contratos ou requisitos para contornar pendências. Registre a evidência exigida e encerre se a fase estiver bloqueada.
- Revisões não corrigem código ou requisitos. Validação não corrige código. Aprovação não altera código nem executa testes. Commit não revalida a entrega.
- Não arquive a mudança nem crie commit sem relatório formal com estado `APROVADA`.
- Não inclua segredos, `target/` ou outros artefatos gerados no commit.
- Quando houver reprovação ou falha, indique objetivamente a primeira fase à qual a mudança deve retornar.

## Fases e gates

### 1. SPEC — definir o contrato

Se a mudança estiver em especificação (`RASCUNHO` ou equivalente), confirme que `proposal.md`, `spec.md`, `DESIGN.md` e `tasks.md` definem objetivo, escopo, restrições, comportamentos, critérios de aceite e cenários de validação. Não implemente nesta fase. Encaminhe o contrato para revisão.

### 2. Revisão da SPEC — verificar se o contrato é implementável

Execute a Skill `spec-review`. Avalie exclusivamente clareza, completude, consistência, escopo, riscos e testabilidade; não invente requisitos nem implemente soluções. Crie o relatório com achados `REV-*` em `reviews/`.

- Aprovada: atualize `proposal.md` e `spec.md` para `SPEC_APROVADA` e avance.
- Reprovada: registre `REPROVADA`, indique os achados bloqueantes e encerre.

### 3. Implementação — cumprir o contrato aprovado

Somente com `proposal.md` e `spec.md` em `SPEC_APROVADA`, execute a Skill `spec-implement`. Implemente exclusivamente os artefatos, contratos e comportamentos aprovados. Crie ou atualize testes de comportamento observável e atualize `tasks.md`.

- Concluída: registre `IMPLEMENTADA` e avance.
- Bloqueada: registre `BLOQUEADA`, a causa e a fase de retorno; encerre.

### 4. Revisão da implementação — comparar entrega e SPEC

Com a mudança em `IMPLEMENTADA`, execute a Skill `implementation-review`. Compare código, configurações, dependências, contratos, testes, critérios de aceite e alterações indevidas contra a SPEC aprovada. Não corrija nada durante a revisão. Salve o relatório com achados `IMP-REV-*` em `reviews/`.

- Aprovada: registre `IMPLEMENTACAO_APROVADA` e avance.
- Reprovada: registre `REPROVADA`, indique as divergências e retorne à implementação; encerre.

### 5. Validação — provar o comportamento por evidências

Somente com a implementação aprovada, execute a Skill `implementation-validate`. Execute todos os testes e verificações aplicáveis e confronte seus resultados com os cenários da SPEC. Em `validation.md`, registre ambiente, versões, comandos, códigos de saída, cenários, resultados e evidências `VAL-*`. Não corrija código nesta fase.

- Êxito: registre `VALIDADA` e avance.
- Falha: registre a evidência, a causa e a fase de retorno; encerre.

### 6. Aprovação — decidir formalmente o encerramento técnico

Somente com a mudança validada, execute a Skill `change-approve`. Não implemente, corrija ou teste nesta fase. Verifique que não existem pendências materiais, falhas ou achados bloqueantes e que os gates anteriores correspondem a:

| Fase | Estado obrigatório |
| --- | --- |
| Revisão da SPEC | `SPEC_APROVADA` |
| Implementação | `IMPLEMENTADA` |
| Revisão da implementação | `IMPLEMENTACAO_APROVADA` |
| Validação | `VALIDADA` |

- Aprovada: produza o relatório formal, atualize [STATUS.md](../../STATUS.md) para `APROVADA` e avance.
- Reprovada: informe a primeira fase que deve ser retomada e encerre.

### 7. Commit — fechar a entrega aprovada

Somente com relatório de aprovação em `APROVADA`, execute a Skill `git-commit`. Atualize `specs/system/`, prepare o arquivamento, revise os arquivos incluídos e crie um único commit rastreável. Depois do commit bem-sucedido, mova a mudança para `archive/`, atualize [STATUS.md](../../STATUS.md) para `ARQUIVADA` e registre o hash no relatório de aprovação.

## Resultado esperado

Ao encerrar, informe a fase alcançada, o status final, os arquivos de evidência criados ou atualizados, comandos e testes executados com seus códigos de saída, bloqueios encontrados, a fase de retorno se houver reprovação e o hash do commit quando houver. Não declare sucesso se algum gate obrigatório não tiver sido concluído.
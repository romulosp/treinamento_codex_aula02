# SPEC: 031-criar-skill-java-javadoc

## Status

`SPEC_APROVADA`

## Referências e dependências

- `proposal.md`
- `DESIGN.md`
- `tasks.md`
- `AGENTS.md`
- `.agents/skills/README.md`
- `specs/shared/process/workflow.md`

## Requisitos funcionais

### RF-001 — Estrutura e descoberta

1. A implementação deve criar a Skill no caminho `.agents/skills/java-javadoc/SKILL.md`.
2. O frontmatter deve declarar `name: java-javadoc` e uma descrição em português do Brasil que selecione a Skill quando código Java for criado ou modificado e JavaDoc puder ser afetado.
3. A Skill deve manter a descoberta automática habilitada; não deve configurar política de acionamento exclusivamente explícito.
4. O catálogo `.agents/skills/README.md` deve listar `java-javadoc` na seção de Skills técnicas, com finalidade e exemplo de uso em português do Brasil.

### RF-002 — Documentação de contratos Java

1. A Skill deve instruir a documentar classes, interfaces, enums, records, annotations, construtores e métodos somente quando a documentação agregar compreensão do contrato, comportamento, restrições ou relação relevante.
2. Para métodos, a Skill deve orientar o uso de `@param`, `@param <T>`, `@return`, `@throws`, `@deprecated`, `{@code ...}`, `{@link ...}` e `{@inheritDoc}` quando aplicáveis ao contrato demonstrado.
3. A documentação de parâmetros, tipos genéricos, retornos e exceções deve explicar significado, pré-condições, nullability, coleções vazias, mutabilidade e condição de falha somente quando essas características forem comprovadas.
4. Efeitos colaterais, concorrência, thread-safety, comportamento transacional e relações entre componentes só devem ser documentados quando identificáveis por implementação, interfaces, annotations, especificações, testes, contratos, tipos ou JavaDoc existente.
5. Métodos deprecated devem ter documentação de depreciação coerente com a annotation ou contrato existente e usar `@deprecated` quando aplicável.

### RF-003 — Precisão e manutenção

1. A Skill deve estabelecer que a documentação explique contrato e comportamento, não traduza mecanicamente nomes de símbolos nem descreva instruções Java evidentes.
2. A Skill não deve permitir afirmações sem evidência; quando uma característica não puder ser comprovada, ela deve ser omitida ou apresentada como questão a esclarecer, sem ser transformada em fato no JavaDoc.
3. Ao alterar código Java com JavaDoc existente, a Skill deve orientar a preservá-lo quando válido, atualizá-lo conforme a mudança, remover conteúdo obsoleto e adicionar documentação para novos contratos comprovados.
4. Comentários internos devem ser limitados a decisões não óbvias, regras de negócio, algoritmos, workarounds, restrições técnicas ou justificativas relevantes; devem priorizar o motivo da decisão.

## Requisitos não funcionais

- **Idioma:** todo texto produzido pela Skill deve ser em português do Brasil; identificadores e termos técnicos podem permanecer em inglês quando isso preservar precisão técnica.
- **Escopo:** a Skill deve ser autocontida em `SKILL.md`; não criar recursos auxiliares sem necessidade comprovada.
- **Compatibilidade:** a Skill deve seguir o padrão local de diretório de primeiro nível com `SKILL.md` como entrada.
- **Qualidade:** o arquivo deve possuir frontmatter YAML válido, nome em kebab-case e não conter marcadores de scaffold.

## Regras de negócio

- Precisão é mais importante que quantidade de JavaDoc.
- A ausência de evidência não autoriza a LLM a assumir validações, persistência, chamadas externas, garantias de não nulidade, exceções, transações ou thread-safety.
- A Skill complementa, sem substituir, instruções de Skills específicas de Quarkus, persistência ou testes.

## Cenários e critérios de aceite

- [ ] **CA-001:** `.agents/skills/java-javadoc/SKILL.md` existe, possui frontmatter válido e declara o nome `java-javadoc`.
- [ ] **CA-002:** a descrição permite descoberta para criação ou modificação de Java e não indica acionamento exclusivamente explícito.
- [ ] **CA-003:** as instruções cobrem os elementos, tags e condições de documentação definidos em RF-002.
- [ ] **CA-004:** as instruções exigem evidência e proíbem documentação especulativa, conforme RF-003.
- [ ] **CA-005:** as instruções abordam atualização de JavaDoc existente e comentários internos conforme RF-003.
- [ ] **CA-006:** `.agents/skills/README.md` cataloga a nova Skill com finalidade e exemplo de acionamento em português do Brasil.
- [ ] **CA-007:** a validação registra o comando, ambiente, resultado e código de saída das verificações estruturais; testes Java/Maven são explicitamente marcados como não aplicáveis.

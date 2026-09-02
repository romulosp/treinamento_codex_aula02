# SPEC: 032-documentar-java

## Status

`SPEC_APROVADA`

## Referências e dependências

- `proposal.md`
- `DESIGN.md`
- `tasks.md`
- `.agents/skills/java-javadoc/SKILL.md`
- `specs/shared/testing/testing-strategy.md`
- Inventário inicial: 40 arquivos, 30 em `src/main/java` e 10 em `src/test/java`, nos módulos `gerenciarcategorias` e `gerenciartarefas`.

## Requisitos funcionais

### RF-001 — Cobertura do inventário

1. Cada arquivo `.java` encontrado nos diretórios de escopo deve receber documentação JavaDoc adequada ao seu tipo e membros relevantes.
2. A implementação deve repetir o inventário antes da alteração e registrar inclusões ou exclusões com justificativa.
3. Nenhum arquivo Java de produção ou teste do escopo pode ser omitido silenciosamente.

### RF-002 — Qualidade e precisão

1. JavaDoc deve ser escrito em português do Brasil e explicar contrato, comportamento, restrições e relações relevantes, sem tradução mecânica de nomes.
2. Classes, interfaces, enums, records, annotations, construtores e métodos devem ser documentados quando houver contrato ou contexto útil.
3. `@param`, `@param <T>`, `@return`, `@throws`, `@deprecated`, `{@code}`, `{@link}` e `{@inheritDoc}` devem ser usados somente quando aplicáveis e comprovados.
4. Nullability, coleções vazias, mutabilidade, efeitos colaterais, concorrência, thread-safety e transações só podem ser afirmados quando evidenciados.
5. JavaDoc existente deve ser preservado e atualizado quando a implementação ou contrato exigir.

### RF-003 — Preservação funcional

1. Não alterar lógica, assinaturas, annotations, imports funcionais, configuração ou testes, exceto comentários JavaDoc e comentários internos autorizados.
2. Comentários internos devem explicar somente decisões não óbvias, regras, algoritmos, workarounds ou restrições.

## Requisitos não funcionais

- **Rastreabilidade:** registrar arquivos alterados, comandos, ambiente, resultados e códigos de saída em `validation.md`.
- **Testabilidade:** executar os testes Maven dos dois módulos; não declarar cobertura sem medição reproduzível.
- **Segurança:** não adicionar segredos, dependências ou artefatos gerados.

## Cenários e critérios de aceite

- [ ] **CA-001:** o inventário pós-implementação contém todos os 40 arquivos inicialmente encontrados ou documenta cada diferença.
- [ ] **CA-002:** cada arquivo do escopo contém JavaDoc útil para seu tipo e membros aplicáveis, sem afirmações não comprovadas.
- [ ] **CA-003:** o diff funcional mostra somente alterações de comentários/documentação, sem mudanças de lógica ou API.
- [ ] **CA-004:** JavaDoc está em português do Brasil e usa tags aplicáveis corretamente.
- [ ] **CA-005:** testes Maven aplicáveis dos dois módulos passam, com ambiente, comandos e códigos registrados.
- [ ] **CA-006:** não há arquivos gerados, segredos ou alterações fora do escopo.

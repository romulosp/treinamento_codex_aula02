# KDoc obrigatório

KDoc é a documentação de API do Kotlin, equivalente ao JavaDoc. Nesta skill, documentação é parte do código e do contrato aprovado.

## Escopo obrigatório

Ao criar ou alterar código Kotlin de produção:

1. Documente com KDoc toda classe, interface, object, annotation, enum, função, propriedade, construtor ou typealias público ou protegido.
2. Documente declarações `internal` ou `private` quando contiverem regra de negócio, contrato arquitetural, concorrência, segurança, efeitos colaterais, integração externa ou comportamento que o nome e o tipo não tornam evidente.
3. Atualize o KDoc sempre que o contrato ou comportamento observável mudar.
4. Em overrides, omita novo KDoc somente quando a documentação herdada continuar integralmente correta e não houver pré-condição, pós-condição, efeito ou erro adicional.

Código gerado e implementações privadas triviais podem ser excluídos. Registre a justificativa no inventário de validação; não use exclusões para ocultar contratos relevantes.

## Conteúdo do contrato

Escreva em português do Brasil e documente somente fatos comprovados pela SPEC e pelo código. Inclua quando aplicável:

- propósito e responsabilidade da declaração;
- pré-condições, pós-condições e invariantes;
- parâmetros, propriedades, tipos genéricos e receiver de extensão;
- significado do retorno, inclusive nulabilidade e estados especiais;
- erros ou exceções observáveis relevantes;
- efeitos colaterais, persistência, rede, logs e alterações de estado;
- requisitos de thread, dispatcher, cancelamento, lifecycle e características de `Flow`;
- segurança, permissões e tratamento de dados sensíveis;
- exemplo ou referência relacionada quando reduzir ambiguidade.

Para Composables, descreva responsabilidade visual, estado recebido, eventos emitidos e efeitos colaterais. Para ViewModels, repositories e casos de uso, explicite fronteiras, fonte de verdade e comportamento de erro quando fizerem parte do contrato.

## Sintaxe e estilo

- Use comentários `/** ... */` imediatamente antes da declaração.
- Prefira uma primeira frase curta como resumo e detalhes adicionais em parágrafos seguintes.
- Use links como `[Tipo]` ou `[função]` para símbolos Kotlin quando ajudarem a navegação.
- Use `@param`, `@property`, `@return`, `@receiver`, `@throws`, `@sample`, `@see` e `@since` somente quando acrescentarem clareza; prefira texto fluido para contratos curtos.
- Use `@Deprecated` no código em vez de uma tag KDoc `@deprecated`.
- Não repita mecanicamente nome, tipo ou implementação. Não invente garantias, exceções ou exemplos.
- Não inclua segredos, tokens, PAN, dados pessoais ou detalhes operacionais sensíveis.

## Validação

Antes de concluir uma Change Kotlin:

1. Inventarie os arquivos `.kt` de produção criados ou alterados.
2. Para cada declaração aplicável, registre onde o KDoc foi criado ou atualizado.
3. Para cada exclusão, registre a declaração e a justificativa objetiva.
4. Revise links, nulabilidade, erros, efeitos colaterais e contratos de concorrência contra a implementação.
5. Execute a tarefa Dokka existente quando o projeto já a configurar ou quando a SPEC exigir documentação publicada. Descubra o nome real da tarefa com o Gradle Wrapper; não presuma `dokkaGenerate` nem instale Dokka automaticamente.

Ausência de Dokka não dispensa KDoc nem a revisão do inventário.

## Fontes oficiais

- KDoc: https://kotlinlang.org/docs/kotlin-doc.html
- Convenções de código Kotlin: https://kotlinlang.org/docs/coding-conventions.html
- Dokka: https://kotlinlang.org/docs/dokka-introduction.html

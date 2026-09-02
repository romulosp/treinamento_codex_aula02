---
name: java-javadoc
description: Crie ou atualize JavaDoc em português do Brasil ao criar ou modificar código Java, documentando contratos comprovados de classes, tipos e métodos sem inventar comportamento. Use em conjunto com Skills específicas de Quarkus, persistência ou testes quando elas também se aplicarem.
---

# JavaDoc para código Java

Produza JavaDoc que torne o contrato do código mais compreensível e permaneça sincronizado com a implementação. Priorize precisão e evidências sobre quantidade de comentários.

## Baseie a documentação em evidências

Antes de escrever ou alterar JavaDoc, examine a implementação, assinaturas, interfaces, annotations, tipos usados, JavaDoc existente, especificações, testes e contratos dos colaboradores.

- Documente somente o que essas fontes comprovam.
- Não presuma validações, exceções, persistência, chamadas externas, publicação de eventos, cache, acesso a arquivos, transações, thread-safety, nulidade ou mutabilidade.
- Quando uma característica relevante não puder ser comprovada, não a apresente como fato no JavaDoc. Solicite esclarecimento apenas se ela for necessária para cumprir uma alteração pedida; caso contrário, omita-a.
- Escreva em português do Brasil. Preserve identificadores, nomes de APIs, annotations, padrões, protocolos e termos técnicos em inglês quando a tradução reduzir a precisão.

## Decida o que documentar

Documente classes, interfaces, enums, records e annotations quando seu propósito, invariantes, ciclo de vida, uso esperado ou relação com outros componentes não forem evidentes apenas pelo nome e pela assinatura. Documente construtores e métodos públicos ou protegidos quando seu contrato, pré-condições, resultado, falhas ou efeitos relevantes precisarem de contexto.

Não crie JavaDoc apenas para repetir um nome. Por exemplo, `@param id id do usuário` não agrega informação; prefira descrever a semântica e as restrições comprovadas de `id`.

Para tipos genéricos, explique o papel de cada parâmetro de tipo com `@param <T>` quando ele fizer parte do contrato. Para componentes relacionados, use `{@link ...}` quando o vínculo for relevante e o destino puder ser identificado corretamente.

## Documente contratos de métodos

Use as tags somente quando aplicáveis:

- `@param`: informe a função do argumento e, se comprovadas, pré-condições como não nulidade, faixa válida, formato ou estado aceito.
- `@param <T>`: informe o significado do parâmetro de tipo genérico.
- `@return`: explique o significado do resultado e, quando demonstrado, se pode ser `null`, se uma coleção pode ser vazia e se o resultado é mutável ou imutável.
- `@throws`: descreva a condição concreta que causa a exceção. Não use descrições genéricas como “erro de parâmetro”.
- `@deprecated`: use quando a API estiver deprecated e explique a substituição apenas se ela estiver definida por annotation, contrato ou código. Não invente uma alternativa.
- `{@code ...}`: use para valores Java, expressões e nomes que devem permanecer literais.
- `{@link ...}`: use para relacionar tipos ou membros cujo acesso ajuda a entender o contrato.
- `{@inheritDoc}`: use apenas quando o contrato herdado se aplicar integralmente. A presença de `@Override` por si só não prova que nenhuma regra foi alterada; complemente ou substitua a documentação quando houver comportamento específico comprovado.

Descreva efeitos colaterais somente quando a evidência os identificar, como persistir dados, chamar outro serviço, operar em rede, publicar eventos, atualizar cache, escrever arquivos ou alterar estado interno. Da mesma forma, mencione concorrência, thread-safety ou comportamento transacional apenas quando forem demonstrados por contratos, annotations ou implementação.

## Mantenha a documentação sincronizada

Ao modificar código Java existente:

1. Preserve JavaDoc válido que continua coerente com o contrato.
2. Atualize as partes afetadas por mudanças de assinatura, comportamento, retorno, exceção, efeito colateral ou relacionamento.
3. Remova afirmações obsoletas ou incompatíveis com o código atualizado.
4. Adicione JavaDoc para contratos novos quando houver evidência suficiente.

Não altere o comportamento do código para corresponder a uma hipótese da documentação. Se código e JavaDoc entrarem em conflito, trate a implementação, a especificação e os testes como evidências a reconciliar; não escolha uma interpretação sem base.

## Comentários internos

Use comentários internos somente para explicar decisões não óbvias, regras de negócio, algoritmos, workarounds, restrições técnicas ou a razão de uma implementação. Evite comentários que apenas narrem instruções Java evidentes. Prefira explicar por que a decisão existe, não o que cada linha faz.

## Exemplo orientado por evidência

Quando implementação, testes ou contrato comprovarem as condições, um método pode ser documentado assim:

```java
/**
 * Localiza um usuário pelo identificador informado.
 *
 * @param id identificador único do usuário; não pode ser {@code null} e deve ser maior que zero
 * @return usuário associado ao identificador informado; nunca {@code null}
 * @throws IllegalArgumentException se {@code id} for {@code null} ou menor ou igual a zero
 * @throws UsuarioNaoEncontradoException se não existir usuário associado ao identificador informado
 */
public Usuario buscarUsuario(Long id) {
    // ...
}
```

Esse exemplo não autoriza assumir essas validações, exceção, retorno ou persistência em outros métodos. Adapte o JavaDoc exclusivamente ao comportamento comprovado no caso em questão.

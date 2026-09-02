# Design: 032-documentar-java

## Contexto

O repositório contém dois módulos backend independentes, organizados em camadas de API, aplicação, domínio e infraestrutura. A Skill `java-javadoc` define documentação baseada em evidências e será aplicada sem alterar a lógica.

## Decisões

1. Abranger produção e testes, conforme o pedido de documentar todos os `.java`.
2. Priorizar JavaDoc de tipos e membros públicos/protegidos; documentar membros de teste e privados quando seu contrato ou decisão não for evidente.
3. Fazer alterações exclusivamente em comentários e JavaDoc, verificando o diff para detectar qualquer mudança funcional.
4. Executar testes Maven com Java 17 e Maven 3.8.8 conforme o prompt operacional, se disponíveis.

## Arquitetura e componentes

```text
apps/backend/gerenciarcategorias/src/{main,test}/java/**/*.java
apps/backend/gerenciartarefas/src/{main,test}/java/**/*.java
        └── JavaDoc em pt-BR, sem alteração de comportamento
```

## Alternativas e consequências

- Documentar somente produção foi descartado porque não atenderia “todo `.java`”.
- Gerar documentação a partir de nomes sem inspecionar o código foi descartado por risco de inventar contratos.
- Introduzir ferramenta ou dependência de lint foi descartado; a validação deve permanecer proporcional e local.

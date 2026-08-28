# Design: 021-definir-diretorio-projeto-gerado

## Contexto

O laboratório passou a suportar mais de uma aplicação independente, mas a convenção anterior colocava o código diretamente em `apps/backend/`. A solução separa o contêiner local dos diretórios de projeto.

## Decisões

| Entrada | Resultado |
| --- | --- |
| `artifactId` | preserva hífens, por exemplo `gerenciar-categorias` |
| diretório-raiz local | `apps/backend/` |
| diretório do projeto | `apps/backend/<artifactId-sem-hifens>/` |
| pacote-base Java | `br.com.romulopenha.<artifactId-sem-hifens>` |
| exemplo categorias | `apps/backend/gerenciarcategorias/` |
| exemplo tarefas | `apps/backend/gerenciartarefas/` |

## Arquitetura e componentes

Cada diretório de projeto é um módulo Maven autônomo:

```text
apps/backend/
├── gerenciarcategorias/
│   ├── pom.xml
│   ├── start_aplicacao.bat
│   └── src/
└── gerenciartarefas/
    ├── pom.xml
    ├── start_aplicacao.bat
    └── src/
```

O caminho de trabalho é calculado uma única vez a partir do `artifactId` e reutilizado para criação, geração, teste, inicialização e limpeza. Nenhum arquivo específico de uma aplicação é escrito diretamente em `apps/backend/`.

## Alternativas e consequências

- Manter `apps/backend/` como módulo único foi rejeitado porque mistura aplicações independentes.
- Usar o nome público com hífens como nome da pasta foi rejeitado para manter a convenção solicitada e permitir o mesmo identificador normalizado no pacote Java.
- A organização em múltiplos módulos Maven foi descartada: cada aplicação continua autônoma e não é criado um `pom.xml` agregador.

A principal consequência é que toda execução deve informar ou acessar a pasta específica do projeto; comandos executados na raiz `apps/backend/` deixam de ser válidos para uma aplicação.

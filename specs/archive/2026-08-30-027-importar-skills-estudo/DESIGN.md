# Design: 027-importar-skills-estudo

## Contexto

As skills do projeto são descobertas diretamente em `.agents/skills/`, onde cada diretório de primeiro nível contém o respectivo `SKILL.md`. A origem usa um agrupamento adicional por categoria, que é útil para documentação, mas incompatível com esse padrão de descoberta.

## Referências

- `proposal.md`
- `spec.md`
- `.agents/skills/README.md`
- `README.md`

## Decisões

1. Manter os diretórios das skills importadas diretamente em `.agents/skills/` para conservar o padrão vigente.
2. Remover apenas o nível categorizador do caminho de destino; todo o conteúdo interno de cada skill será preservado literalmente.
3. Usar o catálogo de `.agents/skills/README.md` para conservar a classificação de origem sem comprometer a descoberta.
4. Validar a cópia por inventário de caminhos relativos e contagem de arquivos; nenhum script fornecido pelas skills será executado.
5. Atualizar o README raiz somente após a cópia ser confirmada, para não documentar recursos inexistentes.

## Arquitetura e componentes

```text
D:\desenvolvimento\ia\estudo\skills\<categoria>\<skill>\
  └─ cópia recursiva, preservada
     .agents\skills\<skill>\
       ├─ SKILL.md
       └─ recursos auxiliares

.agents\skills\README.md  ← catálogo por categoria
README.md                  ← link para o catálogo completo
```

## Alternativas e consequências

- Preservar as categorias como diretórios intermediários reduziria colisões visuais, mas impediria a descoberta homogênea das skills. Foi descartado.
- Copiar somente `SKILL.md` reduziria o volume, mas quebraria skills que dependem de referências, scripts, assets ou licenças. Foi descartado.
- Executar validadores e scripts próprios das skills excederia o escopo e poderia exigir dependências externas. Foi descartado.

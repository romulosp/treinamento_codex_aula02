# Design: 010-reinicializar-workspace-documentado

## Contexto

A mudança substitui a árvore executável por documentação suficiente para reproduzir o ciclo de geração. Diferentemente da mudança 005, que removia somente produtos de build preservando fontes, esta mudança deliberadamente remove todo produto não textual do diretório de trabalho.

## Referências

- `spec.md`
- `specs/shared/process/workflow.md`
- `specs/shared/architecture/backend-java.md`

## Decisões

1. `NotasProjeto.md` será a fonte de orientação operacional na raiz, complementar às fontes normativas mantidas em `AGENTS.md` e `specs/shared/`.
2. O padrão principal de `.gitignore` será `*`, seguido de exceções recursivas para diretórios e arquivos `.md` e `.txt`; uma exceção explícita preservará `.gitignore`.
3. As regras específicas para produtos locais serão mantidas para tornar a intenção clara, embora já sejam abrangidas pela política geral.
4. A limpeza removerá fisicamente os arquivos não textuais versionados e o diretório `target` ignorado. Ela não executará ferramentas de build.
5. A remoção do módulo `apps/backend/` será verificada por ausência de diretório, em vez de testes Maven, pois não haverá código executável a testar.

## Arquitetura e componentes

- `NotasProjeto.md`: síntese do produto e manual de reprodução.
- `.gitignore`: política de retenção somente documental.
- `apps/backend/`: módulo gerado removido.
- `specs/changes/010-reinicializar-workspace-documentado/`: evidência da mudança até seu encerramento.

## Alternativas e consequências

- Manter somente `target/` removido foi descartado, pois não atenderia ao pedido de reinicializar a geração nem à política de não armazenar código gerado.
- Usar `.git/info/exclude` foi descartado porque a política precisa ser compartilhada pelo repositório.
- Ignorar somente extensões de build foi descartado porque fontes e scripts gerados continuariam rastreáveis.

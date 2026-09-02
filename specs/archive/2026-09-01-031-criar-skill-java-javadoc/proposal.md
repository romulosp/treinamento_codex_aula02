# Proposta: 031-criar-skill-java-javadoc

## Status

`SPEC_APROVADA`

## Responsável e data

- Responsável: equipe do projeto
- Data: 2026-09-01

## Referências

- `AGENTS.md`
- `specs/shared/process/workflow.md`
- `.agents/skills/README.md`
- `specs/archive/2026-08-30-027-importar-skills-estudo/`

## Problema e objetivo

O acervo local não possui uma Skill especializada para manter JavaDoc preciso e útil quando código Java é criado ou alterado. O objetivo é disponibilizar a Skill `java-javadoc`, em português do Brasil, para orientar a documentação de contratos Java sem inferir comportamentos não comprovados.

## Escopo

- Criar `.agents/skills/java-javadoc/SKILL.md` com instruções para documentar e atualizar JavaDoc de código Java.
- Cobrir tipos, construtores, métodos, parâmetros, genéricos, retornos, exceções, nullability, efeitos colaterais, concorrência, transações, depreciação e relações entre componentes quando houver evidência no código ou em seus contratos.
- Orientar o uso correto de tags e inline tags JavaDoc aplicáveis.
- Atualizar o catálogo `.agents/skills/README.md` com nome, finalidade e exemplo de acionamento da Skill.

## Fora de escopo

- Alterar JavaDoc de classes da aplicação existente.
- Alterar código Java, APIs, testes, dependências ou configuração do projeto.
- Criar regras que autorizem inventar comportamento, validações, garantias de concorrência ou semântica transacional.
- Modificar a change arquivada `2026-08-30-027-importar-skills-estudo`.

## Impactos e riscos

- A Skill será descoberta automaticamente em tarefas de criação ou alteração de código Java; por isso a descrição deve ser específica para não conflitar com as Skills Java já existentes.
- Documentação excessiva ou especulativa reduz a confiabilidade; a Skill deve privilegiar evidência e omitir afirmações não verificáveis.
- A mudança adiciona apenas instruções e catálogo; não produz alteração observável no software Java.

## Critérios para aprovação da SPEC

- A SPEC define a localização, o nome e o mecanismo de descoberta compatíveis com o padrão local.
- Os critérios de aceite verificam a cobertura requerida, a regra de não invenção e o registro no catálogo.
- O desenho preserva a separação entre a nova Skill e as Skills técnicas Java existentes.

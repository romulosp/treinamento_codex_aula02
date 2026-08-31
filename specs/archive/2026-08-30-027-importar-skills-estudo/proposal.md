# Proposta: 027-importar-skills-estudo

## Status

`SPEC_APROVADA`

## Responsável e data

- Responsável: equipe do projeto
- Data: 2026-08-30

## Referências

- `AGENTS.md`
- `specs/shared/process/workflow.md`
- `.agents/skills/README.md`
- `README.md`
- Origem: `D:\desenvolvimento\ia\estudo\skills`

## Problema e objetivo

O laboratório já possui skills de processo e de backend, mas não disponibiliza o conjunto reutilizável de skills existente no diretório de estudo. O objetivo é importar integralmente as 42 skills identificadas na origem para `.agents/skills/`, preservando seus recursos auxiliares e tornando o catálogo descobrível na documentação do projeto.

## Escopo

- Copiar recursivamente para `.agents/skills/<nome-da-skill>/` cada diretório de origem que contém `SKILL.md`.
- Preservar a estrutura e os arquivos de cada skill, inclusive `references/`, `scripts/`, `assets/`, `templates/`, `rules/`, `README.md`, `AGENTS.md` e `LICENSE.txt` quando existentes.
- Importar as 42 skills das categorias de arquitetura, criação, decisão, desenvolvimento, design, ferramentas, performance, qualidade e segurança.
- Atualizar `.agents/skills/README.md` com um catálogo por categoria, nome e finalidade resumida.
- Atualizar o `README.md` raiz para apontar ao catálogo e informar a ampliação do acervo local.

## Fora de escopo

- Alterar o conteúdo funcional das skills importadas.
- Executar scripts auxiliares, instalar dependências ou configurar integrações externas das skills.
- Substituir as 11 skills já existentes em `.agents/skills/`.
- Alterar código, APIs, banco de dados, aplicações Java ou o processo Spec Driven.
- Declarar disponibilidade de uma skill antes de sua cópia ter sido concluída e validada.

## Impactos e riscos

- A importação adicionará aproximadamente 2 MB de material instrucional e auxiliar ao repositório.
- Quatro skills possuem `LICENSE.txt`; a preservação integral evita perda de atribuição ou de termos de uso.
- Algumas skills são voltadas a React, React Native, Nx, Astro e ferramentas que não pertencem ao backend atual; elas serão apenas disponibilizadas, sem adoção implícita.
- Nomes das skills são únicos em relação às skills atuais; a implementação deve interromper e registrar qualquer colisão não prevista.

## Critérios para aprovação da SPEC

- A SPEC define origem, destino, número de skills, preservação recursiva e tratamento de colisões.
- O catálogo de skills e as categorias estão verificáveis.
- Os critérios de aceite distinguem cópia de arquivos, integridade, documentação e não execução de scripts.
- O plano não amplia o escopo para adoção técnica das skills importadas.

# Design: 031-criar-skill-java-javadoc

## Contexto

As Skills locais são descobertas diretamente em `.agents/skills/<nome-da-skill>/SKILL.md`. A change `027-importar-skills-estudo`, já arquivada, consolidou esse padrão e usa `.agents/skills/README.md` como catálogo. A nova Skill deve se integrar a esse modelo, sem alterar a change encerrada nem as Skills existentes.

## Referências

- `proposal.md`
- `spec.md`
- `.agents/skills/README.md`
- `.agents/skills/java-unit-test/SKILL.md`
- `.agents/skills/java-quarkus-resource/SKILL.md`

## Decisões

1. Criar `java-javadoc` diretamente em `.agents/skills/`, pois esse é o mecanismo local de descoberta.
2. Manter as regras em um único `SKILL.md`, pois não há referência, script ou asset reutilizável que justifique aumentar a estrutura.
3. Usar descoberta automática: o frontmatter terá descrição específica de criação e alteração de Java com impacto em JavaDoc; não haverá `agents/openai.yaml` nem política que bloqueie acionamento implícito.
4. Organizar as instruções por evidência, decisão de documentar, conteúdo de contratos, atualização de documentação existente e comentários internos.
5. Registrar somente uma entrada sucinta no catálogo para tornar a Skill localizável por pessoas, mantendo as instruções operacionais no `SKILL.md`.

## Arquitetura e componentes

```text
.agents/skills/
├── java-javadoc/
│   └── SKILL.md                ← instruções operacionais em pt-BR
├── java-unit-test/             ← Skill existente, complementar
├── java-quarkus-resource/      ← Skill existente, complementar
└── README.md                   ← catálogo e exemplo de acionamento
```

## Alternativas e consequências

- Alterar a change `027-importar-skills-estudo` foi descartado: ela está arquivada e seu escopo proibia modificar conteúdo funcional das Skills importadas.
- Criar arquivos de referência foi descartado: a orientação solicitada é especializada, mas cabe de forma coesa no arquivo de entrada e referências adicionais aumentariam a carga de contexto sem benefício verificável.
- Tornar a Skill explícita foi descartado: o objetivo exige que ela seja considerada sempre que Java for criado ou modificado, sem impedir a seleção automática.

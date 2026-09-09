# Design: 035-skill-golang

## Contexto

O laboratório possui documentos de referência que organizam backends em `api`, `application`, `domain` e `infrastructure`, separam contratos HTTP de entidades de persistência e exigem evidências de teste, segurança e qualidade. Esses documentos não representam uma dependência de código Java: não há projeto Java necessário nesta Change. Golang deve oferecer a mesma governança, em uma especificação autocontida, sem transformar padrões idiomáticos de Go em cópias de JPA, Maven ou Quarkus.

## Decisões

1. **Skill:** a entrada operacional será `.agents/skills/backend-golang/SKILL.md`, com instruções para ler a SPEC aprovada, gerar somente o perfil solicitado, inventariar arquivos Go e interromper em caso de gate ausente.
2. **Skills complementares:** os 30 blocos do anexo serão separados em diretórios operacionais próprios, conforme `skill-import-manifest.md`. A importação preservará o conteúdo entregue e não criará referências auxiliares inexistentes.
3. **Perfis e destinos:** o perfil `api` será gerado em `apps/api/<artifactId-sem-hifens>/`; o perfil `desktop` será gerado em `apps/desktop/<artifactId-sem-hifens>/`. Ambos compartilharão domínio e aplicação, mas o perfil `api` adicionará o adaptador HTTP.
4. **Camadas:** a organização padrão será:
   - `cmd/<nome-do-aplicativo>/`: ponto de entrada e composição da aplicação;
   - `internal/api/`: handlers, DTOs, validação de entrada, mapeamento de erros e documentação REST;
   - `internal/application/`: casos de uso e orquestração;
   - `internal/domain/`: entidades, value objects, regras e portas de repositório;
   - `internal/infrastructure/`: GORM, configurações, logging, observabilidade e integrações técnicas.
5. **REST:** Gin será o adaptador HTTP de referência. O contrato permanecerá orientado por `net/http`, DTOs e OpenAPI, permitindo trocar o framework sem mover regras de domínio.
6. **Persistência:** GORM será a implementação inicial da porta de persistência relacional. O repositório deverá expor operações CRUD equivalentes, por finalidade, às operações conhecidas no padrão Panache, como `FindByID`, `ListAll`, `Persist` e `Delete`, sem importar Panache ou qualquer biblioteca Java.
7. **Identidade do projeto:** `groupId` e `artifactId` serão entradas obrigatórias do perfil. O `artifactId` continuará com hífens nos metadados e no caminho lógico do módulo; o diretório local removerá hífens para obter um nome compatível e determinístico. O módulo será derivado como `<groupId>/<artifactId>`, e identificadores de pacote Go serão normalizados para a sintaxe da linguagem.
8. **Testes:** testes unitários não dependerão de banco, rede, container ou Sonar. A infraestrutura terá testes de integração separados quando houver comportamento que exija ORM ou banco real.
9. **Qualidade:** a cobertura será aferida por `go test ./... -coverprofile=coverage.out` e `go tool cover -func=coverage.out`. O relatório deverá distinguir produção aplicável de arquivos declarativos, entradas `main` e código gerado excluído com justificativa.
10. **Segurança:** a validação usará a Skill `security-audit`, com o escopo Golang explicitamente identificado. O relatório não poderá conter valores secretos reais.
11. **Sonar:** a disponibilidade será verificada pelo orquestrador existente. Se o daemon, scanner, projeto ou configuração necessária não estiver disponível, a validação registrará o fallback de qualidade LLM como resultado da limitação operacional.

## Estrutura de referência

```text
specs/shared/
├── architecture/backend-golang.md
├── api/golang-rest.md
├── testing/golang-testing.md
├── security/golang-security.md
└── backend-golang/
    ├── conventions.md
    ├── skill-import-manifest.md
    ├── templates/
    └── examples/

apps/
├── api/
│   └── <artifactId-sem-hifens>/
│       ├── cmd/<nome-do-aplicativo>/
│       ├── internal/api/
│       ├── internal/application/
│       ├── internal/domain/
│       ├── internal/infrastructure/
│       ├── go.mod
│       └── ...
└── desktop/
    └── <artifactId-sem-hifens>/
        ├── cmd/<nome-do-aplicativo>/
        ├── internal/application/
        ├── internal/domain/
        ├── internal/infrastructure/
        ├── go.mod
        └── ...
```

Os diretórios `templates/` e `examples/` conterão somente material necessário à geração e à demonstração aprovadas. Código produzido durante uma geração continuará sujeito à política local de artefatos ignorados.

## Fluxo de geração

1. Confirmar `SPEC_APROVADA`, perfil, `groupId`, `artifactId`, banco e requisitos de segurança. Nenhum projeto Java ou ferramenta Java é pré-condição.
2. Validar nomes e calcular o diretório sem hífens.
3. Renderizar o módulo isolado no diretório autorizado, sem misturar arquivos com outro projeto.
4. Aplicar arquitetura, contratos, configuração segura, testes e documentação definidos pela SPEC específica da aplicação.
5. Executar testes e cobertura; registrar inclusões e exclusões do inventário.
6. Executar auditoria de segurança e verificar SonarQube ou o fallback LLM.
7. Encaminhar o resultado às fases de revisão, validação, aprovação e encerramento; somente o encerramento atualiza `specs/system/`.

## Alternativas rejeitadas

- **Fiber como padrão inicial:** possui bom desempenho, mas Gin oferece uma referência mais difundida e alinhada ao ecossistema `net/http` para a primeira versão.
- **Ent ou Bun como ORM inicial:** permanecem alternativas futuras; a Change precisa de uma escolha única para que o contrato de persistência seja verificável.
- **Entidades GORM como DTOs:** rejeitado por acoplar contrato HTTP ao armazenamento e contrariar a separação de responsabilidades adotada como princípio de referência.
- **Exigir uma UI desktop nesta Change:** rejeitado para não introduzir um framework frontend ou toolkit que não foi especificado.

## System Documentation

Após a aprovação e o encerramento, esta seção deverá registrar em `specs/system/` a Skill disponível, os perfis suportados, as convenções adotadas, os comandos de teste, o fluxo de auditoria e a política de fallback do SonarQube.

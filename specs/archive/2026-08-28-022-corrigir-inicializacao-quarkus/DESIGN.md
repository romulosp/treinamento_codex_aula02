# Design: 022-corrigir-inicializacao-quarkus

## Decisão

Adicionar ao plugin Quarkus a execução padrão:

```xml
<executions>
    <execution>
        <goals>
            <goal>build</goal>
        </goals>
    </execution>
</executions>
```

O goal `build` permite que o Maven reconheça o módulo como aplicação Quarkus durante `quarkus:dev`. A alteração fica no POM gerado e no prompt operacional, sem mudar o código da API.

## Componentes afetados

- `apps/backend/gerenciarcategorias/pom.xml` — artefato local regenerado.
- `.github/prompts/executar-mudanca-spec-driven.prompt.md` — regra para futuras gerações.
- `specs/archive/2026-08-27-001-criar-projeto-java/` — especificação-base vigente no histórico.

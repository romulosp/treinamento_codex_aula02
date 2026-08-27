# Revisão de SPEC — 001-criar-projeto-java

## Veredito
`SPEC_APROVADA`

## Resultado

Os requisitos identificam coordenadas Maven, plataforma, dependências, plugins, configuração de ambiente e critérios verificáveis. Os limites de escopo impedem a criação prematura de endpoints e entidades de negócio.

## Achado resolvido

| ID | Severidade | Achado | Resolução |
| --- | --- | --- | --- |
| REV-001 | Bloqueante | Java 11 é incompatível com Quarkus 3.2.10.Final. | ADR-001 adotou Java 17. |
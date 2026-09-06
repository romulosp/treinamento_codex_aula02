# SPEC: excluir artefatos locais do Git

## Status
`SPEC_APROVADA`

## Critérios de aceite
- node_modules, target, dist e .quarkus são ignorados em qualquer profundidade, inclusive sob scripts.
- .env, .env.*, chaves privadas .key/.pem e contêineres .p12/.pfx são ignorados inclusive sob scripts.
- Documentação comum e scripts legítimos permanecem com o comportamento anterior.
- Arquivos já rastreados não são removidos. Gitignore não detecta segredos embutidos em código.

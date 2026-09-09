# Convenções reutilizáveis de Golang

## Identidade e diretórios

Toda geração recebe `groupId` e `artifactId`. O módulo preserva o `artifactId` com hífens, enquanto o diretório remove hífens:

```text
module <groupId>/<artifactId>
apps/api/<artifactId-sem-hifens>/
apps/desktop/<artifactId-sem-hifens>/
```

O destino deve ser validado antes da escrita para evitar sobrescrever outro projeto.

## Nomes e pacotes

Use nomes curtos, claros e idiomáticos de Go. Pacotes devem ser minúsculos, sem hífens, underscores ou nomes genéricos como `utils` quando um conceito de domínio for possível. Tipos exportados usam MixedCaps; acrônimos seguem a convenção do ecossistema, como `HTTP`, `ID` e `URL`.

Interfaces pequenas devem ser definidas perto de quem as consome. Construtores devem validar invariantes necessárias e retornar erro quando a criação puder falhar.

## Organização interna

- `cmd`: composição e ponto de entrada.
- `internal/api`: transporte e DTOs do perfil `api`.
- `internal/application`: casos de uso.
- `internal/domain`: regras, entidades e portas.
- `internal/infrastructure`: adaptadores técnicos.

A dependência deve apontar para abstrações e regras internas, não para detalhes de fornecedor.

## Configuração

Configuração deve ser carregada explicitamente, validada no startup e separada por ambiente. Valores sensíveis nunca ficam em arquivos rastreados. Defaults seguros devem ser documentados e timeouts não devem ficar implícitos para chamadas externas.

## Documentação

Toda API pública deve ter comentário GoDoc quando aplicável. O README do projeto deve registrar módulo, perfil, como executar, configuração necessária, testes e limitações. Contratos REST devem estar em OpenAPI quando o perfil `api` existir.

## Testes e qualidade

Arquivos aplicáveis devem possuir testes relacionados e exclusões justificadas no inventário. Execute os comandos definidos em `golang-testing.md`, sem declarar cobertura não medida. Linters, `go vet`, race detector e análise de dependências devem ser usados quando previstos pela SPEC e pelo ambiente.

## Segurança

Use DTOs na fronteira, consultas parametrizadas, autorização no servidor, logs redigidos e contexto com timeout. A auditoria deve distinguir conformidade, não aplicabilidade, limitação e hipótese não confirmada.

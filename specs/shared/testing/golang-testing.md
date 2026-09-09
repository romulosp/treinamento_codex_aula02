# Estratégia de testes para Golang

## Princípios

Os testes devem exercitar comportamento real, permanecer determinísticos e registrar ambiente, comando, resultado e código de saída em `validation.md`. Testes unitários não dependem de banco, rede, container, Sonar ou serviços externos.

## Inventário obrigatório

Para cada projeto Go:

1. Liste todos os arquivos `.go` de produção.
2. Classifique cada arquivo como aplicável ou excluído.
3. Associe cada arquivo aplicável a um teste `_test.go` do mesmo pacote ou a um teste de pacote relacionado.
4. Justifique exclusões na validação.

São normalmente aplicáveis regras de domínio, casos de uso, validadores, mapeadores, repositórios com lógica, handlers e adaptadores com comportamento próprio. Podem ser excluídos, com justificativa, pontos de entrada triviais, structs declarativas, interfaces sem implementação, configuração sem lógica e código gerado.

## Testes unitários

Prefira testes orientados a tabela para variações de entrada e saída. Use dependências reais simples quando isso tornar a regra mais clara e interfaces pequenas para substituir integrações. Cubra sucesso, ausência, erro, limites, entradas vazias, nil, conflitos e cancelamento de contexto quando aplicável.

Mocks devem verificar somente interações que fazem parte do comportamento. Não faça o teste depender de detalhes privados ou de quantidade incidental de chamadas.

## Testes de integração

Use integração quando o comportamento depender de:

- router e middleware Gin reais;
- GORM, transações, migrations ou banco;
- rede, cliente externo ou composição da aplicação;
- configuração e ciclo de vida do processo.

Separe esses testes dos testes unitários e registre pré-requisitos, isolamento, massa e limpeza. A SPEC da aplicação define se o banco real, container ou banco em memória é permitido.

## Comandos mínimos

No diretório do módulo, com a versão de Go registrada:

```text
go test ./...
go test ./... -coverprofile=coverage.out
go tool cover -func=coverage.out
go test -race ./...
```

`-race` é executado quando o módulo e o ambiente suportarem a verificação. Fuzzing e benchmarks são adicionais e devem ser registrados separadamente.

## Cobertura

A cobertura de linhas do código de produção aplicável deve ser maior ou igual a 80%; a meta recomendada é maior ou igual a 90%. Nunca declare um percentual sem comando, escopo, versão do Go, saída observada e código de saída. O percentual não substitui cenários de negócio.

Arquivos excluídos do cálculo devem permanecer no inventário com justificativa. Não use código artificial ou testes sem asserções para aumentar a métrica.

## Qualidade do teste

- Use nomes que descrevam comportamento.
- Evite dependência de ordem global e tempo real não controlado.
- Feche recursos e limpe dados de integração.
- Execute `go vet` e o linter aprovado quando disponíveis.
- Registre falhas sem ocultar warnings relevantes.

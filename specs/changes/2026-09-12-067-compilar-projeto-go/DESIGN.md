# Design: 067-compilar-projeto-go

Autor: Rômulo Penha

## Finalidade da Change

Esta Change cria um atalho operacional para compilar o módulo Go do projeto sem depender de linha de comando manual repetitiva.

## Contexto

O projeto possui um módulo Go em `apps/desktop/libpinpadabecsgo`. Como parte do processo local, a compilação do módulo precisa ser simples e previsível para quem trabalha em ambiente Windows.

## Objetivo

Fornecer um arquivo `.bat` na raiz do projeto que execute a compilação do módulo Go e informe claramente o resultado.

## Decisões

1. O ponto de entrada será um `.bat` localizado na raiz do repositório.
2. O script será destinado ao uso em Windows e sem elevação de privilégio.
3. O script deve localizar `apps/desktop/libpinpadabecsgo` e executar a compilação neste diretório.
4. A compilação será feita com `go build ./...`.
5. O script não altera o ambiente global do sistema, apenas o contexto do processo atual.

## Fluxo operacional

```text
Usuário executa “compilar projeto.bat”
    -> script entra na raiz do repositório
    -> identifica o diretório do módulo Go
    -> verifica se “go” está disponível
    -> realiza go build ./...
    -> exibe sucesso ou erro
    -> finaliza com código de saída compatível
```

## Requisitos de implementação

- o script deve usar `cd /d` para manter o contexto correto;
- o script deve verificar `where go` antes de compilar;
- em caso de falha, a execução deve retornar o código do erro;
- em caso de sucesso, a mensagem final deve indicar conclusão com sucesso;
- a janela deve permanecer aberta para leitura do usuário manualmente.

## Critérios de qualidade

- script simples e de fácil manutenção;
- sem dependência de configuração persistente do sistema;
- resultado operacional claro para usuário humano;
- compilação executada no módulo correto.

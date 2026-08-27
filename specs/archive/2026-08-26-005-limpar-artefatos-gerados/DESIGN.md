# Design: 005-limpar-artefatos-gerados

## Contexto

A limpeza deve cobrir os artefatos gerados atualmente identificados sem executar uma operação destrutiva sobre fontes e documentação.

## Referências

- `specs/changes/005-limpar-artefatos-gerados/spec.md`

## Decisões

1. O script usará `for /d /r` para localizar somente diretórios `target` e `.quarkus` sob `apps/`.
2. O script usará `for /r` para localizar somente arquivos `*.log` sob `apps/`.
3. Cada remoção verificará se o item ainda existe; em falha, o script acumulará erro e retornará código `1` ao fim.
4. A confirmação existente será preservada.

## Arquitetura e componentes

- `deletar-arquivos-gerados.bat`: único componente alterado.
- Teste automatizado de conteúdo: verifica os padrões permitidos, as proteções de escopo e a preservação da confirmação.

## Alternativas e consequências

Excluir todos os arquivos que não sejam `.md` ou `.txt` foi descartado: isso removeria fontes, scripts, configurações e metadados Git, contrariando o objetivo de limpar somente artefatos gerados.
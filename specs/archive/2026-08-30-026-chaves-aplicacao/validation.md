# Validacao: 026-chaves-aplicacao

## Situacao

`VALIDADA`

## Ambiente

- Windows, PowerShell e `powershell.exe`.
- Workspace: `D:\desenvolvimento\ia\aula02`.
- O arquivo externo real foi usado somente na geracao final; seus valores nao foram exibidos.
- Nenhum comando Maven foi necessario, pois a mudanca altera somente gerador PowerShell, template e script BAT local.

## Evidencias

- **VAL-001 / CA-001:** execucao do gerador com caminho inexistente. Resultado: falhou antes da escrita, codigo `1`, e o hash do BAT anterior permaneceu igual.
- **VAL-002 / CA-002:** execucao com arquivo temporario sem `OIDC_CLIENT_SECRET`. Resultado: falhou antes da escrita, codigo `1`.
- **VAL-003 / CA-003:** execucao com o arquivo externo completo. Resultado: geracao concluida, codigo `0`; o BAT final possui zero placeholders, uma linha JDBC, duas linhas de datasource e tres linhas OIDC.
- **VAL-004 / CA-004:** `git check-ignore` do BAT final retornou `0`; `git ls-files --error-unmatch` retornou `1`, confirmando que o BAT preenchido nao e rastreado.
- **VAL-005 / CA-005:** verificacao estrutural do BAT final encontrou zero linhas de log para JDBC, senha ou segredo.
- **VAL-006 / CA-006:** verificacao do BAT de `gerenciarcategorias` encontrou zero referencias ao arquivo externo ou as chaves desta mudanca.
- **VAL-007 / RF-004:** valores sinteticos contendo `&`, `|` e `<` foram gerados com escaping; codigo `0` e contagens de escaping `1,1,1`. O BAT foi regenerado em seguida com o arquivo externo real.
- **VAL-008 / CA-004:** busca estatica em `scripts` e `specs/changes/026-chaves-aplicacao` nao encontrou valores concretos, retornando codigo `0` apos a mensagem sanitizada.
- **VAL-009:** `git diff --check HEAD -- .gitignore` retornou codigo `0`.

## Conclusao

Os cenarios aplicaveis foram aprovados sem expor valores. O BAT final permanece somente no ambiente local e os artefatos versionados contem apenas gerador, template sem valores e documentacao.

# Re-revisão de SPEC — consolidação pós-merge Android

## Status

`SPEC_APROVADA`

## Escopo revisado

Foram reavaliadas a Change `9999-sistema-prototipo-android`, as Changes ativas
`10003` a `10007`, a arquitetura compartilhada de plugins Android, o índice em
`specs/README.md` e as relações com as Changes sucessoras. Esta revisão verifica
a resolução dos achados da auditoria de 2026-09-28; não aprova implementação,
validação funcional, archive ou commit.

## Resolução dos achados

| ID | Evidência da resolução | Resultado |
| --- | --- | --- |
| `REV-001` | As referências 066–072 em `specs/README.md` apontam para `specs/archive/`; a checagem automática de links locais do escopo examinou 85 arquivos sem destino ausente. | Resolvido |
| `REV-002` | RF-005, RF-006 e RN-003 da Change 9999 foram marcados como superados; RF-008 e os critérios de aceite agora distribuem login, cabeçalho e negócio entre os módulos proprietários definidos pelas Changes 10001, 10003, 10006 e 10007. | Resolvido |
| `REV-003` | `minSdk = 29` é o baseline vigente da plataforma. A API 26 permanece somente em registros identificados como históricos da fundação monolítica. DESIGN, plano, matriz, ADR, fontes, tarefas e validação foram alinhados. | Resolvido |
| `REV-004` | A sequência ficou explícita: a Change 10003 preserva `SharedApi` 1.1.0; a Change 10007 eleva a minor para 1.2.0. O host exige a mesma major, aceita `requiredSharedApiMinor <= hostMinor` para `business-menu` e exige minor vigente exata para `startup-auth`. | Resolvido |
| `REV-005` | `MENU-007` passou a representar descarte de resultado tardio por geração, logout ou destruição. A Change 10003 declara expressamente que não há timeout temporal de callbacks. | Resolvido |
| `REV-006` | A Change 10007 foi declarada sucessora do RF-02 da Change 10006. O cabeçalho final pertence ao `CoreShell` de `:app`; `:plugin-login` preserva campos, teclados e rodapé sem segunda instância do cabeçalho. | Resolvido |
| `REV-007` | Os planos de 10003 e 10006 foram identificados como snapshots pré-implementação executados e remetem o estado operacional às tarefas, revisões e validações correspondentes. | Resolvido |

## Verificações complementares

- Não há decisão arquitetural concorrente entre a fundação histórica da Change
  9999 e a topologia vigente de host, API compartilhada e plugins.
- O versionamento da API é coerente com os manifestos de negócio na minor 1 e
  com o plugin de autenticação na minor 2.
- Os requisitos de cabeçalho têm um único proprietário no estado final.
- Os documentos históricos continuam disponíveis, mas não se apresentam como
  contrato vigente quando foram superados.
- As alterações de produção e demais arquivos já presentes no worktree não
  foram modificados por esta correção documental.

## Observação operacional do merge

Os diretórios de archive referenciados pelo índice existem no worktree. Eles
devem permanecer rastreados no commit que encerrar o merge; excluir esses
arquivos tornaria os links inválidos novamente. Esta observação não altera o
contrato das SPECs e não autoriza archive ou commit nesta etapa.

## Veredito

`SPEC_APROVADA`

Não restam contradições materiais ou decisões pendentes nos sete achados
reavaliados. Implementação, validação formal, aprovação final e commit continuam
sujeitos às etapas próprias do workflow.

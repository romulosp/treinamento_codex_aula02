# Design: 026-chaves-aplicacao

## Contexto

O processo precisa gerar um `start_aplicacao.bat` local com valores literais vindos de um arquivo externo, sem colocar esses valores no template, no gerador ou nos documentos versionados.

## Decisoes

1. O caminho do arquivo externo e fixo em `D:\desenvolvimento\chave_des\chave_des.properties`.
2. `scripts/gerar_start_aplicacao.ps1` executa carregamento, validacao e renderizacao.
3. `scripts/templates/start_aplicacao-gerenciartarefas.bat.template` e o gerador versionados contem somente nomes de chaves e logica de composicao.
4. A renderizacao escreve `apps/backend/gerenciartarefas/start_aplicacao.bat` somente apos validar todas as chaves.
5. O BAT final e local, contem valores literais e e ignorado pelo Git.
6. A leitura aceita espacos ao redor de `=` e comentarios `#`.
7. Falha de arquivo ausente, leitura invalida, chave ausente, chave vazia ou escaping invalido e bloqueante.
8. O script de `gerenciarcategorias` nao exige o arquivo porque nao consome banco/SSO.

## Mapeamento sem valores

| Chave externa | Destino no BAT de `gerenciartarefas` |
| --- | --- |
| `HOSTNAME_DB_POSTGRESQL` | host de `POSTGRESQL_JDBC_URL` |
| `PORTA_DB_POSTGRESQL` | porta de `POSTGRESQL_JDBC_URL` |
| `BANCO_DB` | banco de `POSTGRESQL_JDBC_URL` |
| `USER_DB_POSTGRESQL` | `POSTGRESQL_USERNAME` |
| `SENHA_DB_POSTGRESQL` | `POSTGRESQL_PASSWORD` |
| `OIDC_AUTH_SERVER_URL` | `OIDC_AUTH_SERVER_URL` |
| `OIDC_CLIENT_ID` | `OIDC_CLIENT_ID` |
| `OIDC_CLIENT_SECRET` | `OIDC_CLIENT_SECRET` |

`JAVA_HOME`, `MAVEN_HOME` e o diretorio local do Compose permanecem parametros operacionais existentes do template; nao sao credenciais e nao recebem valores do arquivo nesta mudanca.

## Fluxo

1. Confirmar o arquivo externo.
2. Ler as propriedades sem exibir o conteudo.
3. Validar as oito chaves obrigatorias.
4. Escapar os valores para sintaxe segura de `cmd.exe`.
5. Montar a URL JDBC e renderizar um arquivo temporario local.
6. Substituir o BAT final somente apos a renderizacao completa; falha de validacao nao toca no arquivo anterior.
7. Nao imprimir valores, o conteudo do BAT ou a URL JDBC.

## Protecao do artefato

- Template: `scripts/templates/start_aplicacao-gerenciartarefas.bat.template`.
- Gerador: `scripts/gerar_start_aplicacao.ps1`.
- BAT final: `apps/backend/gerenciartarefas/start_aplicacao.bat`.
- O BAT final deve ser ignorado pelo Git antes da escrita e nao entra no commit.

## Testes

O gerador sera testado com arquivos temporarios contendo valores sinteticos, cobrindo arquivo ausente, chave ausente, chave vazia, espacos ao redor de `=`, composicao da URL e caracteres especiais de `cmd.exe`. O arquivo externo real nunca sera usado pelos testes.

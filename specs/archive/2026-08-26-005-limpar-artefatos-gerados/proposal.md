# Proposta: 005-limpar-artefatos-gerados

## Status
`IMPLEMENTADA`

## Problema e objetivo

O script de limpeza remove somente `apps/backend/target/`. Torná-lo capaz de limpar com segurança todos os artefatos gerados conhecidos do projeto, sem remover fontes, documentos, scripts, configurações ou metadados Git.

## Escopo

- Remover diretórios `target` em todos os módulos Maven sob `apps/`.
- Remover diretórios `.quarkus` sob `apps/`.
- Remover arquivos `*.log` sob `apps/`.
- Preservar código-fonte, recursos, testes, scripts `.bat`, arquivos de configuração, documentos `.md` e `.txt` e o diretório `.git`.
- Manter confirmação interativa e código de saída diferente de zero em erro de remoção.

## Fora de escopo

- Excluir arquivos de código, `pom.xml`, scripts, configurações, documentos, ou metadados Git.
- Excluir dependências locais Maven em `.m2` ou alterar variáveis do sistema.
- Alterar o comportamento do backend.

## Impactos e riscos

- A limpeza remove resultados de build e será seguida por recompilação na próxima execução Maven.
- O script deve limitar-se a caminhos abaixo de `apps/` para evitar exclusão fora do workspace.

## Critérios para aprovação da SPEC

- Os caminhos removidos e preservados são explícitos e verificáveis.
- A operação não remove fontes ou documentos versionados.
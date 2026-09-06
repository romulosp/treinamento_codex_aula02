# Galeria de Fotos

## Estado vigente

A Galeria de Fotos é uma aplicação demonstrativa local composta por backend Quarkus 3.2/Java 17 em `apps/backend/fotogaleria/` e frontend React/Vite em `apps/frontend/web/fotogaleria/`. O frontend opera na porta 3000 e encaminha `/api` ao backend na porta 2000.

O backend persiste fotos no PostgreSQL por Hibernate ORM Panache. A API em `/api/fotos` oferece upload, listagem paginada, detalhe, atualização de descrição e exclusão. Entidades JPA permanecem na infraestrutura; os contratos HTTP usam DTOs. Imagens são aceitas nos formatos JPEG, PNG, GIF e WebP, limitadas a 5 MiB e armazenadas como Data URL Base64 em coluna `TEXT`; a listagem não transporta a imagem.

O frontend oferece login demonstrativo `admin/admin`, galeria responsiva, upload múltiplo, progresso por arquivo, edição de descrição, confirmação de exclusão e lightbox. O login existe somente em memória no navegador e não protege a API.

## Configuração e execução local

A fonte externa é `D:\desenvolvimento\chave_des\chave_des.properties`. O gerador `scripts/gerar_start_aplicacao_fotogaleria.ps1` seleciona a seção `##DB POSTGRESQL fotogaleria` e exige, dentro dela, `HOSTNAME_DB_POSTGRESQL`, `PORTA_DB_POSTGRESQL`, `BANCO_DB_TAREFA`, `USER_DB_POSTGRESQL` e `SENHA_DB_POSTGRESQL`.

O gerador renderiza `apps/backend/fotogaleria/start_aplicacao.bat` a partir de um template sem valores, mapeando as propriedades para `POSTGRES_URL`, `POSTGRES_DB`, `POSTGRES_USER` e `POSTGRES_PASSWORD`. O BAT preenchido é local, ignorado pelo Git e não deve ter valores copiados para logs, documentação ou evidências.

`apps/backend/fotogaleria/testar_aplicacao.bat` valida o arquivo externo, executa o gerador e chama o BAT final. O PostgreSQL local é definido pelo `docker-compose.yml` do backend. O frontend é iniciado por `apps/frontend/web/fotogaleria/start_aplicacao_frontend.bat`.

## Segurança vigente

A API não possui autenticação, autorização, usuários, tenants ou isolamento por proprietário. Essa limitação é deliberada e aceita somente para demonstração local; a aplicação não deve ser publicada ou usada em produção sem uma nova Change que implemente controles no servidor.

O arquivo externo e o BAT preenchido não são versionados. O gerador não imprime valores carregados, e o frontend não usa HTML cru ou execução dinâmica de código.

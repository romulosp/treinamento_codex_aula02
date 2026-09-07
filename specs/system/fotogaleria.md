# Galeria de Fotos

## Estado vigente

A Galeria de Fotos é uma aplicação demonstrativa local composta por backend Quarkus 3.2/Java 17 em `apps/backend/fotogaleria/` e frontend React/Vite em `apps/frontend/web/fotogaleria/`. O frontend opera na porta 3000 e encaminha `/api` ao backend na porta 2000.

O backend persiste fotos no PostgreSQL por Hibernate ORM Panache. A API em `/api/fotos` oferece upload, listagem paginada, detalhe, atualização de descrição e exclusão. Entidades JPA permanecem na infraestrutura; os contratos HTTP usam DTOs. Imagens são aceitas nos formatos JPEG, PNG, GIF e WebP, limitadas a 5 MiB e armazenadas como Data URL Base64 em coluna `TEXT`; a listagem não transporta a imagem.

O frontend oferece login demonstrativo `admin/admin`, galeria responsiva, upload múltiplo, progresso por arquivo, edição de descrição, confirmação de exclusão e lightbox. O login existe somente em memória no navegador e não protege a API.

## Configuração e execução local

A fonte externa é `D:\desenvolvimento\chave_des\chave_des.properties`. O gerador `scripts/gerar_start_aplicacao_fotogaleria.ps1` seleciona a seção `##DB POSTGRESQL fotogaleria` e exige, dentro dela, `HOSTNAME_DB_POSTGRESQL`, `PORTA_DB_POSTGRESQL`, `BANCO_DB_TAREFA`, `USER_DB_POSTGRESQL` e `SENHA_DB_POSTGRESQL`.

O gerador renderiza `apps/backend/fotogaleria/start_aplicacao.bat` a partir de um template sem valores, mapeando as propriedades para `POSTGRES_URL`, `POSTGRES_DB`, `POSTGRES_USER` e `POSTGRES_PASSWORD`. O BAT preenchido é local, ignorado pelo Git e não deve ter valores copiados para logs, documentação ou evidências.

`apps/backend/fotogaleria/testar_aplicacao.bat` valida o arquivo externo, executa o gerador e chama o BAT final. O PostgreSQL local é definido pelo `docker-compose.yml` do backend. O frontend é iniciado por `apps/frontend/web/fotogaleria/start_aplicacao_frontend.bat`.

## Persistência e testes — contrato consolidado após Change 011

`FotoEntity.imagemBase64` deve ser `String` com `@Column(columnDefinition = "TEXT")`, **sem `@Lob`**. Não utilizar Clob nem Large Objects para novos uploads. Essa decisão substitui expressamente o DESIGN original da Change 010.

O cenário obrigatório de regressão envia uma imagem PNG por multipart, verifica POST 201 e realiza novas requisições: GET de listagem 200 sem imagemBase64 e GET de detalhe 200 com Data URL idêntico ao enviado. Cada requisição tem sua própria transação; um teste que apenas simula o repositório não prova a persistência. Deve limpar somente os registros sintéticos criados pelo teste.

A suíte padrão usa JUnit 5/Mockito para unidades e `@QuarkusTest`/Rest Assured com H2 em memória para integração HTTP. `quarkus-jdbc-h2` e Rest Assured possuem escopo `test`. Em `src/test/resources/application.properties`, usar `db-kind=h2`, URL `jdbc:h2:mem:fotogaleria_test;DB_CLOSE_DELAY=-1`, Dev Services desativado, esquema `drop-and-create` e porta HTTP de teste dinâmica. Não exigir Docker, porta 55439 ou credenciais externas para esses testes. Nunca aplicar `drop-and-create` ao PostgreSQL de desenvolvimento.

O datasource principal continua PostgreSQL por `POSTGRES_URL`, montada pelo gerador a partir do host, porta e banco da seção `##DB POSTGRESQL fotogaleria`. No ambiente local informado, a porta é 5432. H2 não altera a configuração principal. Testes H2 comprovam o contrato HTTP; a regressão específica de LOB exige a evidência PostgreSQL registrada na Change 011.

Para dados legados, `scripts/corrigir-fotogaleria-lob.sql` converte somente referências numéricas em Data URLs, em transação, preservando os valores antigos na tabela `foto_imagem_lob_backup` e mantendo os Large Objects. A migração não deve rodar automaticamente ao regenerar o projeto. Não apagar banco ou volumes. Validar o banco alvo e executar com `psql -v ON_ERROR_STOP=1` quando realmente houver dados legados a recuperar.

## Fonte para regeneração

Reconstruir os módulos nos caminhos descritos neste documento, respeitando AGENTS.md e o fluxo Spec Driven. Para contratos REST completos, limites, componentes e scripts, consultar `specs/archive/2026-09-06-010-galeria-de-fotos/spec.md`. Aplicar prioritariamente as correções deste documento e `specs/archive/2026-09-06-011-corrigir-upload-postgresql/spec.md`: TEXT sem @Lob e integração padrão H2. Não reaplicar decisões históricas de @Lob, PostgreSQL na porta 55439 ou Dev Services nos testes.

Reutilizar `scripts/gerar_start_aplicacao_fotogaleria.ps1` e seu template. Código em apps permanece local/ignorado; regeneração exige nova validação e não garante identidade byte a byte com arquivos anteriores.

## Segurança vigente

A API não possui autenticação, autorização, usuários, tenants ou isolamento por proprietário. Essa limitação é deliberada e aceita somente para demonstração local; a aplicação não deve ser publicada ou usada em produção sem uma nova Change que implemente controles no servidor.

O arquivo externo e o BAT preenchido não são versionados. O gerador não imprime valores carregados, e o frontend não usa HTML cru ou execução dinâmica de código.

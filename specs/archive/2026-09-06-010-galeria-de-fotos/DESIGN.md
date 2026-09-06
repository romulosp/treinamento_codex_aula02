# DESIGN: Desenvolvimento e Entrega de Galeria de Fotos

## Visão da solução

A solução contém duas aplicações locais independentes. O React na porta 3000 consome caminhos relativos `/api`; o proxy do Vite os encaminha ao Quarkus na porta 2000. O Quarkus persiste metadados e Data URLs Base64 no PostgreSQL iniciado por Docker Compose.

## Backend

### Organização

- `api`: `FotoResource`, formulário multipart, requests, responses e mapeamento de erros HTTP.
- `application`: `FotoService`, validação do arquivo, paginação e orquestração dos casos de uso.
- `domain`: modelo `Foto`, contrato `FotoRepository` e exceções de domínio.
- `infrastructure`: `FotoEntity` e implementação Panache do repositório.

### Fluxos

No upload, a API lê o arquivo temporário disponibilizado pelo Quarkus e entrega bytes, nome e content type ao serviço. O serviço valida, monta o Data URL, registra `LocalDateTime` e usa o contrato de repositório. Nos demais fluxos, o serviço retorna modelos de domínio e a API os converte em DTOs.

### Persistência

`FotoEntity.imagemBase64` usa `@Lob` e `columnDefinition = "TEXT"`. A listagem usa projeção de domínio sem devolver o conteúdo no DTO HTTP. A ordenação padrão é `dataUpload DESC, id DESC`.

### Erros

Uma exceção de foto inexistente é convertida em HTTP 404. Violações de entrada e arquivo são convertidas em HTTP 400. Erros inesperados permanecem sob o tratamento padrão do Quarkus e não terão stack trace produzido pelo código da aplicação na resposta.

## Frontend

### Direção visual

A interface seguirá uma linguagem editorial de arquivo fotográfico: fundo marfim, tipografia serifada expressiva nos títulos, azul-petróleo profundo e acento terracota. A composição será assimétrica no cabeçalho e disciplinada na grade, com animação curta de entrada e respeito a `prefers-reduced-motion`.

### Componentes

- `LoginPage`: autenticação demonstrativa em memória.
- `GalleryPage`: carregamento, estado vazio, erro, paginação e coordenação das operações.
- `PhotoCard`: miniatura e ações acessíveis.
- `UploadDialog`: seleção múltipla, preview, descrição por arquivo e progresso.
- `EditDescriptionDialog`: edição isolada da descrição.
- `PhotoLightbox`: consulta de detalhe sob demanda.
- `ConfirmDeleteDialog`: confirmação explícita.
- `fotoService.js`: única fronteira HTTP.

Objetos URL de preview serão revogados ao substituir ou desmontar a seleção. Operações independentes de upload serão sequenciais para representar progresso real por arquivo e limitar pressão sobre API e banco.

## Segurança

- O backend aceitará somente JPEG, PNG, GIF e WebP e limitará cada arquivo a 5 MiB.
- Nomes serão reduzidos ao nome-base e descrições limitadas a 5.000 caracteres.
- React renderizará texto por JSX, sem HTML cru ou `eval`.
- Um gerador PowerShell lerá o arquivo externo, delimitará a seção `##DB POSTGRESQL fotogaleria` até o próximo cabeçalho `##`, validará as cinco chaves de banco e renderizará o BAT local somente após validação completa.
- O gerador e os scripts não imprimirão valores carregados. O BAT renderizado permanecerá local e ignorado pelo Git.
- A ausência deliberada de autenticação no backend impede uso em produção e será um achado explícito da auditoria.

## Decisões

- Quarkus substitui referências Spring do material original, conforme `AGENTS.md`.
- O limite de 5 MiB recomendado no material é adotado como contrato verificável.
- O Data URL preserva o content type sem adicionar atributo persistido fora da SPEC.
- Material UI atende à exigência de biblioteca visual e fornece controles e ícones acessíveis.
- Os projetos em `apps/` permanecem locais e ignorados pelo Git conforme política vigente; a Change, a especificação de sistema e as evidências são versionadas.

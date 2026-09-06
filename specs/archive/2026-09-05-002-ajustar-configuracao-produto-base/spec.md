# SPEC: Ajustar configuração de execução do Produto Base

## Status
`SPEC_APROVADA`

## Referências e dependências

- Depende da Change `001-produto-base` e de seus projetos em `apps/backend/produtobase/` e `apps/frontend/web/produtobase/`.
- Não altera a convenção de diretórios do sistema.

## Requisitos funcionais

1. O login demonstrativo deve aceitar exclusivamente usuário `root` e senha `root`.
2. O backend Quarkus deve escutar na porta 1000.
3. O servidor de desenvolvimento do frontend deve escutar na porta 2000.
4. O frontend deve usar caminhos relativos para `/produtos`; em desenvolvimento, o proxy deve encaminhá-los para `http://localhost:1000`.
5. Quando publicado no mesmo domínio da interface, o navegador deve chamar `/produtos` no domínio atual, sem `localhost` codificado.
6. Os scripts batch devem iniciar os serviços com as portas definidas nesta Change e informar a URL do frontend `http://localhost:2000`.

## Requisitos não funcionais

- Manter React/Vite no frontend e Quarkus/Maven com Java 17 no backend.
- Não expor endereços ou segredos de ambiente além dos já previstos no escopo local.

## Regras de negócio

- O login continua sendo demonstrativo e não cria sessão no backend.

## Cenários e critérios de aceite

1. Com `root` / `root`, o usuário é direcionado à listagem; com quaisquer outras credenciais, recebe erro e permanece no login.
2. Ao iniciar o backend, ele fica acessível em `http://localhost:1000`.
3. Ao executar `npm run start`, o frontend inicia em `http://localhost:2000`.
4. Em desenvolvimento, uma requisição do frontend para `/produtos` é encaminhada ao backend na porta 1000.
5. A URL usada pelo código do frontend não contém host fixo para o consumo de `/produtos`.

> Decisão aprovada: a porta 2000 é a porta do frontend; a porta 1000 é a porta do backend.

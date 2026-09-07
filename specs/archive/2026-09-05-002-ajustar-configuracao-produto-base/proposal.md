# Proposta: Ajustar configuração de execução do Produto Base

## Status
`SPEC_APROVADA`

## Responsável e data

- Responsável: equipe do Produto Base
- Data: 2026-09-05

## Referências

- Change de origem: `specs/changes/001-produto-base/`.
- Solicitação consolidada recebida em 2026-09-05.

## Problema e objetivo

Há uma solicitação para alterar as credenciais demonstrativas, as portas e o modo de localização da API no frontend. Esses itens divergem da Change `001-produto-base`, já aprovada, e por isso serão tratados em uma Change independente.

## Escopo

- Alterar o login demonstrativo para `root` / `root`.
- Configurar o backend Java para a porta 1000.
- Configurar o frontend para a porta 2000, como interpretação provisória da segunda menção a porta no pedido recebido.
- Fazer o frontend consumir a API por URL relativa, preservando o domínio atual quando publicado e usando o proxy de desenvolvimento local.
- Ajustar scripts, configurações e testes afetados.

## Fora de escopo

- Autenticação real, gerenciamento de usuários ou tokens.
- Reestruturar os diretórios canônicos definidos em `specs/system/README.md`.
- Novas operações de Produto.

## Impactos e riscos

- A expressão "backend porta 2000" no pedido conflita com "backend Java ... porta 1000". Esta proposta assume que 2000 é a porta do frontend; a revisão da SPEC precisa confirmar essa decisão.
- A alteração de porta exige sincronizar Vite, proxy e scripts batch.
- A estrutura `apps/backend/produtobase` e `apps/frontend/web/produtobase` continua obrigatória por ser a convenção do sistema vigente.

## Critérios para aprovação da SPEC

- Confirmação explícita das portas 1000 (backend) e 2000 (frontend).
- Critérios de aceite testáveis para login e consumo da API.

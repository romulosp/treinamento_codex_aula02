# Design: 011-reescrever-readme-profissional

## Contexto

O README será a página inicial para compreensão rápida do repositório após a reinicialização documental efetuada na mudança 010. As instruções completas já vivem em `NotasProjeto.md` e nas fontes normativas de `specs/`.

## Referências

- `spec.md`
- `NotasProjeto.md`
- `specs/shared/process/workflow.md`

## Decisões

1. Usar um título único, subtítulo objetivo e seções curtas para propósito, estado atual, recursos, estrutura, fluxo, contribuição, referências e créditos.
2. Usar links relativos para os documentos internos e não reproduzir regras longas que já possuem fonte canônica.
3. Creditar o autor somente pelo identificador Git `f744113`, sem inserir e-mail ou outros dados pessoais.
4. Não usar badges externos para evitar dependências de imagem e manter a política documental.

## Arquitetura e componentes

- `README.md`: único arquivo de produto alterado.
- Documentos de mudança: trilha de especificação, revisão e validação.

## Alternativas e consequências

- Copiar o conteúdo de `NotasProjeto.md` foi descartado para evitar duplicação e divergência.
- Declarar a API como executável foi descartado porque o módulo foi removido intencionalmente.

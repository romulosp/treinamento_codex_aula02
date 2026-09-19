# Material de origem incorporado

**Autor:** Rômulo Penha

Esta pasta torna a Change autocontida. Nenhuma etapa de especificação ou implementação deve depender do diretório externo usado durante a descoberta.

## Conteúdo

- `codigo-java/`: 154 arquivos Java usados somente para compreender contratos, estados e comportamentos anteriores.
- `configuracao/`: três XMLs e dois esquemas XSD de tema e menu.
- `entrada/`: cópia do pedido textual original, tratado apenas como fonte de requisitos.
- `fontes/`: oito arquivos TTF de conteúdo único.
- `imagens/`: 144 imagens, uma propriedade de tema e duas bases de miniaturas herdadas. A subpasta `referencia/` contém a imagem visual fornecida pelo usuário.

## Regras de uso

- O conteúdo é evidência de migração, não código de produção Android.
- Somente arquivos com direito de uso comprovado podem ser copiados ao módulo `app`.
- Saídas compiladas, metadados de IDE e duplicatas da pasta `bin` não foram incorporados.
- As bases de miniaturas não devem entrar no aplicativo.
- Nomes, pacotes e comentários encontrados no código de origem não definem a nomenclatura do destino.

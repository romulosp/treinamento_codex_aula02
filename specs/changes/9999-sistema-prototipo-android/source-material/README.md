# Material de origem da Change

**Autor:** Rômulo Penha

Esta pasta deve tornar a Change autocontida. No estado atual, somente os
arquivos textuais de entrada estão presentes. Código Java, configurações,
fontes, imagens e a referência visual ainda precisam ser incorporados antes da
implementação que dependa deles.

## Conteúdo

- `codigo-java/`: pendente; destino previsto para os 154 arquivos Java de referência.
- `configuracao/`: pendente; destino previsto para os XMLs e esquemas de referência.
- `entrada/`: cópia do pedido textual original e registro das decisões do prompt2, tratados apenas como fontes de requisitos.
- `fontes/`: pendente; fontes servem apenas à análise de rastreabilidade.
- `imagens/`: pendente; inclui a referência visual destinada à comparação humana.

## Regras de uso

- O conteúdo incorporado é evidência de migração, não código de produção Android.
- A implementação não pode consultar caminhos externos; os materiais necessários devem estar versionados aqui antes de uso.
- Somente arquivos com direito de uso comprovado podem ser copiados ao módulo `app`.
- Saídas compiladas, metadados de IDE e duplicatas da pasta `bin` não foram incorporados.
- As bases de miniaturas não devem entrar no aplicativo.
- Nomes, pacotes e comentários encontrados no código de origem não definem a nomenclatura do destino.

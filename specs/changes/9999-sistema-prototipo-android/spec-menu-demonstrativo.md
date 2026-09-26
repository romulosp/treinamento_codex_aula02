# Especificação complementar: menu demonstrativo

**Autor:** Rômulo Penha

## Objetivo

Demonstrar uma árvore hierárquica após a confirmação local do painel de identificação, usando componentes migrados sem incorporar regras de negócio da origem.

## Modelo

`DemoMenuItem` deve conter identificador, rótulo localizado, disponibilidade e lista de filhos. O estado da tela mantém a trilha atual e permite retornar ao nível anterior.

## Conteúdo neutro mínimo

- Operações.
  - Consulta.
  - Simulação.
- Relatórios.
  - Resumo.
  - Detalhes.
- Configurações.
- Item indisponível para demonstrar estado desabilitado.

## Comportamento

- Item com filhos abre o próximo nível.
- Item folha apresenta mensagem local de demonstração.
- Voltar retorna um nível; no topo, retorna à tela-catálogo.
- Cancelar retorna diretamente à tela-catálogo.
- Estado desabilitado não navega.
- A trilha atual aparece como título ou caminho acessível.

## Critérios específicos

- Testes unitários percorrem a árvore e validam retorno.
- Testes de UI validam item com filhos, folha, desabilitado, voltar e cancelar.
- Nenhum nó exige rede, permissão, serviço externo ou código de transação.

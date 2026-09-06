# Auditoria de segurança — 002-ajustar-configuracao-produto-base

## Escopo e evidências

- Alterações de porta, proxy, login demonstrativo e scripts.
- Busca estática em código de aplicação para HTML cru, `eval`, URL de API fixa e padrões de segredo; `node_modules` e `dist` foram excluídos.

## Resultado executivo

Nenhuma vulnerabilidade nova foi identificada na Change. O login continua sendo demonstrativo e não é controle de acesso do servidor; essa limitação preexistente está registrada como SEC-001 na Change 001. As URLs de API são relativas e não expõem host interno no bundle do frontend.

## Categorias avaliadas

| Categoria | Resultado | Evidência |
| --- | --- | --- |
| Autenticação/autorização | Limitação herdada | `validarCredenciais` serve apenas à demonstração no navegador; o backend continua sem autenticação, fora do escopo desta Change. |
| Segredos | Conforme | Não foi inserido token, chave ou segredo de ambiente. |
| Entrada/XSS/injeção | Conforme na inspeção | Não há `dangerouslySetInnerHTML`, `innerHTML` ou `eval` no código alterado. |
| Comunicação | Conforme | `produtoService.js` usa `/produtos`; o host local existe somente no proxy de desenvolvimento do Vite. |

## Achados

Nenhum achado novo confirmado.

# Validação: Ajustar configuração de execução do Produto Base

## Status

`VALIDADA` em 2026-09-05.

## Ambiente e comandos

| Item | Evidência |
| --- | --- |
| Ambiente | Windows; Node 24.16.0; npm 11.13.0. Java, Maven e Maven Wrapper indisponíveis. |
| Comando | `npm test` em `apps/frontend/web/produtobase` |
| Resultado | 2 testes aprovados; código de saída 0. |
| Comando | `npm run build` em `apps/frontend/web/produtobase` |
| Resultado | Build Vite concluído; código de saída 0. |
| Testes Java | Não aplicáveis: a Change só altera propriedade declarativa do Quarkus, sem alterar código Java. |

## VAL-001 — Critérios de aceite

- `validarCredenciais('root', 'root')` foi aprovado e combina exclusivamente as credenciais demonstrativas definidas na SPEC.
- Credenciais alternativas foram rejeitadas pelo teste unitário.
- `quarkus.http.port=1000`, a porta 2000 do Vite e o proxy para `localhost:1000` foram verificados por inspeção estática.
- `produtoService.js` mantém `/produtos` como URL relativa, sem host fixo.
- Os dois scripts batch exibem as URLs coerentes com as portas aprovadas.

## Auditoria de segurança

Aplicável e registrada em `reviews/2026-09-05-security-audit.md`. Não há achado novo confirmado; a limitação preexistente de autenticação somente no frontend foi registrada como herdada da Change 001.

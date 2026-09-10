# Validação: 063-dashboard-atendimento-servico

## Status

`VALIDADA`

A implementação foi aprovada em revisão e validada automaticamente em 2026-09-07. A cobertura disponível foi aceita pelo solicitante no nível medido. A aprovação final e o arquivamento permanecem pendentes do teste manual com Keycloak e PostgreSQL reais.

## Ambiente

- Sistema operacional: Windows, PowerShell.
- Backend: Java 17.0.11, Apache Maven 3.8.8, Quarkus 3.2.10.Final e H2 no perfil de teste.
- Frontend: Node.js 24.16.0, npm 11.13.0, Vite, TypeScript e Vitest.
- Integrações externas durante a automação: Keycloak simulado e PostgreSQL substituído por H2; nenhuma credencial real foi utilizada.

## Comandos e códigos de saída

| ID | Comando | Resultado | Código de saída |
| --- | --- | --- | ---: |
| VAL-001 | `$env:JAVA_HOME='C:\Desenvolvimento\jdk-17.0.11'; & 'C:\Desenvolvimento\apache-maven-3.8.8\bin\mvn.cmd' clean verify` em `apps/backend/dashboardatendimentoservico` | 32 testes aprovados; pacote Quarkus gerado | 0 |
| VAL-002 | `npm run build; if ($LASTEXITCODE -eq 0) { npm test -- --run }` em `apps/frontend/web/dashboardatendimentoservico` | Build aprovado; 6 arquivos e 9 testes aprovados | 0 |
| VAL-003 | `& scripts/tests/gerar_start_aplicacao_dashboardatendimentoservico.Tests.ps1` | Geração positiva e falha segura por chave ausente aprovadas | 0 |
| VAL-004 | `npm audit` em `apps/frontend/web/dashboardatendimentoservico` | 0 vulnerabilidades conhecidas | 0 |

## Testes unitários, integração e cobertura

### Backend

- Ferramentas: JUnit 5, Mockito, Quarkus Test, Rest Assured, H2 e JaCoCo.
- Resultado: 32 testes aprovados, sem falhas ou erros.
- Cobertura registrada pelo relatório JaCoCo do `clean verify`: 68,08% de linhas (273 cobertas e 128 não cobertas) e 37,23% de branches (70 cobertos e 118 não cobertos).
- Limitação do relatório: na versão Quarkus 3.2 utilizada, o relatório agregado do `quarkus-jacoco` não incorporou todas as execuções dos testes JUnit sem contêiner. O percentual oficial acima foi mantido sem estimativas ou ocultação e aceito pelo solicitante em 2026-09-07.
- Escopo aplicável exercitado: autenticação OIDC, sessão BFF, renovação, logout, filtro de segurança, autorização por papel, isolamento por proprietário/IDOR, regras de compras, paginação, agregações do dashboard, persistência Panache, health checks e limpeza de sessões.
- Exclusões justificadas da medição unitária direta: interfaces sem implementação (`CompraRepository`, `ProvedorAutenticacao` e `KeycloakRestClient`); records/DTOs sem regra própria; declaração JPA de `CompraEntity`, coberta indiretamente pela integração; classes de resposta com mapeamento trivial, cobertas pelos testes dos recursos.

### Frontend

- Ferramentas: Vitest, Testing Library e cobertura V8.
- Resultado: 6 arquivos de teste e 9 testes aprovados.
- Cobertura: 83,66% de statements, 82,75% de branches, 75,86% de funções e 84,21% de linhas.
- Escopo aplicável exercitado: cliente HTTP e CSRF, sessão e guarda de rota, login, dashboard, listagem/edição de compras e comportamentos condicionados ao papel.
- Exclusões configuradas: arquivos de teste e setup, bootstrap `main` e declarações puras de tipos.

## Cenários validados

- Autenticação obrigatória nas rotas protegidas e sessão opaca em cookie `HttpOnly`.
- Renovação e encerramento de sessão sem exposição de tokens ao navegador.
- Validação de origem e CSRF nas requisições mutáveis.
- Isolamento de compras pelo usuário autenticado e bloqueio de acesso por identificador de terceiro.
- Restrições administrativas para alteração de status e exclusão.
- CRUD, filtros, paginação, indicadores, gráficos e dez compras recentes.
- Build e testes do frontend com estados de carregamento, vazio, erro e feedback acessível.
- Geração segura do BAT, inclusive recusa em sobrescrever a saída quando uma chave obrigatória não existe.
- Ausência de dependências Spring no código-fonte e ausência de tokens armazenados no frontend.
- Auditoria de dependências do frontend sem vulnerabilidades conhecidas.

## Auditoria de segurança

- Resultado: nenhum achado confirmado aberto no escopo automatizado.
- Verificações: autenticação, autorização, IDOR, sessão, CSRF, origem, segredos, logs, validação de entrada, XSS/injeção e dependências.
- Evidência: `docs/security-audit/relatorio-063-dashboard-atendimento-servico.pdf`, renderizado integralmente e inspecionado visualmente em duas páginas A4.
- Limitação: a configuração e o comportamento do Keycloak e PostgreSQL reais somente poderão ser confirmados no teste manual.

## Pendências para aprovação final

- Corrigir no arquivo externo de configuração o nome da chave para `BANCO_DB_DASHBOARDCOMPRAS`; o gerador continuará falhando com segurança enquanto a chave esperada não existir.
- Gerar e executar o BAT local, iniciar o frontend e validar login, renovação, logout, dashboard, CRUD, isolamento entre usuários e ações de `ADMIN` contra Keycloak e PostgreSQL reais.
- Registrar a evidência do solicitante para o critério manual CA-011.

## Evidências

- VAL-001: log do Maven com 32 testes aprovados e pacote Quarkus criado.
- VAL-002: log do Vite/Vitest com build e 9 testes aprovados.
- VAL-003: saída do teste sintético do gerador cobrindo sucesso e falha segura.
- VAL-004: saída do `npm audit` com 0 vulnerabilidades.
- Revisão: `reviews/2026-09-07-implementation-review.md` com veredito `IMPLEMENTACAO_APROVADA`.
- Segurança: relatório PDF da auditoria automatizada.

## Veredito

`VALIDADA`

Validação automática aprovada no nível de testes aceito pelo solicitante. A mudança ainda não está `APROVADA`, arquivada ou pronta para commit final porque a validação manual externa permanece obrigatória.

# Validação

## Estado: VALIDADA

Data: 2026-09-06. Windows 10 amd64; Java Oracle 17.0.11; Maven 3.8.8; Node 24.16.0; npm 11.13.0; Vite 5.4.21; Quarkus 3.2.0.Final.

## Evidências

| ID | Diretório/comando | Resultado | Saída |
| --- | --- | --- | --- |
| VAL-001 | Backend: Maven test, baseline | Falha de compilação: MockitoExtension e AssertJ ausentes | 1 |
| VAL-002 | Backend: Maven verify, primeira correção | AssertJ exige versão explícita; corrigida para 3.24.2 | 1 |
| VAL-003 | Backend: Maven -B verify, após revisão | 10 testes, zero falhas/erros/ignorados; BUILD SUCCESS; pacote gerado | 0 |
| VAL-004 | Frontend: npm test | 3 testes aprovados: login válido, inválido e negociação HTML/API | 0 |
| VAL-005 | Frontend: npm run build | Build Vite aprovado | 0 |
| VAL-006 | Frontend: node scripts/smoke.mjs | HTML 200, CRUD PostgreSQL pelo proxy 2000, API 1000, paginação, exclusão 204 e 404 subsequente | 0 |
| VAL-007 | HTTP localhost:9000/api/system/status, timeout 3s | Sonar não disponível; auditoria LLM no relatório de segurança/qualidade | 20 |

Execução Maven: definir JAVA_HOME=C:\Desenvolvimento\jdk-17.0.11 e incluir seu bin no PATH da sessão; executar C:\Desenvolvimento\apache-maven-3.8.8\bin\mvn.cmd -B verify dentro de apps/backend/produtobase. Nenhuma alteração global.

Relatórios locais: target/surefire-reports/br.com.romulopenha.produtobase.application.ProdutoServiceTest.txt (9 testes) e br.com.romulopenha.produtobase.api.ProdutoResourceTest.txt (1 cenário completo). Última execução formal: 00:07, duração Maven 18,195s.

## Correções ao registro anterior

Java/Maven estavam instalados, embora ausentes do PATH. O fallback anterior não comprovava compilação nem funcionamento e não bastava para encerrar a Sprint. A navegação HTML via /produtos retornava 406 antes da correção do proxy; agora retorna a SPA com HTTP 200.

Durante a verificação o backend já passou a ocupar a porta 1000. Uma tentativa adicional de quarkus:dev encontrou conflito de porta e foi encerrada; a instância existente foi preservada e usada no smoke. PostgreSQL existente confirmado saudável e banco produto_base presente. Nenhum container preexistente foi parado.

## Inventário e limites

Nenhum arquivo Java de produção alterado. O teste novo ProdutoResourceTest cobre HTTP e ORM; ProdutoServiceTest existente cobre serviço de aplicação sem container. Entidade, repositório declarativo e DTOs existentes não foram modificados. Não há ferramenta de cobertura configurada: nenhum percentual é declarado.

Smoke exclui somente registros próprios; dados anteriores são preservados. Testes de integração usam H2 em memória; smoke usa PostgreSQL real. Login é verificado por teste unitário, navegação por HTTP; não se declara teste visual ou E2E de cliques no navegador.

Sonar não executado; auditoria LLM e limitações no relatório específico. Autenticação real segue fora do escopo demonstrativo.

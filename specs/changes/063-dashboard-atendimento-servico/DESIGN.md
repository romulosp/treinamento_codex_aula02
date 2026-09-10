# Design: 063-dashboard-atendimento-servico

## Contexto

A solução combina dashboard de compras, autenticação Keycloak e execução local segura. O rascunho anterior misturava Spring e Quarkus, expunha tokens ao navegador, incluía segredos em exemplos e colocava geração de PDF no código da aplicação. Este design corrige esses pontos e adota somente Quarkus/Jakarta no backend.

## Referências

- `proposal.md`
- `spec.md`
- `specs/shared/architecture/backend-java.md`
- `specs/shared/testing/testing-strategy.md`
- Change arquivada `026-chaves-aplicacao`

## Decisões

1. O backend será um único projeto Maven Quarkus; `autenticacao-keycloak` será um componente interno reutilizável por casos de uso do mesmo backend, sem implantação independente.
2. A API usará RESTEasy Reactive/Jakarta REST, Hibernate ORM Panache, Bean Validation, SmallRye OpenAPI, OIDC, REST Client Reactive, SmallRye Health, Micrometer e Scheduler, todos gerenciados pelo BOM do Quarkus.
3. O Keycloak Admin Client não será usado: administração de Keycloak está fora do escopo e operações de token usam endpoints OIDC.
4. O backend atuará como BFF e manterá access/refresh tokens associados a um identificador opaco de sessão. O frontend nunca recebe tokens.
5. Compras são sempre filtradas pelo `sub` autenticado, inclusive detalhe, atualização, exclusão e agregações.
6. React/Vite/TypeScript implementará o frontend e um guard reutilizável validará a sessão no backend.
7. Configuração real será lida do arquivo externo por gerador versionado; apenas template sem valores será rastreado.
8. O relatório PDF de segurança será produzido na validação, fora da aplicação.
9. O padrão Panache Active Record será usado somente pela entidade de persistência em `infrastructure` e ficará escondido atrás do adaptador do repositório.
10. Como Hibernate ORM/JDBC é bloqueante, os casos de uso serão imperativos; não será criado `Uni` apenas para envolver chamadas bloqueantes.
11. O login personalizado adotará Direct Access Grants como pré-condição explícita. A aplicação falhará de forma segura quando o cliente Keycloak não suportar o fluxo.
12. Somente o identificador opaco da sessão será transportado em cookie HttpOnly; os tokens OIDC permanecem no armazenamento limitado do backend.

## Arquitetura e componentes

### Backend

- `api`: `AuthResource`, `CompraResource`, DTOs, validação e mapeadores HTTP.
- `application`: casos de uso de autenticação, sessão, CRUD e dashboard.
- `domain`: `Compra`, status, contratos de repositório e contratos de autenticação.
- `infrastructure`: entidade Active Record/repositório Panache, cliente OIDC Keycloak, validador de token, armazenamento local de sessão, job de limpeza e configuração.

Fluxo de login: frontend envia credenciais por HTTPS/ambiente local ao backend; o adaptador OIDC solicita tokens ao Keycloak; o backend valida o token, cria uma sessão opaca e devolve cookie HttpOnly mais informações públicas. O refresh e logout usam a sessão, nunca tokens enviados pelo navegador.

Fluxo de compra: o recurso obtém o `sub` da sessão autenticada, delega ao caso de uso e o repositório sempre inclui esse identificador no filtro. O DTO de entrada não controla propriedade.

### Frontend

- `LoginPage`: formulário, estados de envio e erro genérico.
- `ProtectedRoute`: consulta de sessão e redirecionamento.
- `DashboardPage`: cards, gráficos e compras recentes.
- `CompraForm`: criação e edição.
- `CompraTable`: listagem, paginação e ações.
- `authClient` e `compraClient`: únicas fronteiras HTTP.

### Configuração

Um gerador PowerShell identifica as seções corretas, valida todas as chaves, escapa caracteres de `cmd.exe`, renderiza arquivo temporário e substitui atomicamente o BAT local. Testes usam arquivo sintético; valores reais não entram em evidências.

### Operação

- Liveness verifica somente se o processo responde; indisponibilidade de banco não deve reiniciar indefinidamente uma instância saudável.
- Readiness verifica PostgreSQL e a configuração necessária ao Keycloak, sem devolver exceções ou endereços sensíveis.
- Métricas não usam `sub`, usuário, token, compra ou mensagem de erro como tags.
- O Scheduler remove apenas sessões expiradas do armazenamento limitado.
- O proxy de desenvolvimento mantém frontend e API na mesma origem lógica. CORS curinga com credenciais é proibido.

## Alternativas e consequências

- Tokens em `localStorage`: rejeitado por aumentar o impacto de XSS.
- Validação somente no frontend: rejeitada porque não protege a API.
- Keycloak Admin Client: rejeitado porque autenticação/token não exige API administrativa.
- Spring Boot/Spring Data: rejeitados por conflito com a arquitetura obrigatória do projeto.
- Serviço de autenticação implantável separado: rejeitado nesta Change; acrescentaria rede, observabilidade, implantação e resiliência não solicitadas.
- Authorization Code com PKCE e login hospedado no Keycloak: rejeitado nesta Change porque altera a experiência de login personalizado especificada; pode ser objeto de Change futura.
- Access token e refresh token diretamente em cookies: rejeitado; embora HttpOnly reduza acesso por JavaScript, envia tokens ao navegador e amplia o impacto de CSRF e vazamentos. O cookie contém apenas identificador opaco.
- Entidade Panache no domínio e chamadas estáticas em recursos/serviços: rejeitadas por violar as camadas obrigatórias e dificultar testes unitários sem Quarkus.
- Qute: rejeitado porque o frontend já é React/Vite.
- `import.sql` padrão: rejeitado porque proprietários artificiais conflitariam com o isolamento pelo `sub` autenticado.
- CORS `*` e respostas contendo classe/mensagem de exceção: rejeitados por exposição desnecessária e conflito com sessão baseada em cookie.

# Revisão da SPEC: 063-dashboard-atendimento-servico

## Escopo da revisão

Foram revisados `proposal.md`, `spec.md`, `DESIGN.md`, `tasks.md`, o material fornecido pelo solicitante e as regras compartilhadas de processo, arquitetura, REST, persistência e testes.

## Correções aplicadas ao rascunho

- Spring Boot, Spring Data, `RestTemplate`, `@MockBean` e propriedades `spring.*` foram substituídos por Quarkus/Jakarta.
- Pacote-base foi corrigido para a convenção `br.com.romulopenha` + artifactId sem hífens.
- Tokens deixaram de ser armazenados no navegador e passaram a ser gerenciados pelo backend.
- O Keycloak Admin Client foi removido do desenho de autenticação.
- Valores concretos de credenciais foram removidos da especificação.
- A chave de banco copiada de fotogaleria foi corrigida para `BANCO_DB_DASHBOARDCOMPRAS`.
- Geração do PDF de segurança foi movida do código da aplicação para a fase de validação.
- Geração automática de tabelas foi limitada a desenvolvimento/teste.
- O complemento v3.1 foi incorporado apenas onde compatível: Quarkus 3.2.10.Final, Panache Active Record encapsulado, observação, filtros, papéis, PATCH de status, health, métricas e limpeza de sessões.

## Achados

| ID | Severidade | Evidência | Impacto | Recomendação |
| --- | --- | --- | --- | --- |
| REV-001 | Bloqueante | O título usa “atendimento de serviço”, mas todos os requisitos funcionais descrevem compras. | Nome, linguagem da interface e documentação podem representar domínio diferente do esperado. | Confirmar compras como domínio ou renomear a Change para `063-dashboard-compras`. |
| REV-002 | Bloqueante | A tela personalizada com usuário/senha exige Direct Access Grants no cliente Keycloak; a disponibilidade não foi confirmada. | Sem essa configuração, `POST /api/auth/login` não consegue autenticar pelo fluxo especificado. | Confirmar Direct Access Grants ou substituir pelo Authorization Code + PKCE/login hospedado. |
| REV-003 | Importante | O pedido solicita extrair o gerenciamento Keycloak, mas não define se é biblioteca ou serviço independente. | Um serviço separado muda implantação, segurança, testes e operação. | Confirmar a decisão proposta de módulo Maven interno `autenticacao-keycloak`. |
| REV-004 | Importante | Campos e valores de `categoria`/`status` vieram do primeiro rascunho, sem regras de negócio confirmadas. | O CRUD pode persistir informações ou transições diferentes das necessárias. | Confirmar campos e estados `PENDENTE`, `APROVADA` e `CANCELADA`. |
| REV-005 | Bloqueante | O complemento coloca `Compra extends PanacheEntity` junto ao modelo funcional e faz serviços/resources chamarem métodos estáticos Panache. | Viola a separação obrigatória entre domínio e infraestrutura e dificulta testes unitários independentes do Quarkus. | Manter Active Record somente na entidade de infraestrutura, atrás do contrato de repositório, como consolidado no `DESIGN.md`. |
| REV-006 | Bloqueante | O complemento contém CORS `*`, valores concretos de banco/OIDC, impressão de endereços e refresh token no header `Authorization`. | Pode expor credenciais/tokens, permitir origens indevidas e confundir access token com refresh token. | Manter sessão BFF/cookie opaco e configuração externa sem valores, conforme REQ-005, REQ-006, REQ-014 e REQ-015. |
| REV-007 | Importante | Qute, Cache, Scheduler, Health, Metrics, YAML, logging JSON e PDFBox foram adicionados como “dependências completas”, sem todos terem caso de uso. | Aumenta superfície de ataque, build e manutenção sem benefício comprovado. | Incluir somente Health, Micrometer e Scheduler pelos requisitos incorporados; excluir Qute, PDFBox e dependências sem uso. |
| REV-008 | Importante | O complemento marca critérios como concluídos (`[x]`) antes de existir implementação e apresenta código parcial como completo. | Produz evidência falsa e poderia permitir avanço indevido nos gates. | Manter critérios não marcados até validação reproduzível registrada em `validation.md`. |
| REV-009 | Importante | O handler proposto devolve `exception.getMessage()` e nome da classe; health devolve mensagem interna. | Pode revelar detalhes técnicos, consultas, endereços ou dados sensíveis. | Usar erros públicos estáveis/correlation id e manter detalhes somente em logs protegidos; health não retorna exceções. |

## Veredito

`REPROVADA`

A especificação e o complemento foram consolidados em Quarkus puro. Os achados REV-005 a REV-009 já possuem correção proposta nos documentos, mas, junto aos pontos de decisão REV-001 a REV-004, permanecem pendentes de aceite formal do solicitante. Eles impedem `SPEC_APROVADA` e qualquer implementação. A aprovação final da Change continuará reservada para depois da implementação, revisão, testes e validação manual solicitada.

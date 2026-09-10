# Revisão da SPEC — resubmissão: 063-dashboard-atendimento-servico

## Escopo

Foram reavaliados `proposal.md`, `spec.md`, `DESIGN.md`, `tasks.md`, a revisão anterior e o complemento v4.0 fornecido pelo solicitante. A revisão respeitou as regras compartilhadas de arquitetura, REST, persistência, testes e evidências.

## Achados reavaliados

| ID | Severidade anterior | Evidência da resolução | Resultado |
| --- | --- | --- | --- |
| REV-001 | Bloqueante | O complemento v4.0 mantém expressamente o nome da Change e detalha compras como domínio funcional. A distinção foi registrada em `proposal.md`. | Resolvido |
| REV-002 | Bloqueante | Direct Access Grants passou a ser pré-condição contratual verificável. Falha de configuração impede login sem fallback silencioso. | Resolvido |
| REV-003 | Importante | A estrutura v4.0 coloca autenticação dentro do backend. O design consolida um componente interno no único projeto Maven Quarkus. | Resolvido |
| REV-004 | Importante | O complemento v4.0 reafirma campos, observação e estados; `spec.md` fixa limites e valores aceitos. | Resolvido |
| REV-005 | Bloqueante | Panache permanece limitado à entidade/adaptador em `infrastructure`; aplicação e domínio usam contrato de repositório. | Resolvido |
| REV-006 | Bloqueante | Configuração concreta permanece externa; CORS não aceita `*`; o cookie contém identificador opaco, e tokens permanecem no backend. | Resolvido |
| REV-007 | Importante | O contrato inclui somente dependências justificadas por casos de uso. Qute, PDFBox, YAML e Keycloak Admin Client não integram a solução. | Resolvido |
| REV-008 | Importante | Nenhum critério de implementação foi marcado como concluído; somente as pré-condições documentais efetivamente cumpridas foram atualizadas. | Resolvido |
| REV-009 | Importante | Erros e health checks não devolvem exceções internas; métricas e logs não contêm identificadores sensíveis. | Resolvido |

## Observações não bloqueantes

- Os blocos de código do material v4.0 são referência de intenção, não implementação normativa. Foram descartados trechos incompatíveis com a SPEC canônica, incluindo segredos literais no BAT, tokens OIDC diretamente em cookies, pacote `br.com.nova`, `Uni` envolvendo Hibernate ORM bloqueante e detalhes de exceção em health checks.
- A aprovação aqui é exclusivamente da SPEC. A Change não está implementada, validada, aprovada nem autorizada para arquivamento/commit final.
- `implementation-plan.md` deve ser criado antes da implementação, conforme o workflow.

## Veredito

`SPEC_APROVADA`

Os requisitos, limites, dependências, riscos, arquitetura e critérios de aceite estão verificáveis e os achados materiais da revisão anterior foram resolvidos. A implementação pode ser planejada, mas a aprovação final da Change permanece obrigatoriamente condicionada à revisão da implementação, testes, auditoria de segurança e validação manual do solicitante.

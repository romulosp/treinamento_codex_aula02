# Proposta de Skill de Auditoria de Segurança

## Objetivo

Transformar o roteiro de auditoria de segurança fornecido pelo solicitante em uma Skill reutilizável do laboratório, capaz de revisar uma implementação concluída com evidências verificáveis e sem expor segredos.

## Escopo

- Criar a Skill operacional `security-audit` em `.agents/skills/security-audit/SKILL.md`.
- Cobrir detecção de stack, isolamento de tenant, autenticação/autorização no servidor, IDOR, segredos e tratamento de entradas/XSS.
- Orientar a análise do histórico Git, a classificação de achados e a redação de recomendações acionáveis.
- Reutilizar o gerador de relatório PDF em `docs/security-audit/` quando o usuário solicitar o artefato.
- Atualizar o catálogo local de Skills.

## Fora do escopo

- Corrigir automaticamente vulnerabilidades sem solicitação explícita.
- Expor valores secretos encontrados em relatórios, chat ou novos arquivos.
- Reescrever o histórico Git ou revogar credenciais em sistemas externos.

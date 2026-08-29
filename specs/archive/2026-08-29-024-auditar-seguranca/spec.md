# SPEC – Skill de Auditoria de Segurança

## Requisitos de aceite

1. A Skill deve ser descoberta como `security-audit` e declarar claramente quando deve ser usada.
2. A Skill deve exigir a identificação da stack e a delimitação do escopo antes dos testes.
3. A auditoria deve verificar, quando aplicável:
   - isolamento por tenant/usuário;
   - autenticação e autorização efetivas no servidor;
   - IDOR e acesso a objetos por identificador;
   - segredos em código, configurações, scripts, deploy, CI e histórico Git;
   - validação de entrada e superfícies de XSS/injeção.
4. A Skill só pode registrar achados sustentados por arquivo, linha, commit ou evidência reproduzível. Suspeitas devem ser classificadas como limitação ou hipótese, não como vulnerabilidade confirmada.
5. Valores secretos devem ser redigidos. A recomendação para segredo exposto deve priorizar rotação/revogação e tratar reescrita de histórico como ação separada e autorizada.
6. A saída deve separar controles conformes, achados confirmados, categorias não aplicáveis, limitações, severidade, exploração e recomendações priorizadas.
7. Quando solicitado um PDF, a Skill deve orientar a execução do gerador existente, a extração de texto e a inspeção visual das páginas antes da entrega.
8. A Skill não deve implementar correções, fazer deploy, enviar mensagens ou alterar histórico sem autorização específica.

## Artefatos

- `.agents/skills/security-audit/SKILL.md`
- `.agents/skills/README.md` atualizado com a Skill técnica.
- Validação da Skill pelo `quick_validate.py` do `skill-creator`.

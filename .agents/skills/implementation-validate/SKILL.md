---
name: implementation-validate
description: 'Use when: validar uma implementação aprovada em revisão, executar testes e registrar evidências contra validation.md.'
argument-hint: 'Informe o caminho da mudança a validar.'
---

# Validação de implementação

1. Confirme `IMPLEMENTACAO_APROVADA`, leia os critérios de aceite e a estratégia de testes.
2. Execute os testes e verificações aplicáveis sem ocultar falhas ou avisos relevantes.
3. Registre em `validation.md`: ambiente, versões, comandos, código de saída, cenários, resultado e evidências `VAL-001`.
4. Conclua com `VALIDADA` ou `REPROVADA`. Pendências importantes tornam a validação reprovada.
5. Não altere código durante a validação; retorne à implementação ou à SPEC conforme a causa da falha.

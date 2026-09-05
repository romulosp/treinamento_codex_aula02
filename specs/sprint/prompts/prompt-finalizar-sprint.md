# Finalizar Sprint

Avalie o encerramento de uma Sprint sem alterar o workflow 01-06 das Changes.

## Objetivo

Confirmar se a Sprint pode ser concluída com base no estado e nas evidências de suas Changes referenciadas.

## Regras

1. Verifique todas as Changes da Sprint conforme `specs/sprint/README.md` e confirme o gate real de cada uma.
2. Para cada item candidato a `DONE`, confirme integralmente a Definition of Done do template e a documentação de cada marco: contrato, revisão da SPEC, plano técnico, implementação, revisão, validação, aprovação, atualização de `system/`, arquivamento, commit e evidências exigidas pela SPEC.
3. Antes de aceitar uma Change como `DONE`, verifique e registre as evidências de testes unitários e de integração aplicáveis, cobertura medida de forma reproduzível quando configurada, JavaDoc ou documentação de código aplicável, verificações de Sonar configuradas e atualização de README ou documentação de uso quando houver impacto relevante. Se o módulo não possuir Sonar ou cobertura, exija a Auditoria de Qualidade Assistida por LLM registrada em `validation.md`, com escopo, arquivos, comandos, resultados, mapeamento de testes, achados e correções.
4. Execute a Skill `security-audit` antes da aprovação final e do commit quando a Change possuir artefato de frontend/backend, API, autenticação, autorização, configuração, dependência, segredo ou integração no escopo. Registre as evidências em `validation.md`; para Change exclusivamente documental, registre a não aplicabilidade e os artefatos inspecionados.
5. Quando a auditoria for aplicável, exija um relatório PDF de segurança atual em `docs/security-audit/`, gerado por `docs/security-audit/gerar_relatorio.py`. Confira o conteúdo e a redação de segredos; não reutilize relatório de auditoria anterior ou estático como evidência. Se o gerador não representar a auditoria atual, registre a Change como `BLOCKED`.
6. Se a auditoria confirmar um achado de segurança, não aceite a Change como `DONE`: corrija-o autonomamente dentro do escopo aprovado, repita revisões e validações necessárias e execute a auditoria novamente. Somente uma auditoria atual sem achados confirmados permite prosseguir.
7. Se a correção exigir alteração da SPEC, ação externa ou decisão fora do escopo aprovado, registre a Change como `BLOCKED`; não solicite uma autorização para ignorar o achado e não marque `DONE`.
8. Não aceite o status `DONE` se houver teste exigido ausente, cobertura abaixo de 80% quando aferível, problema de qualidade, verificação Sonar configurada pendente ou reprovada, Auditoria de Qualidade Assistida por LLM ausente ou com achado não corrigido quando Sonar/cobertura não existirem, documentação aplicável ausente, auditoria de segurança ausente ou achado confirmado sem correção, ou exceção sem decisão justificada e aprovada.
9. Registre pendências, itens `REMOVED` e bloqueios, com suas justificativas objetivas, risco e impacto de segurança ou de negócio e condição objetiva de resolução.
10. Antes de marcar a Sprint como `COMPLETED`, execute a Sprint Review:
   1. verifique as Changes e suas evidências;
   2. confirme a Definition of Done;
   3. mapeie as mudanças entregues;
   4. verifique se o README da raiz precisa ser atualizado;
   5. atualize o README somente quando houver impacto relevante e a Sprint for concreta.
11. Registre em Decisões importantes somente decisões relevantes, com data, contexto e impacto.
12. Antes de marcar a Sprint como `COMPLETED`, registre a retrospectiva com pontos positivos, problemas, lições aprendidas e ações objetivas para a próxima Sprint. Evite observações genéricas ou sem ação prática.
13. Marque a Sprint como `COMPLETED` somente depois de concluir a Review e a Retrospectiva, quando todos os itens estiverem `DONE` ou `REMOVED` com decisão registrada e quando não houver pendência.
14. Permita o status `CANCELLED` somente quando houver motivo registrado em Eventos e decisões, situação documentada de cada Change e retorno dos itens não concluídos ao backlog elegível.
15. Não crie requisitos, critérios de aceite, detalhes técnicos ou uma etapa adicional ao workflow 01-06.

## Resultado esperado

Informe se a Sprint está `COMPLETED`, permanece pendente ou está `CANCELLED`, indicando as evidências verificadas e qualquer pendência, remoção ou bloqueio registrado.

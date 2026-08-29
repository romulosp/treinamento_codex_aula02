# Validação – 001-hardening

**Status:** `VALIDADA`

**Data:** 2026-08-28

## Ambiente e comando

- Java `17.0.11` (`C:\Desenvolvimento\jdk-17.0.11`).
- Maven `3.8.8` (`C:\Desenvolvimento\apache-maven-3.8.8\bin\mvn.cmd`).
- Quarkus `3.2.10.Final`.
- Perfil de teste com H2 em memória e OIDC desabilitado; as variáveis obrigatórias de datasource e OIDC foram fornecidas externamente, sem registrar seus valores.
- Comando executado no diretório `apps/backend/gerenciartarefas`:

```powershell
$env:JAVA_HOME='C:\Desenvolvimento\jdk-17.0.11'
$env:POSTGRESQL_JDBC_URL='<fornecida externamente>'
$env:POSTGRESQL_USERNAME='<fornecida externamente>'
$env:POSTGRESQL_PASSWORD='<fornecida externamente>'
$env:OIDC_AUTH_SERVER_URL='<fornecida externamente>'
$env:OIDC_CLIENT_ID='<fornecida externamente>'
$env:OIDC_CLIENT_SECRET='<fornecida externamente>'
& 'C:\Desenvolvimento\apache-maven-3.8.8\bin\mvn.cmd' '-Dmaven.repo.local=D:\desenvolvimento\ia\aula02\.m2-local' verify
```

**Resultado:** código de saída `0`, `BUILD SUCCESS`, 34 testes executados, 0 falhas, 0 erros e 0 ignorados.

## Evidências VAL

- **VAL-001 – Rotas protegidas:** integração confirmou `401` sem identidade, `403` para papel `USER` nas escritas e acesso permitido para identidade `ADMIN` válida.
- **VAL-002 – Isolamento:** o contexto é derivado do principal autenticado; header divergente retorna `403`; as consultas de listagem e por identificador incluem `tenantId`; exclusão usa a mesma busca protegida.
- **VAL-003 – Entrada:** `@Valid`, `@NotBlank` e `@Size` foram exercitados em cenários válidos, corpo nulo, título vazio e título acima do limite.
- **VAL-004 – Testes unitários:** `TarefaResourceUnitTest`, `TarefaServiceTest`, `SecurityConfigTest`, `TenantContextTest` e `TenantFilterTest` executam sem banco e sem container Quarkus.
- **VAL-005 – Testes de integração:** `TarefaResourceTest` e `TarefaResourceSecurityTest` executam com Quarkus e H2 em memória.
- **VAL-006 – Segredos:** busca no estado atual não encontrou credenciais produtivas no código ou no script. A análise histórica confirmou uma exposição antiga em `62cb91700b60127a77b0842d2fddcaf41480464c`, `apps/backend/start_aplicacao.bat:12,16`; os valores foram redigidos. A remoção atual não elimina a exposição histórica.
- **VAL-007 – Entradas/XSS:** a busca estática por superfícies de HTML cru, `eval`, `new Function`, `javascript:` e equivalentes em `apps`, `docs` e configurações aplicáveis não encontrou ocorrências. Não há frontend implementado no escopo atual.
- **VAL-008 – Relatório:** `docs/security-audit/relatorio-auditoria-seguranca.pdf` foi gerado pelo script local, possui 5 páginas, foi renderizado para inspeção visual e a extração confirmou `redigido=True` e ausência dos segredos reais.

## Inventário de produção e cobertura

O escopo contém 18 classes Java de produção:

| Grupo | Classes | Tratamento |
| --- | --- | --- |
| API | `ListaTarefasResponse`, `MensagemResponse`, `ResultadoExclusaoResponse`, `TarefaInvalidaExceptionMapper`, `TarefaNaoEncontradaExceptionMapper`, `TarefaRequest`, `TarefaResource`, `TarefaResponse` | DTOs/records e mappers declarativos são excluídos da cobertura unitária por não conterem regra customizada; `TarefaRequest` e `TarefaResource` são aplicáveis e cobertos. |
| Aplicação | `TarefaService` | Aplicável; teste unitário independente de banco. |
| Domínio | `StatusTarefa`, `Tarefa`, `TarefaInvalidaException`, `TarefaNaoEncontradaException` | Enum sem lógica e modelos/exceções declarativos são excluídos com justificativa; a lógica observável do domínio é exercitada pelo serviço. |
| Infraestrutura | `SecurityConfig`, `TarefaEntity`, `TarefaRepository`, `TenantContext`, `TenantFilter` | Configuração declarativa, entidade e contexto simples são excluídos quando não há regra customizada; `SecurityConfig`, `TarefaRepository` e `TenantFilter` têm cenários dedicados ou de integração. |

JaCoCo `0.8.12` foi executado durante `mvn verify`. Resultado relevante: `TarefaResource` atingiu 100% de linhas, branches e instruções; `TarefaService` atingiu aproximadamente 97,4% de linhas e 80% de branches; `TenantFilter` 100% de linhas e aproximadamente 81,3% de branches; `SecurityConfig` 100% de linhas e 75% de branches. A classe `TarefaRepository` não recebeu percentual artificial: o relatório registrou incompatibilidade entre bytecode instrumentado e transformado pelo Quarkus, embora suas consultas sejam exercitadas nos testes de integração. A meta específica da SPEC para as rotas modificadas foi atendida.

## Conclusão

Todos os critérios aplicáveis foram validados. O único achado de segurança confirmado é histórico: credenciais que já foram comprometidas no Git devem ser rotacionadas/revogadas. Não houve alteração destrutiva do histórico nesta mudança.

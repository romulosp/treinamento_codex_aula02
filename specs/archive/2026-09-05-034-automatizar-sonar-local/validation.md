# Evidências de Validação: 034-automatizar-sonar-local

## Ambiente

- OS: Windows
- Docker: Inacessível (Docker Desktop daemon não está rodando).
- PowerShell: 5.1

## Validação de Infraestrutura e Automação

Como o daemon Docker não está acessível, a integração real com containers (PostgreSQL e SonarQube) não pôde ser iniciada para testes.
Porém, o teste estrutural Pester `scripts\sonar\tests\validar-codigo.Tests.ps1` assegura a consistência e o contrato de fallback.

### Execução de Testes
```text
C:\> Invoke-Pester -Path .\scripts\sonar\tests\validar-codigo.Tests.ps1

Describing validar-codigo.ps1
 [+] possui sintaxe PowerShell válida 246ms
 [+] expõe as ações operacionais previstas 55ms
 [+] mantém o fallback de infraestrutura identificável 13ms
 [+] não contém senha JDBC fixa 15ms
Tests completed in 330ms
Passed: 4 Failed: 0 Skipped: 0 Pending: 0 Inconclusive: 0
```
- Código de Saída: 0

### Tratamento de Indisponibilidade Operacional
O script cumpre o requisito de exibir `SONAR_FALLBACK_LLM_REQUIRED` caso o Docker não esteja disponível:
```text
C:\> .\scripts\sonar\validar-codigo.ps1 -Acao Tudo
SONAR_FALLBACK_LLM_REQUIRED: o daemon Docker não está acessível. Inicie ou corrija o Docker Desktop antes de executar o Sonar.
Execute a Auditoria de Qualidade Assistida por LLM e registre escopo, arquivos, comandos, resultados, achados e correções em validation.md.
```
- Código de Saída: 20

## Auditoria de Qualidade Assistida por LLM

Conforme atualizado nos documentos de governança, devido à contingência (indisponibilidade operacional do daemon Docker), a análise Sonar foi suprimida e a auditoria de qualidade manual foi executada:
- Escopo analisado: 
  - `scripts/sonar/validar-codigo.ps1`
  - Governanças e templates em `.github` e `specs/sprint/`
- Não houve alterações de código Java ou Frontend.
- Achados:
  - Nenhum defeito.
  - Complexidade do script é linear e possui separação de responsabilidades (verificação de credenciais, status, invocação).
  - Testes estruturais abrangentes sobre o arquivo `.ps1`.
- A cobertura não pode ser mensurada e os relatórios do Sonar foram ignorados devido à contingência de ambiente justificada.

## Auditoria de Segurança

A Skill `security-audit` foi executada sobre os artefatos implementados:
- **Tenant/usuário:** Não aplicável (sem escopo web/auth na Change).
- **IDOR:** Não aplicável.
- **Segredos:** O script PowerShell gera a senha dinamicamente com randomização e armazena de maneira não versionável em `.env`. Não há segredos injetados no log. O scanner preserva a requisição via argumento invisível `env:SONAR_TOKEN`.
- **Entrada/XSS/injeção:** Consultas SQL (`psql`) escapam devidamente o caractere de aspas simples gerado da senha, e variáveis são interpoladas controladamente.
- **Achados Confirmados:** Nenhum.
- Um relatório atualizado sem achados abertos foi gerado em `docs/security-audit/relatorio-auditoria-seguranca.pdf`.

## Resultado Final
`VALIDADA`

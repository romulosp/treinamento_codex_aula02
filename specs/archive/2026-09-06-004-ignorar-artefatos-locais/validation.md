# Evidências técnicas

## Validação formal — VALIDADA

Executada após IMPLEMENTACAO_APROVADA em 2026-09-06, Git 2.54.0.windows.1 e Windows/PowerShell.

- VAL-001: repetidos os 13 caminhos ignorados e dois controles com git check-ignore --no-index -q. Códigos esperados 0 e 1 respectivamente; verificador agregado saiu com 0.
- VAL-002: git ls-files com filtro dos padrões confirmou zero arquivos rastreados abrangidos; saída agregada 0.
- VAL-003: git diff --check, saída 0, somente aviso LF/CRLF.
- VAL-004: PDF atual docs/security-audit/004-ignorar-artefatos-locais.pdf gerado com reutilização de build_styles/p do gerador existente. Saída 0; uma página extraída e renderizada, conferida visualmente sem cortes ou sobreposições. Runtime temporário PyMuPDF usado porque Poppler não estava no PATH; aviso de depreciação do alias fitz sem falha.

Auditoria de segurança: .gitignore:29-40, sem achados confirmados no diff. Limites: gitignore não inspeciona conteúdo nem remove histórico e git add -f pode contorná-lo. Autenticação, IDOR e XSS não aplicáveis à configuração de seleção Git. Nenhum segredo real no diff; histórico da aplicação fora do escopo.

Qualidade: configuração declarativa, sem build, testes de aplicação, cobertura ou Sonar aplicáveis. Testes diretos do Git comprovam comportamento. Duplicação de target/.quarkus intencional para precedência. Nenhum defeito material, complexidade ou tratamento de exceções aplicável. Sem percentual de cobertura declarado.

O PDF fica local conforme política documental; este registro preserva evidência textual no commit.

## Evidências técnicas anteriores

2026-09-06, Windows/PowerShell, Git local. Change IMPLEMENTADA; verificação técnica não equivale à aprovação formal do ciclo completo.

Comando: git check-ignore --no-index -q para 13 caminhos representativos; assertiva PowerShell sobre cada código (0 esperado para exclusões). Controles README.md e scripts/sonar/validar-codigo.ps1 preservados (1 esperado para não ignorados). Resultado agregado: PASS, saída 0.

Cobertos: node_modules/README.md, target/test.txt, dist/README.md, equivalentes em scripts, .quarkus, .env, .env.local, .key, .pem, .p12 e .pfx.

git ls-files com filtro dos padrões: zero arquivos já rastreados abrangidos. Nenhuma remoção do índice necessária.

git diff --check: saída 0. Aviso de conversão futura LF/CRLF do Git, sem erro.

Limite: regras de nomes não detectam credenciais embutidas em código. Política de exclusão de código gerado preservada. Nenhum segredo lido, criado ou exposto; nenhum arquivo de aplicação alterado. Sem testes de aplicação por ser alteração exclusiva de seleção de arquivos do Git.

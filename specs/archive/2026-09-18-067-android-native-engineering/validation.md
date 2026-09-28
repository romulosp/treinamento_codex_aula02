# Validação

**Data:** 2026-09-18  
**Diretório de execução:** `D:\desenvolvimento\ia\aula02`  
**Ambiente:** Windows, PowerShell, Python 3.14.6, `uv` 0.11.19. A validação de metadados executou Python 3.12.12 e PyYAML 6.0.3 em ambiente efêmero do `uv`.

| Evidência | Comando/cenário | Código | Resultado |
| --- | --- | ---: | --- |
| VAL-001 | `python .agents/skills/android-native-engineering/scripts/validate_android_project.py $valid` | 0 | `OK: invariantes estruturais Android atendidos`. Fixture com Wrapper, settings, build e Manifest aceito. |
| VAL-002 | `python .agents/skills/android-native-engineering/scripts/validate_android_project.py $invalid` | 1 | `ERRO: Gradle Wrapper ausente (gradlew ou gradlew.bat)`. O fixture mantinha os demais invariantes e isolou a ausência do Wrapper. |
| VAL-003 | `uv run --with pyyaml python C:\Users\RomuloPenha\.codex\skills\.system\skill-creator\scripts\quick_validate.py .agents/skills/android-native-engineering` | 0 | `Skill is valid!`. A skill também foi descoberta no catálogo de skills disponível nesta sessão do Codex. |
| VAL-004 | `python -m py_compile .agents/skills/android-native-engineering/scripts/validate_android_project.py` | 0 | Script compilado sintaticamente sem diagnóstico. |
| VAL-005 | `git ls-remote https://github.com/android/skills.git refs/heads/main` | 0 | HEAD oficial resolvido para `b1f707d90904129b5972b3cc6436b568583effe5`. |
| VAL-006 | consulta à árvore GitHub `repos/android/skills/git/trees/b1f707d90904129b5972b3cc6436b568583effe5?recursive=1` | 0 | 24 arquivos `SKILL.md` identificados; quatro candidatas diretamente relacionadas ao escopo foram analisadas em `research.md`. |
| VAL-007 | `Get-Command android -ErrorAction SilentlyContinue` | 0 | Nenhum comando localizado (`ANDROID_CLI=NOT_FOUND`); nenhuma instalação foi executada. |
| VAL-008 | `uv run --with pyyaml python C:\Users\RomuloPenha\.codex\skills\.system\skill-creator\scripts\quick_validate.py .agents/skills/android-native-engineering` | 0 | `Skill is valid!` após a inclusão da referência obrigatória de KDoc. |
| VAL-009 | verificação PowerShell dos links Markdown relativos em `SKILL.md` e `references/*.md` | 0 | `LOCAL_MARKDOWN_LINKS=OK`; cinco referências encontradas, incluindo `kdoc-guidelines.md`. |
| VAL-010 | buscas `rg` por atribuições semelhantes a segredo e por APIs Python perigosas no escopo | 1 por busca | Nenhuma correspondência encontrada; para `rg`, código `1` significa zero matches. |
| VAL-011 | `uv run --with reportlab python docs/security-audit/gerar_relatorio.py --change-067` | 0 | PDF atual gerado em `docs/security-audit/relatorio-067-android-native-engineering.pdf`. |
| VAL-012 | validação com `pypdf` e renderização de todas as páginas com PyMuPDF | 0 | Duas páginas, 2.473 caracteres extraídos, Change e resultado presentes; inspeção visual sem cortes, sobreposições ou títulos órfãos. |

## Inventário KDoc desta Change

Nenhum arquivo `.kt` de produção foi criado ou alterado nesta Change; portanto, não existem declarações Kotlin a documentar ou exclusões a justificar nesta validação. O comportamento introduzido é a política que exigirá esse inventário nas Changes Android consumidoras.

Dokka não foi executado porque esta Change não contém projeto Android/Kotlin, não possui tarefa Dokka configurada e não exige publicação de documentação. Essa limitação está de acordo com `RNF-004` e `CA-008` e não dispensa KDoc em projetos consumidores.

## Auditoria de segurança

**Relatório:** `reviews/2026-09-18-security-audit.md`  
**PDF atual:** `docs/security-audit/relatorio-067-android-native-engineering.pdf`  
**Resultado:** nenhum achado de segurança confirmado em aberto.

- Autenticação, autorização, tenant, IDOR, XSS, banco e deploy foram marcados como não aplicáveis porque essas superfícies não existem na Change.
- O validador recebe um caminho local e o usa somente em operações de leitura; não executa shell, SQL, código dinâmico, rede ou desserialização.
- Nenhum padrão de segredo foi encontrado nos artefatos atuais da Change.
- Os caminhos novos ainda não possuem histórico Git. Correspondências históricas em outros caminhos permaneceram fora do escopo e nenhum valor foi exposto.
- O `.pyc` produzido por `py_compile` foi removido antes do commit; ele é regenerável.
- Poppler (`pdfinfo`, `pdftotext`, `pdftoppm`) não está disponível no ambiente. A extração e renderização foram realizadas com `pypdf` e PyMuPDF em ambiente efêmero do `uv`.

## Preparação reproduzível dos fixtures

Comando executado em PowerShell:

```powershell
$fixtureRoot = Join-Path ([System.IO.Path]::GetTempPath()) ('android-native-engineering-' + [guid]::NewGuid().ToString('N'))
$valid = Join-Path $fixtureRoot 'valid'
$invalid = Join-Path $fixtureRoot 'invalid-sem-wrapper'
foreach ($project in @($valid, $invalid)) {
    New-Item -ItemType Directory -Force -Path (Join-Path $project 'app\src\main') | Out-Null
    New-Item -ItemType File -Force -Path `
        (Join-Path $project 'settings.gradle.kts'), `
        (Join-Path $project 'app\build.gradle.kts'), `
        (Join-Path $project 'app\src\main\AndroidManifest.xml') | Out-Null
}
New-Item -ItemType File -Force -Path (Join-Path $valid 'gradlew.bat') | Out-Null
```

Nesta execução, `$fixtureRoot` foi resolvido como `C:\Users\RomuloPenha\AppData\Local\Temp\android-native-engineering-dfc4b8cc7c69405eb9aabd20f59b5003`. Os fixtures são descartáveis; o bloco acima recria os mesmos invariantes sem depender desse caminho específico.

## Limitações

- O Android CLI não está disponível no `PATH`; nenhuma skill oficial foi instalada ou alterada.
- Não existe projeto laboratório Android nesta Change, portanto build Gradle, lint, testes Android e execução em emulador não foram alegados.
- A pesquisa completa proposta no documento externo foi reduzida ao primeiro incremento aprovado; ondas avançadas continuam futuras.
- O catálogo oficial foi consultado somente como fonte no commit registrado; nenhuma skill externa foi importada. As candidatas e decisões estão em `research.md`.
- A política de KDoc foi validada estruturalmente na skill; sua aplicação sobre declarações Kotlin deverá ser evidenciada por cada Change consumidora que altere arquivos `.kt`.
- A auditoria é estática e não substitui a auditoria contextual de uma aplicação Android consumidora.

**Resultado:** `VALIDADA` para o escopo da skill e do validador estrutural.

# Proposta: 010-reinicializar-workspace-documentado

## Status
`ARQUIVADA`

## Responsável e data

Solicitante e equipe do projeto — 2026-08-27.

## Referências

- `AGENTS.md`
- `specs/shared/process/workflow.md`
- `specs/archive/2026-08-26-005-limpar-artefatos-gerados/`

## Problema e objetivo

O workspace contém a aplicação Java gerada e artefatos de compilação após as mudanças concluídas. O objetivo é preservar somente a documentação textual versionada, registrar um guia completo para repetir o fluxo e remover a aplicação e os demais arquivos não textuais para permitir nova execução do processo a partir do zero.

## Escopo

- Criar `NotasProjeto.md` na raiz com o resumo técnico, funcional e metodológico do projeto e instruções para gerar, testar e executar um módulo de API.
- Alterar `.gitignore` para ignorar todo arquivo que não seja `.md` ou `.txt`, exceto o próprio `.gitignore`, mantendo as exclusões específicas de diretórios gerados.
- Remover do diretório de trabalho todos os arquivos versionados que não sejam `.md` ou `.txt`, exceto `.gitignore`, incluindo o módulo gerado em `apps/backend/`, scripts e diagramas binários ou de edição.
- Remover artefatos não versionados produzidos pela compilação, incluindo `apps/backend/target/`.

## Fora de escopo

- Regenerar ou executar novamente a aplicação Java nesta mudança.
- Alterar o conteúdo histórico dos documentos em `specs/archive/`.
- Reescrever o histórico Git ou remover credenciais que já constem em commits anteriores.
- Alterar as regras funcionais da API documentadas nas mudanças arquivadas.

## Impactos e riscos

- Após a limpeza não haverá módulo executável em `apps/backend/`; sua regeneração deve ocorrer por uma nova mudança Spec Driven.
- A política de ignorar arquivos não textuais impede o versionamento futuro de fontes Java, POM, propriedades, scripts e imagens. Esses itens existirão localmente somente enquanto forem gerados para validação.
- `.gitignore` é uma exceção técnica indispensável à regra, pois é o arquivo que a aplica.

## Critérios para aprovação da SPEC

- A lista de itens preservados, removidos e ignorados é explícita e verificável.
- A documentação não revela valores de variáveis sensíveis.
- A instrução de reexecução não sugere que o módulo removido ainda esteja disponível.

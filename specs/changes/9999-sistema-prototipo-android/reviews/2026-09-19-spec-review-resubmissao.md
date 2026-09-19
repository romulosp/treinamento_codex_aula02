# Revisão da SPEC — ressubmissão: 9999-sistema-prototipo-android

## Data e escopo

- Data: 2026-09-19.
- Fase avaliada: revisão da SPEC após correção de `REV-001` a `REV-004`.
- Artefatos revisados: `proposal.md`, `spec.md`, `DESIGN.md`, `tasks.md`, especificações complementares, `inventario-origem.md`, `migration-matrix.md`, `sources-and-decisions.md`, `implementation-plan.md`, `validation.md` e `source-material/README.md`.
- Regras verificadas: `AGENTS.md`, `specs/shared/process/workflow.md`, `specs/shared/process/evidence-conventions.md`, Skill `spec-review` e referências obrigatórias da Skill `android-native-engineering`.
- Fontes temporais verificadas em 2026-09-19: documentação oficial Android para API 36, Java 17 e BOM Compose estável `2026.09.00`.

## Verificação dos achados anteriores

### REV-001 — Resolvido — SDK mínimo

- Evidência: `spec.md`, `spec-fundacao-aplicativo.md`, `DESIGN.md` e `sources-and-decisions.md` fixam `minSdk = 26` e validação nos níveis de API 26 e 36.
- Resultado: o intervalo de plataforma está inequívoco e testável.

### REV-002 — Resolvido — Distribuição

- Evidência: `spec.md`, `spec-fundacao-aplicativo.md`, `DESIGN.md` e `sources-and-decisions.md` limitam a Change à distribuição interna para desenvolvimento, demonstração e validação; publicação exige nova Change.
- Resultado: o canal e suas exclusões estão definidos sem alterar `compileSdk = 36` ou `targetSdk = 36`.

### REV-003 — Resolvido — Direitos de uso

- Evidência: `spec.md`, `DESIGN.md`, `spec-tema-assets-tipografia.md`, `sources-and-decisions.md` e `migration-matrix.md` proíbem incorporar imagens e fontes legadas e exigem substituições neutras por Compose/sistema.
- Resultado: a implementação não depende de presumir licença ou autorização e há um gate verificável contra cópia de binários.

### REV-004 — Resolvido — 118 referências ausentes

- Evidência: `migration-matrix.md` contém 212 linhas, uma por definição do XML, 212 destinos únicos e decisão final em todas as linhas. A verificação automática confirmou 234 caminhos únicos de imagem, 116 presentes e 118 ausentes; definições com binário são `SUBSTITUIR` e as demais são `ADAPTAR`.
- Resultado: nenhuma variante exige decisão inventada durante a implementação.

## Matriz de verificabilidade

| Item | Evidência | Resultado |
| --- | --- | --- |
| Identidade e fundação | RF-001 e `spec-fundacao-aplicativo.md`. | Aprovado |
| Plataforma | API 26 a 36, Java 17, Compose estável e distribuição interna. | Aprovado |
| Escopo e exclusões | `proposal.md` separa protótipo visual de negócio, rede, autenticação, periféricos e publicação. | Aprovado |
| Arquitetura | Perfil `SIMPLE`, módulo único, UDF, estado imutável e layout adaptativo. | Aprovado |
| Famílias e comportamento | RF-003 e dez especificações complementares. | Aprovado |
| Recursos | 212 definições rastreadas; nenhum binário legado autorizado no APK. | Aprovado |
| Critérios de aceite | CA-001 a CA-010 e critérios específicos cobrem estrutura, UI, estados, segurança, acessibilidade e adaptabilidade. | Aprovado |
| Testabilidade | Testes unitários, Compose UI, screenshots, lint, cobertura e emuladores API 26/36 possuem critérios reproduzíveis. | Aprovado |

## Novos achados

Nenhum achado bloqueante ou importante.

## Conclusão

`SPEC_APROVADA`

O contrato corrigido está claro, consistente, implementável e testável. A implementação pode iniciar exclusivamente após o preenchimento de `implementation-plan.md`, sem copiar binários legados e sem ampliar o escopo aprovado.

# DESIGN — Correção de leitura e processamento

## Estrutura e fronteiras

Preservar microkernel, `compileOnly(:shared-api)`, plugin e Bridge. Correção no consumidor da 10004 e nos serviços/domínio/fachada da biblioteca Go. Não promover `TransactionGCX` da 074 nem adicionar endpoint Bridge. Aplicar [arquitetura compartilhada](../../shared/architecture/android-business-plugin.md).

## Alterações planejadas

| Componente | Responsabilidade |
| --- | --- |
| `scripts/iniciar-saque-cartao.ps1` do projeto Android | Adicionar `CriptografiaPath`, com origem padrão indicada pelo solicitante, validar fonte completa e provisionar arquivo privado adicional; `ValidateOnly` valida sem I/O no pinpad. Manter verificação de integridade e limpar temporários criados pelo launcher. |
| `SaqueRepository.kt` / perfil | Ler snapshot privado adicional, validar perfil DUKPT, encaminhar configuração ao core sem inserir valores em estado UI. Não manter literais de método/índice PIN em chamadas. |
| `internal/domain/command` e parser Go | Usar constantes de compra; validar configuração, AARR e PAN interno com dados sintéticos; interpretar 00/03/05/06 conforme constantes existentes. |
| `internal/application/service` | Guardar contexto de captura privado por serviço/sessão e operationID; coordenar estado de GCX/GTK/GPN; emitir diagnóstico seguro de comando/status e eliminar contexto em todas as saídas. |
| `mobile/journey.go` / fachada aditiva | Oferecer operação de PIN pós-GTK que recebe operationID, método, índice, WK validada e mensagem, sem argumento PAN e sem retorno sensível. A ABI genérica existente mantém o significado de PAN vazio e não é alterada silenciosamente. |
| `SaqueJourney.kt` | Usar modo tipado; GCX→GTK→GOX/GPN. Manter decisão EMV e fluxo de confirmação; separar erro principal e cleanup. |
| AAR/proveniência | Recompilar a fachada, distribuir artefato correspondente ao fonte e conferir APIs/ABI; manter Bridge opaco. |

Os nomes definitivos da nova operação devem ser registrados no `implementation-plan.md` antes de alterar código. Sua assinatura pública não recebe PAN, suas pré-condições são sessão atual/mesma correlação/GTK concluído/modo sem EMV, seu consumo é único e seu resultado é apenas sucesso ou falha sanitizada. Método diferente de 3, WK não vazia ou índice inválido falham antes de enviar GPN.

## Fluxo

```text
Preflight (perfil + tabela + três campos criptográficos)
  → abertura/capacidades/carga EMV confirmadas
  → tela 1 (débito/crédito) → resumo congelado → DEX/GKY
  → GCX compra, três interfaces e candidatos da modalidade
      → fallback normativo, mantendo snapshot
      → 00/05: preservar PAN mínimo privado no Go
  → GTK criptografado
      → 03/06: GOX → FCX somente quando GOX=2
      → 00/05: GPN DUKPT pós-GTK com PAN explícito interno
  → fechamento confirmado / contexto descartado / resultado de homologação
```

## Estado da sessão

Representar captura válida, GTK consumido, continuação consumida e encerramento. Uma transição inválida não envia comando. Novo GCX invalida o contexto anterior antes de capturar; erro/cancelamento/timeout/desconexão invalidam o contexto. Operação não pode ler contexto de outra correlação. A fila do dispositivo continua única; proteger contexto também contra concorrência entre APIs da fachada.

## Criptografia e segurança

Importar as três propriedades sem valores em stdout. Um arquivo privado separado evita misturar WK com tarifas/tabela. Não usar serialização automática que publique o contexto sensível nem exceções que incluam fonte externa. Como WK atual é vazia, não há necessidade de chave de trabalho no Kotlin; qualquer valor não vazio bloqueia o perfil DUKPT antes de hardware. Validar no Go as mesmas pré-condições do parser/provisionamento.

A decisão de PAN temporário está em [ADR-001](adr/ADR-001-contexto-pan-no-core.md). Não decryptar trilhas, manter cache em disco, usar singleton global ou recorrer a PAN do operador. Retenção mínima apenas até GPN/erro/encerramento.

## Compatibilidade e investigação física

Não alterar comportamento de consumidores genéricos sem teste de regressão. A jornada da 10004 passa a compra e corrige o significado de 05/06; contratos antigos de GPN antes de GTK são substituídos nesta jornada. Recompilar o AAR antes dos testes Android; registrar commit/build/hash dos artefatos, não conteúdo criptográfico.

O log histórico não permite atribuir `PINPAD_ERROR` a GOX, GTK, GPN ou fechamento. Adicionar metadados sanitizados na fronteira de serviço Go, sem abrir frames redigidos. A confirmação física de causa e encerramento permanece gate de validação, não pré-condição para escrever a SPEC.

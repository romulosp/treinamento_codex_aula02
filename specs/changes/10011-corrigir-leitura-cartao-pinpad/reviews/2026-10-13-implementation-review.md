# Revisão de implementação e arquitetura — 10011

Data: 2026-10-13. Entrada: `IMPLEMENTADA`, proposal/spec `SPEC_APROVADA` pela
REV-001. Revisão solicitada pelo usuário após implementação. Skills:
implementation-review, modular-design-principles e base Android/referências
de qualidade/KDoc. Revisão local executada pelo agente; não é auditoria
independente HIGH_ASSURANCE nem aprovação final da mudança.

## Matriz de aderência

| Requisito / aceite | Implementação e evidência examinadas | Resultado da revisão |
| --- | --- | --- |
| RF-001 / AC-01 | Builders compra 00h, filtro 01/02, valor N12; summary congelado; testes de payload/fallback e binding através do Bridge. | Aderente. Não envia AIDLIST na jornada. |
| RF-002 / AC-02/03 | Candidatos integrais/AARR, validação da seleção retornada; enum Kotlin 00/03/05/06; fixtures base/estendido; tabela DES externa aceita com 36 registros. | Código aderente; demonstração física dos AIDs pendente. |
| RF-003 / AC-04 | `purchaseGCX`, contagem consecutiva 081, limite de repetição, status de desabilitação CTLS e DEX em 086; testes dos status preservam snapshot. | Aderente; sem retry irrestrito ou troca de modalidade. |
| RF-004 / AC-05 | Parser Go/PowerShell integral, UTF-8/BOM/limite, WK vazia, índice sintético 17 e DATA separado; preflight Android antes de Open. | Aderente. Fonte PIN real validada estaticamente sem imprimir valores. |
| RF-005 / AC-03/06 | Contexto por serviço/correlação/geração, PAN de GCX, single use; GTK obrigatório; GOX 03/06 e GPN 00/05; cancel/erro/reset/close/transporte; APIs aditivas. | Aderente nos caminhos revisados e testes. Segurança de runtime tem o limite declarado no ADR. |
| RF-006 / AC-07/08 | GOX 0/1/2, FCX só 2 na jornada tipada, GPN como CAPTURED; comando/status sanitizados; close sem Cancel indevido; perda de transporte não confirma close. | Aderente. Principal/cleanup separados no estado e mensagem. |
| RNF / AC-08/09 | Dependências/topologia existentes, dispatchers/lifecycle, KDoc, sanitização, hashes/API/ABI, Go/JVM/lint/build e instrumentados. | Gates técnicos disponíveis passaram. Race, auditoria independente e host/pinpad físico pendentes. |

## Arquitetura e escopo

Ver [inventário da arquitetura implementada](../evidence/arquitetura-implementada.md).
Host/SharedApi permanecem contratos de carga; plugin não depende de host ou
outro plugin concreto. Android orquestra estado permitido, adapter usa ABI
gomobile, core detém protocolo/contexto e Bridge transporta dados opacos.
Não houve upgrade de dependências, mudança de layout, instalação de chave ou
ampliação do protocolo Bridge. Foram revisados os arquivos produtivos alterados,
novas APIs, testes, launcher e empacotamento no host.

O [inventário externo](../evidence/configuracao-externa-des.md) separa PIN,
perfil, EMV DES e credenciais gerais. Incompatibilidades de nomes nos geradores
de backend são preexistentes e não são corrigidas nesta Change de cartão.

## Registros e gates restantes

| ID | Severidade / natureza | Evidência e impacto | Ação / estado |
| --- | --- | --- | --- |
| IMP-REV-001 | Alta, gate de validação | AC-02/09 e arquitetura exigem host/login/menu, seleção física, PIN e encerramento/ownership. Mocks e os 15 instrumentados standalone não demonstram esses fatos. | Aberto: homologação DES no host/pinpad, conforme validation.md. Impede aprovação final; não identifica divergência adicional de código. |
| IMP-REV-002 | Média, ambiente/segurança | `go test -race` em Go amd64/CGO falhou pela ausência de gcc; auditoria independente de PIN permanece pendente. | Aberto: executar em ambiente suportado e obter revisão de segurança. Não declarar race/auditoria aprovados. |
| IMP-REV-003 | Informativa, configuração externa fora do escopo | Fonte geral tem bancos/OIDC nomeados por serviço, enquanto dois geradores exigem outros nomes. Não afeta as três fontes de pagamento validadas. | Documentado no inventário; abrir Change própria se solicitado. Não gerar BAT nem alterar backend nesta revisão. |

Não foram identificadas divergências adicionais de código que exijam retorno
à implementação. Os registros abertos são limites de validação/ambiente e uma
observação externa de escopo. Nenhuma alteração produtiva ocorreu durante esta
revisão; foram acrescentados documentação e verificadores de evidência.

## Decisão

`IMPLEMENTACAO_APROVADA` para entrada na etapa de validação. Esta decisão
aprova a aderência do código revisado à SPEC, não a liberação em hardware.
Validação integral e aprovação final permanecem sem aceite até resolução dos
gates materiais IMP-REV-001/002. Sem atualização de system, commit ou archive.

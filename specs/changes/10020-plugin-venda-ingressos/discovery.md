# Discovery inicial

## Evidências
- A [SPEC 10004](../10004-plugin-saque-cartao/spec.md) descreve a jornada de cartão de homologação. A [10019](../10019-saque-cartao-componente/spec.md) estabelece sua mediação pelo host e informa que os resultados são locais, sem autorização financeira.
- `IComponentHostContext.launchCardWithdrawal(onResult)` não recebe dados do pedido; `CardWithdrawalResult` devolve apenas status e confirmação de cleanup.
- `BusinessPluginManager` descobre APKs verificados, lê serviço e manifesto e constrói a folha a partir do caminho declarado pelo plugin.

## Classificação inicial
| Assunto | Classificação | Registro |
| --- | --- | --- |
| Cinema e assentos | CONFIRMADO | Primeiro cenário escolhido pelo usuário. |
| Pagamento e ingresso | CONFIRMADO | Simulação explícita, ingresso sem validade real. |
| Caminho de menu | CONFIRMADO | Declarado pelo plugin e organizado pelo host. |
| Shows | EM ABERTO | Setores, lotes, apresentações e validação de acesso são candidatos para incremento posterior. |
| Núcleo comum | HIPÓTESE | Pedido, preço e emissão podem compartilhar conceitos entre cinema e shows. |
| Estoque e persistência | EM ABERTO | Localização, concorrência, duração da reserva e recuperação ainda precisam de decisão. |
| Autorização, cancelamento e reembolso reais | EM ABERTO | Dependem de contrato financeiro real e política comercial, fora da simulação. |
| Perfis e permissões | EM ABERTO | Rótulo visual de perfil não representa autorização. |
| Auditoria, relatórios, privacidade e regras legais | EM ABERTO | Contexto, retenção e regras aplicáveis precisam ser descobertos. |

## Experimentos candidatos
Validar fluxo de sessão e assentos com dados fictícios; testar escolha concorrente conforme o modelo de armazenamento decidido; comprovar carga dinâmica e caminho de menu no host; exercitar somente dados sintéticos e estados suportados do componente de cartão em Change própria. Registrar evidências e limitações, sem operar hardware automaticamente.

## Skills
Reutilizar `android-native-engineering` e suas sub-skills conforme gatilhos, além das skills do ciclo de SPEC, implementação, revisão e validação. `domain-analysis` e ferramentas de segurança são candidatas segundo a necessidade; nenhuma importação externa foi aprovada ou executada.

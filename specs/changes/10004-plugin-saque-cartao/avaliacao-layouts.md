# Avaliação das sugestões de layout — 10004

**Data:** 2026-10-10. Avaliação dos arquivos SVG e de sua estrutura/textos;
não representa teste de usabilidade nem inspeção da UI Android executada.

## Decisão visual

Atualização solicitada em 2026-10-10: a série Gemini torna-se referência
visual principal das cinco etapas por [REV-006](reviews/2026-10-10-spec-review-layout.md).
A comparação abaixo permanece como histórico; as restrições funcionais,
segurança, dados reais e adaptação de janela continuam aplicáveis.

Combinar o indicador de cinco etapas, cartões e destaque do total do Gemini
com as instruções explícitas de chip/tarja/aproximação da versão original.
Preservar o cabeçalho real do host e os tokens compatíveis com a aplicação.
As formas serão implementadas em Compose, sem embutir telas SVG de 1280 × 800.

| Aspecto | Original | Gemini | Escolha e consequência |
| --- | --- | --- | --- |
| Orientação da jornada | Título por tela; sem visão das etapas. | Indicador numérico, mas várias telas só rotulam a etapa atual. | Cinco etapas sempre identificáveis, estado atual textual; compacto mostra também "Etapa N de 5". |
| Modalidade | Seleção azul forte; formulário e resumo separados. | Controle segmentado; taxa explícita e total em superfície destacada. | Seleção com semântica/estado claro e total destacado. |
| Resumo | Base 100, tarifa 5, total 105. | Base 150, tarifa 7,50, total 157,50. | Calcular e congelar um único resumo; valores dos SVGs são exemplos. |
| Ação principal | Botão arredondado central. | Botão sólido azul-escuro, sem ícones. | Ação sólida sem ícones; alvos e foco nativos. |
| Confirmação | Resumo e instrução de CANCELA/CONFIRMA. | Painel com estado de aguardo. | Instrução para o cliente, resumo consistente e aguardo condicionado à operação real. |
| Leitura | Três representações e verbo específico para cada interface. | Opções visuais compactas com stepper. | Manter as três instruções, sem transformar desenhos em seleção manual do modo de entrada. |
| Senha | Instrução no pinpad. | Instrução e feedback de aguardo. | PIN exclusivamente no hardware; texto descreve solicitação real, sem campo Android. |
| Conclusão | Arquivo 5 repete conteúdo da senha. | Aprovação, recibo e botão de impressão. | Conclusão própria para cada resultado; impressão não faz parte desta revisão. |
| Cabeçalho | Marca e identificadores estáticos de operador/terminal. | Cabeçalho próprio e operador de exemplo. | Reutilizar shell do host sem duplicação nem dados de exemplo. |

## Correções verificadas nas referências

`original/5_FINALIZACAO_TRANSACAO.svg` tem o título "DIGITAÇÃO DE SENHA" e
repete a instrução de PIN, portanto não especifica finalização.

`gemini/5_FINALIZACAO_TRANSACAO.svg` mostra "Valor Autorizado: R$ 150,00",
enquanto a confirmação anterior usa total R$ 157,50. Sem esclarecer valor base
e total, isso gera inconsistência. O NSU 482910 e "Sucesso (00)" são dados do
mockup; não provam autorização ou um retorno ABECS. O indicador verde em todas
as etapas também precisa de variante para cancelamento/erro precoce.

O rótulo "1x (Sem juros)" não tem regra de juros no material da jornada.
Remover essa afirmação até existir contrato. Os complementos sugerem fallback
genérico entre interfaces; a implementação deve usar as regras do core 074,
não essa sequência presumida. A alegação de PCI DSS no texto de justificativa
não comprova conformidade da integração.

## Justificativa das cinco telas

1. **Dados:** entrada e resumo separados permitem conferir modalidade, tarifa
   e total antes de iniciar hardware; total destacado recebe o mesmo cálculo
   usado nas fases seguintes.
2. **Confirmação:** o operador orienta o cliente e vê o resumo enviado;
   indicação textual de aguardo acompanha a espera externa. Não há ação
   Android capaz de substituir a confirmação no pinpad.
3. **Leitura:** três instruções explícitas orientam o gesto; o hardware informa
   o modo efetivo. Fallback muda a instrução conforme resposta do componente.
4. **PIN/processamento:** orientar a seguir o pinpad protege a entrada de PIN
   e atende cenários sem senha. A UI não promete que captura de senha autoriza
   financeiramente o saque.
5. **Resultado:** aprovado, negado, erro, cancelado e indeterminado têm texto
   próprio, resumo consistente e retorno ao menu após cleanup. Não simular
   recibo, NSU ou impressão ausentes da integração.

## Adaptabilidade e fontes

Separação estado/renderização e preservação de entradas devem seguir o contrato
de lifecycle aprovado. A documentação Android descreve as opções para
[salvar estado Compose](https://developer.android.com/develop/ui/compose/state-saving);
isso não autoriza restaurar e reenviar uma transação física automaticamente.

Catálogo oficial de skills consultado em 2026-10-10:
[Android skills](https://developer.android.com/tools/agents/android-skills/browse).
Candidata "Make app adaptive": classificação REJECT para incorporação nesta
revisão, porque as skills locais já cobrem a UI por constraints; pacote,
commit e licença da candidata não foram analisados. Nenhuma skill externa foi
instalada ou copiada. Uso de biblioteca/versão nova não está proposto.

## Identificação

Assistente: Codex, da OpenAI, baseado em GPT-6 conforme identificação da
sessão. Data da revisão: 10/10/2026. Não há identificador exato de build/versão
do modelo disponível nesta sessão; as identificações Gemini/DeepSeek nos
anexos são atribuições do material recebido.

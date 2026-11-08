# SPEC — selecionar filme com sessão já configurada

## Status
`SPEC_APROVADA` — [revisão](reviews/2026-11-08-spec-review.md).

## Requisitos
1. O cinema demonstrativo contém catálogo local imutável; cada filme identifica uma única sessão pré-configurada. Não apresentar Configurar sessão, Cadastrar sessão, formulário ou seleção/edição de horário/sala/preço. Não acessar rede ou chave de imagens.
2. Inicialmente mostrar os filmes e pedir seleção. Tocar um filme seleciona sua sessão configurada e preenche automaticamente filme, sala, horário, preço base, assentos e resumo. Repetir a seleção do mesmo filme não reinicia o assento ou pedido. Horário, sala e preço são somente informativos. O usuário escolhe filme e um assento livre.
3. Catálogo demonstrativo inicial, com IDs estáveis por filme/sessão e o mesmo mapa de 12 lugares A1–A6/B1–B6:

| Filme | Sala | Horário fixo | Preço base | Ocupados configurados |
| --- | --- | --- | --- | --- |
| Batman | SALA 04 | 19:30 | R$ 44,00 | A4, A5, B4, B5 |
| Avatar 3 | SALA 02 | 17:15 | R$ 48,00 | A2, B2, B3 |
| Coringa 2 | SALA 03 | 20:00 | R$ 36,00 | A6, B1, B6 |
| Spider-Man | SALA 01 | 14:30 | R$ 32,00 | A3, A4, B5 |

Dados são fictícios de configuração do “Cinema demonstração”, não programação comercial real. Não acrescentar horários alternativos ou inventar classificação/tecnologia.
4. Configuração inválida é rejeitada na construção da jornada: catálogo vazio, identificadores vazios/repetidos, título/sala/horário vazios, preço fora de 1..99999 centavos ou assento ocupado desconhecido. Selecionar ID inexistente não altera sessão, seleção ou pedido e informa indisponibilidade.
5. Disponibilidade por sessão é ocupação configurada mais vendas demonstrativas emitidas nessa sessão. Não permitir selecionar ocupado/vendido. Trocar filme sem pedido limpa seleção temporária e carrega disponibilidade própria; voltar ao filme preserva suas vendas. Assentos de sessões diferentes não compartilham vendas. A consulta de ingressos mostra somente ingressos emitidos localmente, nunca ocupados pré-configurados.
6. Pedido pendente ou pagamento em progresso bloqueiam troca de filme na UI e na jornada. Não alterar filme/sessão, preço congelado, assento ou ID do pedido. Cancelar libera troca sem produzir venda. Emissão após APPROVED local e cleanup preserva venda na sessão e libera novo atendimento. Nenhuma escolha dispara pagamento.
7. Manter contrato da 10021: CardPaymentRequest envia ID imutável e centavos exatos do preço base configurado; tarifa/total permanecem no componente existente. Preservar política de nova tentativa, cleanup, estados locais, prevenção de duplicação e avisos demonstrativos. Não acionar hardware automaticamente.
8. Preservar composição visual 10022 r4 e recursos carregados pelo classloader do plugin. O carrossel é ligado ao catálogo, não a um rascunho. Horário/sala/preço aparecem como texto de conferência sem ações de escolha. Layout compacto/amplo, foco nativo, rolagem e semântica dos assentos permanecem acessíveis.

## Arquitetura e baseline
Módulo `:plugin-ingressos`, package `br.com.romulopenha.sistemaprototipoandroid.pluginingressos`, perfil SIMPLE, Kotlin/Compose, Java 17 como alvo, minSdk 29/targetSdk 36/compileSdk 37 existentes. Sem novas versões ou dependências. Catálogo em objeto local imutável injetável no construtor de CinemaJourney; nenhum trabalho bloqueante. CinemaJourney continua owner da seleção/pedido e estoque volátil por sessão. CinemaRoute encaminha seleção de filme e pagamento; CinemaScreen recebe estado/catálogo e callbacks. Não alterar host de produção, shared-api, componente financeiro ou Go/Bridge.

Reutilizar skills locais Android já adotadas na 10022: componentes, estado/efeitos, foco, testes e redação. Consulta de catálogo oficial e decisão de não importar skills externas herdadas da 10022; nenhuma atualização de stack nesta Change.

## Critérios de aceite
- CA-01: clicar Batman carrega SALA 04, 19:30, R$44,00 e seus ocupados, sem diálogo. Clicar Avatar 3 carrega sua configuração distinta. Nenhum horário editável/selecionável.
- CA-02: ocupado bloqueado, seleção limpa ao trocar filme, vendas preservadas ao voltar e isoladas entre sessões.
- CA-03: pedido impede troca e mantém ID, assento e preço exatos; cancelamento não vende; emissão continua condicionada a APPROVED/cleanup.
- CA-04: integração no processo host carrega imagens e chama somente componente sintético com 4400 centavos ao selecionar Batman, A1 e pagamento explicitamente.
- CA-05: testes JVM/UI/host, cobertura elegível mínima 80%, lint, APK/test APK, validador estrutural e capturas ampla/compacta/fonte ampliada com evidências de comandos, ambiente e saídas; registrar falhas preexistentes sem declarar gate aprovado.
- CA-06: host final instalado e aberto para teste humano sem login/cartão/PIN automáticos. Não arquivar/commitar antes dos gates formais.

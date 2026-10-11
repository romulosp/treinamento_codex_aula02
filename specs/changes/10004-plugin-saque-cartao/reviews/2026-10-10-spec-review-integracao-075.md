# Integração física no padrão 075 — REV-010

Data: 2026-10-10. Decisão: `SPEC_APROVADA`, por solicitação explícita do
solicitante após teste com Bridge e pinpad físico. Complementa
REV-005/006/007/008/009.

## 1. Origem

No teste humano, a primeira tela funcionou e a etapa 2 falhou com "erro de
integração" sem indicar a causa. A verificação do ambiente mostrou Bridge ativo
em `127.0.0.1:39100` e **nenhum arquivo provisionado** em `files/saque-cartao`.

Causa apurada: a REV-008 liberou AVANÇAR com perfil DES sintético sem tabela
EMV; com o Bridge ativo, `NewClient → Ping → Open` e `GIX` têm sucesso e a
jornada falha em seguida na carga da tabela (`emv.json` ausente), apresentando
apenas "Erro na homologação". O encadeamento já chamava a stack 074; o que
faltava era configuração provisionada e diagnóstico visível.

## 2. Decisões

1. **Perfil e tabela obrigatórios**: AVANÇAR volta a exigir perfil válido e
   tabela EMV carregada. O perfil DES sintético da REV-008 é removido, assim
   como o campo `defaultProfileInUse`. O formulário continua editável e
   calculando sem configuração; o início físico não.
2. **Diagnóstico no padrão 075**: as falhas passam a expor código e fase
   sanitizados (`Mobile.errorCode`/`errorPhase`), com a mesma whitelist já
   existente no core (`BRIDGE_UNREACHABLE`, `TIMEOUT`, `PROTOCOL_ERROR`,
   `BUSY`, `PINPAD_ERROR`, `CANCELED`, `DISCONNECTED`, `PINPAD_CLOSED`,
   `BINDING_ERROR` e fases reconhecidas). Nenhum texto livre, caminho,
   parâmetro ou conteúdo de cartão é exibido.
3. **Códigos locais de provisionamento**: a leitura privada distingue
   `MISSING` (perfil ausente), `TABLE_MISSING` (tabela ausente) e
   `CONFIGURATION` (conteúdo inválido), com mensagem correspondente na tela.
4. **Confirmação no pinpad por DSP + GKY**, conforme seção 3.3.5 e 3.3.10 do
   manual ABECS 2.12: `DSP032` com duas linhas S16 e `GKY` aguardando tecla
   (OK = confirmação, CANCELA = cancelamento). Quatro páginas exibem o que o
   solicitante pediu: valor total da compra, quantidade de vezes (1..12), valor
   total da tarifa e a pergunta de confirmação/cancelamento. Cada página exige
   uma tecla; valores que não couberem em 16 colunas falham sem truncamento
   silencioso. O caminho DEX deixa de ser usado nesta etapa.
5. **Paridade das demais chamadas**: GCX filtrado pela modalidade escolhida,
   com pesquisa de AID base/estendido, modo de entrada e fallback preservados no
   core; GTK resumido em todas as operações; GPN DUKPT método 3/índice 8 com PAN
   vazio somente na tarja; GOX no ICC/CTLS EMV; FCX com os cenários
   APPROVE/DENY/COMMUNICATION_ERROR. Não há novo comando, endpoint ou regra
   ABECS em Kotlin.

## 3. Aceite

Testes de jornada provam: bloqueio do início sem perfil/tabela; quatro páginas
DSP com quatro GKY; cancelamento na primeira tecla cancelando a jornada; código
e fase propagados até o estado; e ausência de perfil sintético. Os testes de
UI provam AVANÇAR desabilitado sem configuração e exibição do código da falha.
Não valida hardware, cartão, PIN ou autorização financeira.

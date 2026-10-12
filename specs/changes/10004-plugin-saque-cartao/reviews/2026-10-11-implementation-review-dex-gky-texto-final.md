# IMP-REV-019 — Texto final DEX e decisão GKY

Data: 2026-10-11. Resultado: `IMPLEMENTACAO_APROVADA` no escopo técnico;
sem aprovação final da Change ou comprovação física do pinpad.

SaqueJourney preserva DEX → GKY único e a seleção de retorno vigente. Mensagem
agora contém total, tarifa, compra, QUANTIDADE/Nx e CANCELA/CONFIRMA em nove
linhas; somente o título pode receber espaços iniciais. Fallback sem alinhamento
quando colunas inválidas ou mensagem alinhada excede S160. Não há padding final,
CR inicial ou bloqueio pela altura. Sem mudança no adapter, tela/SVG, Go ou AAR.

27 testes JVM passaram. Novos cenários conferem mensagens literais em displays
16/40 colunas, capacidades ausentes/inválidas, 12 parcelas e valores grandes,
DEX pendente/falha, GKY único e ausência de captura para CANCELA/LIMPA/F1..F4.
Unitários Go do parser/GKY passaram. Proveniência do AAR e hashes das fontes
service/parser/fachada correspondem ao material compilado.

Lint, cobertura 243/249 linhas (97,59%) e build do host passaram. Validador
estrutural segue apontando local.properties preexistente. KDoc atualizado.
Alinhamento com espaços é opcional e não prova renderização do firmware.
Evidências de instalação/abertura ficam em validation.md; teclado físico exige
operador com pinpad e cartão de teste, separado de testes sintéticos.

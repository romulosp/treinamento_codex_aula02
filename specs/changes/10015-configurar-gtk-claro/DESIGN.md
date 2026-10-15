# DESIGN — 10015

Reusar os dois campos existentes, sem flag redundante: método vazio é escolha
explícita em claro, método30 é criptografado. Mobile omite ponteiro KeyIndex
no modo em claro; core permite apenas as duas combinações. Builder ABECS já
suporta omissão dos campos e não precisa alteração. Android e PowerShell
validam tipo string antes de aceitar vazio para evitar null/coerção.

paymentContext registra modo DATA e índice enviados junto com resultadoGTK.
Snapshot privado expõe getters de configuração não secreta. Para modo em claro,
índice operativo ausente representado por -1, não posição criptográfica enviada;
índice08 do perfil é preservado para uso futuro em30. Nenhuma nova API gomobile.
Não modificar UI/SharedApi/topologia/stack. Mantém snapshots e limite da sessão.
Ver [ADR-001](adr/001-gtk-claro-des.md).

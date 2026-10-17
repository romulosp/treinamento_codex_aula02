# Plano de migração da Change 9999

## Status

`PENDENTE_IMPLEMENTACAO`

1. Congelar os contratos visuais aprovados da tela em paisagem da Change 9999:
   cabeçalho, identificação, campos editáveis, teclado alfanumérico, teclado
   numérico e ações de confirmação/cancelamento.
2. Extrair apenas os componentes e estado da identificação para
   `:plugin-login`; tokens de tema compartilháveis passam para `:shared-api`
   somente se forem contratos de plataforma, sem transferir regra de negócio ao
   Core.
3. Substituir a invocação direta da tela no `:app` pelo pedido da capacidade
   `startup-auth` e pela `PluginScreenFactory` do plugin ativo.
4. Preservar bloqueio por falha de plugin e criar fallback técnico no host.
5. Executar regressão visual manual no Android Studio, em paisagem, contra a
   referência aprovada na Change 9999.

Não há migração automática de usuários, dados ou sessões: o protótipo atual não
possui autenticação real nem persistência de negócio dentro de seu escopo.

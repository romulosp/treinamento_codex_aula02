# Design

Modelo configurável de mapa, tipo de posição, setores e categoria no módulo atual. CinemaJourney continua proprietário do estado; CinemaScreen recebe estado e callbacks; rota mantém timer e chamada CALLER. Snapshot de pedido guarda itens e total; estoque por apresentação em memória. API compartilhada e hardware não mudam. Acessores internos anteriores são derivados do estado múltiplo, sem fonte de verdade duplicada.

Decisões REQUIRED: plano aprovado. OPTIONAL: nenhuma dependência nova. Catálogo oficial Android consultado em 2026-11-08 (https://developer.android.com/tools/agents/android-skills/browse); candidatas adaptive/testing REJECT para incorporação: sobreposição com skills locais, sem necessidade de instalar. Nenhuma skill externa copiada.

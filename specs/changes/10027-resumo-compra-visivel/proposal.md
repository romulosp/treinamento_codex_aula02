# Resumo da compra visível

Status: SPEC_APROVADA. Solicitação do usuário em 2026-11-08 autoriza melhorar a experiência do resumo deslocado pelo carrossel.

Em tablet (largura interna ≥840dp), alinhar resumo ao topo da área útil, ao lado do catálogo e do mapa. Resumo ocupa altura disponível; só detalhes/itens rolam, total e ações permanecem visíveis. Após selecionar evento, carrossel menor mantém filtros, seleção e animação. Mapa e pista têm rolagem independente do resumo. Layout compacto e fonte ampliada continuam operáveis; não alterar pedido, estoque, preços, chamada CALLER, sucesso ou timer. Manter avisos demonstrativos. Validar viewport tablet com altura limitada pelo cabeçalho do host, grande pedido e rolagem do mapa. Após teste/reinstalação final, reprovisionar os três snapshots antes de entregar, sem iniciar hardware.

Baseline: mesmo módulo/package/stack/SDK/SharedApi da 10026; nenhuma dependência nova. Gate estrutural mantém ressalva de local.properties ignorado conforme ADR-001 da 10026.

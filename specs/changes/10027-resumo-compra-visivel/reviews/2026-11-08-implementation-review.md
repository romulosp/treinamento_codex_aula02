# Revisão de implementação

Status: IMPLEMENTACAO_APROVADA para validação; implementação concluída.

- CinemaScreen agora posiciona catálogo/mapa à esquerda e resumo com altura limitada à direita desde o topo. Rolagem esquerda não envolve o resumo.
- OrderPanel mantém cabeçalho/total/ações fora da rolagem do corpo. Em layout compacto não limitado, corpo usa teto 240dp e mantém rolagem externa para as ações.
- PosterCarousel reduz largura dos cartazes para 176dp após selecionar, preservando filtro, animação, acessibilidade e todos os eventos.
- Estado da venda, preços, CardPaymentRequest, API1.5/CALLER, callbacks e sucesso/timer não foram alterados. Apenas metadados do plugin evoluem para 1.2.1/versionCode4.
- KDoc atualizado nos componentes alterados; nenhuma declaração pública nova. Sem nova dependência, credencial, acesso de rede ou protocolo de hardware.
- Novos testes comprovam resumo alto em viewport 1100×540dp, total/ação estáveis após rolagem independente de mapa e lista de 30 lugares e redução do carrossel com preservação da seleção. Regressões compactas/fonte150%, pares/pista/pagamento sintético permanecem na suíte.

Revisão técnica não representa aprovação humana final. Instalar e reprovisionar no fim é obrigatório para evitar o incidente anterior MISSING. Sem archive/commit.

## Nova revisão após validação
Rolagem inline simplificada: SeatRows usa eixo horizontal; o eixo vertical pertence à coluna esquerda. Mapa ampliado preserva viewport próprio. O helper de testes percorre ancestrais de cada eixo por semântica, porque performScrollTo isolado atua no ancestral mais próximo e ações fixas não têm ancestral rolável. Não foi alterada a regra de seleção. IMPLEMENTACAO_APROVADA após reavaliação.

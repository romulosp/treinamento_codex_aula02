# Revisão de SPEC: 030-template-ecommercer-001 — reinterpretação Vintage

## Veredito

`SPEC_APROVADA`

### REV-007 — Conversão de Shopify para demonstração React

- Severidade: informativa.
- Evidência: RF-006 separa 30 telas e 71 módulos de referência de qualquer execução de Liquid ou dependência Shopify.
- Impacto: a cobertura solicitada é verificável sem introduzir infraestrutura, credenciais, pagamento ou servidor incompatíveis com o escopo atual.
- Recomendação: cada tela de cliente e compra deve declarar seu caráter demonstrativo.

### REV-008 — Proteção contra cópia do tema

- Severidade: alta, tratada.
- Evidência: proposal e RF-006 proíbem explicitamente copiar ou adaptar Liquid, CSS, JavaScript, textos, imagens, marca e markup da referência; DESIGN.md define marca, conteúdos e composição próprios.
- Impacto: reduz o risco de plágio mesmo com a cobertura funcional inspirada pelo inventário local.
- Recomendação: usar somente nomes de módulos como mecanismo de rastreabilidade e revisar os dados locais contra `shopify://`.

### REV-009 — Ausência de mídia no pacote fornecido

- Severidade: informativa.
- Evidência: inventário de `assets/` encontrou 101 CSS e 41 JavaScript, sem PNG, JPG, WebP ou SVG.
- Impacto: não é possível reaproveitar fotografia do pacote sem usar fontes externas não incluídas.
- Recomendação: criar mídia original do projeto e registrar sua origem.

## Conclusão

O adendo possui escopo, limites, inventário, critérios de aceite e estratégia de cobertura verificáveis. A implementação React original está autorizada, mantendo os gates de validação visual antes de revisão formal.

# Revisão de SPEC: 030-template-ecommercer-001 — reapresentação visual

## Veredito

`SPEC_APROVADA`

### REV-004 — Correção da descoberta de classes do Tailwind

- Severidade: alta, corrigida na SPEC.
- Evidência: o `.gitignore` global exclui arquivos não textuais e o CSS compilado na primeira apresentação não continha utilitárias do Tailwind.
- Impacto: o shell foi renderizado como HTML sem a linguagem visual especificada.
- Recomendação: declarar `@source "./"` no CSS principal e confirmar no artefato de build que as utilitárias usadas estão presentes.

### REV-005 — Recursos visuais remotos documentados

- Severidade: informativa.
- Evidência: a nova solicitação autoriza fontes Google e imagens; a SPEC limita os recursos a duas famílias CSS com `display=swap` e imagens estáticas licenciadas do Unsplash.
- Impacto: torna a avaliação visual possível sem introduzir SDK, rastreador, segredo, backend ou dado de catálogo externo.
- Recomendação: manter fallbacks locais, alt text, dimensões proporcionais e registrar os provedores na validação técnica.

### REV-006 — Exceção de versionamento limitada ao aplicativo

- Severidade: alta, corrigida na SPEC.
- Evidência: o padrão global do `.gitignore` ignora também os arquivos TypeScript, CSS e JSON do novo app.
- Impacto: além de suprimir as utilitárias do Tailwind, a implementação não poderia ser incluída em um commit futuro.
- Recomendação: liberar somente o caminho da aplicação e reverter a permissão para dependências, build e cobertura dentro dele.

## Conclusão

O ajuste preserva o objetivo e os limites da mudança. O `HeroSection` é antecipado exclusivamente para a reapresentação visual da Fase 1; vitrine, produtos e FastCart continuam pendentes.

# Revisão da SPEC — 002-ajustar-configuracao-produto-base

## Resultado: SPEC_APROVADA

## Achados

### REV-001 (Informativo)

- **Evidência:** A solicitação original menciona 1000 para o backend Java e uma segunda porta 2000. A proposta, a SPEC e o design registram a decisão de usar 1000 no Quarkus e 2000 no Vite.
- **Impacto:** Remove a ambiguidade operacional entre backend, frontend, proxy e scripts de inicialização.
- **Recomendação:** Implementar exatamente as portas aprovadas e validar o encaminhamento de `/produtos`.

### REV-002 (Informativo)

- **Evidência:** `produtoService.js` utiliza o caminho relativo `/produtos`; a SPEC mantém esse contrato e define o proxy somente para desenvolvimento.
- **Impacto:** O frontend não fica acoplado a `localhost` quando hospedado no mesmo domínio da API.
- **Recomendação:** Não introduzir URL absoluta da API no código do navegador.

## Veredito

O objetivo, o escopo, as dependências, os riscos e os critérios de aceite são verificáveis. A decisão de portas foi consolidada em 1000 (backend) e 2000 (frontend). A SPEC está `SPEC_APROVADA`.

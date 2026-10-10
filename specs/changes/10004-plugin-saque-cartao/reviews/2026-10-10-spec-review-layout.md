# Revisão visual da SPEC — REV-006

Data: 2026-10-10. Entrada: solicitação explícita de corrigir o layout conforme
os cinco SVGs `telas_geradas_gemini`, já preservados em `source-material/gemini/`.

Decisão: `SPEC_APROVADA`. A série Gemini passa a ser a referência visual
principal das cinco etapas, substituindo a combinação visual anterior.

REV-006-01 — divergência visual alta: a tela atual usa uma coluna de textos e
campos, sem cartões, modalidade, stepper conectado ou destaque do total.
Implementar dois painéis na etapa 1; cartão central nas etapas 2–5; fundo
#F4F7F9/#E2E8F0, azul #003366, bordas #E2E8F0, cantos 16/8 dp, hierarquia
tipográfica e ilustrações de chip/tarja/aproximação conforme SVGs. Responsividade
deve preservar essas relações, com coluna e rolagem quando necessário.

REV-006-02 — sem alteração funcional: os SVGs são referências de aparência.
Valores, taxas e resultados vêm do estado validado. Manter cabeçalho do host,
textos pt-BR e homologação explícita; não copiar operador, NSU, certificação,
"sem juros", fallback ativo presumido nem criar impressão fora de escopo.
Na etapa 4, representação estática do terminal não indica quantidade de dígitos
digitados. Instrução PIN somente quando solicitada pela jornada.

Aceite visual: capturas determinísticas das cinco etapas em janela expandida,
teste de modalidade/callbacks/cancelamento/retorno, ausência de campo PIN,
e ação acessível em janela compacta com fonte ampliada. Esta revisão não
aprova implementação nem valida hardware.

Adendo aprovado: animações equivalentes ao comportamento visual pedido como
"Flutter", implementadas em Compose na plataforma existente. Spinner
indeterminado durante aguardo real; troca de etapas por fade e deslocamento
curto (~280 ms), sem disparar comandos ou retardar cancelamento.
AnimatedContent usa identidade da etapa e snapshot da própria lambda;
edição de valores não inicia transição. Respeitar escala de animação do sistema;
com animações desativadas, manter texto e indicador estático.

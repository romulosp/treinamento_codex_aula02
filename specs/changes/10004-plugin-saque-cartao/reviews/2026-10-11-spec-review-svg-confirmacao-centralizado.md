# Ajuste pontual do SVG de confirmação

**RETIFICADO:** o solicitante esclareceu que o alvo é o display físico do
pinpad, não o SVG/tela Android. O ajuste descrito abaixo foi desfeito; SVG e
inventário recuperaram o SHA-256 anterior, verificado por assertion (exit 0).
Esta revisão não autoriza alteração visual no SVG.

Solicitação explícita de 2026-10-11: centralizar somente as informações da
confirmação em `2_CONFIRMACAO_COMPRA_PINPAD.svg`.

Decisão: `SPEC_APROVADA` para este ajuste visual pontual na referência Gemini,
selecionada pela REV-006. Os seis textos do bloco «Dados Enviados ao Pinpad»
passam a usar `x="640"` e `text-anchor="middle"`, com rótulo e valor em linhas
distintas dentro do mesmo painel. Valores, estilos e demais elementos preservados.
Inventário SHA-256 atualizado. Nenhuma alteração de código Android nesta edição.

Revisão: XML e conteúdo conferidos por assertions; evidências em `validation.md`.

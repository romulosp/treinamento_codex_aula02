# IMP-REV-018 — Layout da confirmação DEX/GIX

**REVOGADA em 2026-10-11:** ajuste revertido por solicitação do usuário
após regressão no pinpad. Contrato REV-017 restaurado. Texto abaixo histórico.


Data: 2026-10-11. Resultado: `IMPLEMENTACAO_APROVADA` no escopo técnico
da REV-018; não equivale à aprovação final da Change ou validação física.

Confronto: adapter mantém dimensões textuais e gráficas GIX; formatação usa
grade textual, compacta pares quando cabem, centraliza sem espaços finais,
limita altura/largura e Latin-1 a 160 bytes antes do DEX. GKY único e cleanup
mantidos. Não há alteração de UI/SVG, Go, Bridge ou AAR nesta revisão.

Testes usam vetores literais, sem repetir a implementação; verificam dimensões
inválidas, conteúdo que não cabe e orçamento DEX excedido sem display/tecla/captura.
KDoc do contrato GIX e do formatador atualizado. Unitários, lint, cobertura e
build aprovados; validador estrutural aponta local.properties preexistente.

Limite: a grade GIX descreve modo texto, não uma fonte configurável do DEX.
O alinhamento real precisa de conferência no PPP100; sem evidencia física nesta
execução. Nenhum valor gráfico foi usado para inventar métrica de fonte.

# REV-018 — Layout DEX conforme capacidades GIX

**REVOGADA em 2026-10-11:** ajuste revertido por solicitação do usuário
após regressão no pinpad. Contrato REV-017 restaurado. Texto abaixo histórico.


Status: `SPEC_APROVADA`, pela solicitação explícita de centralizar no display
físico Perto PPP100 usando DEX e dimensões obtidas por GIX, em 2026-10-11.

Escopo: apenas mensagem da confirmação no pinpad, adapter de capacidades e
testes correspondentes. Preservar tela Android, SVGs, Bridge, AAR e GKY único.

GIX: `PP_DSPTXTSZ` (8020h, LLCC) fornece linhas/colunas para texto;
`PP_DSPGRSZ` (8021h, LLLLCCCC) fornece altura/largura em pixels para imagens.
Fonte: manual local ABECS 2.12, página 40. DEX suporta CR e até 160 bytes,
sem alinhamento ou fonte configuráveis nesta versão (página 70).
Não converter pixels em caracteres sem métrica de fonte comprovada.

Formatação: para cada campo, usar rótulo e valor na mesma linha quando couberem
nas colunas GIX; caso contrário, usar duas linhas. CANCELA/CONFIRMA permanece
última linha. Centralizar horizontalmente com espaços somente à esquerda,
evitando espaços finais que consomem bytes e podem provocar quebra automática.
Centralizar o bloco verticalmente com CR iniciais conforme linhas GIX.
Validar linhas, colunas e tamanho final Latin-1 antes de enviar DEX.
Dimensões ausentes/inválidas, campo que não cabe, altura insuficiente ou mensagem
acima de 160 bytes resultam em CONFIRMATION_ERROR, sem DEX/GKY/captura.
Não truncar valores nem reduzir a largura real para caber no orçamento.

Dimensões gráficas são preservadas no adapter, sem uso para dimensionar texto.
Teste físico continua obrigatório para confirmar a renderização do firmware.

# Proposta — 072 Corrigir abertura, estado e log Android/Bridge

Autor: Rômulo Penha

## Status

`SPEC_APROVADA`

Decisão técnica registrada em
[reviews/2026-09-27-spec-review.md](reviews/2026-09-27-spec-review.md).
Revisão da porta por ambiente:
[reviews/2026-09-28-spec-review-porta-ambiente.md](reviews/2026-09-28-spec-review-porta-ambiente.md).

## Objetivo

Corrigir o cenário relatado: o diagnóstico Android falha ao abrir o pinpad,
exibe `OPEN` após consultar um cliente fechado e o arquivo Windows
`LogPinpadAbecs.txt` contém somente registros de ativação. O resultado esperado
é uma abertura comprovada ou um erro identificável, com estado coerente e
rastro persistido no destino compartilhado.

## Baseline e motivação

As imagens iniciais mostravam uma porta diferente da enumeração observada em
28/09/2026. A variável de usuário `PORTA_PINPAD` foi atualizada pelo operador.
Isso reforça que o valor lido do ambiente no início de cada execução, e não uma
COM fixada na SPEC, no launcher ou no teste físico, é a fonte de verdade.

A inspeção confirmou sobrescrita da variável no BAT, atribuição incondicional
de `OPEN` no ViewModel, erro renderizado abaixo do catálogo e erros de sessão
descartados pelo Bridge. A ausência de listener e reverse em consultas
posteriores é uma observação temporal, não a causa original comprovada.
Veja [diagnostico.md](diagnostico.md).

## Escopo

- preservar `PORTA_PINPAD` herdada pelo processo Windows;
- tornar a inicialização e o encerramento do BAT/Bridge verificáveis;
- preparar e verificar o reverse quando houver emulador selecionável;
- registrar eventos técnicos e rastro serial no mesmo arquivo existente;
- propagar erros controlados, correlacionados e visíveis na área de estado;
- separar estado da operação, conectividade do Bridge e sessão ABECS;
- corrigir liberação/ownership e notificação de falhas de sessão necessárias
  ao ciclo de abrir, fechar e reabrir;
- documentar contratos com GoDoc/KDoc e validar regressão sem e com hardware.

## Fora do escopo

Novos comandos ABECS, alteração de CRC ou payload, transação completa reservada,
acesso remoto/LAN, USB Android direto, troca de toolchain, atualização geral de
dependências, aplicação de pagamento e mudança de política de dados sensíveis.
Não se presume que a COM enumerada está livre. Não haverá fallback para outra
COM, transporte scripted ou outro host durante uma operação física.

## Relação com changes anteriores

Esta change complementa 067–070 e substitui o contrato de sobrescrita da porta
do BAT da 071. Para os comportamentos corrigidos, o contrato normativo é
[spec.md](spec.md). As evidências antigas permanecem históricas; o encerramento
das entregas afetadas exige evidência da correção, sem reescrever resultados
anteriores como sucesso físico.

## Aceite e dependências

Os critérios CA-072-01 a CA-072-15 estão em `spec.md`, com procedimentos e
gates em `validation.md`. Dependências: Windows, Go do módulo, ADB/emulador
para os cenários Android, AAR e APK reproduzíveis e pinpad físico para o gate
final. Testes scripted não substituem o gate da porta física efetiva.

Não há aprovação de implementação, validação física ou encerramento implícita
na revisão desta proposta.

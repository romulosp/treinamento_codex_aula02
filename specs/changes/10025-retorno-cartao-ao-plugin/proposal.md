# Proposta — resultado do cartão apresentado pelo plugin chamador

## Status
`SPEC_APROVADA` — [revisão](reviews/2026-11-08-spec-review.md).

## Objetivo e autorização
O componente parou em Captura de homologação concluída aguardando VOLTAR, impedindo a tela de sucesso da bilheteria. O usuário quer personalizar o retorno para ingressos e confirmou: CAPTURED deve concluir a venda demonstrativa e mostrar sucesso, mantendo aviso de simulação. Não representa aprovação financeira.

## Escopo
Opção aditiva de apresentação do resultado na API compartilhada 1.5. Ingressos solicita retorno automático; componente devolve APPROVED ou CAPTURED após cleanup confirmado, sem VOLTAR. Host restaura consumidor e entrega o status original; bilheteria conclui demonstração, mostra sua tela de sucesso e reinicia conforme 10024. Caminhos antigos mantêm resultado/tela e retorno manual.

## Fora de escopo
Autorizador, transformação de CAPTURED em APPROVED, alteração Go/AAR/Bridge, chaves/perfis, captura física automática, mudanças de taxa/valor, impressão ou persistência. Sem identidade de plugin hardcoded no componente e sem dependência entre plugins concretos.

# Proposta — 10013: retorno PIN criptografado e log DES privado

Status: `SPEC_APROVADA` pela [REV-001](reviews/2026-10-14-spec-review.md).
Pedido explícito de 2026-10-14: devolver PIN block e
KSN à integração privada Go/Android e registrá-los no log da aplicação. Premissa
reforçada: TIPO_CHAVE/INDICE_CHAVE/WORKING_KEY sempre do arquivo, nunca fixos.

A fachada tipada da 10011 descarta os bytes. Adicionar retorno proprietário
privado e log de homologação autorizado, preservando APIs antigas, roteamento
EMV, controle do kernel e fonte integral. Retirar literal WK do adapter.

Sem autorizador financeiro, chave em claro, PAN em logs, instalação de chave,
troca automática de slot/método, PRD, UI nova ou upgrade. O incidente GTK=011
antecede GOX/GPN; retorno não resolve chave ausente no equipamento.

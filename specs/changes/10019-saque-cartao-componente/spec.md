# SPEC: 10019-saque-cartao-componente

## Status
`SPEC_APROVADA` — revisão técnica r3 em reviews/2026-10-30-spec-review-r3.md.

## Contexto
O APK atual implementa `IPluginNegocioApp`, publica `business-menu` e retorna ao menu global. O host descobre plugins de negócio no respectivo gerenciador. A [10004](../10004-plugin-saque-cartao/spec.md) e as Changes 10011 a 10016 documentam a jornada e a integração com o pinpad; correções posteriores prevalecem sobre trechos históricos.

## Requisitos
- O saque deve ser um APK de componente, sem folha própria no menu nem Activity launcher. Descoberta e carga devem preservar assinatura, integridade, manifesto, versão da API e validação de bibliotecas nativas.
- Um plugin de negócio solicita ao host a abertura por contrato compartilhado, sem depender da implementação do componente. O host hospeda a View e devolve o controle ao chamador.
- Preservar telas, valores, mensagens, sequência e integração atuais, substituindo o retorno ao menu pelo retorno ao chamador. Discovery e criação de View não iniciam hardware; a preparação inicia no attach.
- Comunicar resultado local distinguindo `APPROVED`, `DENIED`, `CAPTURED`, `CANCELED`, `ERROR` e `UNKNOWN`. Nenhum deles representa autorização financeira ou autoriza entrega de dinheiro. Não transmitir PAN, trilhas, PIN, blocos criptográficos, KSN, credenciais ou dados EMV.
- Informar falha de descoberta ou indisponibilidade sem sucesso falso. Falha de encerramento após captura continua produzindo resultado indeterminado.
- Cancelamento, saída, detach e perda do chamador preservam cleanup cooperativo, impedem callbacks tardios e não reproduzem a transação. Uma invocação produz no máximo um retorno terminal.
- Preservar compatibilidade dos plugins existentes; versionar a API e exigir versão adequada para o novo contrato. Não modificar protocolo, dependências ou perfil criptográfico do pinpad.

## Critérios de aceite
1. Consumidor demonstrativo chama o componente carregado pelo host, exibe a jornada e recebe resultado após conclusão ou cancelamento.
2. O saque não aparece como item independente no menu, inclusive com APK antigo previamente verificado; demais plugins de negócio continuam carregando.
3. Componente ausente, inválido ou falho não causa sucesso falso; remoção da View ou logout invalida callbacks e limpa recursos.
4. Testes e emulador cobrem descoberta, isolamento, retorno, erros e lifecycle; homologação física é registrada separadamente, sem compra automática.
5. Revisão de segurança, KDoc, acessibilidade, cobertura e demais gates aplicáveis são registrados em `validation.md`.

## Segurança
Perfil Android HIGH_ASSURANCE devido à integração de pagamento e dados sensíveis. Evidências e execução física obedecem à [memória operacional 10011](../10011-corrigir-leitura-cartao-pinpad/AGENTS-MEMORY.md).

## Contrato complementar
Aplicam-se as assinaturas, transições, compatibilidade e migração definidas em DESIGN.md e ADR-001. O consumidor de teste é o módulo existente `:plugin-negocio`. A indisponibilidade retorna UNAVAILABLE; concorrência retorna BUSY. O resultado só é entregue ao consumidor ainda ativo, após cleanup. Falha de cleanup impede nova sessão até reinício. A autenticação 1.2 permanece elegível no host 1.3 sem alteração do evento.

## Aceite técnico complementar
6. SAIR e Voltar retornam uma vez, após cleanup, à mesma instância de consumidor; detach/logout suprimem o callback.
7. Chamadas concorrentes retornam BUSY e não abrem hardware; componente ausente ou inválido retorna UNAVAILABLE.
8. Testes sintéticos exercitam encerramento confirmado e falho sem comandos ao pinpad real; teste humano usa host, login, Operações e Abrir pagamento com cartão.


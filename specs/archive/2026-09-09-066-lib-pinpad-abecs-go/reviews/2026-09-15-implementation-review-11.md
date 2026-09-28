# Revisão de implementação — reconexão automática e evidência visual

**Data:** 2026-09-15  
**SPEC:** `spec.md`, `spec-infra-serial-cancel.md`, `spec-command-dsi.md`  
**Resultado:** `IMPLEMENTACAO_APROVADA`

A implementação em `internal/application/service/service.go` atende ao aditivo aprovado: após três CAN sem EOT, o serviço executa uma única reconexão controlada (Close/Open, CAN/EOT e OPN), sem reenviar o comando cujo resultado ficou indeterminado. O caminho de recuperação usa contexto independente do cancelamento do consumidor, marca a instância como dessincronizada durante a operação e bloqueia novos comandos até o resultado da reconexão. Falhas de cada etapa são preservadas e impedem falsa conclusão.

Os testes cobrem timeout MLE/GIX, ausência de reenvio, progresso somente após MLE000, fallback do Reset, falhas parciais de Close/Open/CAN/OPN, tentativa única e proteção da fila. O CLI da opção DSI informa que DSI000 confirma apenas aceitação do comando e exige confirmação visual no dispositivo.

A revisão não encontrou divergência de implementação em relação às SPECs aprovadas. A confirmação em COM7 da reconexão automática e a evidência visual dos pixels continuam sendo requisitos de validação física, registrados separadamente em `validation.md`.


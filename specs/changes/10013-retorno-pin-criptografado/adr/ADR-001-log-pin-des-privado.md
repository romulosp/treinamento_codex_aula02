# ADR-001 — Log criptográfico DES autorizado

Decisão em 2026-10-14: usuário solicitou explicitamente PIN block+KSN na
integração privada e no log da aplicação. A 10011 descartava todos os bytes e
proibia esses valores em logs; registrar uma exceção delimitada nesta Change.

Log contém valores criptografados sensíveis, não PIN em claro nem chaves. Não é
autorização financeira e não vai ao Bridge, Logcat ou UI. Opt-in por perfil DES,
somente host debug, sandbox/noBackup e limite64KiB reduzem exposição. Quem exportar
o arquivo deve preservar seu caráter de diagnóstico privado. Buffers controlados
são sobrescritos; runtime/strings e arquivo autorizado não têm apagamento garantido.

Não enviar a autorizador inexistente nem guardar em estado global/savedState.
Chave08 ausente permanece problema de provisionamento do equipamento e não é
contornada escolhendo02/07 ou método diferente. Validação física continua gate.

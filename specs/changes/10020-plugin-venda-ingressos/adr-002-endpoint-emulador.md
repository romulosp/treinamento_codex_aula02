# ADR-002 — endpoint explícito de homologação no emulador

## Estado
Aceito na revisão r4, após diagnóstico no processo host.

## Evidência
O componente já chama NewClient → Ping → Open → GIX → carga EMV.
Sem adb reverse, localhost:39100 falha com BRIDGE_UNREACHABLE/ping no binding
real do host. O mesmo binding passa com 10.0.2.2:39100, mantendo CLOSED.
Restaurar reverse faz localhost passar, porém o encaminhamento voltou a estar
ausente no uso posterior pelo Android Studio; a causa da remoção não foi provada.

## Decisão
Aplicar explicitamente host=10.0.2.2 no perfil DES externo deste ambiente,
com backup do original, e reprovisionar o snapshot privado pelo launcher existente.
A SPEC 074 permite esse override e o diagnóstico 068/069 já suporta o endereço.
Não há fallback automático ou mudança no core, no Bridge, na COM, nos valores,
no cenário ou nos parâmetros criptográficos. O listener continua em loopback.

## Limites
10.0.2.2 é específico do Android Emulator. Outros dispositivos exigem endpoint
adequado. Ping comprova conexão com Bridge, não OPN, chaves ou autorização.
Após reprovisionar, criar nova jornada para reler o perfil. Teste físico continua
manual; não executar compra automaticamente.

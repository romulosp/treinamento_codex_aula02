# Revisão r4 — diagnóstico da conexão pelo binding real

**Decisão:** `SPEC_APROVADA`.

O usuário confirmou integração física funcional e solicitou comparação das SPECs
074, 069 e 068. A cadeia do componente contém NewClient/Ping/Open/GIX/carga;
BRIDGE_UNREACHABLE/ping precede Open. Acrescentar diagnóstico instrumentado
opt-in, executado no host Android com AAR realmente carregado, apenas
Version/NewClient/Ping/GetState/Close sem ACQUIRE ou ABECS. Resultado não
confirma operação física. Se necessário, usar override explícito 10.0.2.2
permitido pela 074, preservando fonte e parâmetros financeiros/criptográficos.
Não implementar fallback financeiro ou replay automático.

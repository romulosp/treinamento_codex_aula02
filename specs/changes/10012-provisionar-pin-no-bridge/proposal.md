# Proposta — 10012: provisionamento PIN no caminho do Bridge

Status: `SPEC_APROVADA` pela [REV-001](reviews/2026-10-14-spec-review.md).
Solicitação explícita de correção em 2026-10-14.

A 10011 exige fonte PIN privada antes de OPN, mas `testar_bridge_pinpad.bat`
prepara somente reverse. Host observado contém perfil/tabela de 11-out e nenhum
arquivo PIN; tela apresenta CRYPTO_MISSING/preflight. Listener TCP ativo não
supre configuração Android. Corrigir o provisionamento por esse entrypoint e
recuperar o host instalado, preservando validações da 10011.

Escopo: launcher PowerShell Android, helper/BAT Windows, testes de preparação,
documentação e memória. Sem alteração Go/Kotlin/AAR, fluxo EMV, UI, dependências,
instalação de chaves, credenciais gerais, PRD ou compra automática.

Dependências: 074/075, 10004 e 10011. Uso somente Windows continua possível mesmo
sem ADB/host. Não encerrar listener nem processos de terceiros.

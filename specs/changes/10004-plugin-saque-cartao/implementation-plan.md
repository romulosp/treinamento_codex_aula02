# Plano técnico

## Plano autorizado em 2026-10-10

Entrada: proposal.md e spec.md em SPEC_APROVADA por REV-005. Saída desta
execução: IMPLEMENTADA para teste humano, sem fases posteriores.

1. Go: adicionar builder GCX filtrado, serviço que reutiliza fallback e binding
   resumido; validar filtros/tipo/timeout antes de I/O. Acrescentar importação
   JSON e TLR de registros completos, validando integralmente antes de TLI.
   Testar vetores sintéticos, partição, inválidos e compatibilidade das APIs.
2. Android: separar modelos monetários/perfil, repository Go main-safe,
   proprietário de estado e tela Compose. Carregar somente arquivos privados
   provisionados no host debug. Remover completamente o campo PIN local.
3. Jornada: congelar resumo, abrir sessão e carregar tabela; confirmar páginas
   reais no display, capturar cartão com filtros, executar GPN antes de GTK na
   tarja, GTK criptografado e GOX/FCX condicionais. Cancelar/fechar explicitamente
   em detach, retorno ou falha, preservando causa principal e impedindo replay.
4. UI: stepper não interativo, cartões e total destacado, ações textuais azul
   escuro. Duas colunas quando couber; rolagem em janela/fonte ampliada. Estado
   textual, alvos 48 dp e sem dados técnicos ou de cartão nas mensagens.
5. Integração: gerar AAR com proveniência e distribuir apenas dependências
   técnicas necessárias ao loader do host. Testar ABIs/nativas; aplicar ajuste
   genérico de carregamento se necessário, sem regras de saque no host.
6. Launcher: validar perfil/toolchain/COM/endpoint/dispositivo, iniciar Bridge
   oculto e confirmar PBRG/Ping; realizar reverse e provisionamento debug antes
   de abrir host. Não carregar PRD nem chaves PIN, não encerrar processos alheios.
7. Checks: validador estrutural Android, testes JVM de dinheiro/perfil/estado/
   sequência/cancelamento, Go, lint, APKs e instrumentação de UI/host conforme
   ambiente. Cobertura elegível mínima 80%; registrar comando, toolchain, exit
   code e limites sem converter fake/emulador em evidência do pinpad físico.

Riscos técnicos: APIs novas exigem regeneração do AAR; session cleanup precisa
alcance Go mesmo após cancelamento da coroutine; resposta tardia não pode
alterar estado novo. Tabela e perfil ficam fora de APK, logs e repositório.
O laboratório fornece parâmetros obrigatórios; erro de configuração impede
I/O e aparece na UI. Falta de evidência física limita a declaração da entrega.

## Baseline de 2026-09-25

Implementação vertical: contrato/manifesto, fluxo Compose local e teste unitário. Sem alteração no host ou transação real.

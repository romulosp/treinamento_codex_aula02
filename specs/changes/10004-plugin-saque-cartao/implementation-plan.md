# Plano técnico

## Espera sem ação de cancelamento — REV-016

Restringir botão CANCELAR às etapas 2..4 e afirmar ausência de CANCELAR,
CANCELANDO e SAIR no teste existente da preparação. Executar teste UI focado
de espera e regressão do cancelamento transacional, build/lint e instalar host.

## Ordem da jornada autorizada REV-015

Mover Open/capacidades/carga para load, guardar capacidades e manter sessão
até Begin/saída. Criar espera técnica anterior ao formulário e tratar falha
com cleanup. Testar ordem, duplicação, carga pausada/falha/cancel/detach,
spinner/transição; executar gates Gradle, instrumentação e atualizar APK host.

## Correção autorizada REV-013 — 2026-10-11

Corrigir quoting remoto no launcher e validar hashes sob run-as. Executar
provisionamento privado da tabela real com comparação de hash, Parser PowerShell
e ValidateOnly; sem recompilar APK ou reiniciar Bridge, pois não há mudança
Kotlin/Go. Perfil candidato permanece local até escolha explícita do cenário.

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
4. UI: cartões e total destacado, ações textuais azul escuro. Duas colunas
   quando couber; rolagem em janela/fonte ampliada. Estado textual, alvos 48 dp
   e sem dados técnicos ou de cartão nas mensagens.
   Adendo REV-006: seguir os cinco SVGs Gemini; AnimatedContent por etapa
   com fade/deslocamento curto, spinner Material durante aguardo real e
   snapshots determinísticos para comparação visual e adaptação.
   Adendo REV-009: sem indicador de etapas em nenhuma tela, liberando altura
   sob o cabeçalho do host e evitando rolagem na primeira tela.
   Adendo REV-010: confirmação por DSP032 + GKY em quatro páginas, início
   físico condicionado a perfil e tabela provisionados e falha exibida com
   código/fase sanitizados no padrão do diagnóstico 075.
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


## Correção da primeira tela — REV-007

Contrato aprovado em [REV-007](reviews/2026-10-10-spec-review-primeira-tela.md):
remover faixa Voltar ao menu do host, manter Voltar Android, oferecer SAIR
vermelho abaixo de AVANÇAR. Modalidade e valor da compra editáveis sem perfil;
cálculo local independente do hardware. Taxa = 5,34% + 0,80 ponto percentual
por parcela adicional (1..12); débito em uma parcela. Total = compra + tarifa
HALF_UP em centavos. Perfil fornece limites e filtros de aplicação distintos,
não a taxa. AVANÇAR exige perfil válido. Testar cálculo, edição, seleção enviada
à captura, retorno e layout; preservar animações já implementadas.


## Correções de conformidade — REV-011

[REV-011](reviews/2026-10-11-spec-review-correcoes.md) aprova a validação local
gomobile ValidateFormattedEMVTableJSON antes da prontidão da UI, reutilizando
regras Go sem transporte. Entrada monetária inválida/excessiva é bloqueada sem
truncamento; débito tem tarifa fixa R$ 1,00. Testar arquivos privados sintéticos,
zero cliente/hardware em preflight e colagem/edição real. AAR será regenerado
com proveniência; Bridge e comandos permanecem compatíveis.


## Modalidade da tela — REV-012

[REV-012](reviews/2026-10-11-spec-review-modalidade.md): modalidade/compra vêm
da tela 1. Para a tabela DES indicada, o domínio mapeia crédito→01 e débito→02,
conforme seus tipos/rótulos, não por bandeira. Filtros externos no perfil
não são exigidos e são ignorados. Resumo congelado determina filtro e total
enviados ao Go. Versão/limite/prazos/cenário continuam no perfil do laboratório.

## Plano preparatório REV-019

Escopo: formatador da mensagem física, testes JVM e documentos da Change.
Remover padding global; ordenar total/tarifa/compra/quantidade/ações, preservar
formatação monetária e Nx. Alinhar somente título com fallback; validar bytes.
Testes: mensagens em 16/40 colunas e dimensões inválidas, limite 160, todos os
retornos de GKY sem captura exceto confirmação, DEX concluído antes de GKY.
Gates: validador estrutural, unitários, lint, cobertura, build host/plugin.
Entrega: encerrar Medium_Tablet, iniciar sem snapshot, instalar host com -r,
conferir hash do asset, PBRG/Ping, reverse e abertura; preservar arquivos privados.
Risco: fontes do firmware DEX podem diferir da grade GIX; alinhamento opcional.
Hardware/card/PIN exigem operador e evidência separada; não declarar fake físico.

# Tarefas: 10004-plugin-saque-cartao

**Autor:** Rômulo Penha

## Status
`IMPLEMENTADA`: correções REV-011 a REV-015 entregues para nova revisão formal.
A [revisão anterior](reviews/2026-10-11-implementation-review.md) permanece
registrada como REPROVADA. SPEC aprovada; homologação física pendente.

## Correção da primeira tela — REV-007

- [x] Registrar aprovação do contrato solicitado para layout, edição e taxas.
- [x] Remover faixa do host e manter retorno pelo Voltar Android.
- [x] Acrescentar SAIR vermelho abaixo de AVANÇAR, retornando sem logout.
- [x] Liberar modalidade/compra/parcelas sem perfil; bloquear somente início físico.
- [x] Aplicar 5,34% + 0,80 ponto por parcela adicional, total exato e limites.
- [x] Preservar modalidade/filtro e total na integração.
- [x] Executar 15 testes unitários, 6 de UI/perfil e 20 do host; cobertura 96,4%.
- [x] Registrar captura atual, builds, lint e pré-validação do launcher sem taxa externa.

## Pagamento com Cartão e usabilidade — REV-008

- [x] Renomear textos de interface, descritores publicados, manifesto do plugin e log do launcher.
- [x] Substituir o campo de parcelas por combobox; débito fixo em 1x sem lista.
- [x] Converter a compra para entrada monetária em centavos com teclado numérico.
- [x] Aplicar tarifa fixa de R$ 1,00 no débito e manter 5,34% + 0,80 por parcela no crédito.
- [x] Habilitar AVANÇAR com resumo válido e adotar perfil DES sintético sem arquivo externo.
- [x] Enfatizar transições de etapa e entrada das ações com deslocamento, fade e escala.
- [x] Executar 18 unitários, 8 de UI/perfil e 20 do host; cobertura de linhas 94,6%.
- [x] Atualizar capturas, hashes e inventário KDoc em evidence/ e validation.md.
- [ ] Registrar revisão da implementação e validação formal após teste humano.

## Remoção do indicador de etapas — REV-009

- [x] Registrar a decisão do solicitante e o motivo (rolagem sob o cabeçalho do host).
- [x] Remover o indicador "1..5"/"Etapa X de 5" das cinco telas do plugin.
- [x] Remover o estado `completedSteps`, cujo único consumidor era o indicador.
- [x] Atualizar testes de jornada e de UI; cobrir ausência do indicador e ausência de rolagem.
- [x] Executar 18 unitários, 9 de UI/perfil e 20 do host; cobertura de linhas 94,6%.
- [x] Atualizar spec, proposal, DESIGN, implementation-plan, capturas e hashes.
- [ ] Registrar revisão da implementação e validação formal após teste humano.

## Integração física no padrão 075 — REV-010

- [x] Apurar a causa do erro da etapa 2 com Bridge ativo (perfil e tabela não provisionados).
- [x] Exigir perfil válido e tabela EMV carregada para habilitar AVANÇAR.
- [x] Remover o perfil DES sintético e o estado `defaultProfileInUse`.
- [x] Distinguir perfil ausente, tabela ausente e conteúdo inválido com mensagem específica.
- [x] Expor código e fase sanitizados das falhas no padrão do diagnóstico 075.
- [x] Confirmar no pinpad por DSP032 + GKY em quatro páginas (total, parcelas, tarifa, confirmação).
- [x] Executar testes unitários e de UI/perfil e os 20 do host; cobertura 94,2%.
- [x] Registrar evidências e atualizar spec, proposal, DESIGN, implementation-plan e contrato.
- [ ] Repetir a jornada humana com pinpad físico e perfil/tabela do laboratório.

## Baseline histórica de 2026-09-25

As tarefas abaixo pertencem ao fluxo local anterior e não validam hardware.

- [x] Criar módulo e topologia Gradle.
- [x] Implementar contrato, manifesto e descritor de serviço.
- [x] Implementar fluxo Compose local e seguro.
- [x] Criar teste de contrato e executar testes técnicos.
- [x] Integrar abertura pelo clique no menu do host e retorno por `IPluginRouter`.
- [x] Remover o launcher incorreto do módulo de plugin.

## Revisão de 2026-10-10

- [x] Incorporar as duas séries de SVGs, jornada e complementos com hashes.
- [x] Avaliar e justificar a combinação visual das cinco etapas.
- [x] Revisar proposta, SPEC e design nesta mesma Change com dependência da 074.
- [x] Registrar lacunas concretas de GCX, GPN, perfil e autorização.
- [x] Definir homologação física DES e receber referência externa da tabela EMV.
- [x] Inspecionar estrutura DES sem publicar registros/chaves; não acessar PRD.
- [x] Definir DUKPT TDES, método 3, índice 08 e workingKey vazia com o solicitante.
- [x] Mapear dados por operação para consumo direto da fachada existente da 074.
- [x] Definir cenário controlado de resultado GOX/FCX.
- [x] Definir provisionamento Android e caminho Go para registros TLR pré-formatados.
- [x] Definir tarifa, limites, parcelas do débito e prazos por fase.
- [x] Aprovar contratos Go aditivos na 10004, preservando a 074.
- [x] Resolver REV-003/REV-004 e registrar SPEC_APROVADA em proposal.md e spec.md.
- [x] Atualizar implementation-plan.md após essa aprovação.
- [x] Implementar launcher Bridge/prontidão/reverse/abertura do host.
- [x] Gerar AAR com proveniência e provar carregamento dinâmico/nativas.
- [x] Implementar repository/estado e cinco etapas sem PIN no Android.
- [x] Implementar encadeamento GCX/GTK/GOX/GPN/FCX aprovado, cancelamento e cleanup.
- [x] Executar unitários, cobertura, lint, build, UI e testes de lifecycle.
- [x] Testar discovery/menu/View/nativas no host e callbacks de retorno na UI.
- [x] Registrar testes técnicos e inventário KDoc em validation.md.
- [x] Aplicar layout dos cinco SVGs Gemini e animações Compose por REV-006.
- [ ] Executar jornada humana completa com login e pinpad físico DES.
- [ ] Registrar revisão da implementação e validação formal após teste humano.
- [ ] Obter aprovação formal antes de atualizar system, archive e commit.


## Revisão de implementação — 2026-10-11

- [x] Revisar últimas alterações e conferir ambiente sem OPN.
- [x] Reexecutar 18 unitários, cobertura 94,2%, lint/build e 11 testes de UI/perfil.
- [x] Resolver IMP-REV-001: validação integral da tabela antes de iniciar hardware.
- [x] Resolver IMP-REV-002: rejeitar entrada monetária inválida sem conversão/truncamento.
- [x] Resolver IMP-REV-003: consolidar tarifa de débito, KDoc e métrica REV-010.
- [x] Receber escolha APPROVE do perfil proposto e provisionar perfil/tabela DES, usando PORTA_PINPAD.

## Correções e modalidade — REV-011/012

- [x] Aprovar contrato aditivo de validação Go local antes da implementação.
- [x] Validar tabela antes de publicar configReady, sem abrir cliente/hardware.
- [x] Preservar entrada inválida e impedir início com sinal, texto ou excesso de dígitos.
- [x] Gerar APPTYPE 01/02 da modalidade selecionada; dispensar filtros no perfil externo.
- [x] Conferir SPE_APPTYPE no GCX do manual local (páginas 35, 129 e 243).
- [x] Executar 19 unitários, 13 instrumentados e 1 teste focado do host; cobertura 94,3%.
- [x] Gerar AAR com quatro ABIs, exportação da validação e proveniência atualizada.
- [x] Registrar comandos, ambiente, resultados e limitações em validation.md.
- [ ] Obter nova revisão formal e completar homologação física.

## Provisionamento corrigido — REV-013/014

- [x] Reproduzir MISSING e erro de quoting/normalização CRLF no launcher.
- [x] Corrigir escrita privada via Base64 e verificar SHA-256 de cada arquivo.
- [x] Corrigir adquirente da tabela real para 04, preservando APPTYPE 01/02.
- [x] Receber escolha APPROVE e salvar perfil externo reutilizável.
- [x] Provisionar perfil e 36 registros no host com hashes idênticos à origem.
- [x] Validar Parser PowerShell, ValidateOnly e tabela integral pelo Go sem hardware.
- [ ] Conferir reentrada da tela após login humano e executar jornada física DES.

## Preparação EMV exclusiva do plugin — REV-015

- [x] Registrar ordem solicitada e escopo exclusivo do plugin em SPEC_APROVADA.
- [x] Iniciar preparação no attach da View, sem I/O no discovery/criação sem exibição.
- [x] Exibir espera/spinner e carregar EMV antes de mostrar formulário.
- [x] Reutilizar sessão/display; AVANÇAR inicia confirmação DSP/GKY sem carga repetida.
- [x] Tratar falha/cancelamento/detach na preparação e fechar sessão ao sair da tela 1.
- [x] Executar 24 unitários e cobertura 239/245 linhas (97,6%), lint e builds.
- [x] Conferir tela de espera e transição para formulário no emulador e capturar PNG.
- [x] Concluir 15 testes instrumentados e smoke test focado do host com APK atualizado.
- [ ] Executar teste humano da carga física ao entrar no plugin e confirmar/cancelar.

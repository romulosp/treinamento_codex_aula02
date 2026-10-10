# Tarefas: 10004-plugin-saque-cartao

**Autor:** Rômulo Penha

## Status
`IMPLEMENTADA` — disponível para teste humano conforme prompt de implementação para teste.

## Correção da primeira tela — REV-007

- [x] Registrar aprovação do contrato solicitado para layout, edição e taxas.
- [x] Remover faixa do host e manter retorno pelo Voltar Android.
- [x] Acrescentar SAIR vermelho abaixo de AVANÇAR, retornando sem logout.
- [x] Liberar modalidade/compra/parcelas sem perfil; bloquear somente início físico.
- [x] Aplicar 5,34% + 0,80 ponto por parcela adicional, total exato e limites.
- [x] Preservar modalidade/filtro e total na integração.
- [x] Executar 15 testes unitários, 6 de UI/perfil e 20 do host; cobertura 96,4%.
- [x] Registrar captura atual, builds, lint e pré-validação do launcher sem taxa externa.

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

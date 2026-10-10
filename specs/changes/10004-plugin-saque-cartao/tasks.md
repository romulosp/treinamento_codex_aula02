# Tarefas: 10004-plugin-saque-cartao

**Autor:** Rômulo Penha

## Status
`SPEC_APROVADA` — contrato executável aprovado em REV-005; implementação para teste humano.

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
- [ ] Implementar launcher Bridge/prontidão/reverse/abertura do host.
- [ ] Gerar AAR com proveniência e provar carregamento dinâmico/nativas.
- [ ] Implementar repository/estado e cinco etapas sem PIN no Android.
- [ ] Implementar encadeamento GCX/GTK/GOX/GPN/FCX aprovado, cancelamento e cleanup.
- [ ] Executar unitários, cobertura, lint, build, UI e testes de lifecycle.
- [ ] Validar host/login/discovery/menu/saque/retorno e hardware separadamente.
- [ ] Registrar revisão de implementação, validação e inventário KDoc.
- [ ] Obter aprovação formal; depois atualizar system, preparar archive e commit.

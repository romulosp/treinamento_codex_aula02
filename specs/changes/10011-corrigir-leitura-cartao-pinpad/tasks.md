# Tasks — 10011

## Estado da implementação

`IMPLEMENTADA` em 2026-10-12. Entrega de código, testes e artefatos preparada por
solicitação explícita do usuário. Testes técnicos registrados em `validation.md`;
revisão de código/arquitetura aprovada em 2026-10-13. Validação técnica disponível
passou; validação integral reprovada por gates materiais pendentes, conforme
validation.md. Aprovação final continua pendente.

## Especificação

- [x] Inspecionar log fornecido sem tentar recuperar frames redigidos.
- [x] Inspecionar roteamento de cartão, tipo de transação, filtros e fallback existentes.
- [x] Inventariar todas as propriedades criptográficas sem copiar material sensível.
- [x] Conferir manual ABECS primário e registrar restrição de PAN após GTK.
- [x] Criar proposta, SPEC, DESIGN, ADR, diagnóstico e plano de validação.
- [x] Executar revisão da SPEC e registrar decisão em `reviews/`.
- [x] Executar verificações documentais e testes baseline aplicáveis.

## Implementação — executada após SPEC_APROVADA

- [x] Criar `implementation-plan.md`, inventário de código/testes e sub-skills acionadas.
- [x] Importar as três propriedades no launcher/preflight e no core; retirar hardcodes PIN.
- [x] Garantir compra/filtros da tela 1/candidatos AID base e estendido, com fixtures sintéticas; demonstração física pendente.
- [x] Implementar contexto Go e operação aditiva de GPN pós-GTK conforme ADR-001.
- [x] Corrigir roteamento tipado 00/03/05/06 e sequência GTK→GOX/GPN.
- [x] Preservar fallback limitado e mensagem para ação externa do portador.
- [x] Corrigir diagnóstico sanitizado e encerramento sem cancelamento desnecessário.
- [x] Atualizar testes reais de jornada; o teste antigo que exige PIN antes de GTK foi substituído.
- [x] Atualizar KDoc/documentação dos contratos; recompilar AAR com proveniência.
- [x] Executar gates Go/Android, testes de sanitização e registrar evidências/limitações; race não executável sem compilador C.

## Revisão, homologação e encerramento futuros

- [x] Revisar implementação e arquitetura contra AC-01..AC-09 em `reviews/`.
- [x] Documentar diretório externo, consumidores e limites de compatibilidade na SPEC.
- [x] Criar memória para agentes, vinculá-la ao AGENTS.md e registrar ponto de continuidade.
- [x] Conferir fonte/AAR/native/plugin/host e executar instrumentação do APK final.
- [ ] Executar race em toolchain Go/CGO suportada com compilador C e auditoria formal de segurança.
- [ ] Homologar fisicamente crédito/débito, chip/tarja/CTLS EMV/CTLS simulada, AIDs e fallback.
- [ ] Confirmar erro principal e fechamento real com diagnóstico sanitizado.
- [ ] Obter aprovação formal, atualizar `specs/system/` e preparar commit/archive pelo workflow.

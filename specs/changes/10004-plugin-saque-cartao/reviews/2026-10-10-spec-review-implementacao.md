# REV-005 — Contrato de implementação para homologação

**Data:** 2026-10-10  
**Revisor:** Codex, implementador para teste humano  
**Decisão:** `SPEC_APROVADA`

Revisados proposta, SPEC, DESIGN, tarefas, evidências, contrato executável,
integração 074 e perfil DES. Consultado o manual local ABECS 2.12 indicado
pelo solicitante, extraído com `pdftotext -raw`, páginas 85, 128–141,
156–160 e seção 6.5.11. A leitura não altera o PDF nem acessa PRD.

| Achado anterior | Resolução contratual |
| --- | --- |
| REV-003-01 | Captura GCX filtrada aditiva na 10004; referências AARR ou adquirente/tipos de aplicação; kernel seleciona AID. `TransactionGCX` e Bridge da 074 preservados. |
| REV-003-02 | GPN existente recebe PAN vazio após GCX e antes de GTK; nenhum PAN passa ao Android. Método PIN 3, índice 8 e WK vazia confirmados. |
| REV-003-03 | Homologação DES explícita. Cenário FCX obrigatório no perfil, aplicado somente se GOX requer online. Decisões reais GOX/FCX prevalecem; resultado não autoriza dinheiro. |
| REV-003-04 | Taxa e limite obrigatórios no perfil; cálculo exato/HALF_UP; crédito 1–12 e débito uma parcela; prazos limitados. Ausência de configuração bloqueia comandos. |
| REV-004-01 | Nova importação Go valida registros completos antes de TLI e preserva TAB_LEN, ordem e limite TLR. API anterior não muda. |
| REV-004-02 | Launcher debug provisiona perfil e tabela em armazenamento privado do host via ADB; fonte Windows não é tratada como caminho Android. |

GTK usa exclusivamente método `30` DUKPT TDES DAT#1 ECB e índice DATA
explicitamente configurado, independente do índice PIN. TAB_RECIDX dos
tipos AID e CAPK é A2, permitindo índices alfanuméricos. GOX `01000` impede
aprovação offline somente em ICC; resultado CTLS offline continua possível.
Confirmação no display usa capacidades reais, GKY em cada página e rejeita
tecla inesperada. Nenhuma confirmação Android substitui confirmação física.

Não restam ambiguidades materiais para implementar o contrato. Configuração
do laboratório e evidência física são pré-requisitos da execução humana,
não valores financeiros a inventar durante implementação. Falha de nativas,
COM, chave ou configuração deve aparecer como erro real.

Esta decisão aprova somente a SPEC. Seguir o prompt solicitado: implementar,
executar testes técnicos, registrar evidências e parar em `IMPLEMENTADA`,
sem revisão de implementação, validação formal, aprovação final ou commit.

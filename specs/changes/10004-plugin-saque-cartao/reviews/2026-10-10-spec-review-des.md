# REV-004 — Reavaliação com tabela de homologação DES

**Data:** 2026-10-10  
**Decisão:** `REPROVADA` — decisões restantes e contratos Go ainda pendentes.

## Novas evidências

Solicitante confirmou pinpad físico/cartão de homologação e modo DES, com
`emv_des.json` externo. O PRD não foi acessado. A inspeção encontrou 36
registros pré-formatados, com 15 AIDs e 21 CAPKs. Ver
[perfil DES](../perfil-homologacao-des.md) para evidência e limites.

## Evolução dos achados REV-003

| Achado | Situação atual |
| --- | --- |
| REV-003-01 | Mantido: contrato completo GCX/modalidade/AIDs ainda reservado na 074. |
| REV-003-02 | Corrigida conclusão anterior: builder aceita PAN vazio, e manual admite uso antes de GTK sob condições End-to-End. Validar sequência no perfil, sem presumir obrigatória uma nova fachada. |
| REV-003-03 | Parcialmente resolvido: homologação DES, tabela e PIN DUKPT TDES/3/08/workingKey vazia confirmados. Cenário controlado GOX/FCX permanece pendente; chave PIN ainda exige prova física. |
| REV-003-04 | Mantido: taxa, limites, débito/parcelas e prazos não respondidos. |

## REV-004-01 — Importação TLR pré-formatada

**Severidade:** bloqueante para carga da tabela indicada.
**Evidência:** TAB_LEN dos 36 registros inclui N3; `BuildTLRCommand` adiciona
outro N3 calculado, e `LoadCompleteEMVTable` usa esse builder.
**Impacto:** envio direto do arquivo pela fachada duplica comprimento;
remoção ingênua do prefixo altera a convenção de comprimento.
**Resolução:** contrato Go que valide/preserve registros formatados e vetores
normativos. A carga não deve ser improvisada em Kotlin ou no Bridge.

## REV-004-02 — Provisionamento do perfil

**Severidade:** importante.
**Evidência:** arquivo reside no Windows; gomobile/core roda no Android;
Bridge da 074 só encaminha PBRG opaco.
**Impacto:** o caminho Windows sozinho não fornece configuração ao core Android.
**Resolução:** especificar provisionamento local autorizado de tabela/perfil,
mantendo dados sensíveis fora de UI, logs e arquivos versionados.

O manifesto do host já declara INTERNET; não é alteração necessária por si só.
Carregamento das ABIs/nativas no plugin ainda precisa de teste de integração.
Nenhum envio físico, carga de chaves, build ou teste Android foi executado.

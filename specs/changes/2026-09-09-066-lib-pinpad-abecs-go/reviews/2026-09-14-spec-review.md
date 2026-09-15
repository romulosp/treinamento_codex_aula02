# Revisão da SPEC aditiva — multimídia LMF/DMF e recuperação de comunicação

**Data:** 2026-09-14  
**Escopo:** aditivo multimídia da Change 066.  
**Fontes:** manual ABECS v2.12, seções 2.2.2, 2.2.2.3, 3.4.4, 3.4.5, 6.6.4 e 6.6.5; log físico `LogPinpadAbecs.txt`.

## Revisão

### REV-001 — Campos repetidos na resposta LMF

**Severidade:** alta.  
**Evidência:** o vetor publicado na seção 3.4.4 contém cinco campos `PP_MFNAME`
com a mesma tag `0x805E`; a lista pode ser vazia.  
**Impacto:** um parser que guarda uma única ocorrência perde quatro nomes e
torna uma resposta vazia indistinguível de falha se inventar conteúdo.  
**Decisão:** resolvido na SPEC. LMF preserva zero ou várias ocorrências, mantém a
ordem do dispositivo e converte nomes A8 válidos para maiúsculas.

### REV-002 — Exclusão DMF com vários nomes

**Severidade:** alta.  
**Evidência:** a seção 3.4.5 publica uma requisição com dois `SPE_MFNAME`; a
seção 6.6.5 determina que nomes desconhecidos ou inválidos sejam ignorados pelo
pinpad.  
**Impacto:** um builder limitado a um nome não implementaria a operação plural.
O validador local precisa impedir campos A8 malformados sem rejeitar nomes
válidos que ainda não existam no dispositivo.  
**Decisão:** resolvido na SPEC. O builder aceita um ou mais nomes A8 válidos; o
pinpad permanece responsável por ignorar nomes inexistentes. A tolerância do
firmware para entradas malformadas não pode ser demonstrada por teste de host.

### REV-003 — Respostas tardias após timeout

**Severidade:** alta.  
**Evidência:** o log registra ACK do MLE, timeout de resposta após 10 segundos e,
mais tarde, os retornos `MLE000` e `DSI000` acumulados antes da leitura posterior
de GIX. A seção 2.2.2 fixa até 10 segundos para comando não bloqueante, e a
seção 2.2.2.3 define CAN/EOT para cancelar e voltar ao estado ocioso.  
**Impacto:** sem recuperação, o próximo comando pode consumir a resposta antiga
e os erros passam a se propagar para as operações seguintes.  
**Decisão:** resolvido na SPEC. O prazo normativo não aumenta; o serviço drena
respostas pendentes até EOT após timeout, resposta incompatível ou exaustão de
tentativas. Falha de recuperação bloqueia novos comandos comuns até Reset ou
reabertura.

## Consistência e escopo

As alterações mantêm os formatos publicados: LMF literal, DMF com parâmetros
repetidos, nomes A8, resposta LMF pela tag `0x805E` e status ABECS original.
`proposal.md`, `spec.md`, `DESIGN.md`, `tasks.md`, SPECs individuais,
conformidade normativa e logging foram alinhados. A SPEC não declara que host
implementará lógica de firmware para armazenamento, exibição, centralização,
validação de imagem ou limpeza de arquivo temporário; esses comportamentos são
responsabilidade do pinpad, e os testes físicos permanecem no gate de validação.

Nenhum ADR é necessário: não há nova plataforma, dependência ou escolha
arquitetural irreversível. Não há dependência de API REST, banco de dados ou
persistência local de conteúdo multimídia.

## Decisão

Os achados REV-001 a REV-003 foram resolvidos no contrato. Os critérios de
aceite são verificáveis por vetores do manual e testes unitários; o estado do
firmware permanece condicionado à validação física prevista pelo workflow.

`SPEC_APROVADA`

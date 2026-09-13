# Revisão da implementação — 066-lib-pinpad-abecs-go (conformidade ABECS 2.12)

**Data:** 2026-09-12  
**Estado de entrada:** `IMPLEMENTADA`  
**Fonte normativa:** *Pinpad Abecs — Protocolo de Comunicação e Funcionamento*, versão 2.12, 11-abr-2019.  
**Escopo:** código, SPECS, builders, parsers, fachada, transporte serial e testes do módulo `apps/desktop/libpinpadabecsgo/`.

## Critério da revisão

Esta reavaliação adota o manual ABECS 2.12 como fonte normativa. Um teste só é
considerado evidência de conformidade quando a entrada, os bytes produzidos, a
resposta e a sequência exercitada correspondem ao formato, aos limites e ao
fluxo descritos no manual. Testes que apenas repetem o comportamento atual do
código não comprovam o protocolo.

A revisão anterior de 2026-09-11 não comparou cada comando aos layouts e vetores
publicados no manual. Por isso, sua conclusão não cobre as divergências listadas
neste documento.

## Evidências executadas

| Verificação | Resultado |
| --- | --- |
| `go test ./... -count=1` | código 0; todos os pacotes passaram |
| `go vet ./...` | código 0 |
| `go test ./... -coverprofile=<perfil> -count=1` | código 0; cobertura total 81,2% |
| `go tool cover -func=<perfil>` | código 0; mostrou funções sem cobertura e cobertura parcial em funções críticas |
| Inspeção visual e extração textual do PDF | páginas 17–22, 29–43, 45–60, 70–110, 129–142 e 168–172 verificadas |
| Pinpad físico | não executado nesta revisão; a conformidade física continua pendente |

A cobertura total supera 80%, mas não significa que cada função esteja validada
contra o manual. Entre as funções com 0% estão `GenerateSecureOPN`, `TagHex`,
`joinOptions`, `Queue.Size` e `Queue.IsEmpty`; builders críticos possuem execução
nos testes, mas com oráculos incompatíveis com a ABECS 2.12.

## Matriz por comando e fluxo

| Comando/fluxo | Resultado da comparação com ABECS 2.12 | Evidência principal |
| --- | --- | --- |
| Enlace/CRC | **Não conforme** | Não há retransmissão após NAK, NAK após CRC inválido, três tentativas, timeout de ACK de 2 s ou limite de pacote original. |
| CAN | **Não conforme** | Envia uma vez e aceita EOT, mas não repete três vezes, não ignora bytes concorrentes e `Open` não inicia a comunicação com CAN. |
| OPN clássico | **Não conforme** | O manual envia `OPN`; a implementação e os testes enviam `OPN000`. |
| OPN seguro | **Parcial** | O builder RSA/AES tem a estrutura básica, mas `OpenSecure` envia primeiro um OPN clássico incorreto e os testes não usam o vetor criptográfico publicado. |
| CLO | **Não conforme** | O manual exige `CLO032` + `CLO_MSG` S32; a implementação envia `CLO000`. |
| CLX | **Parcial** | Os parâmetros ABECS estão estruturados, mas o comando é enviado em claro durante sessão segura; o manual só isenta OPN de criptografia e define apenas a resposta CLO/CLX em claro. |
| GIX | **Parcial** | O parser genérico preserva TLVs, mas as capacidades de mídia e dimensões gráficas são interpretadas incorretamente e o catálogo de status/tags não está integralmente alinhado. |
| DSP | **Parcial** | O comprimento S32 é produzido, mas não há teste com o vetor do manual nem validação do conjunto de caracteres de um byte. |
| DEX | **Parcial** | A estrutura N3 + S..160 é compatível, mas os testes não comparam o vetor do manual e contam bytes UTF-8 como caracteres do protocolo. |
| MNU | **Não conforme** | Falta limitar 20 opções, 24 bytes por opção e timeout X1 até 255; o parser aceita seleção de um dígito que o manual não permite. |
| GKY | **Não conforme** | O manual define comando bruto `GKY` e tecla em `RSP_STAT`; o projeto inventa modo/timeout no payload e procura a tecla em `Data`. |
| GPN | **Não conforme** | Os builders omitem método, WKENC H32, PANLEN, PAN A19, ENTRIES, MIN/MAX e montam prefixos `MK`/`DU` inexistentes. |
| GTK | **Não conforme** | Impede trilhas em claro, rejeita os métodos 90/91 e exige índice onde o manual o dispensa para 9x. |
| MLI | **Não conforme** | Deveria usar `SPE_MFNAME` e `SPE_MFINFO` binário com tamanho X4, CRC B2, tipo B1 e RUF B3; usa nome + tamanho decimal e CRC-32 separado. |
| MLR | **Não conforme** | Deveria transportar um ou mais `SPE_DATAIN`; envia bytes posicionais e usa limite 1024 incompatível com o parâmetro de até 995 bytes. |
| MLE | **Não conforme** | O manual define somente `MLE`; a implementação acrescenta comprimento e nome. |
| DSI | **Não conforme** | O manual define `SPE_MFNAME` TLV; a implementação envia o nome como campo posicional. |
| TLI | **Não conforme** | Não exige versão A10; a SPEC interpreta ao contrário o status 020. O manual permite continuar com TLR/TLE após 000 ou 020. |
| TLR | **Não conforme** | O manual exige NREC N2 seguido de registros concatenados, cada qual com seu tamanho; a implementação une registros com `|`. |
| TLE | **Não conforme** | O manual define somente `TLE`; a implementação acrescenta comprimento e versão. |
| GCX | **Não conforme** | `SPE_GCXOPT` foi corrigido, porém `SPE_TRNDATE` é AAMMDD no manual e a API/testes exigem DDMMAA; faltam regras de nova tentativa CTLS. |
| GOX | **Não conforme** | O TLV básico existe, mas tamanhos/formatos de campos opcionais e regras condicionais não são integralmente validados; respostas mandatórias não são exigidas. |
| FCX | **Não conforme** | Aceita bits RUF diferentes de zero, não exige todos os campos condicionais em formato normativo e não valida dados mandatórios da resposta. |
| RST | **Fora da ABECS 2.12** | Não existe comando `RST` no manual revisado; o projeto inventa `RST003000`. |
| QR Code | **Não conforme com a própria SPEC** | `PositionWarning` é preenchido mesmo com `xPos=0,yPos=0`; a exibição depende do fluxo multimídia atualmente inválido. |
| Logging/redaction | **Conforme no escopo automatizado** | Há testes de criação do destino, SPE/PP/RSP e redação; isso não torna válidos os payloads registrados. |
| Fila/sessão/configuração | **Parcial** | Os testes gerais passam, mas os timeouts uniformes da fachada conflitam com os tempos normativos de enlace e com comandos blocantes. |

## Achados

### IMP-REV-016 — A linha de base da SPEC contradiz a fonte normativa

- **Severidade:** Crítica
- **Requisito:** proposta, seção “Referências”, e solicitação de que cada teste unitário corresponda à ABECS 2.12.
- **Evidência:** `spec.md` define `OPN000`, `CLO000`, GCX em `DDMMYY`, modos artificiais de GKY e um comando RST. As SPECS específicas repetem `CLO000`, GCX em `DDMMAA` e encerramento da carga depois de `TLI020`. O manual define OPN clássico bruto, CLO com mensagem S32, GCX em `AAMMDD`, GKY bruto, continuação da carga depois de 000/020 e não contém RST.
- **Impacto:** código e testes podem cumprir a SPEC aprovada e, simultaneamente, descumprir o protocolo do dispositivo.
- **Ação necessária:** retornar a Change à fase de SPEC, corrigir `proposal.md`, `spec.md`, `DESIGN.md`, `tasks.md` e todas as SPECS de comando divergentes; revisar e aprovar novamente antes da implementação.

### IMP-REV-017 — O nível de enlace não implementa retransmissão e tempos normativos

- **Severidade:** Crítica
- **Requisito ABECS:** seções 2.2.1 e 2.2.2, páginas 17–22.
- **Evidência:** `exchangeCommandWithTracePolicy` escreve uma única vez, encerra no primeiro NAK e retorna diretamente qualquer erro de CRC. `ReadFullResponse` apenas valida uma resposta. Não há envio de NAK para resposta corrompida, três tentativas, timeout próprio de ACK de 2 segundos nem limite de PKTDATA original. `submit` aplica o timeout genérico de configuração.
- **Impacto:** erros recuperáveis de transmissão viram falha definitiva; comandos blocantes podem expirar indevidamente e comandos não blocantes podem aguardar além dos 10 segundos definidos.
- **Ação necessária:** implementar a máquina de enlace e testes determinísticos com os exemplos das páginas 20–22, inclusive NAK de comando, NAK de resposta, três tentativas, ACK/EOT em 2 segundos e limites 1024/2044/2049 aplicáveis.

### IMP-REV-018 — Cancelamento e abertura não restauram o estado do pinpad conforme o manual

- **Severidade:** Crítica
- **Requisito ABECS:** seção 2.2.2.3, páginas 21–22.
- **Evidência:** `Reset` transmite CAN uma única vez; cancelamento de contexto interrompe somente a leitura local; `Open` abre a porta e envia OPN sem o CAN inicial obrigatório.
- **Impacto:** um comando blocante residual pode contaminar a próxima sessão e o pinpad pode continuar ocupado após o consumidor cancelar a operação.
- **Ação necessária:** enviar CAN ao iniciar a comunicação; aguardar EOT por 2 segundos, ignorando outros bytes, e repetir CAN até três vezes. Ao cancelar comando blocante, concluir esse mesmo handshake antes de liberar a fila.

### IMP-REV-019 — OPN, CLO e comunicação segura possuem sequência e proteção incorretas

- **Severidade:** Crítica
- **Requisito ABECS:** seções 3.2.1, 3.2.2, 3.2.6 e 5.2.2.
- **Evidência:** `Open` envia `OPN000`; `OpenSecure` chama `Open` e depois envia outro OPN; `Close` envia `CLO000`; `exchangePayloadWithTracePolicy` exclui CLO e CLX da proteção de comandos durante sessão segura. A ABECS envia OPN clássico como `OPN`, usa o OPN seguro como abertura, exige `CLO032` + S32 e isenta somente OPN no envio criptografado. Somente as respostas de CLO/CLX são declaradas em claro.
- **Impacto:** status 011/009, dupla abertura e encerramento inseguro ou não reconhecido pelo equipamento.
- **Ação necessária:** separar abertura clássica e segura, corrigir CLO, proteger os comandos CLO/CLX quando KSEC estiver ativa e adicionar os vetores publicados para OPN/AES-CBC.

### IMP-REV-020 — GKY, GPN e RST são contratos inventados ou incompatíveis

- **Severidade:** Crítica
- **Requisito ABECS:** seções 3.3.10 e 3.3.11, páginas 80–83.
- **Evidência:** `BuildGKYCommand` cria payload com modo e timeout; `WaitForKeyPress` busca um byte em `Data`, embora as teclas sejam os status 000, 004–008 e 013. Os builders GPN usam prefixos `MK`/`DU` e omitem a maior parte do layout fixo. `RST` não aparece no manual.
- **Impacto:** os três fluxos podem ser rejeitados pelo pinpad; GKY trata teclas válidas como erro; GPN pode capturar PIN com parâmetros diferentes dos autorizados.
- **Ação necessária:** implementar os layouts exatos e os vetores da página 83; modelar o resultado GKY a partir de `RSP_STAT`; remover RST do escopo ABECS 2.12 ou documentar outra fonte normativa em Change separada.

### IMP-REV-021 — Multimídia e exibição de imagem não usam os campos ABECS

- **Severidade:** Crítica
- **Requisito ABECS:** seções 3.4.1–3.4.6, páginas 93–102.
- **Evidência:** MLI usa nome + tamanho decimal, MLR envia bytes sem `SPE_DATAIN`, MLE inclui nome e DSI envia nome sem `SPE_MFNAME`. `CalculateFileCRC` usa CRC-32, enquanto `SPE_MFINFO` exige CRC B2. Há ainda dois limites MLR conflitantes: 1024 em `command` e 436 em `protocol`.
- **Impacto:** MLI/MLR/MLE/DSI e qualquer QR Code que dependa deles não produzem pacotes aceitos segundo o manual.
- **Ação necessária:** reescrever os builders com `SPE_MFNAME`, `SPE_MFINFO` e `SPE_DATAIN`, CRC-16 B2, validação de nome/tipo/tamanho e exemplos binários exatos das páginas 93–102.

### IMP-REV-022 — Carga de tabelas EMV contradiz a sequência ABECS

- **Severidade:** Crítica
- **Requisito ABECS:** seções 3.5.2–3.5.4, páginas 106–110.
- **Evidência:** `BuildTLICommand` não exige A10; `BuildTLRCommand` usa `|` sem NREC; `BuildTLECommand` acrescenta versão. `LoadCompleteEMVTable` retorna imediatamente em 020. O manual declara que 000 e 020 iniciam a carga e ambos permitem TLR/TLE.
- **Impacto:** registros podem não ser carregados, e a biblioteca interpreta “versão diferente” como se a tabela já estivesse atualizada.
- **Ação necessária:** corrigir os três layouts e inverter a regra de 020; adicionar os vetores TLI/TLR/TLE publicados e cenários 000, 020, 010 e 021.

### IMP-REV-023 — Builders e parsers transacionais validam um subconjunto insuficiente

- **Severidade:** Alta
- **Requisito ABECS:** MNU 3.3.13, GTK 3.3.12 e GCX/GOX/FCX 3.7.1–3.7.4.
- **Evidência:** MNU aceita timeout até 999 e não limita opções; GTK exige criptografia e não reconhece 90/91; GCX valida data com layout Go `020106` e o menu pede DDMMAA; GOX aceita comprimentos arbitrários em campos N/B; FCX aceita RUF diferente de zero; os parsers não rejeitam a ausência dos dados de resposta marcados M/MD/MR.
- **Impacto:** payloads formalmente inválidos passam na validação local, enquanto respostas incompletas podem ser tratadas como sucesso.
- **Ação necessária:** criar validação por campo, dependências condicionais e testes tabelados a partir dos vetores das páginas 89–90 e 129–141. GCX deve usar `AAMMDD`.

### IMP-REV-024 — Interpretação de GIX e catálogo de status têm valores incorretos ou ausentes

- **Severidade:** Alta
- **Requisito ABECS:** seções 3.1.1, 3.1.3.2 e 3.2.4.
- **Evidência:** `DisplayCapabilitiesFromResponse` procura os textos `PNG`, `JPG` e `GIF`, mas `PP_MFSUP` é um mapa de posições `1xxx...`; trata `PP_DSPGRSZ` como largura/altura, embora o formato seja linhas/colunas. O catálogo declara status 047, ausente da tabela 2.12, e omite 051 (`ST_NOSAM`).
- **Impacto:** capacidades válidas são informadas como ausentes e códigos reais perdem descrição correta.
- **Ação necessária:** mapear cada campo conforme as páginas 39–43 e validar com a resposta GIX completa publicada, inclusive mapas de chaves, memória TLR e tags dinâmicas.

### IMP-REV-025 — Os testes unitários atuais têm oráculos incompatíveis com a ABECS 2.12

- **Severidade:** Alta
- **Requisito:** CA-003 a CA-006, CA-010, CA-014, CA-016 e solicitação atual.
- **Evidência:** testes esperam `OPN000`, `CLO000`, GCX em DDMMAA e interrupção após `TLI020`; para DSI/MLI/MLE/TLE/RST conferem somente os três caracteres iniciais. GPN apenas verifica ausência de erro e tamanhos. O teste seguro faz round-trip com dados gerados pelo próprio código em vez de comparar o vetor AES da página 170.
- **Impacto:** a suíte permanece verde mesmo com layouts que o manual contradiz; regressões normativas não são detectadas.
- **Ação necessária:** para cada builder/parser e fluxo serial, incluir ao menos um vetor positivo byte a byte do manual, limites e campos condicionais, respostas publicadas e casos de erro normativos. Remover fixtures que canonizam comportamento incorreto.

### IMP-REV-026 — `DisplayQRCode` não cumpre o critério aprovado para posição zero

- **Severidade:** Média
- **Requisito:** `spec-command-qrcode.md`, RF-QR-001 e CA-QR-001.
- **Evidência:** `DisplayQRCode` sempre preenche `PositionWarning`, inclusive quando `xPos == 0 && yPos == 0`; o teste cobre apenas posição diferente de zero.
- **Impacto:** o consumidor recebe aviso indevido no caso em que não solicitou posicionamento.
- **Ação necessária:** preencher o aviso somente quando uma das posições for diferente de zero e adicionar o cenário CA-QR-001.

## Requisitos que permanecem atendidos

- O módulo, pacote-base e isolamento do adaptador serial estão no local aprovado.
- Não foi encontrado servidor HTTP/WebSocket nem API pública de comando hexadecimal bruto.
- A fila, sessão, configuração, logging e redaction possuem testes úteis de comportamento próprio.
- CRC-16-CCITT e substitution produzem o resultado esperado no caso básico; faltam os vetores normativos e a máquina completa de retransmissão.
- O parser BER-TLV e a preservação defensiva de bytes têm cobertura automatizada relevante.

## Decisão

`REPROVADA`

A Change deve retornar à revisão de SPEC antes de qualquer correção de código,
porque vários testes atuais implementam literalmente requisitos aprovados que
contradizem o manual ABECS 2.12. Depois da nova aprovação, a implementação deve
ser corrigida e novamente revisada. A validação formal e o arquivamento permanecem
bloqueados até que os achados críticos e altos sejam resolvidos e os comandos
aplicáveis sejam comprovados também no pinpad físico.

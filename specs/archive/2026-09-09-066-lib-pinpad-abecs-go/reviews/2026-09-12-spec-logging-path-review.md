# Revisão da SPEC — caminho e emissão do logging ABECS

Autor: Rômulo Penha

**Data:** 2026-09-12

**Skill aplicada:** `spec-review`

**Escopo:** `proposal.md`, `spec.md`, `spec-logging.md`, `DESIGN.md`,
`tasks.md`, `implementation-plan.md`, `spec-protocolo-seguro.md`, SPECs de
MLI/MLR/MLE/TLR/TLE e impacto em `validation.md` da Change
`2026-09-09-066-lib-pinpad-abecs-go`.

## Método

Foram comparados o comportamento de logging do arquivo externo
`br_com_execucao_jbc_PinpadSerialProtocol.c`, a evidência operacional dos dois
arquivos `LogPinpadAbecs.txt`, o contrato vigente e as responsabilidades das
camadas Go. O arquivo C foi tratado apenas como referência de comportamento:
destino explícito em append, `open`, `close`, `SPE`, `PP` e erros de I/O.

## Achados tratados

### REV-001 — Caminho relativo permitia arquivos homônimos

- **Severidade:** bloqueante.
- **Evidência:** o arquivo esperado em
  `apps/desktop/libpinpadabecsgo/logs/LogPinpadAbecs.txt` estava com zero byte,
  enquanto `cmd/libpinpadabecsgo/logs/LogPinpadAbecs.txt` continha linhas. O
  contrato anterior usava `logs/LogPinpadAbecs.txt` sem proibir resolução pelo
  diretório de trabalho.
- **Impacto:** o operador poderia abrir o arquivo correto segundo a
  documentação, mas o processo escrever em outro local e aparentar ausência de
  logging.
- **Recomendação aplicada:** RF-L001.1, CA-L011, CA-L014, DESIGN e plano agora
  exigem caminho absoluto ancorado na raiz do módulo, caminho ativo exibido e
  falha de inicialização quando a raiz não puder ser determinada.

### REV-002 — Responsabilidade de `SPE` contraditória

- **Severidade:** importante.
- **Evidência:** RF-L003 e RF-L008.1 atribuíam `SPE` à aplicação, mas a
  referência C registra o envio no ponto de I/O e o adaptador Go é quem conhece
  a escrita integral efetivamente aceita.
- **Impacto:** implementações poderiam omitir, antecipar ou duplicar a linha
  `SPE`.
- **Recomendação aplicada:** o adaptador serial real passou a ser a fonte única
  de `open`, `close`, `SPE`, `PP` e erros; o serviço informa o comando ativo e
  permanece fonte única de `RSP`.

### REV-003 — Arquivo vazio não era detectado na ativação

- **Severidade:** importante.
- **Evidência:** o contrato permitia abrir/criar o arquivo sem uma escrita de
  confirmação antes do primeiro I/O.
- **Impacto:** um destino incorreto, vazio ou não observável só seria percebido
  após uma operação física.
- **Recomendação aplicada:** RF-L001 e CA-L015 exigem marcador `TRACE ...
  enabled` imediatamente legível antes do menu.

### REV-004 — Falhas de persistência podiam ser silenciosas

- **Severidade:** importante.
- **Evidência:** não havia critério para erro de escrita, flush ou fechamento
  do arquivo.
- **Impacto:** o comando poderia aparecer como concluído no console enquanto a
  evidência em arquivo era perdida.
- **Recomendação aplicada:** RF-L010 e CA-L017 tornam essas falhas observáveis e
  fatais para a sessão do utilitário de validação, preservando causas quando
  coexistirem com erro de comunicação.

### REV-005 — Cobertura de comandos não era exaustiva

- **Severidade:** moderada.
- **Evidência:** os exemplos citavam poucos comandos e não havia teste de
  matriz do catálogo completo.
- **Impacto:** alguns fluxos do menu poderiam continuar sem `CMD=` mesmo com os
  exemplos básicos aprovados.
- **Recomendação aplicada:** RF-L005 e CA-L016 enumeram todos os 22 comandos
  tipados da Change e definem o tratamento de operações compostas e stubs que
  não chegam à serial.

### REV-006 — Rastro hexadecimal conflitava com redaction transversal

- **Severidade:** bloqueante.
- **Evidência:** a regra genérica de `SPE/PP` permitiria registrar blocos MLR,
  registros TLR e frames de comunicação segura, enquanto as SPECs de mídia,
  EMV e segurança proíbem persistir esses conteúdos.
- **Impacto:** cumprir o diagnóstico byte a byte poderia violar requisitos de
  proteção de mídia, tabela EMV e material criptográfico.
- **Recomendação aplicada:** RF-L006 agora define política de redaction antes
  do I/O, inclui MLR, TLR, negociação OPN, comunicação segura e conteúdo
  marcado pelo consumidor. As SPECs afetadas referenciam explicitamente o
  marcador de redação, mantendo visíveis apenas bytes de controle isolados.

### REV-007 — Pacotes SPE/PP não estavam destacados como evidência obrigatória

- **Severidade:** bloqueante.
- **Evidência:** a saída operacional mostrava a conclusão JSON de GIX, mas o
  arquivo consultado não mostrava o pacote enviado nem o ACK recebido no
  formato esperado.
- **Impacto:** seria possível aprovar um logger que registrasse apenas
  `CMD=GIX`/`STATUS=000`, sem permitir conferir framing, substitution, CRC e
  fragmentação da resposta.
- **Recomendação aplicada:** `spec-logging.md` agora contém a regra obrigatória
  com as linhas `[COM7#001] SPE ... CMD=GIX` e `[COM7#001] PP  06`; RF-L003,
  RF-L004, CA-L004, CA-L005, CA-L015 e CA-026 exigem comparação byte a byte com
  escrita e leituras físicas.

## Verificações finais

- Objetivo, escopo, fora de escopo, dependências, riscos e critérios de aceite
  permanecem coerentes com uma biblioteca desktop/headless.
- O contrato mantém `slog` operacional e tracer serial como canais distintos;
  uma linha JSON no console não substitui a evidência SPE/PP/RSP no arquivo.
- `FUNC` e `DATA_HORA` têm formato verificável e não dependem de stack trace.
- A redação integral é obrigatória para GCX, GTK, GOX, FCX, GPN, MLR, TLR,
  negociação criptográfica, comunicação segura e conteúdo marcado como
  sensível pelo consumidor.
- A validação anterior foi marcada como superada e não autoriza aprovação da
  implementação revisada.

## Conclusão

Os achados foram resolvidos documentalmente e os critérios atualizados são
objetivos, verificáveis e compatíveis com o comportamento de referência sem
copiar o código C para o repositório.

**Resultado:** `SPEC_APROVADA`

**Próxima fase autorizada:** implementar os itens pendentes de logging em
`tasks.md`, executar os testes CA-L001 a CA-L017 e repetir revisão de
implementação e validação física sanitizada.

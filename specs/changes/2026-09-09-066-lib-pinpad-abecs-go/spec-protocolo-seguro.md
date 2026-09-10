# SPEC: 066-lib-pinpad-abecs-go — Comunicação segura ABECS

## Status
`RASCUNHO`

## Objetivo
Definir a conversão das funções de comunicação segura existentes no legado Java + JNI + C para Go, sem misturar criptografia com o parser de comandos em claro. Esta SPEC é transversal, mas necessária para `OPN` seguro, `CLO`/`CLX` seguro, pacotes iniciados por `DC2` e os comandos que transportam dados protegidos.

## Escopo
- RSA PKCS#1 usado na negociação prevista pelo manual.
- Estabelecimento, armazenamento temporário e descarte de `KSEC`.
- AES-CBC, IV, padding e formato do pacote cifrado, somente conforme manual/legado comprovado.
- Estados `CLEAR`, `SECURE`, `CLOSING` e falha, caso sejam necessários ao dispositivo.
- Integração com `SerialPort`, framing e tracer sem expor material criptográfico.

## Fora de escopo
- Criar um protocolo proprietário.
- Escolher algoritmo, tamanho de chave, padding ou IV por conveniência.
- Armazenar chaves em arquivo, log, banco ou variável persistente.
- Usar uma sessão fake para declarar segurança funcional.

## Requisitos funcionais

### RF-SEC-001 — OPN inicial
`OPN` deve ser enviado em claro quando o manual exigir. A resposta deve ser validada antes de iniciar qualquer estado seguro.

### RF-SEC-002 — Negociação RSA
A chave pública, formato de bloco, padding e mensagens devem ser exatamente os definidos pela revisão do manual adotada. A implementação deve rejeitar chave, tamanho ou bloco inválido.

### RF-SEC-003 — KSEC temporária
`KSEC` deve existir somente durante a sessão autorizada, não ser registrada e ser descartada ao fechar, falhar ou cancelar a sessão segura.

### RF-SEC-004 — AES-CBC
Pacotes protegidos devem usar o formato aprovado, incluindo IV, padding, `DC2`, tamanho e CRC na ordem documentada. O framing ABECS não pode calcular CRC sobre representação diferente da definida no manual.

### RF-SEC-005 — CLO/CLX
O encerramento seguro deve ocorrer antes do fechamento físico quando requerido. Falha de encerramento não pode ser ocultada pelo fechamento da porta.

### RF-SEC-006 — Redaction
SPE, PP, RSP, `slog`, erros e métricas nunca podem conter RSA, KSEC, AES, IV, PIN, PIN block, KSN, PAN ou pacote cifrado convertido em texto.

## Segurança e validação

- Usar somente primitivas da biblioteca padrão ou dependência aprovada na SPEC.
- Não implementar criptografia manual.
- Fazer comparação e validação sem aceitar truncamento silencioso.
- Usar vetores documentados sem chaves reais nos testes unitários.
- Validar negociação e operação em pinpad físico de laboratório.

## Critérios de aceite

- [ ] Inventário do legado identifica cada função JNI/C de RSA/AES e seu equivalente Go.
- [ ] Manual e dispositivo utilizado confirmam formato, chaves, padding, IV e sequência.
- [ ] OPN seguro é aceito pelo pinpad físico real.
- [ ] Um comando protegido é enviado e respondido corretamente sem dados em claro no log.
- [ ] CLO/CLX encerra a sessão segura e deixa a porta em estado consistente.
- [ ] Cancelamento, timeout, NAK e falha criptográfica são erros distinguíveis.
- [ ] `go test -race ./...` não encontra corrida ou vazamento.
- [ ] Evidências sanitizadas são registradas em `validation.md`.

## Dependências
`spec-command-opn.md`, `spec-command-clo.md`, `spec-command-clx.md`, `spec-infra-serial-cancel.md` e `spec-logging.md`.

## Referências
Manual ABECS v2.12, seções de OPN seguro e comunicação protegida; funções equivalentes identificadas nos fontes Java/JNI/C. A revisão formal deve confirmar se a versão do manual usada pelo dispositivo é compatível.

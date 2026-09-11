# SPEC: 066-lib-pinpad-abecs-go — Comunicação segura ABECS

## Status
`SPEC_APROVADA`

## Objetivo
Definir a conversão da comunicação segura existente no legado Java + JNI + C
para Go, sem misturar criptografia com o parser de comandos em claro. Esta
SPEC é transversal e integra o escopo desta Change para `OPN` seguro, `CLO`,
`CLX`, pacotes iniciados por `DC2` e comandos protegidos. `CLX` permanece uma
operação visual na fachada, mas o pinpad encerra a comunicação segura e limpa
`KSEC` quando o recebe, conforme manual ABECS v2.12, seção 6.4.5.

## Escopo
- RSA PKCS#1 usado na negociação prevista pelo manual.
- Estabelecimento, armazenamento temporário e descarte de `KSEC`.
- AES-CBC, IV, padding e formato do pacote cifrado, somente conforme manual/legado comprovado.
- Estados `CLEAR`, `SECURE`, `CLOSING` e falha, caso sejam necessários ao dispositivo.
- Integração com `SerialPort`, framing e tracer sem expor material criptográfico.
- Implementação somente após confirmar no manual e no dispositivo a sequência e
  o formato aprovados para o perfil de comunicação segura.

## Fora de escopo
- Criar um protocolo proprietário.
- Escolher algoritmo, tamanho de chave, padding ou IV por conveniência.
- Armazenar chaves em arquivo, log, banco ou variável persistente.
- Usar uma sessão fake para declarar segurança funcional.

## Requisitos funcionais

### RF-SEC-001 — OPN inicial
`OPN` deve ser enviado em claro quando o manual exigir. A resposta deve ser validada antes de iniciar qualquer estado seguro.

### RF-SEC-002 — Negociação RSA

O legado identifica uma negociação por `OPN` com par RSA de 2048 bits e
expoente público `65537`, seguida da obtenção de `KSEC` criptografada. A
implementação Go deverá aceitar somente esse perfil enquanto nenhuma revisão
normativa aprovada estabelecer outro; deverá validar tamanho, formato do bloco
e resposta antes de instalar a chave de sessão. A chave pública, formato do
bloco e padding devem ser exatamente os definidos pela revisão do manual
adotada; a implementação deve rejeitar chave, tamanho ou bloco inválido.

### RF-SEC-003 — KSEC temporária
`KSEC` deve existir somente durante a sessão autorizada, não ser registrada e ser descartada ao fechar, falhar ou cancelar a sessão segura.

### RF-SEC-004 — AES-CBC
Pacotes protegidos devem usar o formato aprovado, incluindo IV, padding, `DC2`, tamanho e CRC na ordem documentada. O framing ABECS não pode calcular CRC sobre representação diferente da definida no manual.

### RF-SEC-005 — CLO e encerramento

`CLO` encerra a comunicação segura antes do fechamento físico da porta quando o
perfil adotado assim exigir. `CLX` também a encerra no pinpad, embora não feche
a porta física. Falha de encerramento não pode ser ocultada pelo fechamento da
porta; ainda assim, `KSEC`, IV e outros buffers temporários devem ser limpos e
a fachada deve retornar a estado consistente.

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
- [ ] CLO encerra a sessão segura e deixa a porta em estado consistente; CLX é
  comprovado como comando visual que também encerra a sessão segura no pinpad,
  sem fechar a porta física.
- [ ] Cancelamento, timeout, NAK e falha criptográfica são erros distinguíveis.
- [ ] `go test -race ./...` não encontra corrida ou vazamento.
- [ ] Evidências sanitizadas são registradas em `validation.md`.

## Dependências
`spec-command-opn.md`, `spec-command-clo.md`, `spec-infra-serial-cancel.md` e
`spec-logging.md`.

## Referências
Manual ABECS v2.12, seções de OPN seguro e comunicação protegida; funções equivalentes identificadas nos fontes Java/JNI/C. A revisão formal deve confirmar se a versão do manual usada pelo dispositivo é compatível.

# SPEC: 066-lib-pinpad-abecs-go — Comando CLX e encerramento seguro

## Status
`RASCUNHO`

## Objetivo
Documentar o encerramento da sessão segura ABECS, caso o perfil do pinpad e o manual aplicável confirmem o uso de `CLX`. Esta SPEC impede que `CLX` seja confundido com o fechamento físico da porta ou com `CLO` em claro.

## Escopo
- Pacotes: `internal/domain/command`, `internal/domain/protocol`, `internal/application/service` e segurança.
- Definir payload, sequência, resposta e estado seguro somente após confirmação documental.
- Limpar chaves, IVs e material temporário da memória quando tecnicamente possível.
- Não registrar KSEC, chaves, IV, RSA ou pacotes cifrados em claro.

## Dependências
Depende da SPEC de comunicação segura OPN/RSA/AES. Não implementar `CLX` com suposições sobre criptografia.

## Critérios de aceite
- [ ] Manual e legado comprovam o formato do comando.
- [ ] Sessão segura real é encerrada no pinpad físico.
- [ ] Repetição é idempotente ou retorna erro documentado.
- [ ] Falha de CLX não deixa o serviço fingir que a sessão segura continua ativa.
- [ ] Logs permanecem redigidos.

## Referências
Manual ABECS v2.12, seções de comunicação segura; `spec-command-opn.md`, `spec-command-clo.md` e `spec-logging.md`.

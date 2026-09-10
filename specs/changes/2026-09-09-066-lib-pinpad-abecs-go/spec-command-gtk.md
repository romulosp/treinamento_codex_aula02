# SPEC: 066-lib-pinpad-abecs-go — Comando GTK

## Status
`RASCUNHO`

## Objetivo
Definir a obtenção das trilhas completas e dos KSNs relacionados, quando permitidos pelo perfil ABECS e pelo legado convertido.

## Contrato
- GTK é separado de GCX; a resposta não deve ser forçada para `GCXResponse`.
- Campos completos de trilha, PAN e KSN são dados sensíveis e devem ser protegidos no modelo, no retorno e no logging.
- O comando só pode ser exposto por método tipado e autorizado; não criar `SendRawCommand`.
- Definir no design final se os dados são devolvidos em memória, transformados ou bloqueados por política.

## Segurança
Nunca registrar trilhas, KSN, PAN, PIN block ou chaves em texto, hexadecimal, erro ou métrica. Redaction integral deve ocorrer no tracer.

## Critérios de aceite
- [ ] Manual e legado comprovam payload e tags de GTK.
- [ ] Resposta real é parseada sem atribuir seus campos a GCX.
- [ ] Dados sensíveis não aparecem em logs capturados.
- [ ] Timeout, cancelamento, NAK e status são distinguíveis.
- [ ] Validação usa pinpad físico e cartão de laboratório.

## Referências
`spec-command-gcx.md`, `spec-logging.md`; manual ABECS v2.12, seção de GTK.

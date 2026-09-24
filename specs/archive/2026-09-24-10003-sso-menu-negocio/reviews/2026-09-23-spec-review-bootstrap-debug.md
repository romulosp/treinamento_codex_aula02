# Revisão da SPEC — bootstrap debug do plugin de login

## Resultado

`SPEC_APROVADA`

## Contexto

O teste manual em `Medium Tablet API 35` demonstrou que o Run padrão do Android
Studio instala apenas `app-debug.apk`. Como `plugin-login-debug.apk` permanecia
no computador, o comportamento observável era a espera indefinida pelo plugin,
apesar de ambos os módulos compilarem.

## Decisão

| ID | Item | Resultado |
| --- | --- | --- |
| `REV-BOOT-001` | O problema e o comportamento esperado estão verificáveis em RF-07 | Conforme |
| `REV-BOOT-002` | O bootstrap é exclusivo de debug e não altera a distribuição release | Conforme |
| `REV-BOOT-003` | O plugin incorporado continua atravessando todas as validações do microkernel | Conforme |
| `REV-BOOT-004` | O host não ganha dependência de classes de implementação do plugin | Conforme |
| `REV-BOOT-005` | A execução pelo botão Run passa a ser critério de aceite explícito | Conforme |

## Aprovação

O solicitante pediu explicitamente a atualização das SPECs e a implementação
para que a abertura do aplicativo entre na tela de login. A extensão está
`SPEC_APROVADA` para implementação.

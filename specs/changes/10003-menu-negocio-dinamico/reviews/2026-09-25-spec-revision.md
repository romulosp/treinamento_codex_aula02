# REV-002 — Correção de execução integrada do menu de negócio

## Decisão

`SPEC_APROVADA`

## Achados e correções

| ID | Situação | Correção aprovada |
| --- | --- | --- |
| REV-002 | O design afirmava que o inbox não era lido, embora o host precisasse receber APKs de negócio externos e assets de desenvolvimento. | O texto passou a exigir que todo candidato atravesse staging, quarentena, validação e promoção antes de alcançar o classloader. |
| REV-003 | O requisito de timeout não tinha implementação nem necessidade no fluxo solicitado. | A SPEC passou a exigir o mecanismo efetivamente verificável de geração/cancelamento e descarte de publicação tardia. |
| REV-004 | Faltava evidência de que o caminho vinha do plugin e não do host. | O teste instrumentado passou a verificar `Outros Serviços > Saque Cartão` a partir do APK real. |

## Conclusão

A revisão preserva o contrato de segurança e a topologia modular, elimina a rota direta de plugin e mantém a implementação apta para a fase de revisão de implementação.

# Revisão da SPEC — inicialização Quarkus em modo dev

## Resultado

`SPEC_APROVADA`

## Evidência da divergência

O launcher carregou as chaves externas e chamou `quarkus:dev`, mas o Maven
encerrou em seguida com o aviso de módulo de suporte. O `pom.xml` gerado não
reutilizou a execução `<goal>build</goal>` já definida pelo template canônico e
documentada em `specs/system/README.md`.

## Decisão

| ID | Item | Resultado |
| --- | --- | --- |
| `REV-DEV-001` | A correção restaura configuração canônica preexistente | Conforme |
| `REV-DEV-002` | Endpoints, pacotes e dependências de negócio não mudam | Conforme |
| `REV-DEV-003` | O critério exige processo ativo e resposta HTTP real | Conforme |
| `REV-DEV-004` | O warning original passa a ser explicitamente proibido | Conforme |
| `REV-DEV-005` | A API usa 8180 porque o pgAdmin já publica 8080 no ambiente local | Conforme |

O solicitante autorizou a correção e a atualização das SPECs. A extensão está
`SPEC_APROVADA`.

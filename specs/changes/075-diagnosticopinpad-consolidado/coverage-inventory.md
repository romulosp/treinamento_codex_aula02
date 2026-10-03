# Inventário de cobertura — 075

Medição em 2026-10-03, JaCoCo 0.8.14 via AGP 9.4.1, tarefa
`createDebugUnitTestCoverageReport`. Código de saída 0; 27 testes JVM passaram.
Relatórios XML/HTML estão em `app/build/reports/coverage/test/debug/`.

## Escopo JVM

| Unidade | Linhas cobertas / total | Testes |
|---|---:|---|
| DiagnosticViewModel.kt | 267 / 278 | estado, erros, endpoint, Cancel, Close, exit, onCleared |
| Catalog.kt | 81 / 81 | catálogo, disponibilidade e formatos |
| DiagnosticFailure.kt | 25 / 25 | classificação e sanitização |
| EndpointConfig (classe) | 12 / 12 | endpoint/limites |
| Soma desse escopo | 385 / 396 = 97,22% | 27 testes na suíte JVM |

O ViewModel isolado tem 96,04% de cobertura de linhas. Esses números são de
linhas, não de branches, e não substituem os cenários de integração.

## Módulo completo e partes de plataforma

O relatório JVM completo mantém todos os arquivos: **388 / 736 linhas = 52,72%**.
Nenhuma exclusão foi aplicada à configuração do relatório para elevar esse
percentual. O escopo acima é uma leitura adicional explicitamente delimitada.

DiagnosticScreen, MainActivity e DiagnosticApplication dependem de Android/
Compose/lifecycle; JsonLineLogger depende de Context/armazenamento privado;
GoMobileRepository depende do JNI/AAR. Essas partes continuam no denominador
do relatório completo e precisam de cobertura instrumentada própria.

Os oito testes instrumentados executados nesta retomada cobrem UI, recriação,
cancelamento, saída e binding scripted. Testes instrumentados aprovados não
fornecem, por si sós, percentual JaCoCo. A cobertura integrada Android/JNI
continua pendente; não se declara atingido o mínimo global Kotlin.

## Configuração e fonte técnica

`enableUnitTestCoverage = true` foi habilitado somente em debug, usando o
mecanismo oficial do AGP, sem plugin adicional ou atualização da toolchain.
Referência: [relatórios de cobertura Android](https://developer.android.com/studio/test/coverage-report).

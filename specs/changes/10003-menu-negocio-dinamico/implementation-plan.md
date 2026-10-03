# Plano técnico preparatório: 10003-menu-negocio-dinamico

## Status

`EXECUTADO — SNAPSHOT_PRE_IMPLEMENTACAO`

Este plano registra a estratégia anterior à implementação. O estado operacional
da Change está em `tasks.md`, na revisão de implementação e em `validation.md`.

Após aprovação, a implementação foi organizada em slices: (1) contrato e testes
de árvore puros em JVM; (2) discovery ZIP/DEX e integração do manager; (3) UI,
lifecycle e instrumentação. O risco principal era a compatibilidade binária da
API; a decisão preservou a major 1 e a versão 1.1.0 nesta Change, mantendo o
construtor de quatro campos. A validação do manifesto ocorre antes de instanciar
a classe. Os gates foram o validador estrutural, Gradle test/lint, build e teste
em emulador.

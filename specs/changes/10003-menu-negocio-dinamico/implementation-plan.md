# Plano técnico preparatório: 10003-menu-negocio-dinamico

## Status

`PRONTO_PARA_IMPLEMENTACAO`

Após aprovação, a implementação será feita em slices: (1) contrato e testes de árvore puros em JVM; (2) discovery ZIP/DEX e integração do manager; (3) UI, lifecycle e instrumentação. O risco principal é a compatibilidade binária da API; por isso a major será elevada e a validação do manifesto ocorrerá antes de instanciar a classe. Os gates serão o validador estrutural, Gradle test/lint, build e teste em emulador.

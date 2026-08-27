# Arquitetura do backend Java

O backend usa Quarkus e organização por responsabilidade:

- `api`: recursos REST, contratos de entrada e saída.
- `application`: casos de uso e orquestração.
- `domain`: regras e modelos de domínio sem dependência de HTTP.
- `infrastructure`: persistência, clientes externos e configurações técnicas.

Recursos REST delegam trabalho à camada de aplicação. Entidades JPA não são contratos públicos da API.

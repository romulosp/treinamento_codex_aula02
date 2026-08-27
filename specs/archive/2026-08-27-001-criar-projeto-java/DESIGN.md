# Design: 001-criar-projeto-java

## Contexto

Esta é a mudança inicial do laboratório. Os parâmetros fornecidos definem Maven, Quarkus 3.2.10.Final, integração DB2 e ferramentas de qualidade. A propriedade Java 11 original conflita com a linha Quarkus 3; o ADR-001 fixa Java 17 para obter uma combinação suportada.

## Decisões

| Assunto | Decisão |
| --- | --- |
| Módulo | `apps/backend` é o único módulo executável inicial. |
| Maven | `br.com.romulopenha:nome_da_api_gerada:1.0.0.1`. |
| Java | Release 17, conforme ADR-001. |
| Framework | Quarkus 3.2.10.Final e extensões RESTEasy clássicas solicitadas. |
| Persistência | Panache, Agroal e DB2 em produção; H2 apenas no perfil de teste. |
| Configuração | HTTP em 8080, testes em 8083, Swagger incluído no artefato e OpenAPI em `/swagger_nome_api_projeto.json`. |
| Repositório Maven | `.mvn/maven.config` seleciona `.mvn/settings.xml`, que espelha todos os repositórios no `NEXUS_INTERNO`. |
| Segurança de configuração | Os exemplos `XXXXXX` foram convertidos em variáveis de ambiente `DB2_JDBC_URL`, `DB2_USERNAME` e `DB2_PASSWORD`. |
| Driver licenciado | `db2jcc_license_cisuz` é opt-in no perfil `db2-license`, pois pode não estar disponível em repositórios públicos. |
| Qualidade | Surefire, JaCoCo 0.8.8 e propriedades Sonar configurados no POM. A versão 0.8.6 recebida não gera relatórios para classes Java 17. |

## Estrutura

```text
apps/backend/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/br/com/romulopenha/nomedaapigerada/
    │   └── resources/application.properties
    └── test/java/br/com/romulopenha/nomedaapigerada/
```

## Fluxo de configuração

```text
Variáveis de ambiente DB2 → application.properties → Agroal → Hibernate Panache → DB2
Perfil de teste → H2 em memória → @QuarkusTest
```

O perfil de teste reduz `initial-size` e `min-size` para não exceder o `max-size=13` informado para H2. Os valores sem sufixo de unidade recebidos foram normalizados para `1m` e `5s`, formato de duração do Quarkus. A opção `AUTO_SERVER=true` foi removida, pois é incompatível com a URL H2 em memória e provoca falhas de conexão em segundo plano.

## Alternativas rejeitadas

- Manter Java 11 com Quarkus 3: incompatível.
- Embutir usuário, senha ou URL DB2: inseguro e não portável.
- Criar endpoint de negócio: fora do escopo.

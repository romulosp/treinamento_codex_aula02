# SPEC: 025-provedor-fila-mq

## Status
`SPEC_APROVADA`

## Referências e dependências

- `proposal.md`
- `DESIGN.md`
- `tasks.md`
- `specs/shared/architecture/backend-java.md`
- `specs/shared/process/evidence-conventions.md`
- [RabbitMQ Connector Reference do Quarkus](https://quarkus.io/guides/rabbitmq-reference)
- [Apache Kafka Reference do Quarkus](https://quarkus.io/guides/kafka/)
- [Redis Extension Reference do Quarkus](https://quarkus.io/guides/redis-reference)
- [IBM MQ: desenvolvimento Java com repositório Maven](https://www.ibm.com/docs/en/ibm-mq/9.2.0?topic=applications-java-application-development-using-maven-repository)

## Requisitos funcionais

### RF-001 — Seleção de provedor

1. O contrato de geração/configuração deve aceitar o campo `filaMq`.
2. Os únicos valores válidos são `RABBITMQ`, `KAFKA`, `IBM_MQ` e `REDIS`, sem diferenciação de maiúsculas e minúsculas após normalização.
3. Quando `filaMq` estiver ausente ou nulo, o valor normalizado deve ser `RABBITMQ`.
4. Um valor diferente dos quatro permitidos deve ser rejeitado antes da gravação de qualquer artefato.
5. A seleção é uma decisão de build/configuração do projeto. Não é permitido alternar o provedor em runtime somente por variável de ambiente.

### RF-002 — Dependência Maven exclusiva

O `pom.xml` do projeto deve conter a dependência produtiva correspondente ao valor normalizado, conforme a matriz abaixo. A escolha altera efetivamente as dependências do `pom.xml`; não é permitido adicionar as quatro dependências produtivas simultaneamente.

| `filaMq` | Dependência Maven produtiva | Uso previsto | Observações |
| --- | --- | --- | --- |
| `RABBITMQ` | `io.quarkus:quarkus-messaging-rabbitmq` | SmallRye Reactive Messaging sobre AMQP 0.9.1 | É a opção padrão; o conector deve ser configurado com `smallrye-rabbitmq`. Em versões de Quarkus que ainda não ofereçam o novo nome, registrar explicitamente a compatibilidade aprovada antes de implementar. |
| `KAFKA` | `io.quarkus:quarkus-messaging-kafka` | SmallRye Reactive Messaging sobre Kafka | O cliente Kafka é transitivo da extensão; tópico, grupo e brokers são configuração do canal. |
| `IBM_MQ` | `com.ibm.mq:com.ibm.mq.jakarta.client:${ibm.mq.version}` | Cliente Java/Jakarta Messaging do IBM MQ | `ibm.mq.version` deve ser uma versão aprovada pela organização e gerenciada no `pom.xml` ou BOM corporativo; não usar JAR manual sem decisão registrada. |
| `REDIS` | `org.redisson:redisson:${redisson.version}` | `RQueue`/primitiva de fila do Redisson | A dependência é para fila; não habilitar nem documentar cache Redisson nesta change. Streams Redis podem substituir `RQueue` apenas mediante decisão técnica específica. |

Dependências comuns do projeto, como REST, CDI, validação e testes, não fazem parte desta matriz. A implementação deve verificar tanto a presença do artefato selecionado quanto a ausência dos outros três artefatos produtivos.

### RF-003 — Convenções comuns de configuração

1. As propriedades devem referenciar variáveis por `${env.NOME_DA_VARIAVEL}`.
2. Valores reais de host, senha, token, certificados e URLs não podem ser gravados em `application.properties`, código, testes versionados ou logs.
3. Nomes de filas, tópicos e grupos são configuração do ambiente e devem ser distintos por ambiente quando a operação exigir isolamento.
4. TLS deve ser habilitado e validado para ambientes que o exigirem; não desabilitar verificação de certificado como solução permanente.
5. O projeto deve documentar timeout, reconexão, confirmação/ack e política de erro do cliente escolhido quando esses parâmetros forem relevantes para o contrato de consumo.

### RF-004 — RabbitMQ

RabbitMQ é o padrão e deve usar mensageria tradicional com roteamento explícito.

Variáveis:

| Variável | Obrigatória | Descrição |
| --- | --- | --- |
| `RABBITMQ_HOST` | Sim | Host ou endereço do broker. |
| `RABBITMQ_PORT` | Não | Porta AMQP; padrão recomendado `5672`, ou `5671` com TLS. |
| `RABBITMQ_USERNAME` | Sim | Usuário técnico do broker. |
| `RABBITMQ_PASSWORD` | Sim | Senha do usuário técnico; segredo. |
| `RABBITMQ_VHOST` | Não | Virtual host; padrão `/` quando permitido pelo ambiente. |
| `RABBITMQ_QUEUE` | Sim | Fila de entrada consumida pela aplicação. |
| `RABBITMQ_EXCHANGE` | Sim para publicação | Exchange de saída. |
| `RABBITMQ_ROUTING_KEY` | Sim para publicação | Chave usada no roteamento da mensagem. |
| `RABBITMQ_TLS_ENABLED` | Não | Indica uso de TLS conforme suporte da versão do conector. |

Exemplo sem valores reais:

```properties
mp.messaging.incoming.eventos.connector=smallrye-rabbitmq
mp.messaging.incoming.eventos.queue.name=${env.RABBITMQ_QUEUE}
mp.messaging.outgoing.eventos-publicados.connector=smallrye-rabbitmq
mp.messaging.outgoing.eventos-publicados.exchange.name=${env.RABBITMQ_EXCHANGE}
mp.messaging.outgoing.eventos-publicados.routing-keys=${env.RABBITMQ_ROUTING_KEY}
mp.messaging.connector.smallrye-rabbitmq.rabbitmq-host=${env.RABBITMQ_HOST}
mp.messaging.connector.smallrye-rabbitmq.rabbitmq-port=${env.RABBITMQ_PORT:5672}
mp.messaging.connector.smallrye-rabbitmq.rabbitmq-username=${env.RABBITMQ_USERNAME}
mp.messaging.connector.smallrye-rabbitmq.rabbitmq-password=${env.RABBITMQ_PASSWORD}
mp.messaging.connector.smallrye-rabbitmq.rabbitmq-virtual-host=${env.RABBITMQ_VHOST:/}
```

O nome exato das propriedades específicas de fila/exchange deve ser conferido contra a versão do conector fixada no `pom.xml`; a matriz de dependência e a configuração não podem ser atualizadas de forma independente.

### RF-005 — Apache Kafka

Kafka deve ser usado para streaming de alto volume, retenção histórica persistente e integração com Big Data.

Variáveis:

| Variável | Obrigatória | Descrição |
| --- | --- | --- |
| `KAFKA_BOOTSTRAP_SERVERS` | Sim | Lista de brokers `host:porta`, separada por vírgula. |
| `KAFKA_TOPIC` | Sim | Tópico de entrada/saída conforme o canal. |
| `KAFKA_CONSUMER_GROUP` | Sim para consumidor | Grupo de consumidores; determina a distribuição e o offset. |
| `KAFKA_SECURITY_PROTOCOL` | Não | `PLAINTEXT`, `SSL`, `SASL_SSL` ou valor aprovado pelo ambiente; produção deve usar o padrão seguro aplicável. |
| `KAFKA_SASL_MECHANISM` | Condicional | Mecanismo SASL quando exigido pelo protocolo. |
| `KAFKA_SASL_USERNAME` | Condicional | Usuário SASL; segredo operacional associado. |
| `KAFKA_SASL_PASSWORD` | Condicional | Senha SASL; segredo. |
| `KAFKA_AUTO_OFFSET_RESET` | Não | Política inicial de offset, por exemplo `earliest` ou `latest`, definida pela operação. |

Exemplo:

```properties
mp.messaging.incoming.eventos.connector=smallrye-kafka
mp.messaging.incoming.eventos.bootstrap.servers=${env.KAFKA_BOOTSTRAP_SERVERS}
mp.messaging.incoming.eventos.topic=${env.KAFKA_TOPIC}
mp.messaging.incoming.eventos.group.id=${env.KAFKA_CONSUMER_GROUP}
mp.messaging.incoming.eventos.auto.offset.reset=${env.KAFKA_AUTO_OFFSET_RESET:latest}
mp.messaging.incoming.eventos.security.protocol=${env.KAFKA_SECURITY_PROTOCOL:SASL_SSL}
mp.messaging.incoming.eventos.sasl.mechanism=${env.KAFKA_SASL_MECHANISM:PLAIN}
mp.messaging.incoming.eventos.sasl.jaas.config=${env.KAFKA_SASL_JAAS_CONFIG}
mp.messaging.outgoing.eventos-publicados.connector=smallrye-kafka
mp.messaging.outgoing.eventos-publicados.bootstrap.servers=${env.KAFKA_BOOTSTRAP_SERVERS}
mp.messaging.outgoing.eventos-publicados.topic=${env.KAFKA_TOPIC}
```

`KAFKA_SASL_JAAS_CONFIG`, quando adotado, deve ser fornecido apenas pelo ambiente ou por mecanismo seguro equivalente, nunca como valor literal versionado.

### RF-006 — IBM MQ

IBM MQ deve ser usado em ambientes corporativos que exigem máxima segurança, governança e transacionalidade, como integrações bancárias. A configuração deve ser compatível com o modelo de conexão escolhido (client mode, bindings, JMS/Jakarta Messaging e transação).

Variáveis:

| Variável | Obrigatória | Descrição |
| --- | --- | --- |
| `IBM_MQ_HOST` | Sim em client mode | Host do queue manager. |
| `IBM_MQ_PORT` | Sim em client mode | Porta do listener, usualmente `1414` quando aplicável. |
| `IBM_MQ_QUEUE_MANAGER` | Sim | Nome do queue manager. |
| `IBM_MQ_CHANNEL` | Sim em client mode | Canal de conexão do cliente. |
| `IBM_MQ_QUEUE` | Sim | Nome da fila IBM MQ. |
| `IBM_MQ_USERNAME` | Condicional | Usuário autenticado no queue manager. |
| `IBM_MQ_PASSWORD` | Condicional | Senha; segredo. |
| `IBM_MQ_CIPHER_SUITE` | Condicional | Cipher suite/TLS aprovado para o canal. |
| `IBM_MQ_APPLICATION_NAME` | Não | Nome da aplicação para observabilidade e governança. |

Exemplo de propriedades da aplicação:

```properties
app.queue.ibm-mq.host=${env.IBM_MQ_HOST}
app.queue.ibm-mq.port=${env.IBM_MQ_PORT:1414}
app.queue.ibm-mq.queue-manager=${env.IBM_MQ_QUEUE_MANAGER}
app.queue.ibm-mq.channel=${env.IBM_MQ_CHANNEL}
app.queue.ibm-mq.queue=${env.IBM_MQ_QUEUE}
app.queue.ibm-mq.username=${env.IBM_MQ_USERNAME}
app.queue.ibm-mq.password=${env.IBM_MQ_PASSWORD}
app.queue.ibm-mq.cipher-suite=${env.IBM_MQ_CIPHER_SUITE}
app.queue.ibm-mq.application-name=${env.IBM_MQ_APPLICATION_NAME:aplicacao-fila-mq}
```

O código deve construir o cliente/JMS usando essas propriedades e fechar conexões, sessões e consumidores no ciclo de vida da aplicação. A necessidade de transação local/XA, confirmação e rollback deve ser explicitada pela aplicação consumidora; a simples presença do cliente não garante transacionalidade.

### RF-007 — Redis como fila exclusiva

Redis pode ser escolhido para filas leves e ultrarrápidas de tarefas de segundo plano. A implementação deve usar `RQueue` do Redisson ou Redis Streams com grupos de consumidores, conforme decisão registrada do projeto.

Variáveis:

| Variável | Obrigatória | Descrição |
| --- | --- | --- |
| `REDIS_HOST` | Sim | Host do Redis. |
| `REDIS_PORT` | Não | Porta; padrão `6379`, ou a porta TLS do ambiente. |
| `REDIS_USERNAME` | Condicional | Usuário ACL, quando habilitado. |
| `REDIS_PASSWORD` | Condicional | Senha/credencial ACL; segredo. |
| `REDIS_DATABASE` | Não | Índice lógico, quando suportado pelo modo de conexão. |
| `REDIS_SSL_ENABLED` | Não | Habilita TLS conforme o cliente. |
| `REDIS_QUEUE_NAME` | Sim | Nome da fila ou stream. |
| `REDIS_CONSUMER_GROUP` | Obrigatória para Streams | Grupo de consumidores do stream. |

Exemplo para configuração própria do cliente Redisson:

```properties
app.queue.redis.host=${env.REDIS_HOST}
app.queue.redis.port=${env.REDIS_PORT:6379}
app.queue.redis.username=${env.REDIS_USERNAME}
app.queue.redis.password=${env.REDIS_PASSWORD}
app.queue.redis.database=${env.REDIS_DATABASE:0}
app.queue.redis.ssl-enabled=${env.REDIS_SSL_ENABLED:false}
app.queue.redis.queue-name=${env.REDIS_QUEUE_NAME}
app.queue.redis.consumer-group=${env.REDIS_CONSUMER_GROUP}
app.queue.redis.mode=${env.REDIS_QUEUE_MODE:RQUEUE}
```

Quando `REDIS_QUEUE_MODE=RQUEUE`, usar `RQueue`; quando `REDIS_QUEUE_MODE=STREAMS`, usar Streams e o grupo indicado. Não criar `RMapCache`, `RLocalCachedMap`, chaves de cache ou configuração de TTL de cache como parte desta integração.

> Nota obrigatória: quando Redis for escolhido para fila, ele não deve ser usado para cache neste escopo. Cache, suas chaves, políticas de expiração e observabilidade pertencem a outro contexto e outra SPEC.

### RF-008 — Configuração e segurança

- O projeto deve falhar com mensagem sanitizada quando faltar variável obrigatória ou quando a configuração selecionada não puder ser validada.
- Logs não podem imprimir senhas, tokens, strings SASL, URIs completas ou propriedades que contenham segredos.
- TLS, certificados, truststores e keystores devem ser fornecidos por segredo/volume/configuração segura do ambiente; caminhos e nomes podem ser documentados sem conteúdo sensível.
- O consumidor deve usar confirmação (`ack`) compatível com o cliente e impedir perda silenciosa em erro. A política de retry/DLQ é responsabilidade da aplicação e deve ser documentada quando implementada.

## Requisitos não funcionais

1. **Reprodutibilidade:** a seleção de provedor, dependências e exemplos devem ser reproduzíveis com Java 17 e Quarkus/Maven do projeto.
2. **Compatibilidade:** a versão do conector RabbitMQ/Kafka, cliente IBM MQ e Redisson deve ser fixada ou herdada de gerenciamento de dependências; exemplos não podem misturar propriedades de versões incompatíveis.
3. **Segurança:** nenhum segredo, host produtivo ou certificado real deve ser versionado.
4. **Isolamento:** o artefato gerado deve possuir somente uma dependência produtiva de fila e somente o bloco de propriedades do provedor escolhido.
5. **Observabilidade:** a configuração deve permitir identificar provedor, destino e grupo sem registrar credenciais.
6. **Testabilidade:** os testes automatizados não podem depender de broker externo; devem verificar a matriz e, quando necessário, usar doubles/in-memory específicos do cliente.

## Regras de negócio

1. RabbitMQ é o provedor default quando o seletor não é informado.
2. A seleção do provedor é exclusiva e determinada antes do build.
3. A configuração de destino deve ser compatível com o modelo do provedor: fila/exchange/routing key em RabbitMQ, tópico/grupo/offset em Kafka, queue manager/channel/queue em IBM MQ e fila/stream/grupo em Redis.
4. Redis, se selecionado, representa somente uma fila de tarefas; não é uma decisão de cache.

## Considerações de design e adequação

| Provedor | Mais adequado quando | Trade-offs principais |
| --- | --- | --- |
| RabbitMQ | Há mensageria tradicional, alta confiabilidade, confirmação explícita e roteamento detalhado por exchange, fila e routing key. | Exige modelagem de topologia e operação do broker; retenção histórica não é o foco principal. |
| Apache Kafka | Há streaming de alto volume, retenção persistente, replay de dados históricos, pipelines analíticos e Big Data. | Requer governança de tópicos, partições, grupos e retenção; não deve ser tratado apenas como uma fila simples. |
| IBM MQ | O ambiente corporativo exige máxima segurança, governança e transacionalidade, especialmente em bancos e integrações legadas críticas. | Licenciamento, operação e configuração são mais especializados; versionamento do cliente e TLS precisam de controle corporativo. |
| Redis | Há filas leves e ultrarrápidas para tarefas de segundo plano, com operação simples e baixa latência. | Persistência, durabilidade, replay e semântica de entrega dependem da primitiva escolhida e da configuração; não inclui cache neste escopo. |

## Cenários e critérios de aceite

- [ ] **CA-001:** entrada sem `filaMq` gera uma configuração RabbitMQ e inclui `io.quarkus:quarkus-messaging-rabbitmq`.
- [ ] **CA-002:** entrada `RABBITMQ` gera somente o cliente RabbitMQ, propriedades de host/porta/credenciais/vhost e destino de fila/exchange/routing key.
- [ ] **CA-003:** entrada `KAFKA` gera somente `io.quarkus:quarkus-messaging-kafka`, propriedades de brokers, tópico, grupo e segurança condicional.
- [ ] **CA-004:** entrada `IBM_MQ` gera somente `com.ibm.mq:com.ibm.mq.jakarta.client` com versão gerenciada e propriedades de host, porta, queue manager, canal, fila e credenciais/TLS.
- [ ] **CA-005:** entrada `REDIS` gera somente `org.redisson:redisson`, propriedades de conexão e nome da fila/stream; a documentação não contém cache Redis.
- [ ] **CA-006:** valor inválido é rejeitado antes de criar artefato parcial.
- [ ] **CA-007:** nenhum exemplo contém credencial, host produtivo, token, URI real ou certificado real.
- [ ] **CA-008:** matriz, `pom.xml` e `application.properties` não permitem coexistência de mais de um cliente produtivo.
- [ ] **CA-009:** documentação explica a adequação dos quatro provedores e explicita que a seleção altera o `pom.xml`.
- [ ] **CA-010:** verificação automatizada da change conclui com sucesso e sem dependência de brokers externos.

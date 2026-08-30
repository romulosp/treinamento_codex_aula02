# Provedor de fila de mensagens

## Estado vigente

Projetos que dependem de fila de mensagens selecionam um único provedor em build time pelo campo `filaMq`. Os valores válidos são `RABBITMQ`, `KAFKA`, `IBM_MQ` e `REDIS`; quando o campo está ausente ou nulo, o padrão é `RABBITMQ`. Um valor inválido deve ser rejeitado antes da gravação de artefatos.

A seleção altera o `pom.xml` e deixa somente uma dependência produtiva de fila:

| Provedor | Dependência |
| --- | --- |
| RabbitMQ | `io.quarkus:quarkus-messaging-rabbitmq` |
| Apache Kafka | `io.quarkus:quarkus-messaging-kafka` |
| IBM MQ | `com.ibm.mq:com.ibm.mq.jakarta.client:${ibm.mq.version}` |
| Redis | `org.redisson:redisson:${redisson.version}` |

As propriedades de conexão, credenciais e destinos são fornecidas por variáveis de ambiente referenciadas em `application.properties`. Nenhum segredo, host real, token, certificado ou URI produtiva deve ser versionado.

## Adequação

- RabbitMQ é a escolha padrão para mensageria tradicional, alta confiabilidade e roteamento detalhado por exchange, fila e routing key.
- Kafka é indicado para streaming de alto volume, retenção persistente, replay histórico e integração com Big Data.
- IBM MQ é indicado para ambientes corporativos que exigem máxima segurança, governança e transacionalidade, como bancos.
- Redis é indicado para filas leves e ultrarrápidas de tarefas de segundo plano, usando `RQueue` ou Streams.

Quando Redis for escolhido, ele será usado exclusivamente como fila. Cache Redis, chaves de cache, TTL e políticas de invalidação pertencem a outro contexto e não fazem parte desta configuração.

## Fonte detalhada

Consulte a change arquivada `2026-08-30-025-provedor-fila-mq` para a matriz completa, variáveis de ambiente, exemplos de `application.properties`, critérios de aceite e evidências de validação.

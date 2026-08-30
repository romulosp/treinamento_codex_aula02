# Design: 025-provedor-fila-mq

## Contexto

O projeto precisa ser capaz de adotar uma fila de mensagens sem carregar clientes de brokers que não serão utilizados. A escolha impacta o build, o modelo de configuração e as propriedades específicas do destino. O laboratório já utiliza uma matriz declarativa para selecionar dependências de infraestrutura; esta change aplica o mesmo princípio ao provedor de fila.

## Referências

- `proposal.md`
- `spec.md`
- `specs/shared/architecture/backend-java.md`
- `specs/archive/2026-08-27-016-renderizar-configuracao-banco-selecionado/DESIGN.md`
- Documentação oficial do Quarkus para RabbitMQ, Kafka e Redis.
- Documentação oficial do IBM MQ para o cliente Java/Jakarta Messaging.

## Decisões

1. **Seleção em build time:** `filaMq` é normalizado antes da renderização. Ausência ou nulo significa `RABBITMQ`; valor inválido interrompe a geração sem artefato parcial.
2. **Catálogo único:** uma entrada por provedor concentra identificador, dependência Maven, versão quando necessária, estilo de integração, nomes de propriedades e variáveis. `pom.xml` e `application.properties` são renderizados a partir da mesma entrada.
3. **Uma dependência produtiva:** somente o cliente do provedor escolhido aparece no `pom.xml`. Dependências de teste, quando existirem, ficam separadas e não representam outro broker produtivo.
4. **Configuração por ambiente:** propriedades versionadas referenciam `${env.*}`. Credenciais, hosts reais, certificados e tokens permanecem fora do repositório.
5. **Abstração mínima:** a camada de aplicação pode depender de uma porta de envio/consumo, mas não deve esconder diferenças essenciais de ack, offset, transação, ordenação ou replay. Essas diferenças devem ser declaradas por adaptador.
6. **Redis isolado de cache:** o adaptador Redis usa `RQueue` ou Streams exclusivamente como fila. Qualquer cache Redis exige change própria e não pode compartilhar as propriedades ou componentes desta integração.
7. **IBM MQ explícito:** o adaptador IBM MQ usa o cliente Jakarta aprovado, com versão gerenciada. A necessidade de transação é uma decisão da aplicação que usa a fila, não uma propriedade implícita da dependência.

## Matriz de renderização

| Entrada normalizada | Dependência produtiva | Destino principal | Configuração central |
| --- | --- | --- | --- |
| `RABBITMQ` | `io.quarkus:quarkus-messaging-rabbitmq` | fila + exchange + routing key | `mp.messaging.*` e propriedades do conector RabbitMQ |
| `KAFKA` | `io.quarkus:quarkus-messaging-kafka` | tópico + consumer group | `mp.messaging.*` do conector Kafka |
| `IBM_MQ` | `com.ibm.mq:com.ibm.mq.jakarta.client:${ibm.mq.version}` | queue manager + channel + queue | `app.queue.ibm-mq.*` consumido pelo adaptador JMS |
| `REDIS` | `org.redisson:redisson:${redisson.version}` | `RQueue` ou stream + group | `app.queue.redis.*` consumido pelo adaptador Redisson |

## Arquitetura e componentes

```text
Entrada de geração: filaMq (ausente = RABBITMQ)
                    |
                    v
       normalização + validação de enum
                    |
                    v
        catálogo único de provedores
             /                  \
            v                    v
       renderiza pom.xml    renderiza application.properties
            |                    |
            +---------+----------+
                      v
       projeto com um adaptador de fila
                      |
      +---------------+----------------+
      |               |                |
  RabbitMQ          Kafka          IBM MQ / Redis
```

Camadas esperadas no backend Java:

- `application`: porta e casos de uso de envio/consumo, sem detalhes de broker.
- `domain`: mensagem e regras mínimas de validação, sem dependência de cliente.
- `infrastructure`: adaptador do provedor selecionado e leitura segura das propriedades.
- `api`: somente se uma change posterior definir endpoints; esta change não cria API de negócio.

## Alternativas e consequências

- **Incluir todos os clientes e escolher por perfil em runtime:** rejeitado; aumenta o artefato, permite configuração incompatível e não atende ao requisito de a escolha alterar o `pom.xml`.
- **Escolher o broker apenas por variável de ambiente:** rejeitado; variáveis configuram conexão, mas não removem dependências incompatíveis do build.
- **Usar Kafka como implementação universal:** rejeitado; RabbitMQ/IBM MQ possuem roteamento e transação próprios, e Redis tem semântica de fila distinta.
- **Usar Redis também como cache por conveniência:** rejeitado; mistura responsabilidades e viola o limite explícito desta change.
- **Usar dependência IBM MQ sem versão gerenciada:** rejeitado; dificulta reprodutibilidade, conformidade e atualização de segurança.

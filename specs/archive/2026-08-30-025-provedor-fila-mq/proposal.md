# Proposta: 025-provedor-fila-mq

## Status
`SPEC_APROVADA`

## Responsável e data

- Responsável: equipe do projeto
- Data: 2026-08-30

## Referências

- `AGENTS.md`
- `specs/shared/process/workflow.md`
- `specs/shared/process/evidence-conventions.md`
- `specs/shared/architecture/backend-java.md`
- `specs/archive/2026-08-27-016-renderizar-configuracao-banco-selecionado/spec.md`
- [Documentação do conector RabbitMQ do Quarkus](https://quarkus.io/guides/rabbitmq-reference)
- [Documentação do conector Kafka do Quarkus](https://quarkus.io/guides/kafka/)
- [Documentação do cliente Redis do Quarkus](https://quarkus.io/guides/redis-reference)
- [Documentação oficial do IBM MQ para desenvolvimento Java com Maven](https://www.ibm.com/docs/en/ibm-mq/9.2.0?topic=applications-java-application-development-using-maven-repository)

## Problema e objetivo

Projetos gerados ou configurados pelo laboratório precisam de uma fila de mensagens, mas a escolha do broker ainda não possui contrato único para dependência Maven, propriedades de conexão, destinos e critérios de seleção. Isso pode produzir artefatos com clientes incompatíveis, credenciais expostas ou configuração de mais de um broker sem necessidade.

Definir uma especificação implementável para selecionar um único provedor de fila de mensagens, documentar sua dependência Maven e gerar a configuração necessária por variáveis de ambiente. RabbitMQ será o padrão quando nenhuma opção for informada.

## Escopo

- Definir o seletor `filaMq` com os valores `RABBITMQ`, `KAFKA`, `IBM_MQ` e `REDIS`.
- Definir `RABBITMQ` como valor padrão para entrada ausente ou nula.
- Definir uma matriz única que vincule provedor, dependência Maven, modo de acesso, destino e variáveis de ambiente.
- Documentar exemplos de `application.properties` para cada opção.
- Definir os cuidados de segurança para credenciais, TLS, logs e rotação de segredos.
- Explicar quando cada provedor é adequado e registrar que Redis será usado exclusivamente como fila, nunca como cache nesta change.
- Validar automaticamente a completude e a consistência da documentação entregue.

## Fora de escopo

- Implementar um consumidor ou produtor de negócio específico.
- Definir troca dinâmica de broker em uma instância já compilada.
- Usar mais de um provedor produtivo no mesmo artefato.
- Criar cluster, broker local, Docker Compose, provisionamento de infraestrutura ou topologia de produção.
- Implementar cache Redis, invalidação, TTL de cache ou compartilhamento de configuração com o contexto de cache.
- Definir semântica de negócio para retry, DLQ, ordenação ou exatamente uma vez além das recomendações da SPEC.

## Impactos e riscos

- Trocar o provedor altera a dependência do `pom.xml` e exige novo build; não é uma simples troca de variável de ambiente.
- RabbitMQ e IBM MQ usam semânticas e clientes diferentes de Kafka; uma abstração comum deve permanecer limitada ao contrato de envio/recebimento adotado pelo projeto.
- Dependências IBM MQ podem exigir versão aprovada e repositório corporativo; a versão não deve ser omitida nem obtida de arquivo JAR manual sem decisão de infraestrutura.
- Configuração incorreta de TLS ou credenciais pode impedir inicialização ou expor dados; valores reais não serão registrados.
- Redis oferece primitivas de fila, mas não substitui um cache neste escopo; misturar responsabilidades dificulta retenção, observabilidade e operação.

## Critérios para aprovação da SPEC

- Os quatro provedores estão descritos com dependência Maven, variáveis obrigatórias/opcionais, exemplo e justificativa de uso.
- A ausência do seletor resulta em RabbitMQ e uma opção explícita resulta em somente o provedor escolhido.
- A SPEC deixa inequívoco que a escolha altera o `pom.xml` e que não devem ser incluídas dependências produtivas dos demais provedores.
- Credenciais e URLs reais são tratados como segredos de ambiente e não aparecem em arquivos versionados.
- Redis está documentado somente como fila, sem configuração ou comportamento de cache.
- Todos os critérios de aceite podem ser verificados por inspeção documental ou teste automatizado da matriz.

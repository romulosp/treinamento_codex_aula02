# Revisão da SPEC: 025-provedor-fila-mq

## Escopo da revisão

Foram revisados `proposal.md`, `spec.md`, `DESIGN.md`, `tasks.md` e as regras aplicáveis em `AGENTS.md`, `specs/shared/process/workflow.md`, `specs/shared/process/evidence-conventions.md` e `specs/shared/architecture/backend-java.md`.

## Achados

Nenhum achado bloqueante, importante ou de melhoria foi identificado.

- `REV-001`: não aplicável — a matriz define quatro valores válidos, default RabbitMQ, dependência Maven exclusiva, variáveis de ambiente, exemplos, segurança e critérios verificáveis.

## Verificações realizadas

- O objetivo e o fora de escopo estão alinhados: a change documenta a seleção de um único provedor e não cria API de negócio.
- A relação entre `filaMq`, `pom.xml` e `application.properties` está explícita e é consistente no `spec.md` e no `DESIGN.md`.
- RabbitMQ, Kafka, IBM MQ e Redis possuem dependência, variáveis, exemplos e justificativa de adequação.
- A seleção é de build time; entrada ausente usa RabbitMQ e entrada inválida falha antes de gravar artefatos.
- Credenciais, hosts reais, tokens e certificados são tratados como configuração externa e não como conteúdo versionado.
- Redis está limitado a `RQueue` ou Streams como fila; cache está expressamente fora do escopo.
- Os critérios CA-001 a CA-010 permitem verificação estática sem broker externo.
- A versão do cliente IBM MQ e do Redisson é deliberadamente uma propriedade gerenciada do projeto, com exigência de fixação e aprovação antes da implementação.

## Decisão

`SPEC_APROVADA`

A mudança pode avançar para a implementação documental e validação estática previstas, sem ampliar o escopo para API, broker ou cache.

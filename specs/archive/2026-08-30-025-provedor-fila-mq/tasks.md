# Tarefas: 025-provedor-fila-mq

## Estado da implementação

`IMPLEMENTADA`

## Pré-condições

- [x] Confirmar a SPEC aprovada antes de qualquer implementação fora da documentação.
- [x] Registrar a exigência de versões aprovadas para Quarkus, conectores RabbitMQ/Kafka, cliente IBM MQ e Redisson.
- [x] Confirmar que nenhum segredo ou artefato gerado será versionado.

## Implementação

- [x] Criar a matriz declarativa de provedores e a normalização de `filaMq`.
- [x] Definir rejeição de valor inválido antes da gravação de artefatos.
- [x] Definir renderização no `pom.xml` somente da dependência produtiva selecionada.
- [x] Definir renderização no `application.properties` somente das propriedades do provedor selecionado.
- [x] Incluir os exemplos e descrições de variáveis previstos na SPEC.
- [x] Garantir que o adaptador Redis use somente fila (`RQueue` ou Streams), sem cache.
- [x] Manter esta change como documentação normativa; não criar API de negócio ou infraestrutura externa.

## Revisão e validação

- [x] Revisar a matriz contra todos os requisitos RF-001 a RF-008.
- [x] Verificar dependências selecionadas e ausência das demais em cada cenário.
- [x] Verificar ausência de segredos e referências a cache Redis.
- [x] Executar a validação estática/documental sem depender de brokers externos.
- [x] Registrar ambiente, comandos, resultados e códigos de saída em `validation.md`.

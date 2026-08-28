# Tarefas: 016-renderizar-configuracao-banco-selecionado

## Estado da implementação

`IMPLEMENTADA`

Em revisão da SPEC para registrar `DB2` como valor padrão de geração. O bloqueio anterior será reavaliado na implementação após a regeneração local autorizada.

## Pré-condições

- [x] Aprovar novamente proposta, SPEC, design e plano de tarefas revisados em revisão formal.
- [x] Identificar o módulo e criar o renderizador de infraestrutura para `pom.xml` e `application.properties`.
- [x] Confirmar os dialetos Hibernate ORM e a propriedade técnica de datasource usados pela matriz de geração.

## Implementação

- [x] Atualizar a regra compartilhada de persistência para geração de um banco produtivo por projeto.
- [x] Adicionar `bancoDados` opcional ao contrato de entrada do gerador, normalizando ausência ou `null` para `DB2`, e validar os demais valores antes de gravar arquivos.
- [x] Implementar a matriz central de dependência, `db-kind`, variáveis, dialeto e propriedades técnicas.
- [x] Renderizar condicionalmente no template de `pom.xml` somente o driver JDBC produtivo selecionado.
- [x] Renderizar condicionalmente no template de `application.properties` somente a configuração produtiva do banco selecionado.
- [x] Garantir que a regeneração não mantenha fragmentos do banco anteriormente selecionado.

## Testes, revisão e validação

- [x] Criar testes para as gerações `DB2`, `POSTGRESQL` e `MYSQL`, verificando presença do fragmento selecionado e ausência dos demais.
- [x] Criar teste para `bancoDados` ausente ou `null` como DB2 e para valor inválido sem artefato parcial.
- [x] Executar testes do gerador e registrar ambiente, comando, resultado, código de saída e artefatos em `validation.md`.
- [x] Revisar a implementação contra esta SPEC e obter validação e aprovação formal antes do encerramento.

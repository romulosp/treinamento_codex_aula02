# Persistência relacional em Golang

## Decisão

GORM é o adaptador relacional inicial aprovado para aplicações Go, sempre atrás de uma porta de repositório definida no domínio ou na aplicação. A escolha não introduz Panache, JPA ou qualquer biblioteca Java.

## Porta de repositório

A porta deve expressar a finalidade do caso de uso e receber `context.Context` quando houver operação de I/O. Como referência conceitual, pode cobrir busca por identificador, listagem, persistência e exclusão, além de ausência, conflitos e transação quando aplicáveis.

A interface não deve expor tipos GORM, sessões, `*gorm.DB` ou entidades de persistência ao domínio. Os modelos persistentes ficam no adaptador de infraestrutura e são convertidos para objetos de domínio.

## Adaptador GORM

O adaptador deve:

- receber conexão e configuração por composição;
- usar consultas parametrizadas e contexto;
- mapear ausência de registro para erro de domínio conhecido;
- delimitar transações explicitamente quando a operação exigir atomicidade;
- fechar ou liberar recursos conforme o ciclo de vida definido;
- evitar que detalhes SQL vazem para handlers e casos de uso.

O dialeto, banco, migrations e política de pool são definidos pela SPEC da aplicação gerada. Esta orientação não escolhe fornecedor nem cria schema.

## Testes

Testes unitários de domínio e aplicação usam uma implementação falsa ou mock da porta e não precisam de banco. Testes do adaptador GORM devem ser de integração quando o comportamento depender de SQL, transação, migrations ou dialeto. A massa, isolamento e limpeza devem ser registrados em `validation.md`.

## Segurança e operação

Credenciais vêm do ambiente seguro e não são versionadas. Configure timeouts, limites de pool, logs redigidos e observabilidade sem registrar consultas com valores sensíveis. Falhas de persistência devem ser mapeadas para respostas públicas estáveis, sem SQL ou stack trace.

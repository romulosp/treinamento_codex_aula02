# Design: 018-suportar-api-sem-banco

## Contexto

O renderizador da mudança 016 tem uma matriz de bancos produtivos. `SEM_BANCO` é uma quarta saída da mesma matriz, mas sem fragmentos de dependência ou configuração de datasource.

## Referências

- `spec.md`
- `specs/archive/2026-08-27-016-renderizar-configuracao-banco-selecionado/DESIGN.md`

## Decisões

1. Estender a matriz com `SEM_BANCO`, cujo fragmento Maven e fragmento de propriedades produtivas são vazios.
2. Remover da base de categorias as dependências Hibernate ORM/Panache e H2, pois ela não tem entidades, repositórios ou testes de persistência.
3. Manter as propriedades HTTP/OpenAPI e as dependências REST, testes, JaCoCo e Maven.
4. O script `start_aplicacao.bat` permanece responsável somente por Java, Maven e repositórios Maven; não terá configuração de banco.

## Arquitetura e componentes

```text
bancoDados
  ├─ DB2 / POSTGRESQL / MYSQL → um driver + um bloco produtivo
  └─ SEM_BANCO                → nenhum driver + nenhum bloco produtivo
                                      ↓
                            API em memória / integração externa
```

## Alternativas e consequências

- Usar H2 mesmo sem banco foi rejeitado: adicionaria um driver e configuração que a API não utiliza.
- Tratar ausência de `bancoDados` como `SEM_BANCO` foi rejeitado: quebraria o padrão DB2 previamente aprovado.

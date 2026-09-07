# Plano de implementação

Adicionar mockito-junit-jupiter, assertj-core e jdbc-h2 no escopo test, aproveitando versões gerenciadas pelo BOM. Teste Quarkus com banco em memória e cenários HTTP. Executar Maven verify e frontend test/build. Verificar PostgreSQL existente sem exibir credenciais. Qualidade e segurança: conferir isolamento do banco de teste e restrição do proxy; ausência de cobertura configurada não substitui execução dos testes. JavaDoc de testes conforme skill java-javadoc.

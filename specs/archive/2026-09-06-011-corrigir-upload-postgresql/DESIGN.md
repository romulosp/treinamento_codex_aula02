# DESIGN

Atualização solicitada: suíte HTTP padrão com H2 em memória e driver de escopo test. Evidência PostgreSQL abaixo é histórica da reprodução; não é requisito de Docker para executar a suíte atual.

Remover @Lob de FotoEntity.imagemBase64, mantendo columnDefinition TEXT. Não alterar contratos REST. SQL PostgreSQL transacional converte somente referências numéricas com lo_get e convert_from, mantém backup por id e não remove objetos antigos. Teste @QuarkusTest/Rest Assured envia PNG válido e consulta por requisições independentes; configuração exclusiva de teste usa banco isolado. Mantém Java 17 e Quarkus 3.2.

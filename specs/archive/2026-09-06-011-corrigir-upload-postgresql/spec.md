# SPEC — regressão do upload

Status: `SPEC_APROVADA`.

## Contrato e aceite

1. Persistir imagemBase64 como String/Data URL em TEXT, sem @Lob nem acesso JDBC a Clob.
2. Após POST multipart retornar 201, novas requisições GET de listagem e detalhe devem retornar 200; a lista omite imagemBase64 e o detalhe reproduz os bytes enviados.
3. Testar o contrato HTTP com Quarkus e H2 em memória na suíte padrão, conforme estratégia compartilhada e orientação do solicitante. Preservar a evidência da reprodução específica de LOB com PostgreSQL real já executada; H2 não substitui essa evidência de compatibilidade do driver.
4. Converter referências numéricas legadas existentes em TEXT para o conteúdo UTF-8 do Large Object. Preservar valores originais em tabela de backup e preservar os Large Objects. Executar em transação, rejeitando conteúdo que não seja Data URL de imagem. Reexecução não altera Data URLs já convertidos.
5. Registrar comandos, ambiente, resultados e limitações em validation.md. Não declarar integração aprovada apenas com cobertura unitária.
6. Testes padrão e contínuos usam H2 em memória sem Docker, porta TCP ou arquivo externo de credenciais. Modo dev continua lendo POSTGRES_URL e configuração externa na porta 5432. A dependência H2 tem escopo test.

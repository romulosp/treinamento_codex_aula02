# Plano

Complemento: substituir datasource de teste por H2 em memória, renomear teste HTTP para não alegar execução PostgreSQL e executar mvn verify sem banco isolado ativo. Manter configuração principal de PostgreSQL intacta.

Após SPEC aprovada: adicionar Rest Assured e perfil PostgreSQL isolado ao teste; demonstrar falha com @Lob, remover anotação e repetir teste e suite. Inventário Java de produção: somente FotoEntity, mapeamento declarativo, sem nova regra unitária; prova obrigatória é integração real. Executar SQL primeiro no banco isolado e depois no fotogaleria local, preservando backup e objetos. Não alterar credenciais. Risco: referências LOB ilegíveis abortam a transação. Testes não usam banco local do usuário.

# Regras de persistência

O banco de produção é DB2. As credenciais e a URL são fornecidas por variáveis de ambiente; nunca devem ser versionadas.

Mudanças de esquema exigem uma decisão explícita em uma SPEC futura. Flyway não foi incluído na mudança 001 e não deve ser adicionado sem mudança aprovada.

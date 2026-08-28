# SPEC: 017-configurar-fallback-repositorio-maven

## Status
`SPEC_APROVADA`

## Requisitos funcionais

1. O projeto gerado deve conter `.mvn/settings-nexus.xml`, com espelho padrão `NEXUS_INTERNO` em `http://binario.caixa:8081/repository/caixa-group`.
2. O projeto gerado deve conter `.mvn/settings-public.xml`, sem espelho Nexus e com acesso aos repositórios públicos definidos pela plataforma Maven/Quarkus.
3. O script de inicialização deve tentar usar `settings-nexus.xml` por padrão.
4. Quando a verificação de alcance do Nexus falhar, o script deve informar que a VPN/Nexus não está disponível e executar Maven com `settings-public.xml`.
5. Quando Maven falhar ao resolver dependências usando Nexus, o script deve oferecer uma nova tentativa explícita com `settings-public.xml`; a repetição não pode mascarar erros de compilação, testes ou configuração que não sejam de resolução.
6. A escolha pública deve valer somente para a execução atual e não pode alterar `settings.xml` global, variáveis permanentes ou o `pom.xml`.
7. O script deve permitir forçar o modo público por argumento documentado `public` e o modo Nexus por `nexus`.

## Requisitos não funcionais

1. Nenhuma configuração pode conter credenciais, tokens ou valores sensíveis.
2. O Maven Central é fallback; o Nexus continua seleção padrão.
3. As mensagens e documentação devem estar em português do Brasil.

## Cenários e critérios de aceite

- [ ] Sem argumento e com Nexus acessível, Maven usa `settings-nexus.xml`.
- [ ] Sem argumento e com Nexus inacessível, Maven usa `settings-public.xml` e informa o fallback.
- [ ] Com `public`, Maven usa somente `settings-public.xml`.
- [ ] Com `nexus`, Maven usa somente `settings-nexus.xml` e informa a falha se o Nexus não estiver acessível.
- [ ] A seleção não altera a configuração Maven global do usuário nem grava segredos.

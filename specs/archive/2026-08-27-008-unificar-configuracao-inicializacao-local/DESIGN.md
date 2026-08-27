# Design: 008-unificar-configuracao-inicializacao-local

## Contexto

O backend usa variáveis de ambiente em `application.properties`. A mudança anterior carregava essas variáveis de um segundo arquivo local. Esta mudança substitui o mecanismo por atribuições diretas no único script de inicialização solicitado.

## Referências

- `specs/changes/008-unificar-configuracao-inicializacao-local/spec.md`
- `apps/backend/start_aplicacao.bat`
- `apps/backend/src/main/resources/application.properties`

## Decisões

1. `start_aplicacao.bat` será a única fonte da configuração de inicialização local e definirá as sete variáveis antes de Maven.
2. Serão preservados `setlocal`, as configurações de Java/Maven e o `pushd` para o diretório do script.
3. A chamada condicional, a validação de ausência e o arquivo `start_aplicacao.local.bat` serão removidos, pois o script autocontido sempre definirá os valores solicitados.
4. `.gitignore` deixará de ter a regra específica do arquivo removido.

## Arquitetura e componentes

- `apps/backend/start_aplicacao.bat`: configura Java, Maven, OIDC e DB2 para a sessão e inicia o Quarkus.
- `apps/backend/src/main/resources/application.properties`: mantém o consumo das variáveis existentes sem modificação.
- `.gitignore`: deixa de tratar um segundo arquivo de configuração inexistente.

## Alternativas e consequências

- Manter o arquivo separado foi descartado a pedido do solicitante, pois não atende ao requisito de um único `.bat`.
- Alterar `application.properties` para valores literais foi descartado: o arquivo continua parametrizado e o script fornece as variáveis na sessão de execução.

# SPEC: 10004-plugin-saque-cartao

**Autor:** Rômulo Penha

## Status
`SPEC_APROVADA`

O módulo `:plugin-saque-cartao` depende exclusivamente de `:shared-api`,
implementa `IPluginNegocioApp` e declara capacidade `business-menu`. O contrato
mínimo e a minor requerida no manifesto são 1.1. O host 1.2 mantém essa versão
elegível pela regra `requiredSharedApiMinor <= hostMinor` para plugins de
negócio. Seu manifesto declara
`br.com.romulopenha.sistemaprototipoandroid.saquecartao`; o serviço declara sua
entry class. A única folha é `Saque Cartão`, no caminho
`Principal > Outros Serviços > Saque Cartão`.

O plugin exibe, exclusivamente na sua própria Compose View: solicitação de leitura do cartão, senha mascarada localmente, conclusão e solicitação de retorno ao menu inicial por `IPluginRouter`. A senha não é registrada, persistida, enviada ao host ou usada como autorização real.

O plugin NÃO DEVE declarar Activity `MAIN/LAUNCHER` em nenhuma variante. O teste humano DEVE executar `:app`, autenticar pelo plugin de login, selecionar a folha no menu montado pelo host e somente então renderizar `createBusinessScreen`.

## Critérios de aceite

1. O host não tem dependência de implementação no módulo.
2. O APK possui manifesto e `META-INF/services` válidos.
3. O caminho e o item de menu são determinísticos.
4. A senha é mascarada e apagada antes da conclusão.
5. Build e testes unitários do módulo passam.
6. O plugin não possui launcher; após login, o host exibe a folha e o clique do operador renderiza a tela do saque.
7. A conclusão solicita ao roteador o retorno ao menu principal do host.
8. O manifesto com major 1 e minor requerida 1 é aceito pelo host 1.2 como
   plugin `business-menu`.

# Design: saque como componente

## Contrato decidido
A SharedApi passa de 1.2 para 1.3, com contratos aditivos. `IComponentHostContext : PluginHostContext` oferece `launchCardWithdrawal(onResult)`, na main thread. Plugins antigos continuam usando as interfaces originais. A autenticação 1.2 permanece compatível porque o evento não muda; startup-auth aceita minors de 1.2 até a vigente. O AAR publicado acompanha 1.3.0.

`ICardWithdrawalComponent : IPluginApp` oferece `createWithdrawal(Context, callback): CardWithdrawalSession`. A sessão fornece `view` e `cancel()`. O resultado contém somente enum local e confirmação de cleanup. O enum inclui APPROVED, DENIED, CAPTURED, CANCELED, ERROR, UNKNOWN, UNAVAILABLE e BUSY. Os dois últimos são falhas do host, sem execução de hardware. A interface não recebe dados da operação: o operador preenche a jornada existente.

## Hospedagem e lifecycle
O host entrega um contexto de componente vinculado à identidade do consumidor. `BusinessPluginManager.createScreen` cria um FrameLayout com a View do consumidor. O lançamento só é aceito para esse consumidor ativo e com o contêiner anexado. O componente é sobreposto nesse contêiner e a View original fica oculta, mantendo sua instância e estado. O host não recebe classes concretas entre plugins.

Somente uma sessão de componente é permitida; concorrência recebe BUSY. A jornada mantém a tela final até SAIR. SAIR ou Voltar Android solicita cancelamento/fechamento, espera cleanup e então emite um único resultado; o host remove a View sobreposta, restaura o consumidor e entrega o callback na main thread. Falha de cleanup bloqueia novas invocações até reinício do host, e o callback preserva UNKNOWN quando a captura já começou. Nenhuma transação é repetida automaticamente.

Detach do contêiner, logout ou destruição invalidam o callback antes de solicitar cleanup. O componente continua vivo até seu cleanup terminar, mas não apresenta resultado ao consumidor destruído. Recriação da Activity não restaura nem reenvia operação. Uma View criada sem attach não abre pinpad.

## Carga e migração
O APK mantém identidade e pacote atuais, passa a versão 1.2.0, exige SharedApi 1.3 e declara exclusivamente `card-withdrawal-component`. O serviço META-INF passa de IPluginNegocioApp para ICardWithdrawalComponent. Não há launcher.

O staging debug usa `plugins/components`; o loader exige identidade exata do saque e nova capacidade. Usar o pipeline atual de quarentena, certificado, digest, manifesto, API, repositório privado imutável e extração de nativas por ABI. O manifesto da instância deve coincidir com o descritor verificado.

Uma única revisão distinta promovida no staging atual é selecionada para carga. Após lifecycle bem-sucedido, seu digest é persistido atomicamente em files/plugins/components/selected.sha256. No próximo boot sem staging, o host revalida os APKs e exige que esse digest continue elegível. Sem seleção persistida e sem staging, apenas um candidato é aceito. Múltiplos digests no mesmo staging, seleção inválida/ausente do repositório ou candidatos ambíguos falham fechados. APKs históricos permanecem intactos e não interferem na revisão explicitamente selecionada; não escolher por mtime ou semver arbitrariamente.

O gerenciador de negócio filtra a identidade do saque antes da instanciação, inclusive versões antigas já verificadas, e o staging de negócio deixa de embutir o APK do saque. Não apagar APKs antigos nem dados do aplicativo.

## Consumidor e testes
Reutilizar `:plugin-negocio`, cuja folha Principal > Operações já é demonstrativa. Sua UI oferece Abrir pagamento com cartão e apresenta apenas o enum recebido, com aviso de homologação. Manifesto exige 1.3.0; nenhuma dependência no módulo de saque ou no host. A demonstração preserva sua ação local anterior.

Testar API e identidade sem menu, cleanup antes de retorno, cancelamento ativo, exclusão de callbacks após detach/logout, indisponibilidade, concorrência e restauração do consumidor. Instrumentação usa APKs reais para verificar assinatura/capacidade/serviço/nativas e um componente sintético injetado somente no construtor de teste do manager para lifecycle sem hardware. A jornada real é aberta somente manualmente.

## Decisões e limites
Perfil HIGH_ASSURANCE herdado. Stack/SDKs permanecem os dos módulos existentes (compile 37, min 29, target 36, Java 17); nenhuma atualização de dependência. Não há autorização financeira. Teste físico e revisão independente posteriores permanecem pendentes no ponto IMPLEMENTADA.

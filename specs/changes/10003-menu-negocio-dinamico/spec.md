# SPEC: 10003-menu-negocio-dinamico

## Status

`SPEC_APROVADA`

## Referências e dependências

A Change depende da infraestrutura segura e do repositório privado verificado da Change 10002. O perfil é STANDARD: módulos `:app`, `:shared-api` e plugins, Kotlin/Compose, minSdk 29, targetSdk 36, compileSdk 37 e Java 17.

## Requisitos funcionais

### RF-01 — Contrato e versão

`IPluginNegocioApp` DEVE estender `IPluginApp` e `IMenuProvider` e declarar `fun getCaminhoMenu(): String`. Um plugin de negócio declara exatamente uma folha. `menuItems()` permanece somente como compatibilidade e NÃO é usado para posicionar ou reconstruir a árvore. A API compartilhada preserva a versão 1.1.0 e o construtor binário de quatro campos de `BusinessMenuItem`.

`BusinessMenuItem` DEVE representar tanto agrupador quanto folha: mantém `id`, `titulo`, `rota` e `ordem`, e recebe `filhos: List<BusinessMenuItem>`. Agrupador tem filhos não vazios e rota neutra; folha tem filhos vazios e conserva os metadados oferecidos pelo plugin. Cada plugin DEVE expor exatamente um item-folha em `businessMenuItems`; a infraestrutura usa esse único descritor como metadado da folha e nunca usa sua posição para formar a árvore. A UI consome a mesma classe, agora recursiva.

### RF-02 — Gramática e normalização do caminho

O caminho é obrigatório e contém ao menos dois segmentos, separados por `>`: `Principal > <...> > Folha`. Cada segmento, após trim e colapso de whitespace, DEVE ser não vazio. `>` é delimitador reservado e não pode fazer parte de um label; portanto qualquer delimitador cria segmentos, e segmentos vazios são inválidos. A raiz deve ser `Principal` sem distinção de maiúsculas/minúsculas.

A forma de exibição usa `segmento1 > segmento2`; a chave de comparação é essa forma em minúsculas com `Locale.ROOT`. IDs são a mesma sequência normalizada em minúsculas, com espaços convertidos para hífen. A normalização não depende da ordem de descoberta.

### RF-03 — Descoberta e montagem

Após a autenticação, o host DEVE procurar todos os APKs de negócio no repositório privado verificado. Para cada APK compatível, cria um `DexClassLoader` isolado e lê exclusivamente `META-INF/services/br.com.romulopenha.sistemaprototipoandroid.sharedapi.IPluginNegocioApp`. Não é permitido varrer classes por reflection. Cada nome de classe do descritor é carregado, instanciado sem argumentos e testado contra o contrato.

O host chama `getCaminhoMenu()` uma vez por instância, valida o resultado, detecta folhas com a mesma chave e, somente sem erro fatal, insere os segmentos em uma árvore. Agrupadores iguais no mesmo caminho são compartilhados. Filhos são ordenados alfabeticamente por label com comparação case-insensitive estável.

### RF-04 — Falhas e resultados

| Código | Condição | Resultado |
| --- | --- | --- |
| MENU-001 | Duas folhas com o mesmo caminho normalizado | Fatal: alerta, log e encerramento do startup do menu. |
| MENU-002 | Caminho nulo, vazio ou whitespace | Fatal: alerta, log e encerramento do startup do menu. |
| MENU-003 | Raiz, delimitadores ou segmentos inválidos | Fatal: alerta, log e encerramento do startup do menu. |
| MENU-004 | Nenhum plugin elegível | Warning; UI permanece funcional com estado vazio. |
| MENU-005 | Arquivo/carga de APK corrompida | Falha isolada; log e continuação. |
| MENU-006 | Descritor de serviço ausente ou inválido | Falha isolada; log e continuação. |
| MENU-007 | Timeout de discovery, instanciação ou caminho | Falha isolada; log e continuação. |
| MENU-008 | Major da API incompatível | Falha isolada; log e continuação. |

No erro fatal o log contém data/hora, caminho normalizado, classes e APKs envolvidos; no conflito contém literalmente `[FATAL] Inicialização interrompida por conflito de caminho de menu entre plugins.` O modal informa o código e plugins envolvidos; após reconhecimento, a Activity é encerrada. Falhas isoladas jamais derrubam a Activity.

### RF-05 — Threading, cancelamento e lifecycle

Discovery, leitura de ZIP, classe, instanciação e montagem ocorrem fora da main thread, por um executor do manager. Cada inicialização recebe uma geração; resultado produzido para sessão anterior é descartado. `onDestroy` encerra o executor e libera referências. O handle conserva o `DexClassLoader` e metadados enquanto o plugin for apresentado; é descartado quando a sessão termina, ocorre falha isolada ou o processo reinicia.

## Requisitos não funcionais

- O classloader recebe exclusivamente APKs privados, assinados e já validados.
- Logs não podem registrar sessão, credencial, token ou manifesto integral.
- Declarações Kotlin públicas/protegidas alteradas terão KDoc em português.
- A ordenação, IDs e árvore devem ser reprodutíveis entre execuções.

## Cenários e critérios de aceite

1. Dois plugins com pai compartilhado formam um agrupador com duas folhas em ordem alfabética e IDs estáveis.
2. Caminhos duplicados, inclusive por case, produzem `MENU-001`, modal e log.
3. Caminhos nulo/vazio produzem `MENU-002`; raiz, um único segmento ou segmento vazio produzem `MENU-003`.
4. APK sem descritor de serviço, inválido, incompatível ou lento é isolado; os demais plugins continuam disponíveis.
5. Sem plugins elegíveis, a UI mostra o estado vazio sem modal crítico.
6. A tela não bloqueia a main thread; logout ou destruição impedem a publicação tardia de resultado e destacam os plugins cooperativamente.
7. Build, testes unitários, lint, teste instrumentado e validador estrutural são executados e registrados antes da conclusão.

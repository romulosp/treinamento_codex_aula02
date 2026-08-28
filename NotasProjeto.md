# Notas do Projeto — Aula 02

## 1. Propósito e estado do laboratório

Este repositório é um laboratório de geração de APIs Java orientado a especificações. O objetivo não é manter o código gerado como ativo permanente: a documentação descreve o resultado esperado, o processo registra as decisões e o módulo executável pode ser recriado do zero para demonstrar repetibilidade.

Em 2026-08-27, a aplicação que estava funcional foi removida intencionalmente para reinicialização. Permanecem versionados os documentos Markdown e texto, além de `.gitignore`, que é a exceção técnica necessária para aplicar esta política. Assim, não há backend executável em `apps/backend/` até que uma nova mudança gere esse módulo.

## 2. O que foi desenvolvido antes da reinicialização

| Mudança | Entrega concluída |
| --- | --- |
| 001 — criar projeto Java | Base Maven com Java 17, Quarkus 3.2.10.Final, RESTEasy, OpenAPI, Health, Panache, DB2, Rest Client, JaCoCo, Sonar e dependências de teste. |
| 002 — consolidar governança | Processo Spec Driven único, Skills operacionais, prompts didáticos preservados e regras de evidência, aprovação, arquivamento e commit. |
| 003 — gerenciar categorias | API REST em memória para listar, detalhar, criar, atualizar e excluir categorias, com DTOs, OpenAPI, validações e testes de integração. |
| 004 — parametrizar nome | Regra para derivar nome público, `artifactId`, OpenAPI e schema de teste a partir do identificador da mudança. |
| 005 — limpar artefatos | Script seguro para excluir `target`, `.quarkus` e logs abaixo de `apps/`. |
| 006 — proteger categorias | OAuth 2.0/OIDC Bearer obrigatório, autorização pelo claim `azp`, lista de clientes autorizados e filtro HTTP de saída. |
| 007 — inicialização local segura | Evolução temporária do carregamento e validação de variáveis de ambiente sem versionar arquivo local sensível. |
| 008 — unificar inicialização | Script único de inicialização com Java e Maven configurados apenas para a sessão do processo. |
| 009 — URL OIDC de desenvolvimento | Atualização isolada da URL de desenvolvimento, com regressão aprovada sem serviços externos. |
| 010 — reinicializar workspace documentado | Aplicação da política documental estrita, preservando apenas documentação versionada. |
| 011 — reescrever readme profissional | Atualização do README com visão do ciclo Spec Driven e arquitetura de referência. |
| 012 — diretrizes de testes unitários | Estabelece estratégia de testes unitários com JUnit 5, Mockito e JaCoCo. |
| 013 — gerenciar categorias | Regeneração da API de categorias em memória com suíte de testes completa. |
| 014 — suportar DB2 e PostgreSQL | Adição de suporte e matriz para PostgreSQL e DB2. |
| 015 — adicionar MySQL | Extensão da matriz de bancos relacionais suportados com MySQL. |
| 016 — renderizar configuração banco selecionado | Script e automação para renderização exclusiva do banco no pom.xml e application.properties. |
| 017 — fallback repositório Maven | Configuração de settings com fallback automático para Maven Central quando fora do Nexus. |
| 018 — suportar API sem banco | Adição da opção SEM_BANCO para geração de APIs desacopladas de persistência. |
| 019 — gerenciar tarefas | Criação da API independente de tarefas com persistência em PostgreSQL via Hibernate Panache e testes H2. |

### API que existia no módulo gerado

A API anterior tinha o nome público `gerenciar-categorias` e implementava uma coleção em memória. A massa inicial era:

| Id | Nome | Quantidade de produtos |
| --- | --- | --- |
| 1 | CAMISAS | 2 |
| 2 | ACESSÓRIOS | 1 |
| 3 | VIDEO-GAMES | 4 |

| Operação | Contrato de sucesso |
| --- | --- |
| `GET /categorias/` | `200` com o atributo `categorias`. |
| `GET /categorias/{id_categoria}` | `200` com uma categoria. |
| `POST /categorias/add` | `201` com a categoria criada; recebe `nome_categoria` e `quantidade_produtos`. |
| `PUT /categorias/{id_categoria}` | `200` com a categoria atualizada. |
| `DELETE /categorias/deletar/{id_categoria}` | `200` com `{"RESULTADO":"CATEGORIA EXCLUIDA COM SUCESSO"}`. |

Entradas inválidas retornavam `400` com o atributo `mensagem`; identificadores inexistentes retornavam `404` com `mensagem`. Todas as operações exigiam token Bearer válido: ausência ou invalidez retornava `401`, e cliente sem o claim `azp` autorizado retornava `403`.

As categorias não eram persistidas. Cada reinício restaurava a massa inicial, o que mantém os cenários determinísticos para testes e demonstração.

## 3. Arquitetura e tecnologia

O módulo gerado segue Java 17, Maven e Quarkus 3.2.10.Final. A organização separa responsabilidades:

- `api`: recursos REST, DTOs de requisição e resposta, mapeamento de erros e OpenAPI;
- `application`: casos de uso e orquestração;
- `domain`: modelos e regras de negócio independentes de HTTP;
- `infrastructure`: persistência, armazenamento em memória, clientes externos, filtros e configuração técnica.

Recursos REST delegam para a camada de aplicação e não expõem entidades de persistência. Em produção, a configuração de banco é recebida por variáveis de ambiente; em testes, o Quarkus usa H2 em memória e OIDC desabilitado. Os testes unitários usam JUnit 5 e Mockito; os testes de integração usam `@QuarkusTest` e Rest Assured.

## 4. Metodologia Spec Driven

Toda mudança começa em `specs/changes/<id>-<nome>/` e contém, no mínimo:

```text
proposal.md       objetivo, escopo, riscos e fora de escopo
spec.md           requisitos verificáveis e critérios de aceite
DESIGN.md         decisões, componentes e consequências
tasks.md          plano de execução rastreável
validation.md     ambiente, comandos, resultados e evidências
reviews/          revisões da SPEC, implementação e aprovação
```

O fluxo obrigatório é:

1. **Especificação** — elaborar proposta, SPEC, design e tarefas.
2. **Revisão da SPEC** — registrar relatório `REV-*`; somente `SPEC_APROVADA` libera a implementação.
3. **Implementação** — alterar exclusivamente o escopo aprovado e marcar as tarefas concluídas.
4. **Revisão da implementação** — comparar cada requisito com o que foi produzido; registrar `IMP-REV-*`.
5. **Validação** — executar testes e verificações aplicáveis; registrar `VAL-*`, ambiente, comando, código de saída e resultado.
6. **Aprovação** — consolidar os gates em `APR-*`; somente `APROVADA` permite encerramento.
7. **Encerramento** — atualizar `specs/system/`, mover a mudança para `specs/archive/AAAA-MM-DD-<id>-<nome>/` e criar commit rastreável.

Reprovação, falha ou bloqueio interrompe o fluxo e retorna à primeira fase adequada. O documento canônico do ciclo é `specs/shared/process/workflow.md`; `AGENTS.md` contém as regras do repositório; Skills executam cada fase. Os prompts legados são material didático, não substitutos do processo.

## 5. Como criar uma nova API ou módulo Java

1. Escolha o próximo identificador livre e crie `specs/changes/<id>-<nome-da-api>/` usando os modelos em `specs/templates/`.
2. Defina os contratos HTTP, validações, respostas de erro, segurança, persistência e critérios de aceite em `spec.md`. Não escreva código antes de obter `SPEC_APROVADA`.
3. Para um módulo Quarkus, use `br.com.romulopenha` seguido do `artifactId` sem hífens como pacote-base (por exemplo, `gerenciar-tarefas` gera `br.com.romulopenha.gerenciartarefas`), Java 17 e as camadas `api`, `application`, `domain` e `infrastructure`.
4. Derive o nome público pelo sufixo após o primeiro hífen do diretório da mudança. Por exemplo, `010-consultar-pedidos` gera `consultar-pedidos`; o schema de teste correspondente é `CONSULTAR_PEDIDOS`.
5. Gere localmente o módulo em `apps/backend/`. Configure o `artifactId`, `quarkus.application.name`, o caminho OpenAPI e o schema H2 com o nome derivado. Como arquivos de módulo são ignorados, eles não devem ser adicionados ao Git.
6. Implemente DTOs na fronteira HTTP, delegação para a camada de aplicação e testes automatizados para cada comportamento observável aprovado. Não exponha entidades JPA como contratos REST.
7. Execute as fases de revisão, validação, aprovação e arquivamento. O resultado documental deve permitir recriar o mesmo módulo e reproduzir a mesma suíte de testes.

## 6. Como testar e executar um módulo específico após gerá-lo

Os exemplos abaixo valem **somente após** a regeneração de `apps/backend/`.

1. Instale ou selecione JDK 17 e Maven compatível com o módulo. Confirme que `java -version` e `mvn -version` apontam para as versões esperadas.
2. A partir de `apps/backend/`, execute `mvn test`. Os testes devem usar o perfil de teste com H2 em memória e não depender de DB2 ou de servidor OIDC externo.
3. Para executar em desenvolvimento, defina no ambiente do processo os valores necessários para OIDC e DB2: `AUTH-SERVER-URL`, `CLIENT-ID`, `SECRET`, `CLIENTS-AUTHORIZED`, `DB2_JDBC_URL`, `DB2_USERNAME` e `DB2_PASSWORD`. Nunca registre os valores em documentos, commits ou scripts versionados.
4. No mesmo diretório, execute `mvn quarkus:dev` ou use o script local gerado para o módulo, se a SPEC aprovada o prever. O processo deve manter variáveis apenas na sessão local.
5. Consulte a interface Swagger no endereço exposto pelo Quarkus e o documento OpenAPI no caminho definido para a API. Para o exemplo histórico, o caminho era `/swagger_gerenciar-categorias.json`.

Para testar somente uma classe Maven após a geração, execute `mvn -Dtest=NomeDoTeste test` dentro do módulo. Registre o comando, ambiente, código de saída, número de testes e quaisquer avisos relevantes em `validation.md`.

## 7. Política de retenção e reinicialização

O repositório agora guarda somente documentação `.md` e `.txt`, além do arquivo de política `.gitignore`. Todo arquivo de código, configuração, script, binário, diagrama, imagem ou produto de build é ignorado. Essa regra mantém a especificação como fonte de verdade e evita versionar código gerado.

Para uma nova reprodução, mantenha este repositório documental, abra uma mudança nova, aprove a SPEC e gere o módulo localmente. Ao terminar uma tentativa, remova o módulo e seus artefatos locais — especialmente `target/`, `.quarkus/` e logs — sem alterar os documentos arquivados que formam a trilha de auditoria.

## 8. Fontes de consulta

- `AGENTS.md`: regras obrigatórias do projeto.
- `specs/shared/process/workflow.md`: processo e gates canônicos.
- `specs/shared/architecture/backend-java.md`: divisão de camadas do backend.
- `specs/shared/api/rest-conventions.md`: convenções para contratos REST.
- `specs/shared/testing/testing-strategy.md`: estratégia de testes.
- `specs/archive/`: SPECs, revisões, validações e decisões das mudanças concluídas.
- `specs/system/README.md`: estado funcional vigente registrado antes da reinicialização.

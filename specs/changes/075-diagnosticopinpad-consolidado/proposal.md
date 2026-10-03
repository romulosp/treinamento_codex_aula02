# Proposta — 075 diagnosticopinpad consolidado

Autor: Rômulo Penha  
Data: 2026-09-30

## Status

- SPEC: `SPEC_APROVADA`
- Change: `IMPLEMENTADA`

A revisão anterior foi implementada, mas não consolidava integralmente os
efeitos da Change 072 presentes nas fontes 068/069 nem declarava a Change 074
como baseline normativa do stack. A implementação e as evidências anteriores
permanecem históricas. A reconciliação foi executada em 2026-10-03; as novas
evidências técnicas estão em `validation.md` e no registro de implementação.

## Objetivo

Criar uma única Change canônica para o estado final acumulado das Changes
arquivadas `068 diagnosticopinpad` e `069 diagnosticopinpad Functional Lab`,
incluindo todas as correções posteriores que essas fontes incorporam da 072.

O laboratório Android deve atravessar, de forma reproduzível, o caminho
Kotlin/Compose → AAR Go → `PBRG` v1 → Bridge Windows → transporte scripted ou
COM real, consumindo a baseline canônica da Change 074 sem duplicar protocolo,
estado ABECS ou regras de infraestrutura no Kotlin.

## Fontes e dependências normativas

Fontes históricas consolidadas diretamente:

- `specs/archive/2026-09-27-068-diagnosticopinpad`;
- `specs/archive/2026-09-27-069-diagnosticopinpad-functional-lab`.

Baseline normativa do stack:

- `specs/changes/074-pinpad-android-bridge-stack`.

A 074 consolida 066, 067, 070, 071 e 072. A 075 não copia todo o core Go, REST,
serial, ownership ou launcher; ela incorpora de forma autocontida os efeitos
observáveis desses contratos sobre o laboratório Android e exige conformidade
da 074 como Gate P0.

## Estado final consolidado

- o laboratório mínimo de versão, Ping, abertura, estado, `GetInfo`, fechamento
  e cancelamento é preservado;
- as 28 opções do catálogo funcional são apresentadas, com 6 e 25 visíveis e
  desabilitadas e 28 encerrando com segurança;
- a fachada gomobile usa apenas métodos nomeados e tipados;
- o core Go é a fonte de verdade de ABECS e do estado da sessão;
- Ping confirma apenas conectividade do Bridge e nunca abre COM ou sessão;
- uma tentativa de Open em cliente fechado executa preflight Ping controlado;
- estado de ação, conectividade do Bridge e sessão Go são independentes;
- erros possuem categoria, fase, mensagem segura e correlação estáveis;
- o erro permanece visível acima do catálogo e acessível sem depender de cor;
- o Android mantém apenas log privado de metadados; o rastro ABECS/Bridge
  compartilhado pertence ao processo Windows definido pela 074;
- `PORTA_PINPAD` nunca é lida pelo Android e é obrigatória em todo processo
  físico Windows, sem COM fixa ou fallback, conforme decisão aprovada da 074;
- scripted e hardware físico produzem evidências separadas.

## Escopo

- projeto Android nativo Kotlin/Compose, single-module `app`;
- AAR local reproduzível via `gomobile bind`, sem versionar o binário;
- fachada Go nomeada para as capacidades já aprovadas no core;
- tela única com cinco seções, diálogos e formulários para as 28 opções;
- configuração de host, porta TCP e timeout;
- preflight, estado fiel, erros por fase, correlação e visibilidade operacional;
- `operationID`, timeout, cancelamento, endpoint e lifecycle seguros;
- integração com Bridge scripted e físico definido pela 074;
- testes Go, JVM, Compose/instrumentados, integração, multiprocesso relevante e
  hardware quando disponível;
- GoDoc/KDoc, redaction, rastreabilidade e evidências reproduzíveis.

## Fora do escopo

- implementar novos comandos ABECS ausentes no core;
- executor genérico, comando raw ou payload ABECS editável;
- parser, CRC, framing ou máquina de estados ABECS no Kotlin;
- USB Host/OTG, LAN, backend, analytics ou publicação em loja;
- expor a API REST da 074 como fronteira do Android;
- remover ou redefinir a API REST que continua pertencendo à baseline 074;
- persistir dados de negócio ou material sensível;
- implementar a transação completa reservada da opção 25;
- alterar retroativamente as Changes arquivadas.

## Precedência

Em caso de conflito:

1. `AGENTS.md` e `specs/shared/process/workflow.md`;
2. comportamento do produto definido nesta Change 075;
3. contratos de infraestrutura, protocolo e lifecycle da Change 074;
4. fontes históricas 068 e 069;
5. evidências históricas, que nunca substituem nova execução.

Nenhum texto da 075 pode relaxar segurança, redaction, PBRG, ownership,
readiness, logging ou cleanup definidos na 074. Nenhum texto da 074 amplia por
si só a UI para REST ou para comandos fora do catálogo aprovado da 075.

## Resultado esperado

O operador seleciona uma capacidade, informa parâmetros válidos e acompanha
conectividade, sessão, fase, duração, correlação e resultado sanitizado. O
fluxo físico usa o endpoint TCP configurado no Android e a porta serial efetiva
resolvida exclusivamente pelo processo Windows. Falhas nunca fabricam estado
`OPEN`, sucesso físico ou evidência de log inexistente.

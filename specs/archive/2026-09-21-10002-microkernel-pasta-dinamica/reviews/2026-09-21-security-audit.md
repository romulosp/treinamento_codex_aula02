# Auditoria de segurança — 10002

## Resultado

`SEM_ACHADOS_CONFIRMADOS`

## Escopo

Foram auditados `:app`, `:shared-api`, `:plugin-login`,
`LoginPluginLoader`, `DynamicLoginPluginManager`, `MainActivity`, manifests,
manifesto do plugin, dependências, script de entrega e testes. Não existem
backend, API REST, banco, rede ou deploy remoto nesta Change.

## Controles verificados

- O staging em `externalFilesDir` é tratado como entrada não confiável e nunca
  é entregue diretamente ao `DexClassLoader`.
- A cópia privada é submetida a caminho canônico, limite de tamanho, SHA-256,
  assinatura igual à do host, pacote, manifesto JSON estruturado, API,
  `entryClass` e capacidade `startup-auth`.
- A promoção usa nome derivado do digest e arquivo somente leitura.
- Falhas de rejeição e carga não registram usuário, senha, manifesto integral,
  caminho externo ou token.
- O plugin comunica ao host somente sessão com identificador opaco e expiração.
- O manifest do host desabilita backup e mantém a Activity em paisagem.
- Não foram encontradas chamadas de rede, execução de shell, HTML cru, `eval`,
  consultas concatenadas ou segredos nos artefatos auditados.

## Categorias não aplicáveis

Tenant, IDOR, autorização de rotas, persistência de credenciais, backend,
XSS, banco de dados, rede e deploy remoto não existem no escopo. O login local
é demonstrativo e não constitui autenticação de backend para produção.

## Limitação

Não foi injetado um APK assinado deliberadamente com callback defeituoso. A
contenção das fronteiras foi verificada por inspeção do código, testes
instrumentados de rejeição e execução manual do plugin válido.

## Evidências

- `python .agents/skills/android-native-engineering/scripts/validate_android_project.py ...`: código 0.
- Build, lint e testes instrumentados Android: código 0.
- Execução dinâmica no AVD: `DISCOVERED → STAGED → VERIFIED → LOADED →
  ATTACHED → ACTIVE`.
- Busca por segredos e APIs perigosas nos artefatos da Change: nenhuma
  ocorrência confirmada.
- Relatório PDF atual: `docs/security-audit/relatorio-10002-microkernel-pasta-dinamica.pdf`.

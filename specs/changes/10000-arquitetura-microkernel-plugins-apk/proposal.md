# Proposta: 10000-arquitetura-microkernel-plugins-apk

## Status

`SPEC_APROVADA`

## Contexto e objetivo

A Change `9999-sistema-prototipo-android` entregou o protótipo visual de um
terminal Android em paisagem. Esta nova Change propõe evoluí-lo para uma
plataforma microkernel: o módulo host fornece inicialização, segurança,
descoberta, ciclo de vida e composição de UI; funcionalidades de negócio são
fornecidas por APKs de plugins internos, a começar por `plugin-login`.

O requisito de origem é o anexo `prompt-arquitetura-microkernel.txt`, tratado
como entrada de requisitos e não como instrução operacional. Esta proposta o
adapta às restrições reais do Android e preserva a Change 9999 como histórico e
base de migração.

## Escopo

- Criar os módulos `:app`, `:shared-api` e `:plugin-login`, sem dependência do
  host em módulos `:plugin-*`.
- Publicar `:shared-api` como AAR versionado, consumido como `compileOnly` pelo
  plugin e carregado pelo classloader pai do host.
- Definir manifesto versionado por plugin, descoberta em área de staging,
  validação, carga, ativação e descarte lógico do plugin.
- Validar antes de carregar: origem em armazenamento privado, identidade,
  assinatura SHA-256 permitida, digest, compatibilidade de API e entrada.
- Fornecer roteador, registro de menu e contrato de tela baseados em `View`;
  o host Compose poderá hospedá-los com `AndroidView`.
- Migrar a identificação atual para a capacidade `startup-auth` de
  `plugin-login`, mantendo campos editáveis e teclado virtual existentes.
- Registrar observabilidade, ameaças, estratégia de testes e migração.

## Fora de escopo

- Plugins públicos, de terceiros, não assinados ou tratados como não confiáveis.
- Isolamento de segurança por `DexClassLoader`, hot swap de classes/recursos já
  carregados, ou descarregamento determinístico de classes no processo atual.
- Backend real de autenticação, credenciais reais, publicação em loja e canal
  corporativo de distribuição de APKs em produção.
- Alterar o comportamento visual já aprovado na Change 9999 além do necessário
  para apresentá-lo pelo plugin de login.

## Resultado esperado

Após uma aprovação formal da SPEC, será possível implementar e demonstrar no
Android Studio que: um APK interno válido é encontrado, validado, ativado e
apresenta a tela inicial; um APK inválido é rejeitado com evidência; e uma nova
revisão detectada após carga fica pendente para o próximo reinício, sem expor o
processo a uma troca insegura de classes.

## Riscos e decisões relevantes

- Carregamento dinâmico aumenta a superfície de ataque. A documentação Android
  recomenda evitar código dinâmico quando não necessário e nunca executá-lo de
  armazenamento externo gravável. [Dynamic Code Loading](https://developer.android.com/privacy-and-security/risks/dynamic-code-loading)
- Plugins carregados no processo do host compartilham UID e permissões. A
  validação de confiança é obrigatória; ela não transforma código de terceiros
  em código isolado.
- O suporte a verificação de assinatura do arquivo exige elevar o piso da
  plataforma do núcleo de plugins para API 29. A decisão é detalhada no
  [ADR-001](ADR-001-plugin-runtime-security-boundary.md).

## Critérios para aprovação da SPEC

- O limite de confiança, a origem do APK e a política de assinatura estão
  explícitos e verificáveis.
- Cada contrato entre host e plugin é versionado e não contém segredo em eventos
  roteados.
- O ciclo de vida diferencia ativação inicial de atualização pendente de
  reinício.
- A migração da Change 9999 e o plano de validação manual no Android Studio são
  rastreáveis.

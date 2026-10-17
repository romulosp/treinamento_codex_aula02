# Plano de implementação: 10006-layout-login-referencia-svg

## Status

`SPEC_APROVADA`

## Impactos prováveis

- `plugin-login/.../LoginPlugin.kt`: somente composables e constantes visuais.
- `plugin-login/.../LoginScreenTest.kt`: asserções semânticas da nova
  composição, mantendo os testes funcionais existentes.
- Artefatos desta Change: acompanhamento de tarefas e evidências.

## Estratégia

1. Preservar `LoginRoute`, estado, redutor, ViewModel e autenticação.
2. Reorganizar `LoginScreen` em cabeçalho, conteúdo adaptativo e rodapé.
3. Aplicar cores, gradiente, bordas, dimensões e sombras da referência.
4. Reorganizar apenas a apresentação das ações do teclado e manter `ENTER`
   ausente.
5. Comprovar textos e ausências do cabeçalho por teste Compose controlado.

## Testes e qualidade

- Executar testes JVM existentes do `:plugin-login`.
- Compilar o APK debug do plugin e do host.
- Executar Android lint configurado.
- Executar testes Compose instrumentados quando houver dispositivo disponível;
  caso contrário, registrar a limitação sem declarar validação visual.
- Executar o validador estrutural da skill Android.
- Executar `git diff --check` nos arquivos da Change.

## Segurança e privacidade

Nenhuma fronteira de confiança ou manipulação de credencial será alterada.
Os composables não persistem nem registram usuário ou senha.

## Riscos e contenção

- Corte em viewport menor: derivar tamanho de teclas com `BoxWithConstraints`.
- Regressão funcional: não alterar eventos e manter testes existentes.
- Interferência com mudanças paralelas: editar somente arquivos do
  `:plugin-login` e da Change 10006.

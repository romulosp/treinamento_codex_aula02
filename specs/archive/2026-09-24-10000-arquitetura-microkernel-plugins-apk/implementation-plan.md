# Plano de implementação

## Status

`IMPLEMENTADA`

Implementação autorizada pelo solicitante e concluída em 2026-09-24. O AAR
`shared-api:1.1.0` é publicado localmente em `shared-api/build/local-maven`.

## Sequência prevista

1. Atualizar Gradle para os módulos `:app`, `:shared-api` e `:plugin-login`,
   elevando `minSdk` para 29 e preservando `compileSdk = 37`, `targetSdk = 36`
   e Java 17; registrar a saída de build.
2. Implementar a API AAR, publicação local de desenvolvimento e teste de ABI
   que assegure ausência de `shared-api` dentro do APK do plugin.
3. Implementar o pipeline de staging, quarentena privada, digest, assinatura,
   manifesto e compatibilidade antes de qualquer classloader.
4. Implementar `PluginManager`, estados, recuperação de exceção, observação de
   diretório e persistência mínima do inventário validado.
5. Implementar UI host, roteador e bloqueio de boot por capacidade.
6. Extrair a tela de identificação da Change 9999 para `plugin-login`, sem
   alterar seu contrato visual aprovado: paisagem, campos editáveis, teclados e
   paleta azul predominante.
7. Executar testes unitários, instrumentados, segurança, benchmark e roteiro
   manual; anexar evidências a `validation.md`.

## Pré-condições pendentes

- Aprovação formal da SPEC.
- A publicação da API nesta Change será somente em repositório Maven local de
  projeto (`build/local-maven`) para desenvolvimento e testes. Um endpoint
  corporativo é uma decisão posterior, fora do escopo.
- A entrada em staging para a demonstração é manual e controlada no ambiente de
  teste. O canal de distribuição de APKs em produção está fora do escopo.

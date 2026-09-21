# ADR-001: Limite de confiança e validação de APK de plugin

## Status

`PROPOSTO`

## Contexto

Plugins APK serão carregados dinamicamente. O Android recomenda evitar essa
prática quando possível e alerta que código em local gravável externamente pode
ser alterado. [Dynamic Code Loading](https://developer.android.com/privacy-and-security/risks/dynamic-code-loading)

Além disso, um `DexClassLoader` não cria processo nem UID separados: código do
plugin executará com as permissões concedidas ao host.

## Decisão

- Aceitar somente plugins internos, com certificado cujo SHA-256 esteja na
  allowlist embutida no build de release.
- Tratar staging como não confiável, copiar para quarentena privada e validar
  assinatura, digest, manifesto, pacote, API e entrada antes de carregar.
- Usar diretórios privados de código otimizado e repositório privado de APKs
  verificados.
- Elevar `minSdk` do runtime de plugins para 29, permitindo uma política
  uniforme de leitura de certificado de APK em toda a matriz.
- Permitir artefato não assinado apenas em build debug, por constante de build,
  sem configuração de usuário ou remota.

## Consequências

Há aumento de custo de validação e de gestão de chaves, mas a superfície de
ataque fica explícita e auditável. Plugins não confiáveis exigirão arquitetura
separada em processo/aplicativo e nova Change.

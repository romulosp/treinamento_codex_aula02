# Especificação complementar: fundação do aplicativo

> **SUPERSEDIDA PARA A TOPOLOGIA ATUAL.** A fundação de módulo único foi
> substituída pelas Changes 10000 e 10001: `:app` é host, `:shared-api` contém
> contratos e `:plugin-login` contém a identificação. Este documento é apenas
> registro da fundação original.

**Autor:** Rômulo Penha

## Identidade

- Diretório obrigatório: `apps/frontend/smartphone/sistema-prototipo-android/`.
- `rootProject.name`: `sistema-prototipo-android`.
- Módulo inicial: `app`.
- Grupo: `br.com.romulopenha`.
- `namespace`: `br.com.romulopenha.sistemaprototipoandroid`.
- `applicationId`: `br.com.romulopenha.sistemaprototipoandroid`.

## Plataforma

- Kotlin.
- Jetpack Compose Material 3.
- Java 17.
- `compileSdk = 37`.
- `targetSdk = 36`.
- `minSdk = 26` decidido para Android 8.0 ou superior.
- Orientação fixa: paisagem (`android:screenOrientation="landscape"`).

## Distribuição

- Distribuição interna para desenvolvimento, demonstração e validação.
- Publicação em Google Play, outra loja ou canal público está fora de escopo e exige nova Change.
- Dependências estáveis e centralizadas em `gradle/libs.versions.toml`.

## Estrutura

O projeto deve iniciar com um módulo `app`, uma Activity e pacotes por responsabilidade. Cada componente público fica em pasta própria dentro de sua família. Arquivos de preview e testes podem permanecer próximos ao componente quando o padrão de projeto permitir.

## Manifesto

- Declarar explicitamente `android:exported` na Activity inicial.
- Não declarar permissões perigosas.
- Não declarar serviços, receivers, providers ou deep links sem nova revisão da SPEC.
- Nome visível neutro: `Sistema Protótipo Android`.

## Inicialização

A Activity deve aplicar o tema e exibir a tela-catálogo como destino inicial. O menu demonstrativo pode ser aberto por evento explícito da tela, sem framework de navegação complexo se uma máquina de estado simples atender ao fluxo.

## Critérios específicos

- O projeto sincroniza e compila pelo Gradle Wrapper.
- O validador estrutural da skill local retorna código `0`.
- A configuração não contém caminho absoluto para o material de origem.
- Nenhuma dependência de produção é adicionada sem uso demonstrável.

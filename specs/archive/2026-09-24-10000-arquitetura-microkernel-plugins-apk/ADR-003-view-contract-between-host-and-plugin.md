# ADR-003: Contrato de UI baseado em View

## Status

`PROPOSTO`

## Contexto

O host atual usa Compose. Expor `Fragment` ou tipos Compose em uma API que será
consumida por APK independentemente compilado aumentaria o acoplamento a
versões, classloaders, composição e recursos.

## Decisão

O AAR expõe `PluginScreenFactory` que cria uma `View` com contexto e recursos do
plugin. O host mantém o contêiner nativo e pode encaixar a View em Compose com
`AndroidView`. Um plugin pode usar Compose internamente, desde que sua fronteira
externa seja View.

## Consequências

Há uma ponte pontual entre Compose e View, porém as versões do toolkit visual
não tornam-se parte do contrato binário entre host e plugin.

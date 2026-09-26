# Matriz de compatibilidade — 9999-sistema-prototipo-android

**Data da verificacao:** 2026-09-19  
**Resultado:** `PASS`

| Componente | Baseline solicitado | Evidencia/verificacao | Resultado |
| --- | --- | --- | --- |
| JDK | 17 | AGP 9.4.0: versão mínima e padrão 17 | `VERIFIED` |
| Gradle | 9.6.0 | AGP 9.4.0: versão mínima e padrão 9.6.0 | `VERIFIED` |
| AGP | 9.4.0 | Suporte máximo oficial à API 37 | `VERIFIED` |
| Kotlin/KGP | 2.2.10 | Dependência de runtime mínima e padrão do AGP 9.4.0 | `VERIFIED` |
| Compose Compiler | plugin Kotlin 2.2.10 | Compose Compiler Gradle Plugin alinhado ao Kotlin | `VERIFIED` |
| Compose BOM | 2026.09.00 | BOM stable indicado pela documentação oficial | `VERIFIED` |
| Compose | 1.12.x stable | Linha estável que requer `compileSdk = 37` e AGP 9 | `VERIFIED` |
| compileSdk | 37 | Compatível com AGP 9.4.0 | `VERIFIED` |
| targetSdk | 36 | Decisão independente; requisito vigente da Google Play | `VERIFIED` |
| minSdk | 26 | Compatível com Compose e com o suporte interno aprovado | `VERIFIED` |

## Fontes oficiais consultadas

- [AGP 9.4.0](https://developer.android.com/build/releases/agp-9-4-0-release-notes), Android Developers, 2026-09-19: suporta API 37 e registra Gradle 9.6.0, JDK 17 e KGP 2.2.10.
- [Java versions in Android builds](https://developer.android.com/build/jdks), Android Developers, 2026-09-19: AGP requer JDK 17.
- [Google Play target API](https://developer.android.com/google/play/requirements/target-sdk), Android Developers, 2026-09-19: API 36 para novos apps e updates.
- [Kotlin Gradle compatibility](https://kotlinlang.org/docs/gradle-configure-project.html), Kotlin Documentation, 2026-09-19.
- [Compose BOM](https://developer.android.com/develop/ui/compose/bom), Android Developers, 2026-09-19.

## Conclusao

A combinação documental completa é compatível. O `PASS` autoriza retomar o
planejamento e a implementação, mas não substitui a resolução real das
dependências, o build e os quality gates pelo Gradle Wrapper.

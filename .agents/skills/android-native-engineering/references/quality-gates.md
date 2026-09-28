# Quality gates Android

## Inventário

Mapeie arquivos de produção, testes correspondentes e exclusões justificadas. Diferencie unitários JVM, instrumentados, UI Compose, screenshots e integração.

## Sequência mínima aplicável

Use tarefas existentes do projeto, sem inventar nomes. Descubra com `gradlew tasks` quando necessário.

1. Gradle Wrapper válido e build reproduzível.
2. Unit tests.
3. Android lint e format/lint adicional configurado pelo projeto.
4. Inventário e revisão obrigatória de KDoc conforme [kdoc-guidelines.md](kdoc-guidelines.md); execute Dokka somente quando configurado ou exigido pela SPEC.
5. Testes instrumentados/UI quando o comportamento depende do framework ou dispositivo.
6. Cobertura do código elegível: mínimo 80%, alvo 90%, salvo regra mais forte da SPEC.
7. Verificação de dependências e segredos.
8. Acessibilidade, adaptive UI e desempenho quando fazem parte dos critérios.
9. Validação em emulador/dispositivo para comportamentos que testes JVM não comprovam.

Cobertura não substitui cenários. Não crie testes sem asserção, código artificial ou exclusões para atingir percentual.

## Evidência

Para cada comando registre: diretório, toolchain, comando exato, código de saída, resumo do resultado e artefatos. Marque limitações do ambiente de forma explícita.

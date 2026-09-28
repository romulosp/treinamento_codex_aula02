# Plano de implementação

1. Inicializar `android-native-engineering` com o gerador oficial de skills do ambiente.
2. Manter `SKILL.md` curto e rotear detalhes para cinco referências, incluindo a política obrigatória de KDoc.
3. Implementar validador Python sem dependências externas.
4. Testar o validador com fixtures temporários válido e inválido.
5. Executar `quick_validate.py` e inspeção de conteúdo.
6. Registrar evidências em `validation.md` e revisão em `reviews/`.
7. Validar que o requisito de KDoc cobre contratos Kotlin aplicáveis sem exigir comentários redundantes ou instalação automática de Dokka.

Risco principal: duplicar ou congelar conhecimento oficial Android. Mitigação: composição explícita com skills oficiais e consulta temporal antes de decisões de versão.

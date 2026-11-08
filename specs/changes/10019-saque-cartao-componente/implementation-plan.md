# Plano de implementação — 10019

Entrada: proposta e SPEC aprovadas na revisão r2; seleção de revisão corrigida conforme revisão r3 após evidência de atualização.

1. Adicionar API de componente sem modificar métodos antigos; atualizar versão publicada e compatibilidade de autenticação, com testes.
2. Adaptar owner para retorno após cleanup e o APK do saque para novo serviço/capacidade; preservar repositório Go, AAR, perfis e telas.
3. Hospedar componente no contêiner do consumidor, com uma sessão, invalidação por geração e rejeição de versão antiga no menu; usar carga verificada e nativas existentes.
4. Adaptar Operações para chamar o host e exibir resultado sanitizado; adicionar instrumentação de lifecycle usando fixture sem hardware.
5. Executar validador estrutural, testes JVM, lint, cobertura, builds e instrumentação disponível. Corrigir falhas e registrar comandos/saídas, limitações e inventário KDoc em validation.md.
6. Entregar host debug instalado e roteiro para teste humano, encerrando em IMPLEMENTADA conforme implementador-para-teste. Nenhum commit, archive, aprovação final ou alteração de system.

Riscos focais: callback antes de CLO; referências após detach; duplicidade de carga nativa; regressão de compatibilidade login e menu. Cada risco recebe teste ou evidência explícita. Não executar captura real de cartão/PIN automaticamente.

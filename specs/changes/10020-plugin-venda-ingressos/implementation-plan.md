# Plano de implementação

Criar módulo Android independente com manifesto, serviço, API de negócio e tela Compose; reutilizar SDKs e bibliotecas do projeto. Adicionar staging debug do APK ao pipeline existente sem sobrescrever modificações locais. Implementar núcleo em memória e testar entradas, pedidos, disponibilidade, recusa/cancelamento e idempotência; testar fluxo de UI e descoberta dinâmica. Rodar testes, lint, build, cobertura e instrumentação possíveis, registrar os resultados e preparar roteiro manual pelo host. Não alterar a infraestrutura financeira.

Correção r4: comparar inicialização das SPECs 074/069/068; diagnosticar Ping no
processo host com AAR real sem sessão física; aplicar override explícito de
endpoint no perfil de homologação conforme ADR-002 e reprovisionar pelo
launcher existente. Não reconstruir o core nem criar comandos paralelos.

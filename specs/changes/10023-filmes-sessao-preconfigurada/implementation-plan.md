# Plano técnico preparatório

Entrada: proposal.md e spec.md em SPEC_APROVADA após revisão de 08/11/2026.

1. Registrar hashes baseline dos arquivos de integração financeira que permanecerão intactos. Executar validador estrutural antes dos gates.
2. Criar modelos/configuração local em CinemaCatalog.kt; validar catálogo na construção de CinemaJourney. Substituir createSession por selectMovie e manter projeção CinemaSession. Conservar vendas por ID e separar ocupados configurados.
3. Remover campos e cadastro da rota e tela. Ligar carrossel aos IDs do catálogo e apresentar somente leitura do horário/sala/preço; nenhuma API de seleção de horário.
4. Atualizar testes JVM para seleção, ocupados, troca sem perda de vendas, isolamento, bloqueio de pedido, estados de pagamento e entradas de configuração inválidas. Manter cobertura mínima de 80% do owner elegível.
5. Atualizar testes Compose para filme → dados automáticos → assento → pedido e retorno sintético, ausência do formulário e controles editáveis, foco/compacto/fonte ampliada. Ajustar teste do host para conferir 4400 centavos sem hardware.
6. Rodar Wrapper: JVM, cobertura, lint, APK e test APK; instrumentação do plugin e integração do APK no host. Inspecionar capturas e conferir APK embutido por hash. Instalar host final e registrar evidências e inventário KDoc.

Limites: nenhuma atualização de stack, nova dependência, alteração de host de produção/shared-api/componente/Go ou API Google. Gate estrutural preexistente deve permanecer explicitamente reprovado se continuar detectando local.properties. Teste humano e aprovação formal não são inferidos dos testes técnicos.

# Plano preparatório

Entrada: proposal/spec em SPEC_APROVADA; ADR-001 aceito.

1. Guardar hashes de GoSaqueRepository, AAR e contratos monetários antes de editar; executar validador estrutural e preservar local.properties.
2. Acrescentar opções/facetas compartilhadas 1.5, encaminhamento pelo host sem fallback e método do componente. Atualizar manifestos e publicação interna. Não modificar DTO monetário ou versões SDK.
3. Adicionar política default-false ao owner, retorno após close confirmado e fábrica injetável com construtor público antigo. Ingressos opta por CALLER e aceita CAPTURED somente como demonstração.
4. Testar close pendente/falho, estados e callbacks no owner, factory real com fake repository e legado manual; testar recusa de provedor antigo e opções/status no host. Reexecutar testes compartilhados, componente, ingressos/host e coberturas/lint/builds.
5. Conferir hashes/manifestos/APKs embutidos; instalar host sem provisionar/reiniciar Bridge nem capturar cartão. Atualizar memória 10011 e evidências. Entregar IMPLEMENTADA para teste humano; gates formais pendentes.

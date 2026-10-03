# Tarefas: 10003-menu-negocio-dinamico

**Autor:** Rômulo Penha

## Status

`IMPLEMENTACAO_APROVADA`

## Pré-condições

- [x] Registrar revisão da SPEC sem ressalvas materiais e promover status para `SPEC_APROVADA`.
- [x] Registrar plano técnico preparatório antes de alterar código.

## Implementação

- [x] Evoluir a SharedApi sem elevar a major, preservar o baseline 1.1.0 e o
      construtor compatível, atualizar KDoc e adequar o plugin de demonstração
      e seu descritor de serviço.
- [x] Implementar validador de caminho, árvore determinística e conversão recursiva.
- [x] Implementar descoberta por APK/descritor de serviço, handles isolados e falhas catalogadas.
- [x] Integrar executor, cancelamento por geração, lifecycle, modal fatal e UI hierárquica.
- [x] Criar testes unitários para gramática, conflito e ordenação.
- [x] Criar testes instrumentados para APK/DEX e estado vazio.

## Revisão e validação

- [x] Executar validador estrutural Android (bloqueado somente pelo `local.properties` local preservado).
- [x] Executar testes aplicáveis, lint e build pelo Gradle Wrapper.
- [x] Revisar implementação contra SPEC e registrar relatório.
- [x] Validar em emulador e registrar ambiente, comandos e códigos de saída.

# Tarefas: 017-configurar-fallback-repositorio-maven

## Pré-condições

- [x] Revisar e aprovar proposta, SPEC, design e tarefas.

## Implementação

- [x] Gerar os dois arquivos de settings Maven sob `.mvn/`.
- [x] Atualizar o script de inicialização para modos `nexus`, `public` e automático.
- [x] Garantir que a sessão Maven use repositório local gravável e não altere configuração global.

## Status da implementação

`IMPLEMENTADA`

## Retorno da validação

`IMPLEMENTADA`

A implementação corrigiu a elegibilidade CDI de `CategoriaService` e configura `user.home` temporário no script de inicialização, preservando a configuração Maven global do usuário. A nova validação deve confirmar a execução completa.

## Validação

- [ ] Testar modo Nexus em rede com VPN/Nexus disponível (pendência de ambiente corporativo, não bloqueante para o fallback público validado).
- [x] Testar modo público sem Nexus e registrar comando, resultado e código de saída.
- [x] Revisar, validar e aprovar a implementação.

## Status da validação

`VALIDADA`

A validação passou com `mvn clean test` no caminho público; as evidências completas estão em `validation.md`.

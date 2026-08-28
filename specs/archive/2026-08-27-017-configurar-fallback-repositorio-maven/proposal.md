# Proposta: 017-configurar-fallback-repositorio-maven

## Status
`SPEC_APROVADA`

## Problema e objetivo

O Nexus corporativo deve continuar como fonte padrão, mas não é acessível fora da VPN. Permitir que o projeto gerado use o Maven Central quando o Nexus não estiver acessível ou não conseguir resolver uma dependência.

## Escopo

- Gerar configurações Maven separadas para Nexus e Maven Central.
- Selecionar Nexus por padrão e Maven Central por fallback controlado na sessão de execução.
- Registrar testes, mensagens e evidências de cada caminho.

## Fora de escopo

- Alterar permanentemente a configuração Maven global do desenvolvedor, publicar artefatos, gravar credenciais ou substituir o Nexus como padrão.

## Critérios para aprovação da SPEC

- O Nexus permanece padrão quando acessível.
- A seleção pública é reproduzível fora da VPN e não usa espelho corporativo.
- Falhas dos dois caminhos produzem mensagem acionável sem expor segredos.

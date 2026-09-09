# Exemplos de referência Golang

Os exemplos desta pasta são contratuais e documentais; não são um projeto executável. Eles demonstram a separação esperada sem introduzir dependência fora da SPEC.

## Exemplo de fluxo `api`

```text
request HTTP -> handler Gin -> DTO/validação -> caso de uso
             -> porta de domínio -> adaptador GORM
             <- resultado de domínio <- mapeamento para response DTO
```

O handler não deve acessar GORM ou expor o modelo persistente. Erros de domínio são convertidos em códigos HTTP e mensagens públicas estáveis.

## Exemplo de fluxo `desktop`

```text
cmd -> configuração -> composição das dependências -> caso de uso
                                              -> domínio
                                              -> adaptador de infraestrutura
```

O ponto de entrada pode ser substituído por outro adaptador sem alterar regras de domínio e casos de uso.

## Exemplo de contrato de erro

```json
{
  "code": "validation_error",
  "message": "Entrada inválida",
  "correlationId": "<id-da-requisicao>"
}
```

O exemplo não autoriza expor stack trace, SQL, credenciais ou dados pessoais. A aplicação gerada deve adaptar códigos e campos à SPEC específica.

## Exemplo de evidência

```text
arquivo de produção: internal/application/<caso-de-uso>.go
teste relacionado: internal/application/<caso-de-uso>_test.go
classificação: aplicável
cenários: sucesso, ausência, erro de dependência e cancelamento
```

O inventário real deve ser produzido para cada módulo gerado e registrado em `validation.md`.

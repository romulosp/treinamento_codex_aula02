# Tarefas: 067-compilar-projeto-go

Autor: Rômulo Penha

## Pré-condições

- [x] Confirmar necessidade de um atalho de compilação no Windows.
- [x] Confirmar localização do módulo Go em `apps/desktop/libpinpadabecsgo`.
- [x] Definir escopo operacional sem alterar a lógica da biblioteca.

## Implementação

- [x] Criar arquivo `compilar projeto.bat` na raiz do repositório.
- [x] Configurar o script para localizar o diretório do módulo Go.
- [x] Verificar a presença de `go` no `PATH`.
- [x] Executar `go build ./...` no módulo correto.
- [x] Exibir mensagens de sucesso e falha.
- [x] Retornar código de saída compatível com Windows.
- [x] Manter o script sem persistir alterações no sistema.

## Validação

- [ ] Executar o batch em ambiente Windows com Go configurado.
- [ ] Confirmar que o build do módulo termina com sucesso.
- [ ] Registrar o resultado final e código de saída.

## Status

`SPEC_PROPOSTA`

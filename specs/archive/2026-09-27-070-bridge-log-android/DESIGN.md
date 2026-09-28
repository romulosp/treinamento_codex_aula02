# DESIGN — 070 Bridge log Android

1. Extrair ou reutilizar a resolução de destino de log já usada pelo menu
   desktop, evitando duas políticas divergentes e mantendo um único arquivo.
2. Adicionar uma função de composição no entrypoint Bridge que receba o
   transporte, configure o tracer compartilhado antes do servidor aceitar
   conexões e não dependa de acesso do Android ao sistema de arquivos Windows.
3. Manter a camada Bridge sem interpretar comandos ABECS.
4. Testar o contrato de configuração com diretórios temporários e validar a
   integração por `go test ./...`.

## Riscos

- erro de permissão deve falhar no startup, não ser silenciosamente ignorado;
- o arquivo padrão deve ser determinístico independentemente do diretório de
  execução;
- testes não devem depender de COM física nem incluir dados reais.

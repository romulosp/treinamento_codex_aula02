# Auditoria de segurança: multimídia ABECS

**Data:** 2026-09-14  
**Escopo:** módulo Go `apps/desktop/libpinpadabecsgo`, transporte serial,
comandos de mídia e utilitário local.

## Resumo

Não foi confirmada vulnerabilidade alcançável no código avaliado. O módulo não
expõe rotas, listeners de rede, identidade de usuário, persistência de negócio
ou consultas a banco. A entrada é local (arquivo e nomes de mídia) e segue pelo
worker para uma porta serial.

## Controles observados

- O nome A8 de MLI/DSI/DMF é validado como oito caracteres ASCII alfanuméricos
  antes de qualquer escrita serial (`internal/domain/command/advanced.go:131-137, 159-181`).
- LMF valida o tamanho e os caracteres antes de imprimir nomes no terminal
  (`internal/domain/parser/abecs.go:124-153`).
- O conteúdo multimídia é transmitido em blocos; MLR permanece redigido pelo
  tracer. MLI/MLR/DSI/LMF/DMF não registram caminho nem conteúdo do arquivo.
- A busca de padrões de segredo nos arquivos Go/Markdown do módulo não encontrou
  correspondências. `govulncheck` não encontrou símbolos vulneráveis
  alcançáveis.

## Categorias não aplicáveis

Autenticação, autorização, tenant/usuário, IDOR, XSS, SQL injection e segurança
de deploy não se aplicam ao módulo serial local: não foram encontradas rotas,
HTML, banco de dados, identidade remota ou artefatos de deploy neste escopo.

## Alertas de dependências e manutenção

`govulncheck` (Go 1.26.5 windows/386) reportou zero vulnerabilidades
alcançáveis. Também listou duas vulnerabilidades de pacote e sete de módulo que
o call graph não alcança. Entre os sinais, o banco indica correções posteriores
para a biblioteca padrão Go 1.26.5 e para o módulo indireto `golang.org/x/sys`
atualmente requerido pelo adaptador serial. Recomenda-se atualizar o toolchain
e a dependência indireta em uma Change própria e repetir o scanner. Não houve
atualização de dependências neste escopo.

## Recomendações

- **P1:** nenhuma.
- **P2:** atualizar o toolchain e `golang.org/x/sys` para versões corrigidas em
  Change própria; repetir `govulncheck` e testes Windows/serial.
- **P3:** se o utilitário passar a aceitar arquivos de origens não confiáveis ou
  muito grandes, considerar transferência por streaming para limitar a memória
  residente do host.

Não há achados confirmados que requeiram issues de segurança nesta Change.

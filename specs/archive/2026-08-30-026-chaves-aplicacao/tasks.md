# Tarefas: 026-chaves-aplicacao

## Pre-condicoes

- [x] SPEC aprovada e revisao sem achados bloqueantes.
- [x] Definido o caminho fixo Windows do arquivo externo.
- [x] Confirmado que o BAT final sera local, preenchido e ignorado pelo Git.

## Especificacao e design

- [x] Confirmar o manifesto das oito chaves de `gerenciartarefas`.
- [x] Confirmar que `gerenciarcategorias` nao exige chaves externas nesta mudanca.
- [x] Definir parser, escaping e validacao.
- [x] Definir template e destino local do BAT final.

## Implementacao

- [x] Implementar `scripts/gerar_start_aplicacao.ps1`.
- [x] Criar `scripts/templates/start_aplicacao-gerenciartarefas.bat.template` sem valores.
- [x] Gerar localmente o BAT final com valores literais, sem adiciona-lo ao Git.
- [x] Atualizar SPECs vigentes autorizadas sem alterar documentos arquivados.

## Revisao e validacao

- [x] Criar testes com propriedades sinteticas e cenarios de erro.
- [x] Revisar a implementacao contra a SPEC.
- [x] Executar testes e registrar ambiente, comandos, resultados e codigos de saida em `validation.md`.
- [ ] Aprovar formalmente, atualizar `specs/system/`, preparar archive e commit rastreavel.

## Situacao

`ARQUIVADA`

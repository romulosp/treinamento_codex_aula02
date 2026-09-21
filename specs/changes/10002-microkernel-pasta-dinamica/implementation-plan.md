# Plano de implementação

## Status

`IMPLEMENTADA`

1. Desacoplar o build do host da tarefa `:plugin-login:assembleDebug` e remover
   source set de assets gerados.
2. Refatorar `LoginPluginLoader` em duas etapas: verificar/promover e carregar.
3. Criar `DynamicLoginPluginManager` com observador vivo, executor serial,
   debounce, scan, recuperação do repositório verificado e máquina de estados.
4. Fazer `MainActivity` controlar lifecycle e renderizar estado imutável.
5. Proteger callbacks de carga/UI, executar `onDetach` e fallback em `ERROR`.
6. Atualizar o script de desenvolvimento para entrega por pasta dinâmica.
7. Executar quality gates e comprovar ativação após `adb push` sem reiniciar.

## Testes

- Unitários existentes do login devem permanecer verdes.
- Lint do host e plugin.
- Testes Compose instrumentados do plugin.
- Integração manual/automatizada por `adb`: iniciar host sem APK novo, mover o
  plugin para inbox e confirmar a UI por dump do UIAutomator.
- Reiniciar o processo e confirmar carga pelo repositório verificado.

## Riscos

- Eventos perdidos pelo observador: mitigados por scan em todo `onStart`.
- Arquivo parcialmente escrito: mitigado por nome temporário e `MOVED_TO`, além
  de `CLOSE_WRITE` para outros entregadores.
- Execução de arquivo mutável: mitigada por cópia, validação e promoção privada.
- Reprocessamento: mitigado por fingerprint e digest ativo.

## Resultado

O build do host foi desacoplado do plugin. `DynamicLoginPluginManager` mantém o
`FileObserver`, a fila serial e a máquina de estados; `LoginPluginLoader`
implementa quarentena, digest, assinatura, pacote, manifesto, promoção privada
e ciclo de vida. `MainActivity` hospeda a View do plugin em `FrameLayout` dentro
de `AndroidView` e converte falhas cooperativas em fallback.

A revisão `1.0.1` do plugin foi usada para comprovar `PENDING_RESTART` e adoção
após reinício, sem alterar o comportamento funcional da tela de login.

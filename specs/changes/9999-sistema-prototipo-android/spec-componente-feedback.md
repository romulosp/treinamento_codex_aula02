# Especificação complementar: diálogos, mensagens e progresso

**Autor:** Rômulo Penha

## Componentes

- Diálogo de informação.
- Diálogo de confirmação.
- Mensagem de sucesso, alerta e erro.
- Indicador de progresso determinado.
- Indicador de progresso indeterminado.

## Comportamento

- Diálogo aberto é modal, recebe foco inicial previsível e devolve foco ao acionador quando fechado.
- Voltar fecha o diálogo quando o fluxo permitir.
- Confirmação e cancelamento emitem eventos distintos.
- Mensagens usam texto e ícone, sem depender somente de cor.
- Progresso determinado expõe valor semanticamente; indeterminado informa atividade sem percentual falso.
- Nenhum temporizador ou thread do sistema anterior deve ser portado diretamente.

## Critérios específicos

- A tela-catálogo contém acionadores para cada diálogo e mensagem.
- Testes cobrem abrir, confirmar, cancelar, voltar e foco.
- Testes semânticos cobrem status e progresso.

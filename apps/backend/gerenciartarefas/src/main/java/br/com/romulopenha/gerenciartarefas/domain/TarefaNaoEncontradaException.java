package br.com.romulopenha.gerenciartarefas.domain;

/**
 * Indica que não foi encontrada tarefa para o identificador e tenant solicitados.
 */
public class TarefaNaoEncontradaException extends RuntimeException {
    public TarefaNaoEncontradaException(Long id) { super("Tarefa não encontrada"); }
}

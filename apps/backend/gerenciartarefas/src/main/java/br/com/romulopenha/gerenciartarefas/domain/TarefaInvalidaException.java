package br.com.romulopenha.gerenciartarefas.domain;

/**
 * Indica que os dados fornecidos para uma tarefa não atendem às regras de validação.
 */
public class TarefaInvalidaException extends RuntimeException {
    public TarefaInvalidaException(String message) { super(message); }
}

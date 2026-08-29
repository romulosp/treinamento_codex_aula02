package br.com.romulopenha.gerenciartarefas.domain;

public class TarefaInvalidaException extends RuntimeException {
    public TarefaInvalidaException(String message) { super(message); }
}

package br.com.romulopenha.gerenciartarefas.domain;

public class TarefaNaoEncontradaException extends RuntimeException {
    public TarefaNaoEncontradaException(Long id) { super("Tarefa não encontrada"); }
}

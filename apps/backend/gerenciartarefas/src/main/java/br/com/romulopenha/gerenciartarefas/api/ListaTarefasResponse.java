package br.com.romulopenha.gerenciartarefas.api;

import java.util.List;

public record ListaTarefasResponse(List<TarefaResponse> tarefas) { }

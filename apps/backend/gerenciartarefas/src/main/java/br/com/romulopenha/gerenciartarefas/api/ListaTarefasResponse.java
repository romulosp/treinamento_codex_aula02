package br.com.romulopenha.gerenciartarefas.api;

import java.util.List;

/**
 * Representa a coleção de tarefas retornada pela API.
 */
public record ListaTarefasResponse(List<TarefaResponse> tarefas) { }

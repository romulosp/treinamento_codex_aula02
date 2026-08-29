package br.com.romulopenha.gerenciartarefas.api;

import br.com.romulopenha.gerenciartarefas.domain.Tarefa;
import java.time.LocalDateTime;

public record TarefaResponse(Long id, String titulo, String descricao, String status,
                             LocalDateTime data_criacao, LocalDateTime data_conclusao) {
    public static TarefaResponse from(Tarefa tarefa) {
        return new TarefaResponse(tarefa.id(), tarefa.titulo(), tarefa.descricao(), tarefa.status().name(),
                tarefa.dataCriacao(), tarefa.dataConclusao());
    }
}

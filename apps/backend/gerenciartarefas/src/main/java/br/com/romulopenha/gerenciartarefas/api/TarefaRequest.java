package br.com.romulopenha.gerenciartarefas.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Representa os dados validados recebidos pela API para criar ou atualizar uma tarefa.
 */
public record TarefaRequest(@NotBlank @Size(max = 100) String titulo,
                             @NotBlank String descricao,
                             @NotBlank String status) { }
